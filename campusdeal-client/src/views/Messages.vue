<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- 会话列表模式 -->
      <template v-if="viewMode === 'list'">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold">消息</h1>
          <button
            @click="handleMarkAllRead"
            class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors text-gray-700"
          >
            一键已读
          </button>
        </div>

        <div class="space-y-2">
          <!-- 系统通知入口卡片 -->
          <div
            class="card flex items-center space-x-4 !p-4 hover:bg-gray-50 transition-colors cursor-pointer border-l-4 border-l-blue-500"
            @click="enterSystemChat"
          >
            <div class="relative">
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <span
                v-if="systemUnreadCount > 0"
                class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center"
              >
                {{ systemUnreadCount > 99 ? '99+' : systemUnreadCount }}
              </span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <div class="flex items-center space-x-2">
                  <p class="font-medium text-gray-900">系统通知</p>
                  <span class="px-1.5 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded">官方</span>
                </div>
                <span class="text-xs text-gray-400">{{ systemLastTime }}</span>
              </div>
              <p class="text-sm text-gray-500 truncate mt-0.5" v-if="systemLastContent">{{ systemLastContent }}</p>
              <p class="text-sm text-gray-400 italic mt-0.5" v-else>暂无系统通知</p>
            </div>
          </div>

          <!-- 普通会话列表 -->
          <router-link
            v-for="conv in chatStore.conversations"
            :key="conv.conversationId"
            :to="`/chat/${conv.conversationId}${conv.productId ? '?productId=' + conv.productId : ''}`"
            class="card flex items-center space-x-4 !p-4 hover:bg-gray-50 transition-colors"
          >
            <div class="relative">
              <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                <span class="text-primary-600 font-bold">{{ (conv.otherUserName || conv.contactName || 'U').charAt(0) }}</span>
              </div>
              <span
                v-if="conv.unreadCount > 0"
                class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center"
              >
                {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
              </span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <p class="font-medium text-gray-900">{{ conv.otherUserName || conv.contactName || ('用户' + conv.otherUserId) }}</p>
                <span class="text-xs text-gray-400">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <p class="text-sm text-gray-500 truncate mt-0.5">
                <template v-if="isLastMessageFromMe(conv)">我: </template>{{ conv.lastMessage || '暂无消息' }}</p>
            </div>
          </router-link>
        </div>

        <div v-if="chatStore.conversations.length === 0" class="text-center py-12 text-gray-400">
          <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <p class="text-lg">暂无消息</p>
        </div>
      </template>

      <!-- 系统通知聊天模式 -->
      <template v-else>
        <!-- 顶部栏 -->
        <div class="flex items-center space-x-3 mb-4 pb-3 border-b border-gray-200">
          <button
            class="p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
            @click="viewMode = 'list'"
          >
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div class="flex items-center space-x-2">
            <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <span class="font-semibold text-gray-900">系统通知</span>
            <span class="px-1.5 py-0.5 text-xs font-medium bg-blue-100 text-blue-700 rounded">官方</span>
          </div>
        </div>

        <!-- 消息列表 -->
        <div ref="messagesContainer" class="flex-1 bg-gray-100 rounded-lg p-4 space-y-3 overflow-y-auto" style="max-height: calc(100vh - 280px); min-height: 400px;">
          <div v-if="systemMessages.length === 0" class="flex items-center justify-center h-full text-gray-400">
            <p>暂无消息，向管理员发送消息开始对话</p>
          </div>
          <div
            v-for="msg in systemMessages"
            :key="msg.id"
            :class="[
              'flex',
              isFromSystem(msg) ? 'justify-start' : 'justify-end'
            ]"
          >
            <div
              :class="[
                'max-w-[75%] px-4 py-2.5 text-sm',
                isFromSystem(msg)
                  ? 'bg-white rounded-lg rounded-tl-none shadow-sm text-gray-800'
                  : 'bg-blue-500 text-white rounded-lg rounded-tr-none'
              ]"
            >
              <!-- 发送者标签：管理员显示消息类型或"管理员"；用户显示昵称 -->
              <div class="flex items-center space-x-2 mb-1">
                <template v-if="isFromSystem(msg)">
                  <span v-if="msg.messageType === 'order'" class="text-xs px-1.5 py-0.5 rounded bg-green-100 text-green-600">订单通知</span>
                  <span v-else-if="msg.messageType === 'announcement'" class="text-xs px-1.5 py-0.5 rounded bg-blue-100 text-blue-600">公告</span>
                  <span v-else-if="msg.messageType === 'audit'" class="text-xs px-1.5 py-0.5 rounded bg-orange-100 text-orange-600">审核结果</span>
                  <span v-else class="text-xs px-1.5 py-0.5 rounded bg-gray-100 text-gray-500">管理员</span>
                </template>
                <template v-else>
                  <span class="text-xs px-1.5 py-0.5 rounded bg-blue-400 text-white">{{ currentUserName }}</span>
                </template>
              </div>
              <p class="whitespace-pre-wrap break-words">{{ msg.content }}</p>
              <p
                :class="[
                  'text-xs mt-1',
                  isFromSystem(msg) ? 'text-gray-400' : 'text-blue-200'
                ]"
              >{{ formatTime(msg.createdAt) }}</p>
            </div>
          </div>
        </div>

        <!-- 底部输入区 -->
        <div class="mt-3 flex items-center space-x-2">
          <input
            v-model="newAdminMessage"
            type="text"
            placeholder="输入消息..."
            class="flex-1 px-4 py-2.5 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
            @keydown.enter="sendToAdmin"
          />
          <button
            class="px-5 py-2.5 bg-blue-500 text-white rounded-full hover:bg-blue-600 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!newAdminMessage.trim()"
            @click="sendToAdmin"
          >
            发送
          </button>
        </div>
      </template>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { messageApi } from '@/api/message'

