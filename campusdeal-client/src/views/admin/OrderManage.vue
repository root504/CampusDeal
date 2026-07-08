<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div class="flex space-x-2">
        <button
          v-for="s in statuses"
          :key="s.value"
          @click="activeStatus = s.value; currentPage = 1; loadOrders()"
          class="px-3 py-1.5 rounded-full text-sm transition-colors"
          :class="activeStatus === s.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        >
          {{ s.label }}
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4 mb-6">
      <div class="bg-white rounded-xl shadow-sm p-4">
        <p class="text-sm text-gray-500">总订单数</p>
        <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.totalOrders }}</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-4">
        <p class="text-sm text-gray-500">待发货</p>
        <p class="text-2xl font-bold text-orange-600 mt-1">{{ stats.pendingShip }}</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-4">
        <p class="text-sm text-gray-500">已完成</p>
        <p class="text-2xl font-bold text-green-600 mt-1">{{ stats.completed }}</p>
      </div>
      <div class="bg-white rounded-xl shadow-sm p-4">
        <p class="text-sm text-gray-500">总交易金额</p>
        <p class="text-2xl font-bold text-primary-600 mt-1">&yen;{{ stats.totalAmount }}</p>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-gray-600">订单编号</th>
            <th class="px-4 py-3 text-left text-gray-600">买家</th>
            <th class="px-4 py-3 text-left text-gray-600">卖家</th>
            <th class="px-4 py-3 text-left text-gray-600">金额</th>
            <th class="px-4 py-3 text-left text-gray-600">状态</th>
            <th class="px-4 py-3 text-left text-gray-600">时间</th>
            <th class="px-4 py-3 text-left text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 font-mono text-xs">{{ order.orderNo }}</td>
            <td class="px-4 py-3">
              <router-link :to="`/admin/orders/${order.id}`" class="text-primary-600 hover:underline">
                {{ order.buyer?.username || '-' }}
              </router-link>
            </td>
            <td class="px-4 py-3">
              <router-link :to="`/admin/orders/${order.id}`" class="text-primary-600 hover:underline">
                {{ order.seller?.username || '-' }}
              </router-link>
            </td>
            <td class="px-4 py-3 font-medium">&yen;{{ order.totalAmount }}</td>
            <td class="px-4 py-3">
              <StatusBadge :status="order.status" />
            </td>
            <td class="px-4 py-3 text-gray-500">{{ formatDate(order.createdAt) }}</td>
            <td class="px-4 py-3">
              <div class="flex space-x-2">
                <button v-if="order.status === 'pending'" @click="handleRemind(order.id)" class="text-orange-600 hover:text-orange-800 text-xs font-medium">催付款</button>
                <button v-if="order.status === 'paid'" @click="handleShip(order.id)" class="text-blue-600 hover:text-blue-800 text-xs font-medium">发货</button>
                <button v-if="order.status === 'shipped'" @click="handleReceive(order.id)" class="text-green-600 hover:text-green-800 text-xs font-medium">确认收货</button>
                <button v-if="order.status !== 'deleted'" @click="handleDelete(order)" class="text-red-600 hover:text-red-800 text-xs font-medium">删除</button>
                <router-link :to="`/admin/orders/${order.id}`" class="text-gray-500 hover:text-gray-700 text-xs font-medium">详情</router-link>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="orders.length === 0" class="text-center py-8 text-gray-400">暂无订单</div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-6" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import StatusBadge from '@/components/order/StatusBadge.vue'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const statuses = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'pending' },
  { label: '待发货', value: 'paid' },
  { label: '待收货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
  { label: '已删除', value: 'deleted' }
]

const activeStatus = ref('')
const orders = ref([])
const currentPage = ref(1)
const pageSize = 8
const total = ref(0)
const stats = reactive({ totalOrders: 0, pendingShip: 0, completed: 0, totalAmount: 0 })

onMounted(() => { loadOrders(); loadStats() })

async function loadOrders() {
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (activeStatus.value) params.status = activeStatus.value
    const res = await adminApi.getOrders(params)
    const data = res.data?.data
    orders.value = data?.records || data?.list || data || []
    total.value = data?.total ?? data?.totalElements ?? 0
  } catch {
    // ignore
  }
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

async function handleRemind(id) {
  try {
    const { value } = await ElMessageBox.prompt('请输入催付款消息', '催付款', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputValue: '您的订单尚未付款，请尽快完成支付。',
      inputType: 'textarea',
      inputPlaceholder: '留空则使用默认消息'
    })
    const message = value?.trim() || null
    await adminApi.remindOrder(id, message)
    ElMessage.success('催缴通知已发送')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }
}

async function handleShip(id) {
  ElMessageBox.confirm('确认发货？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.shipOrder(id)
      ElMessage.success('发货成功')
      loadOrders()
      loadStats()
    } catch (e) {
      ElMessage.error(e?.message || e?.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

async function handleReceive(id) {
  ElMessageBox.confirm('确认收货？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.receiveOrder(id)
      ElMessage.success('收货成功')
      loadOrders()
      loadStats()
    } catch (e) {
      ElMessage.error(e?.message || e?.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

async function handleDelete(order) {
  ElMessageBox.confirm(`确定要将订单「${order.orderNo}」移至已删除吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.deleteOrder(order.id)
      ElMessage.success('已移至已删除')
      loadOrders()
      loadStats()
    } catch (e) {
      ElMessage.error(e?.message || e?.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

async function loadStats() {
  try {
    const res = await adminApi.getOrderStats()
    if (res.data?.data) {
      const d = res.data.data
      stats.totalOrders = d.totalOrders || 0
      stats.pendingShip = d.pendingShip || 0
      stats.completed = d.completed || 0
      stats.totalAmount = d.totalAmount || 0
    }
  } catch {
    // ignore
  }
}
</script>
