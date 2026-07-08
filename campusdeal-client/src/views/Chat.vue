<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 flex flex-col h-[calc(100vh-64px)]">
      <!-- Chat Header -->
      <div class="bg-white border-b px-4 py-3 flex items-center space-x-3">
        <button @click="$router.push('/messages')" class="text-gray-500 hover:text-gray-700">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div>
          <p class="font-medium text-gray-900">{{ contactName }}</p>
          <p class="text-xs" :class="onlineStatus ? 'text-green-500' : 'text-gray-400'">
            {{ onlineStatus ? '在线' : '离线' }}
          </p>
        </div>
      </div>

      <!-- Product Info Card -->
      <div v-if="productInfo" class="bg-white border-b px-4 py-3">
        <div class="flex items-center space-x-3">
          <img
            :src="productInfo.coverImage || (productInfo.images && productInfo.images[0]?.url) || ''"
            class="w-14 h-14 object-cover rounded-lg bg-gray-100"
          />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ productInfo.title }}</p>
            <div class="flex items-center space-x-2 mt-0.5">
              <span class="text-sm font-bold text-accent-600">&yen;{{ productInfo.price }}</span>
              <span class="text-xs px-1.5 py-0.5 rounded bg-blue-100 text-blue-700">{{ productInfo.conditionLevel }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Messages -->
      <div ref="messageList" class="flex-1 overflow-y-auto bg-gray-50 p-4 space-y-3">
        <MessageBubble
          v-for="msg in messages"
          :key="msg.id"
          :message="msg"
        />
        <div v-if="messages.length === 0" class="text-center py-12 text-gray-400">
          暂无消息，发送第一条消息吧
        </div>
      </div>

      <!-- Input -->
      <ChatInput @send="handleSend" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import MessageBubble from '@/components/chat/MessageBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { productApi } from '@/api/product'
import { userApi } from '@/api/user'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()
const messageList = ref(null)

const conversationId = computed(() => route.params.conversationId)
const messages = computed(() => chatStore.currentMessages)
const contactName = ref('用户')
const onlineStatus = ref(false)
const productInfo = ref(null)

// 从 "userIdA_userIdB" 规范格式解析，productId 从 query 参数获取
const convParts = computed(() => {
  const parts = route.params.conversationId.split('_')
  return {
    userIdA: parts[0],
    userIdB: parts[1],
    productId: route.query.productId || undefined
  }
})

const receiverId = computed(() => {
  const senderId = userStore.userInfo?.id
  if (!senderId) return null
  const sid = String(senderId)
  const parts = route.params.conversationId.split('_')
  return parts[0] === sid ? parts[1] : parts[0]
})

onMounted(async () => {
  await chatStore.fetchMessages(conversationId.value)
  await chatStore.markRead(conversationId.value)
  await loadProductInfo()
  await loadContactInfo()
  scrollToBottom()
})

async function loadProductInfo() {
  const pid = convParts.value.productId
  if (!pid) return
  try {
    const res = await productApi.getProduct(pid)
    productInfo.value = res.data?.data
  } catch {
    // ignore
  }
}

async function loadContactInfo() {
  const currentId = userStore.userInfo?.id
  const rid = receiverId.value
  if (!currentId || !rid) return

  try {
    const res = await userApi.getUserInfo(rid)
    if (res.data?.code === 200 && res.data.data) {
      contactName.value = res.data.data.username || '用户'
    }
  } catch {
    // keep default
  }
}

async function handleSend(text) {
  const senderId = userStore.userInfo?.id
  if (!senderId) {
    ElMessage.warning('请先登录')
    return
  }

  const rId = receiverId.value
  const pid = convParts.value.productId

  const msg = {
    content: text,
    toUserId: Number(rId),
    productId: pid ? Number(pid) : undefined
  }

  const res = await chatStore.sendMessage(msg)
  const saved = res?.data || res

  chatStore.addMessage({
    id: saved?.id || Date.now(),
    content: text,
    fromUserId: Number(senderId),
    toUserId: Number(rId),
    conversationId: conversationId.value,
    createdAt: saved?.createdAt || new Date().toISOString(),
    isRead: 0
  })

  await nextTick()
  scrollToBottom()
}

// 监听路由变化重新加载数据（同组件切换不同会话）
watch(conversationId, async () => {
  await chatStore.fetchMessages(conversationId.value)
  await chatStore.markRead(conversationId.value)
  await loadProductInfo()
  await loadContactInfo()
  scrollToBottom()
})

function scrollToBottom() {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}
</script>
