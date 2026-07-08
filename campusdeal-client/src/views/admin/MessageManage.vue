<template>
  <div>
    <!-- 右上角切换按钮组 -->
    <div class="flex justify-end mb-4">
      <div class="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-0.5">
        <button
          :class="viewMode === 'user' ? 'bg-white shadow-sm text-primary-700 font-medium' : 'text-gray-500 hover:text-gray-700'"
          class="px-4 py-1.5 text-sm rounded-md transition-colors"
          @click="switchView('user')"
        >用户消息</button>
        <button
          :class="viewMode === 'system' ? 'bg-white shadow-sm text-primary-700 font-medium' : 'text-gray-500 hover:text-gray-700'"
          class="px-4 py-1.5 text-sm rounded-md transition-colors"
          @click="switchView('system')"
        >系统通知</button>
      </div>
    </div>

    <!-- ==================== 用户消息视图 ==================== -->
    <template v-if="viewMode === 'user'">
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-gray-600 w-16">ID</th>
              <th class="px-4 py-3 text-left text-gray-600">用户名</th>
              <th class="px-4 py-3 text-left text-gray-600">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in userList" :key="user.userId" class="border-t hover:bg-gray-50">
              <td class="px-4 py-3 text-gray-500">{{ user.userId }}</td>
              <td class="px-4 py-3 font-medium text-gray-900">{{ user.userName }}</td>
              <td class="px-4 py-3">
                <div class="flex flex-wrap items-center gap-1.5">
                  <!-- 前2个聊天对象 -->
                  <button
                    v-for="(partner, pi) in visiblePartners(user)"
                    :key="partner.userId"
                    @click="openUserChat(user, partner)"
                    class="text-blue-600 hover:text-blue-800 text-xs font-medium bg-blue-50 hover:bg-blue-100 px-2.5 py-1 rounded transition-colors"
                  >与{{ partner.userName }}的聊天界面</button>

                  <!-- 更多下拉 -->
                  <div v-if="hiddenPartners(user).length > 0" class="relative">
                    <button
                      @click="toggleDropdown(user.userId)"
                      class="text-gray-500 hover:text-gray-700 text-xs font-medium bg-gray-100 hover:bg-gray-200 px-2.5 py-1 rounded transition-colors"
                    >更多 ▾</button>
                    <div
                      v-if="openDropdowns.has(user.userId)"
                      class="absolute top-full left-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-10 py-1 min-w-[160px]"
                      @click.stop
                    >
                      <button
                        v-for="partner in hiddenPartners(user)"
                        :key="partner.userId"
                        @click="openUserChat(user, partner); openDropdowns.delete(user.userId)"
                        class="block w-full text-left px-3 py-1.5 text-xs text-blue-600 hover:bg-blue-50 transition-colors"
                      >与{{ partner.userName }}的聊天界面</button>
                    </div>
                  </div>

                  <span v-if="!user.partners || user.partners.length === 0" class="text-xs text-gray-400">暂无聊天记录</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="userList.length === 0" class="text-center py-8 text-gray-400">暂无用户消息</div>
      </div>

      <!-- 分页 -->
      <div class="flex justify-center mt-6" v-if="userTotal > 0">
        <el-pagination
          v-model:current-page="userPage"
          :page-size="userPageSize"
          :total="userTotal"
          layout="prev, pager, next"
          @current-change="loadUserList"
        />
      </div>
    </template>

    <!-- ==================== 系统通知视图 ==================== -->
    <template v-if="viewMode === 'system'">
      <span class="text-sm text-gray-500 mb-3 block">共 {{ sysTotal }} 个用户</span>
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-gray-600 w-16">ID</th>
              <th class="px-4 py-3 text-left text-gray-600">用户名</th>
              <th class="px-4 py-3 text-left text-gray-600 w-32">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in sysUserList" :key="user.userId" class="border-t hover:bg-gray-50">
              <td class="px-4 py-3 text-gray-500">{{ user.userId }}</td>
              <td class="px-4 py-3 font-medium text-gray-900">{{ user.userName }}</td>
              <td class="px-4 py-3">
                <router-link
                  :to="`/admin/messages/system/${user.userId}?userName=${encodeURIComponent(user.userName)}`"
                  class="text-blue-600 hover:text-blue-800 text-xs font-medium bg-blue-50 hover:bg-blue-100 px-2.5 py-1 rounded transition-colors inline-block"
                >查看</router-link>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="sysUserList.length === 0" class="text-center py-8 text-gray-400">暂无系统通知记录</div>
      </div>

      <!-- 分页 -->
      <div class="flex justify-center mt-6" v-if="sysTotal > 0">
        <el-pagination
          v-model:current-page="sysPage"
          :page-size="sysPageSize"
          :total="sysTotal"
          layout="prev, pager, next"
          @current-change="loadSystemUsers"
        />
      </div>
    </template>

    <!-- ==================== 用户间聊天弹窗（管理员旁观） ==================== -->
    <div v-if="userChatDialog.visible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="userChatDialog.visible = false">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl mx-4 flex flex-col" style="max-height: 80vh;">
        <div class="px-5 py-3 border-b flex items-center justify-between">
          <h3 class="font-bold text-gray-900">{{ userChatDialog.userName }} 与 {{ userChatDialog.partnerName }} 的聊天记录</h3>
          <button @click="userChatDialog.visible = false" class="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>
        <div class="flex-1 overflow-y-auto px-4 py-4" style="background: #f0f0f0; min-height: 300px;">
          <div v-if="userChatDialog.loading" class="flex items-center justify-center h-full text-gray-400 text-sm">加载中...</div>
          <div v-else-if="userChatDialog.messages.length === 0" class="flex items-center justify-center h-full text-gray-400 text-sm">暂无聊天记录</div>
          <template v-else>
            <div v-for="msg in userChatDialog.messages" :key="msg.id"
              :class="msg.fromUserId === userChatDialog.userId ? 'flex justify-end mb-3' : 'flex justify-start mb-3'">
              <div class="max-w-[75%]">
                <div class="text-xs text-gray-400 mb-0.5" :class="msg.fromUserId === userChatDialog.userId ? 'text-right' : 'text-left'">
                  {{ msg.fromUserId === userChatDialog.userId ? userChatDialog.userName : userChatDialog.partnerName }}
                </div>
                <div
                  :class="msg.fromUserId === userChatDialog.userId
                    ? 'bg-primary-500 text-white rounded-lg rounded-tr-none'
                    : 'bg-white rounded-lg rounded-tl-none shadow-sm'"
                  class="px-4 py-2.5"
                >
                  <p class="text-sm whitespace-pre-line leading-relaxed">{{ msg.content }}</p>
                </div>
                <p class="text-xs mt-1 text-gray-400" :class="msg.fromUserId === userChatDialog.userId ? 'text-right' : 'text-left'">
                  {{ formatDateTime(msg.createdAt) }}
                </p>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 点击空白关闭下拉 -->
    <div v-if="openDropdowns.size > 0" class="fixed inset-0 z-0" @click="openDropdowns.clear()"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

