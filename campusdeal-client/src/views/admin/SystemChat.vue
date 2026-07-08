<template>
  <div class="flex flex-col h-full">
    <!-- 顶部栏 -->
    <div class="px-5 py-3 border-b bg-white flex items-center justify-between">
      <div class="flex items-center space-x-3">
        <router-link
          to="/admin/messages"
          class="text-gray-400 hover:text-gray-600 text-sm"
        >← 返回</router-link>
        <h3 class="font-bold text-gray-900">与 {{ userName }} 的系统通知</h3>
      </div>
    </div>

    <!-- 聊天记录 -->
    <div class="flex-1 overflow-y-auto px-4 py-4" style="background: #f0f0f0; min-height: 300px;" ref="chatContainer">
      <div v-if="loading" class="flex items-center justify-center h-full text-gray-400 text-sm">加载中...</div>
      <div v-else-if="messages.length === 0" class="flex items-center justify-center h-full text-gray-400 text-sm">暂无系统通知</div>
      <template v-else>
        <div v-for="msg in messages" :key="msg.id" class="mb-3"
          :class="isFromAdmin(msg) ? 'flex justify-end' : 'flex justify-start'">
          <div class="max-w-[75%]">
            <!-- 发送者标签 -->
            <div class="text-xs text-gray-400 mb-0.5" :class="isFromAdmin(msg) ? 'text-right' : 'text-left'">
              <template v-if="isFromAdmin(msg)">
                <span v-if="msg.messageType === 'order'" class="px-1.5 py-0.5 text-xs rounded bg-green-100 text-green-600">订单通知</span>
                <span v-else-if="msg.messageType === 'announcement'" class="px-1.5 py-0.5 text-xs rounded bg-blue-100 text-blue-600">公告</span>
                <span v-else-if="msg.messageType === 'audit'" class="px-1.5 py-0.5 text-xs rounded bg-orange-100 text-orange-600">审核结果</span>
                <span v-else class="px-1.5 py-0.5 text-xs rounded bg-gray-100 text-gray-500">管理员</span>
              </template>
              <template v-else>
                {{ userName }}
              </template>
            </div>
            <!-- 气泡 -->
            <div
              :class="[
                isFromAdmin(msg)
                  ? 'bg-primary-500 text-white rounded-lg rounded-tr-none'
                  : 'bg-white rounded-lg rounded-tl-none shadow-sm',
              ]"
              class="px-4 py-2.5"
            >
              <p class="text-sm whitespace-pre-line leading-relaxed">{{ msg.content }}</p>
            </div>
            <p class="text-xs mt-1 text-gray-400" :class="isFromAdmin(msg) ? 'text-right' : 'text-left'">
              {{ formatDateTime(msg.createdAt) }}
            </p>
          </div>
        </div>
      </template>
    </div>

    <!-- 发送区域 -->
    <div class="px-4 py-3 border-t bg-white flex items-center space-x-3">
      <textarea
        v-model="input"
        rows="2"
        class="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm resize-none focus:outline-none focus:ring-1 focus:ring-primary-400"
        placeholder="输入系统通知内容..."
        @keydown.enter.exact.prevent="sendMessage"
      ></textarea>
      <button
        @click="sendMessage"
        :disabled="!input.trim() || sending"
        class="px-4 py-2 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50 flex-shrink-0"
      >{{ sending ? '发送中...' : '发送' }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { adminApi } from '@/api/admin'
import { messageApi } from '@/api/message'
import { ElMessage } from 'element-plus'

const route = useRoute()

const userId = Number(route.params.userId)
const userName = route.query.userName || ('用户' + userId)

const messages = ref([])
const loading = ref(false)
const input = ref('')
const sending = ref(false)
const chatContainer = ref(null)

onMounted(() => loadMessages())

async function loadMessages() {
  loading.value = true
  try {
    const [chatRes, annRes] = await Promise.all([
      adminApi.getAdminChat(userId, { page: 1, size: 200 }),
      messageApi.getAnnouncements()
    ])
    const chatData = chatRes.data?.data
    const chatMessages = chatData?.list || chatData?.records || chatData || []

    // 将公告转为消息格式，混入时间线
    const announcementMessages = convertAnnouncementsToMessages(annRes)

    // 合并并按 createdAt 排序
    messages.value = [...chatMessages, ...announcementMessages].sort(
      (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
    )
  } catch {
    messages.value = []
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

function convertAnnouncementsToMessages(annRes) {
  const announcements = annRes?.data?.data || annRes?.data || []
  return announcements.map(a => ({
    id: `ann_${a.id}`,
    content: `【系统公告】${a.title}\n${a.content || ''}`,
    messageType: 'announcement',
    fromUserId: null,
    createdAt: a.createdAt
  }))
}

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

function isFromAdmin(msg) {
  if (msg.messageType && ['system', 'announcement', 'audit', 'order'].includes(msg.messageType)) {
    return msg.fromUserId !== userId
  }
  return msg.fromUserId !== userId
}

async function sendMessage() {
  const content = input.value.trim()
  if (!content) return
  sending.value = true
  try {
    await adminApi.sendNotification({
      userId,
      content,
      messageType: 'system'
    })
    const res = await adminApi.getAdminChat(userId, { page: 1, size: 200 })
    const data = res.data?.data
    messages.value = data?.list || data?.records || data || []
    input.value = ''
    await nextTick()
    scrollToBottom()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function formatDateTime(date) {
  if (!date) return '-'
  const d = new Date(date)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>
