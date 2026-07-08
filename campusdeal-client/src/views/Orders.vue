<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">我的订单</h1>

      <!-- Role Tabs -->
      <div class="flex space-x-2 mb-4 overflow-x-auto scrollbar-hide">
        <button
          v-for="role in roleTabs"
          :key="role.value"
          @click="activeRole = role.value; loadOrders()"
          class="px-4 py-2 rounded-full text-sm whitespace-nowrap transition-colors"
          :class="activeRole === role.value ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        >
          {{ role.label }}
        </button>
      </div>

      <!-- Status Tabs -->
      <div class="flex space-x-2 mb-6 overflow-x-auto scrollbar-hide">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="activeTab = tab.value; loadOrders()"
          class="px-4 py-2 rounded-full text-sm whitespace-nowrap transition-colors"
          :class="activeTab === tab.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-if="orders.length > 0" class="space-y-4">
        <OrderCard
          v-for="order in orders"
          :key="order.id"
          :order="order"
          :role="activeRole"
          :readonly="isReadonly"
          @pay="handlePay"
          @cancel="handleCancel"
          @receive="handleReceive"
          @contact="handleContact"
          @ship="handleShip"
        />
      </div>

      <div v-else-if="!loading" class="text-center py-12 text-gray-400">
        <p class="text-lg">暂无订单</p>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import OrderCard from '@/components/order/OrderCard.vue'
import { orderApi } from '@/api/order'

const route = useRoute()
const router = useRouter()

const isReadonly = computed(() => route.query.readonly === 'true')

const roleTabs = [
  { label: '我买入的', value: 'buyer' },
  { label: '我卖出的', value: 'seller' }
]

const tabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'pending' },
  { label: '待发货', value: 'paid' },
  { label: '待收货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

const activeRole = ref(route.query.role || 'buyer')
const activeTab = ref(route.query.status || '')
const orders = ref([])
const loading = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    const params = { role: activeRole.value }
    if (activeTab.value) params.status = activeTab.value
    const res = await orderApi.getOrders(params)
    orders.value = res.data?.data?.list || []
  } finally {
    loading.value = false
  }
}

async function handlePay(id) {
  await orderApi.payOrder(id)
  loadOrders()
}

async function handleCancel(id) {
  await orderApi.cancelOrder(id, '买家取消')
  loadOrders()
}

async function handleReceive(id) {
  await orderApi.receiveOrder(id)
  loadOrders()
}

function handleContact(sellerId) {
  // navigate to chat
}

async function handleShip(id) {
  await orderApi.shipOrder(id)
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>
