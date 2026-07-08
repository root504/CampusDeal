<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- 返回按钮 -->
      <button
        @click="$router.back()"
        class="flex items-center text-sm text-gray-500 hover:text-gray-700 mb-4 transition-colors"
      >
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回订单列表
      </button>

      <!-- 加载中 -->
      <div v-if="loading" class="text-center py-12 text-gray-400">
        <p>加载中...</p>
      </div>

      <!-- 订单详情 -->
      <template v-else-if="order">
        <h1 class="text-2xl font-bold mb-6">订单详情</h1>

        <!-- 收货地址 -->
        <div class="card mb-6 cursor-pointer hover:bg-gray-50 transition-colors" @click="$router.push('/settings')">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold mb-3">收货地址</h2>
            <svg class="w-5 h-5 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </div>
          <div v-if="defaultAddress" class="text-sm text-gray-700 space-y-1">
            <p><span class="text-gray-500">收货人：</span>{{ defaultAddress.receiverName }} {{ defaultAddress.phone }}</p>
            <p><span class="text-gray-500">地址：</span>{{ defaultAddress.province }}{{ defaultAddress.city }}{{ defaultAddress.district }} {{ defaultAddress.detail }}</p>
          </div>
          <div v-else class="text-sm text-gray-400">
            暂无收货地址，付款前请先添加收货地址
          </div>
        </div>

        <!-- 订单状态 -->
        <div class="card mb-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold">订单状态</h2>
            <StatusBadge :status="order.status" />
          </div>
          <div class="text-sm text-gray-500 space-y-2">
            <div class="flex justify-between">
              <span>订单编号</span>
              <span class="text-gray-800">{{ order.orderNo }}</span>
            </div>
            <div class="flex justify-between">
              <span>下单时间</span>
              <span class="text-gray-800">{{ formatTime(order.createdAt) }}</span>
            </div>
            <div v-if="order.paymentTime" class="flex justify-between">
              <span>付款时间</span>
              <span class="text-gray-800">{{ formatTime(order.paymentTime) }}</span>
            </div>
            <div v-if="order.shipTime" class="flex justify-between">
              <span>发货时间</span>
              <span class="text-gray-800">{{ formatTime(order.shipTime) }}</span>
            </div>
            <div v-if="order.receiveTime" class="flex justify-between">
              <span>收货时间</span>
              <span class="text-gray-800">{{ formatTime(order.receiveTime) }}</span>
            </div>
            <div v-if="order.completeTime" class="flex justify-between">
              <span>完成时间</span>
              <span class="text-gray-800">{{ formatTime(order.completeTime) }}</span>
            </div>
            <div v-if="order.cancelTime" class="flex justify-between">
              <span>取消时间</span>
              <span class="text-gray-800">{{ formatTime(order.cancelTime) }}</span>
            </div>
            <div v-if="order.cancelReason" class="flex justify-between">
              <span>取消原因</span>
              <span class="text-red-600">{{ order.cancelReason }}</span>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="card mb-6">
          <h2 class="text-lg font-semibold mb-4">商品信息</h2>
          <div v-if="order.items && order.items.length > 0" class="space-y-3">
            <div
              v-for="item in order.items"
              :key="item.id"
              class="flex items-center space-x-4 p-3 bg-gray-50 rounded-lg"
            >
              <img
                v-if="item.productImage"
                :src="item.productImage"
                class="w-16 h-16 object-cover rounded-lg"
              />
              <div
                v-else
                class="w-16 h-16 bg-gray-200 rounded-lg flex items-center justify-center text-gray-400 text-xs"
              >
                无图
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate">{{ item.productTitle }}</p>
                <p class="text-xs text-gray-500 mt-1">
                  &yen;{{ item.price }} x {{ item.quantity }}
                </p>
              </div>
              <span class="text-sm font-medium text-accent-600">
                &yen;{{ (item.price * item.quantity).toFixed(2) }}
              </span>
            </div>
          </div>
          <div v-else class="text-sm text-gray-400">暂无商品明细</div>
        </div>

        <!-- 交易双方与金额 -->
        <div class="card mb-6">
          <h2 class="text-lg font-semibold mb-4">订单信息</h2>
          <div class="text-sm text-gray-500 space-y-2">
            <div class="flex justify-between">
              <span>买家</span>
              <span class="text-gray-800">{{ order.buyerName || order.buyer?.username || order.buyerId }}</span>
            </div>
            <div class="flex justify-between">
              <span>卖家</span>
              <span class="text-gray-800">{{ order.sellerName || order.seller?.username || order.sellerId }}</span>
            </div>
            <div class="flex justify-between border-t pt-2 mt-2">
              <span class="font-medium text-gray-700">合计金额</span>
              <span class="text-lg font-bold text-accent-600">&yen;{{ computedTotal.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="flex justify-end space-x-2">
          <!-- 支付按钮：需要 pending 状态 + 是买家 + 有收货地址 -->
          <button
            v-if="order.status === 'pending' && isBuyer"
            :disabled="!hasAddress"
            @click="handlePay"
            :class="[
              'text-sm py-2 px-4 rounded-lg transition-colors',
              hasAddress
                ? 'btn-primary'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            ]"
            :title="!hasAddress ? '请先添加收货地址' : ''"
          >
            立即付款
          </button>
          <p v-if="order.status === 'pending' && isBuyer && !hasAddress" class="text-xs text-red-500 self-center">
            请先补充收货地址
          </p>
          <!-- 取消订单：pending 或 paid 状态 + 买家 -->
          <button
            v-if="(order.status === 'pending' || order.status === 'paid') && isBuyer"
            @click="handleCancel"
            class="btn-secondary text-sm py-2 px-4"
          >
            取消订单
          </button>
          <!-- 确认收货：shipped 状态 + 买家 -->
          <button
            v-if="order.status === 'shipped' && isBuyer"
            @click="handleReceive"
            class="btn-primary text-sm py-2 px-4"
          >
            确认收货
          </button>
          <!-- 评价：completed 状态 + 买家 -->
          <button
            v-if="order.status === 'completed' && isBuyer"
            @click="$router.push('/review/' + order.id)"
            class="btn-primary text-sm py-2 px-4"
          >
            评价
          </button>
        </div>
      </template>

      <!-- 订单不存在 -->
      <div v-else class="text-center py-12 text-gray-400">
        <p class="text-lg">订单不存在</p>
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
import StatusBadge from '@/components/order/StatusBadge.vue'
import { orderApi } from '@/api/order'
import { addressApi } from '@/api/address'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)
const defaultAddress = ref(null)

