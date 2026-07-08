<template>
  <div>
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500 mb-1">总用户数</p>
        <p class="text-3xl font-bold text-primary-600">{{ dashboard.totalUsers || 0 }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500 mb-1">商品总数</p>
        <p class="text-3xl font-bold text-accent-600">{{ dashboard.totalProducts || 0 }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500 mb-1">订单总数</p>
        <p class="text-3xl font-bold text-green-600">{{ dashboard.totalOrders || 0 }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <p class="text-sm text-gray-500 mb-1">交易总额</p>
        <p class="text-3xl font-bold text-purple-600">&yen;{{ formatAmount(dashboard.totalAmount || 0) }}</p>
      </div>
    </div>

    <!-- Trend Chart -->
    <div class="bg-white rounded-xl p-6 shadow-sm mb-6">
      <h3 class="text-lg font-semibold mb-4">7日交易趋势</h3>
      <div class="overflow-y-auto" style="max-height: 300px;">
        <div class="flex items-end space-x-3 h-48 min-h-[180px]">
          <div
            v-for="(item, index) in trend"
            :key="index"
            class="flex-1 flex flex-col items-center"
          >
            <span class="text-xs text-gray-500 mb-1">{{ item.amount || 0 }}</span>
            <div
              class="w-full bg-primary-500 rounded-t transition-all"
              :style="{ height: getBarHeight(item.amount) }"
            ></div>
            <span class="text-xs text-gray-400 mt-2">{{ item.date }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Category Distribution -->
    <div class="bg-white rounded-xl p-6 shadow-sm">
      <h3 class="text-lg font-semibold mb-4">分类分布</h3>
      <div class="overflow-y-auto" style="max-height: 350px;">
        <div class="space-y-3">
          <div v-for="cat in categoryDist" :key="cat.name" class="flex items-center space-x-3">
            <span class="w-20 text-sm text-gray-600">{{ cat.name }}</span>
            <div class="flex-1 bg-gray-100 rounded-full h-5 overflow-hidden">
              <div
                class="h-full bg-primary-500 rounded-full transition-all"
                :style="{ width: getPercent(cat.count) + '%' }"
              ></div>
            </div>
            <span class="text-sm text-gray-500 w-12 text-right">{{ cat.count }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const dashboard = ref({})
const trend = ref([])
const categoryDist = ref([])

const maxAmount = ref(1)

onMounted(async () => {
  try {
    const [dashRes, trendRes, catRes] = await Promise.all([
      adminApi.getDashboard(),
      adminApi.getTrend(),
      adminApi.getCategoryDist()
    ])
    dashboard.value = dashRes.data?.data || {}
    trend.value = trendRes.data?.data || []
    categoryDist.value = catRes.data?.data || []

    maxAmount.value = Math.max(...trend.value.map(t => t.amount || 0), 1)
  } catch {
    // ignore
  }
})

function getBarHeight(amount) {
  return ((amount || 0) / maxAmount.value * 100) + '%'
}

function getPercent(count) {
  const total = categoryDist.value.reduce((s, c) => s + c.count, 0)
  return total > 0 ? Math.round((count / total) * 100) : 0
}

function formatAmount(amount) {
  if (amount >= 10000) return (amount / 10000).toFixed(1) + '万'
  return amount.toString()
}
</script>
