<template>
  <div>
    <button @click="$router.back()" class="flex items-center text-sm text-gray-500 hover:text-gray-700 mb-4">
      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      返回订单列表
    </button>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>

    <template v-else-if="order">
      <h1 class="text-2xl font-bold mb-6">订单详情</h1>

      <!-- 订单状态 -->
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-semibold">订单状态</h2>
          <StatusBadge :status="order.status" />
        </div>
        <div class="text-sm text-gray-500 space-y-2">
          <div class="flex justify-between">
            <span>订单编号</span>
            <span class="text-gray-800 font-mono">{{ order.orderNo }}</span>
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
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold mb-4">商品信息</h2>
        <table class="w-full text-sm" v-if="order.items && order.items.length > 0">
          <thead>
            <tr class="text-left text-gray-500 border-b">
              <th class="pb-2">商品</th>
              <th class="pb-2">单价</th>
              <th class="pb-2">数量</th>
              <th class="pb-2 text-right">小计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in order.items" :key="item.id" class="border-b">
              <td class="py-3">
                <div class="flex items-center space-x-3">
                  <img v-if="item.productImage" :src="item.productImage" class="w-10 h-10 object-cover rounded" />
                  <div v-else class="w-10 h-10 bg-gray-200 rounded flex items-center justify-center text-xs text-gray-400">无图</div>
                  <span class="text-gray-800">{{ item.productTitle }}</span>
                </div>
              </td>
              <td class="py-3">&yen;{{ item.price }}</td>
              <td class="py-3">{{ item.quantity }}</td>
              <td class="py-3 text-right font-medium">&yen;{{ (item.price * item.quantity).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-sm text-gray-400">暂无商品明细</div>
      </div>

      <!-- 订单信息 -->
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold mb-4">订单信息</h2>
        <div class="text-sm text-gray-500 space-y-2">
          <div class="flex justify-between">
            <span>买家</span>
            <span class="text-gray-800">{{ buyerName }}</span>
          </div>
          <div class="flex justify-between">
            <span>卖家</span>
            <span class="text-gray-800">{{ sellerName }}</span>
          </div>
          <div class="flex justify-between border-t pt-2 mt-2">
            <span class="font-medium text-gray-700">合计金额</span>
            <span class="text-lg font-bold text-primary-600">&yen;{{ computedTotal.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="flex justify-end space-x-3">
        <button v-if="order.status === 'pending'" @click="handleRemind" class="px-4 py-2 text-sm rounded-lg bg-orange-500 text-white hover:bg-orange-600 transition-colors">催付款</button>
        <button v-if="order.status === 'paid'" @click="handleShip" class="px-4 py-2 text-sm rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition-colors">发货</button>
        <button v-if="order.status === 'shipped'" @click="handleReceive" class="px-4 py-2 text-sm rounded-lg bg-green-500 text-white hover:bg-green-600 transition-colors">确认收货</button>
        <button v-if="order.status !== 'deleted'" @click="handleDelete" class="px-4 py-2 text-sm rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors">删除订单</button>
      </div>
    </template>

    <div v-else class="text-center py-12 text-gray-400">订单不存在</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import StatusBadge from '@/components/order/StatusBadge.vue'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)

const buyerName = computed(() => {
  return order.value?.buyer?.username || order.value?.buyerName || (order.value?.buyerId ? '用户' + order.value.buyerId : '-')
})

const sellerName = computed(() => {
  return order.value?.seller?.username || order.value?.sellerName || (order.value?.sellerId ? '用户' + order.value.sellerId : '-')
})

const computedTotal = computed(() => {
  if (order.value?.items?.length) {
    return order.value.items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
  }
  return order.value?.totalAmount || 0
})

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function loadOrder() {
  loading.value = true
  try {
    const id = route.params.id
    const res = await adminApi.getOrderDetail(id)
    order.value = res.data?.data || null
  } finally {
    loading.value = false
  }
}

async function handleRemind() {
  try {
    const { value } = await ElMessageBox.prompt('请输入催付款消息', '催付款', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputValue: '您的订单尚未付款，请尽快完成支付。',
      inputType: 'textarea',
      inputPlaceholder: '留空则使用默认消息'
    })
    const message = value?.trim() || null
    await adminApi.remindOrder(order.value.id, message)
    ElMessage.success('催缴通知已发送')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }
}

async function handleShip() {
  if (!confirm('确认发货？')) return
  try {
    await adminApi.shipOrder(order.value.id)
    ElMessage.success('发货成功')
    loadOrder()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

async function handleReceive() {
  if (!confirm('确认收货？')) return
  try {
    await adminApi.receiveOrder(order.value.id)
    ElMessage.success('收货成功')
    loadOrder()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

async function handleDelete() {
  if (!confirm(`确定要将订单「${order.value.orderNo}」移至已删除吗？`)) return
  try {
    await adminApi.deleteOrder(order.value.id)
    ElMessage.success('已移至已删除')
    router.push('/admin/orders')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(() => loadOrder())
</script>
