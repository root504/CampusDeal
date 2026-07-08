<template>
  <div class="flex" :class="isMine ? 'justify-end' : 'justify-start'">
    <div class="max-w-[70%]">
      <div
        class="px-4 py-2.5 rounded-2xl text-sm leading-relaxed"
        :class="isMine ? 'bg-primary-600 text-white rounded-br-md' : 'bg-white border text-gray-900 rounded-bl-md'"
      >
        {{ message.content }}
      </div>
      <div class="text-xs text-gray-400 mt-1" :class="isMine ? 'text-right' : 'text-left'">
        {{ formatTime(message.createdAt) }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const userStore = useUserStore()
const isMine = computed(() => {
  // 系统消息类型始终显示在左边（白色气泡），不因 fromUserId 是谁而改变
  const sysTypes = ['system', 'order', 'announcement']
  if (props.message.messageType && sysTypes.includes(props.message.messageType)) {
    return false
  }
  const uid = userStore.userInfo?.id || userStore.userInfo?.userId
  return props.message.fromUserId === uid
})

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>
