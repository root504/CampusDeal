<template>
  <div>
    <!-- 返回 + 用户信息 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-4">
        <button @click="goBack" class="flex items-center space-x-1 text-gray-500 hover:text-gray-700 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          <span class="text-sm">返回用户列表</span>
        </button>
      </div>
    </div>

    <!-- 用户信誉分概览 -->
    <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
      <div class="flex items-center space-x-4">
        <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
          <span class="text-primary-600 text-lg font-bold">{{ username?.charAt(0) || 'U' }}</span>
        </div>
        <div>
          <h2 class="text-xl font-bold text-gray-900">{{ username }}</h2>
          <p class="text-sm text-gray-500">用户ID：{{ userId }}</p>
        </div>
        <div class="ml-auto text-right">
          <p class="text-sm text-gray-500">当前信誉分</p>
          <p class="text-3xl font-extrabold" :class="currentScore >= 60 ? 'text-green-600' : 'text-red-500'">{{ currentScore }}</p>
        </div>
      </div>
    </div>

    <!-- 变更记录表格 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b">
        <h3 class="text-lg font-semibold text-gray-900">信誉分变更明细</h3>
      </div>
      <table class="w-full text-sm" v-if="paginatedRecords.length > 0">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-gray-600 font-semibold">变更时间</th>
            <th class="px-6 py-3 text-left text-gray-600 font-semibold">变更类型</th>
            <th class="px-6 py-3 text-left text-gray-600 font-semibold">分值变化</th>
            <th class="px-6 py-3 text-left text-gray-600 font-semibold">变更原因</th>
            <th class="px-6 py-3 text-left text-gray-600 font-semibold">操作后分数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedRecords" :key="item.id" class="border-t hover:bg-gray-50 transition-colors">
            <td class="px-6 py-3 text-gray-600">{{ formatDateTime(item.createdAt) }}</td>
            <td class="px-6 py-3">
              <span
                class="inline-block px-2 py-0.5 rounded-full text-xs font-medium"
                :class="item.changeAmount >= 0 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-500'"
              >
                {{ item.changeAmount >= 0 ? '加分' : '减分' }}
              </span>
            </td>
            <td class="px-6 py-3">
              <span class="font-semibold" :class="item.changeAmount >= 0 ? 'text-green-600' : 'text-red-500'">
                {{ item.changeAmount >= 0 ? '+' : '' }}{{ item.changeAmount }}
              </span>
            </td>
            <td class="px-6 py-3 text-gray-700">{{ item.reason }}</td>
            <td class="px-6 py-3">
              <span class="font-semibold" :class="item.afterScore >= 60 ? 'text-green-600' : 'text-red-500'">
                {{ item.afterScore }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="text-center py-12 text-gray-400">
        <svg class="w-12 h-12 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p>暂无变更记录</p>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="px-6 py-4 border-t flex items-center justify-between">
        <span class="text-sm text-gray-500">共 {{ allRecords.length }} 条记录</span>
        <div class="flex items-center space-x-2">
          <button
            @click="currentPage--"
            :disabled="currentPage <= 1"
            class="px-3 py-1.5 text-sm border rounded-md"
            :class="currentPage <= 1 ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-gray-50'"
          >
            上一页
          </button>
          <span class="text-sm text-gray-600 px-2">{{ currentPage }} / {{ totalPages }}</span>
          <button
            @click="currentPage++"
            :disabled="currentPage >= totalPages"
            class="px-3 py-1.5 text-sm border rounded-md"
            :class="currentPage >= totalPages ? 'text-gray-300 border-gray-200 cursor-not-allowed' : 'text-gray-600 border-gray-300 hover:bg-gray-50'"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const userId = ref(route.query.userId || '')
const username = ref(route.query.username || '未知用户')
const currentScore = ref(Number(route.query.currentScore) || 100)
const allRecords = ref([])
const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(allRecords.value.length / pageSize) || 1)

// 按时间升序排列（旧→新），计算每行的操作后分数
const sortedRecords = computed(() => {
  const records = [...allRecords.value]
  records.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  let runningScore = currentScore.value - records.reduce((sum, r) => sum + r.changeAmount, 0)
  return records.map(r => {
    runningScore += r.changeAmount
    return { ...r, afterScore: runningScore }
  })
})

// 当前页记录（按时间降序展示，新→旧）
const paginatedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const slice = sortedRecords.value.slice(start, start + pageSize)
  return slice.reverse()
})

onMounted(async () => {
  try {
    const res = await adminApi.getUserCreditDetail(userId.value, { page: 1, size: 9999 })
    if (res.data?.code === 200) {
      const data = res.data.data
      allRecords.value = data.list || data.records || []
    }
  } catch {
    allRecords.value = []
  }
})

function goBack() {
  router.push('/admin/credit-rules')
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<style scoped>
.text-green-600 { color: #16a34a; }
.text-green-700 { color: #15803d; }
.text-red-500 { color: #ef4444; }
.text-gray-500 { color: #6b7280; }
.text-gray-600 { color: #4b5563; }
.text-gray-700 { color: #374151; }
.text-gray-900 { color: #111827; }
.text-gray-400 { color: #9ca3af; }
.text-gray-300 { color: #d1d5db; }
.text-primary-600 { color: #6366f1; }
.bg-gray-50 { background: #f9fafb; }
.bg-white { background: #fff; }
.bg-primary-100 { background: #e0e7ff; }
.bg-green-100 { background: #dcfce7; }
.bg-red-100 { background: #fee2e2; }
.text-primary-600 { color: #6366f1; }
.font-extrabold { font-weight: 800; }
</style>
