<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 flex flex-col max-w-2xl mx-auto w-full">
      <!-- 顶部导航 -->
      <div class="bg-white border-b px-4 py-3 flex items-center space-x-3">
        <button @click="$router.push('/messages')" class="text-gray-500 hover:text-gray-700">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <div class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <div>
            <p class="font-medium text-gray-900">系统通知</p>
            <p class="text-xs text-gray-400">CampusDeal 管理员</p>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="messageList" class="flex-1 overflow-y-auto bg-gray-50 p-4 space-y-3">
        <div v-if="messages.length === 0" class="text-center py-12 text-gray-400">
          <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
          </svg>
          <p>暂无系统通知</p>
        </div>

        <div v-for="msg in messages" :key="msg.id || msg._tempId" class="flex" :class="msg.from === 'user' ? 'justify-end' : 'justify-start'">
          <div class="max-w-[80%] rounded-2xl px-4 py-2.5"
            :class="msg.from === 'user'
              ? 'bg-primary-600 text-white rounded-br-md'
              : 'bg-white border border-gray-200 shadow-sm rounded-bl-md'"
          >
            <p class="text-sm whitespace-pre-wrap">{{ msg.content }}</p>
            <p class="text-xs mt-1" :class="msg.from === 'user' ? 'text-primary-200' : 'text-gray-400'">
              {{ formatTime(msg.createdAt) }}
            </p>
          </div>
        </div>
      </div>

      <!-- 发送区域 -->
      <ChatInput @send="handleSend" />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import { messageApi } from '@/api/message'

const messages = ref([])
const messageList = ref(null)

onMounted(async () => {
  await loadMessages()
})

async function loadMessages() {
  try {
    const res = await messageApi.getAdminMessages()
    if (res.data.code === 200) {
      messages.value = res.data.data || []
      await nextTick()
      scrollToBottom()
    }
  } catch {
    // ignore
  }
}

async function handleSend(text) {
  const tempMsg = {
    _tempId: Date.now(),
    content: text,
    from: 'user',
    createdAt: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  await nextTick()
  scrollToBottom()

  try {
    await messageApi.sendAdminMessage({ content: text })
    await loadMessages()
  } catch {
    // remove temp message on failure, reload
    messages.value = messages.value.filter(m => m._tempId !== tempMsg._tempId)
    loadMessages()
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function scrollToBottom() {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}
</script>
