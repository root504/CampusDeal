<template>
  <div class="card mb-4 cursor-pointer hover:shadow-md transition-shadow" @click="$router.push('/orders/' + order.id)">
    <div class="flex items-center justify-between mb-3">
      <span class="text-sm text-gray-500">订单号：{{ order.orderNo }}</span>
      <StatusBadge :status="order.status" />
    </div>
    <div class="text-xs text-gray-500 mb-2">
      <template v-if="role === 'buyer'">
        卖家：{{ order.seller?.username || order.sellerId }}
      </template>
      <template v-else>
        买家：{{ order.buyer?.username || order.buyerId }}
      </template>
    </div>
    <div class="space-y-3">
      <div
        v-for="item in order.items"
        :key="item.id"
        class="flex items-center space-x-4 p-3 bg-gray-50 rounded-lg"
      >
        <img :src="item.productImage" class="w-16 h-16 object-cover rounded-lg" />
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-900 truncate">{{ item.productTitle }}</p>
          <p class="text-xs text-gray-500 mt-1">&yen;{{ item.price }} x {{ item.quantity }}</p>
        </div>
      </div>
    </div>
    <div class="flex items-center justify-between mt-4 pt-3 border-t">
      <span class="text-sm text-gray-600">
        共 {{ totalItems }} 件商品
      </span>
      <div class="flex items-center space-x-2">
        <span class="text-sm text-gray-500">合计：</span>
        <span class="text-lg font-bold text-accent-600">&yen;{{ order.totalAmount }}</span>
      </div>
    </div>
    <div v-if="!readonly" class="flex items-center justify-end space-x-2 mt-3 pt-3 border-t">
      <!-- 买家（我买入的）按钮 -->
      <template v-if="role === 'buyer'">
        <button
          v-if="order.status === 'pending'"
          @click.stop="$emit('pay', order.id)"
          class="btn-primary text-xs py-1.5 px-3"
        >
          立即付款
        </button>
        <button
          v-if="order.status === 'pending'"
          @click.stop="$emit('cancel', order.id)"
          class="btn-secondary text-xs py-1.5 px-3"
        >
          取消订单
        </button>
        <button
          v-if="order.status === 'paid'"
          @click.stop="$emit('cancel', order.id)"
          class="btn-secondary text-xs py-1.5 px-3"
        >
          取消订单
        </button>
        <button
          v-if="order.status === 'shipped'"
          @click.stop="$emit('receive', order.id)"
          class="btn-primary text-xs py-1.5 px-3"
        >
          确认收货
        </button>
        <button
          v-if="order.status === 'paid'"
          @click.stop="$emit('contact', order.sellerId)"
          class="btn-secondary text-xs py-1.5 px-3"
        >
          联系卖家
        </button>
      </template>
      <!-- 卖家（我卖出的）按钮 -->
      <template v-if="role === 'seller'">
        <span
          v-if="order.status === 'pending'"
          class="text-xs text-gray-400"
        >
          等待买家付款
        </span>
        <button
          v-if="order.status === 'paid'"
          @click.stop="$emit('ship', order.id)"
          class="btn-primary text-xs py-1.5 px-3"
        >
          确认发货
        </button>
        <button
          v-if="order.status === 'shipped'"
          @click.stop="$emit('contact', order.buyerId)"
          class="btn-secondary text-xs py-1.5 px-3"
        >
          联系买家
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import StatusBadge from './StatusBadge.vue'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  role: {
    type: String,
    default: 'buyer'
  },
  readonly: {
    type: Boolean,
    default: false
  }
})

defineEmits(['pay', 'cancel', 'receive', 'contact', 'ship', 'review'])

const totalItems = computed(() => {
  return (props.order.items || []).reduce((sum, item) => sum + item.quantity, 0)
})
</script>
