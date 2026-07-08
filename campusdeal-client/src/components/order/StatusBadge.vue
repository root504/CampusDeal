<template>
  <span
    class="badge"
    :class="statusClass"
  >
    {{ statusLabel }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  }
})

const statusMap = {
  pending: { label: '待付款', class: 'bg-orange-100 text-orange-800' },
  paid: { label: '待发货', class: 'bg-blue-100 text-blue-800' },
  shipped: { label: '待收货', class: 'bg-purple-100 text-purple-800' },
  received: { label: '已完成', class: 'bg-green-100 text-green-800' },
  completed: { label: '已完成', class: 'bg-green-100 text-green-800' },
  cancelled: { label: '已取消', class: 'bg-gray-100 text-gray-600' }
}

const statusLabel = computed(() => statusMap[props.status]?.label || props.status)
const statusClass = computed(() => statusMap[props.status]?.class || 'bg-gray-100 text-gray-600')
</script>