const chatStore = useChatStore()
const userStore = useUserStore()

// 当前用户昵称（蓝色消息显示用）
const currentUserName = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '我'
})

// 判断消息是否来自系统方（显示在左边白色气泡）
function isFromSystem(msg) {
  const uid = userStore.userInfo?.id || userStore.userInfo?.userId
  // order/announcement/audit 类型始终是系统方发送
  if (msg.messageType && ['order', 'announcement', 'audit'].includes(msg.messageType)) {
    return true
  }
  // system 类型：用 toUserId 判断方向（toUserId == 当前用户 → 管理员发来的，显示在左边）
  if (msg.messageType === 'system') {
    return msg.toUserId === uid
  }
  // 其他类型（如 text）：根据 fromUserId 判断
  return msg.fromUserId !== uid
}

// 判断会话列表中最后一条消息是否是当前用户发送的
function isLastMessageFromMe(conv) {
  const uid = userStore.userInfo?.id || userStore.userInfo?.userId
  return conv.lastMessageFromUserId === uid
}

// 视图模式
const viewMode = ref('list') // 'list' | 'systemChat'

// 系统通知数据
const systemNotifications = ref([])
const systemMessages = ref([])
const newAdminMessage = ref('')
const messagesContainer = ref(null)

// 计算系统通知未读数
const systemUnreadCount = computed(() => {
  return systemNotifications.value.filter(n => n.isRead === 0).length
})

// 最新系统通知内容（标题 + 摘要）
const systemLastContent = computed(() => {
  if (systemNotifications.value.length === 0) return ''
  const n = systemNotifications.value[0]
  const title = n.title || ''
  const content = n.content || ''
  if (title && content && content.indexOf(title) === -1) {
    return `${title}：${content}`
  }
  return title || content || ''
})

// 最新系统通知时间
const systemLastTime = computed(() => {
  if (systemNotifications.value.length === 0) return ''
  return formatTime(systemNotifications.value[0].createdAt)
})

// 获取系统通知列表（合并系统消息与公告，取最新一条）
async function fetchSystemNotifications() {
  try {
    const [sysRes, annRes] = await Promise.all([
      messageApi.getSystemNotifications(),
      messageApi.getAnnouncements()
    ])
    const sysList = sysRes.data.code === 200 ? (sysRes.data.data?.list || []) : []
    const annList = annRes?.data?.data || annRes?.data || []

    // 将公告转为统一通知格式，便于合并排序
    const annNotifications = annList.map(a => ({
      id: `ann_${a.id}`,
      title: a.title || '系统公告',
      content: a.content || '',
      createdAt: a.createdAt
    }))

    const all = [...sysList, ...annNotifications]
    all.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    systemNotifications.value = all
  } catch {
    // ignore
  }
}

// 加载系统会话消息
async function loadSystemMessages() {
  try {
    const [msgRes, annRes] = await Promise.all([
      messageApi.getAdminMessages(),
      messageApi.getAnnouncements()
    ])
    const msgList = msgRes.data.code === 200 ? (msgRes.data.data?.list || []) : []

    // 将公告转为消息格式，混入时间线
    const annList = convertAnnouncementsToMessages(annRes)

    // 合并并按 createdAt 排序
    systemMessages.value = [...msgList, ...annList].sort(
      (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
    )
  } catch {
    // ignore
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

// 进入系统聊天
async function enterSystemChat() {
  viewMode.value = 'systemChat'
  await loadSystemMessages()
  await nextTick()
  scrollToBottom()
  // 进入系统通知后自动消除未读角标
  try {
    await messageApi.markAllRead()
    fetchSystemNotifications()
  } catch { /* ignore */ }
}

// 发送消息给管理员
async function sendToAdmin() {
  const content = newAdminMessage.value.trim()
  if (!content) return
  try {
    const res = await messageApi.sendAdminMessage({ content })
    if (res.data.code === 200) {
      systemMessages.value.push(res.data.data)
      newAdminMessage.value = ''
      await nextTick()
      scrollToBottom()
    }
  } catch {
    // ignore
  }
}

// 滚动到底部
function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 消息列表变化时自动滚动
watch(systemMessages, () => {
  nextTick(() => scrollToBottom())
}, { deep: true })

// 一键已读
async function handleMarkAllRead() {
  try {
    await messageApi.markAllRead()
    // 刷新列表
    chatStore.fetchConversations()
    fetchSystemNotifications()
  } catch { /* ignore */ }
}

onMounted(() => {
  chatStore.fetchConversations()
  fetchSystemNotifications()
})

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>