// ===== 视图模式 =====
const viewMode = ref('user')

// ===== 用户消息视图 =====
const userList = ref([])
const userPage = ref(1)
const userPageSize = 10
const userTotal = ref(0)

// ===== 系统通知视图 =====
const sysUserList = ref([])
const sysPage = ref(1)
const sysPageSize = 10
const sysTotal = ref(0)

// ===== 更多下拉状态 =====
const openDropdowns = ref(new Set())

// ===== 用户间聊天弹窗 =====
const userChatDialog = reactive({
  visible: false,
  userId: null,
  userName: '',
  partnerId: null,
  partnerName: '',
  messages: [],
  loading: false
})

onMounted(() => loadUserList())

function switchView(mode) {
  viewMode.value = mode
  if (mode === 'user') {
    loadUserList()
  } else {
    loadSystemUsers()
  }
}

// ===== 用户列表 + 聊天对象 =====
async function loadUserList() {
  try {
    const res = await adminApi.getAdminAllConversations()
    const all = res.data?.data || []
    userTotal.value = all.length
    const start = (userPage.value - 1) * userPageSize
    const paged = all.slice(start, start + userPageSize)

    // 并行加载每个用户的聊天对象
    const enriched = await Promise.all(
      paged.map(async (user) => {
        try {
          const pr = await adminApi.getUserChatPartners(user.userId)
          return { ...user, partners: pr.data?.data || [] }
        } catch {
          return { ...user, partners: [] }
        }
      })
    )
    userList.value = enriched
  } catch {
    userList.value = []
  }
}

// ===== 系统通知用户列表 =====
async function loadSystemUsers() {
  try {
    const res = await adminApi.getUsersForNotification()
    const all = (res.data?.data || []).map(u => ({ userId: u.id, userName: u.username }))
    sysTotal.value = all.length
    const start = (sysPage.value - 1) * sysPageSize
    sysUserList.value = all.slice(start, start + sysPageSize)
  } catch {
    sysUserList.value = []
  }
}

// ===== 聊天对象显示逻辑 =====
function visiblePartners(user) {
  if (!user.partners) return []
  return user.partners.slice(0, 2)
}

function hiddenPartners(user) {
  if (!user.partners || user.partners.length <= 2) return []
  return user.partners.slice(2)
}

function toggleDropdown(userId) {
  const s = new Set(openDropdowns.value)
  if (s.has(userId)) {
    s.delete(userId)
  } else {
    s.add(userId)
  }
  openDropdowns.value = s
}

// ===== 打开用户间聊天 =====
async function openUserChat(user, partner) {
  const small = Math.min(user.userId, partner.userId)
  const large = Math.max(user.userId, partner.userId)
  const conversationId = `${small}_${large}`

  userChatDialog.userId = user.userId
  userChatDialog.userName = user.userName
  userChatDialog.partnerId = partner.userId
  userChatDialog.partnerName = partner.userName
  userChatDialog.messages = []
  userChatDialog.loading = true
  userChatDialog.visible = true

  try {
    const res = await adminApi.getAdminUserChat(user.userId, conversationId, { page: 1, size: 200 })
    const data = res.data?.data
    const all = data?.list || data?.records || data || []
    // 只保留纯文本聊天，排除系统消息
    userChatDialog.messages = all.filter(m => !m.messageType || m.messageType === 'text')
  } catch {
    userChatDialog.messages = []
  } finally {
    userChatDialog.loading = false
  }
}

// ===== 工具函数 =====
function formatDateTime(date) {
  if (!date) return '-'
  const d = new Date(date)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>