const currentUserId = computed(() => {
  return order.value ? getUserIdFromToken() : null
})

const isBuyer = computed(() => {
  return order.value && String(currentUserId.value) === String(order.value.buyerId)
})

const hasAddress = computed(() => {
  return defaultAddress.value !== null
})

const computedTotal = computed(() => {
  if (order.value?.items?.length) {
    return order.value.items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
  }
  return order.value?.totalAmount || 0
})

function getUserIdFromToken() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return null
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Number(payload.sub)
  } catch {
    return null
  }
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function loadOrder() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await orderApi.getOrder(id)
    order.value = res.data?.data || null
  } finally {
    loading.value = false
  }
}

async function loadAddress() {
  try {
    const res = await addressApi.getList()
    const addresses = res.data?.data || []
    defaultAddress.value = addresses.find(a => a.isDefault === 1) || addresses[0] || null
  } catch {
    defaultAddress.value = null
  }
}

async function handlePay() {
  try {
    await orderApi.payOrder(order.value.id)
    await loadOrder()
    ElMessage.success('支付成功')
  } catch (err) {
    const msg = err?.response?.data?.message || '支付失败，请稍后重试'
    ElMessage.error(msg)
  }
}

async function handleCancel() {
  await orderApi.cancelOrder(order.value.id, '买家取消')
  await loadOrder()
}

async function handleReceive() {
  await orderApi.receiveOrder(order.value.id)
  await loadOrder()
}

onMounted(() => {
  loadOrder()
  loadAddress()
})
</script>
