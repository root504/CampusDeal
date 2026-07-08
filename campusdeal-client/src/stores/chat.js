import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { messageApi } from '@/api/message'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref([])
  const currentMessages = ref([])
  const unreadCount = ref(0)
  const loading = ref(false)

  const hasUnread = computed(() => unreadCount.value > 0)

  async function fetchConversations() {
    loading.value = true
    try {
      const res = await messageApi.getConversations()
      if (res.data.code === 200) {
        conversations.value = res.data.data || []
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchMessages(conversationId, page = 1, size = 50) {
    const res = await messageApi.getMessages(conversationId, { page, size })
    if (res.data.code === 200) {
      currentMessages.value = res.data.data?.list || []
    }
    return res.data
  }

  async function sendMessage(data) {
    const res = await messageApi.sendMessage(data)
    return res.data
  }

  function addMessage(message) {
    currentMessages.value.push(message)
  }

  async function markRead(conversationId) {
    await messageApi.markRead(conversationId)
    await fetchUnreadCount()
  }

  async function fetchUnreadCount() {
    try {
      const res = await messageApi.getUnreadCount()
      if (res.data.code === 200) {
        unreadCount.value = res.data.data || 0
      }
    } catch {
      // ignore
    }
  }

  return {
    conversations,
    currentMessages,
    unreadCount,
    loading,
    hasUnread,
    fetchConversations,
    fetchMessages,
    sendMessage,
    addMessage,
    markRead,
    fetchUnreadCount
  }
})
