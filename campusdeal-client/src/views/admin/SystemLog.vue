<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center space-x-2">
        <h2 class="text-xl font-bold">系统日志</h2>
        <span class="inline-flex items-center justify-center px-2 min-w-[20px] h-5 rounded-full bg-primary-100 text-primary-700 text-xs font-medium">
          {{ total }}
        </span>
      </div>
      <button @click="showClearDialog = true" class="px-3 py-1.5 text-sm rounded border border-red-200 text-red-600 hover:bg-red-50 transition-colors">
        清空日志
      </button>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-gray-600 w-24">ID</th>
            <th class="px-4 py-3 text-left text-gray-600 w-28">操作类型</th>
            <th class="px-4 py-3 text-left text-gray-600">描述</th>
            <th class="px-4 py-3 text-left text-gray-600 w-24">操作人</th>
            <th class="px-4 py-3 text-left text-gray-600 w-32">IP地址</th>
            <th class="px-4 py-3 text-left text-gray-600 w-40">操作时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500">{{ log.id }}</td>
            <td class="px-4 py-3">
              <span class="badge text-xs" :class="getActionClass(log.action)">{{ log.action }}</span>
            </td>
            <td class="px-4 py-3 text-gray-700 max-w-[400px] truncate" :title="log.description">{{ log.description }}</td>
            <td class="px-4 py-3">{{ log.operator }}</td>
            <td class="px-4 py-3 text-gray-500 font-mono text-xs">{{ log.ip }}</td>
            <td class="px-4 py-3 text-gray-500">{{ formatDateTime(log.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="logs.length === 0" class="text-center py-8 text-gray-400">暂无数据</div>
    </div>

    <div class="flex items-center justify-between mt-4">
      <span class="text-sm text-gray-500">共 {{ total }} 条记录</span>
      <div class="flex items-center space-x-2">
        <button @click="prevPage" :disabled="page <= 1" class="px-3 py-1.5 text-sm rounded border hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">上一页</button>
        <span class="text-sm text-gray-600">{{ page }} / {{ totalPages }}</span>
        <button @click="nextPage" :disabled="page >= totalPages" class="px-3 py-1.5 text-sm rounded border hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">下一页</button>
      </div>
    </div>

    <!-- 清空日志确认弹窗 -->
    <div v-if="showClearDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showClearDialog = false">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">清空系统日志</h3>
        <p class="text-sm text-gray-600 mb-6">是否已将数据导出？</p>
        <div class="flex justify-end space-x-3">
          <button @click="handleNo" class="px-4 py-2 text-sm rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50">否</button>
          <button @click="handleClear" :disabled="clearing" class="px-4 py-2 text-sm rounded-lg bg-red-600 text-white hover:bg-red-700 disabled:opacity-50">
            {{ clearing ? '清空中...' : '是' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const logs = ref([])
const page = ref(1)
const size = ref(8)
const total = ref(0)
const showClearDialog = ref(false)
const clearing = ref(false)
const totalPages = computed(() => Math.ceil(total.value / size.value) || 1)

onMounted(() => loadLogs())

async function loadLogs() {
  try {
    const res = await adminApi.getSystemLogs({ page: page.value, size: size.value })
    const data = res.data?.data
    logs.value = data?.list || data?.records || []
    total.value = data?.total || 0
  } catch {
    // ignore
  }
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadLogs()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    loadLogs()
  }
}

function handleNo() {
  showClearDialog.value = false
  router.push('/admin/logs')
  ElMessage.info('请先导出系统日志后再清空')
}

async function handleClear() {
  clearing.value = true
  try {
    await adminApi.clearLogs()
    showClearDialog.value = false
    ElMessage.success('系统日志已清空')
    page.value = 1
    loadLogs()
  } catch {
    ElMessage.error('清空失败')
  } finally {
    clearing.value = false
  }
}

function getActionClass(action) {
  const map = {
    '用户登录': 'bg-blue-100 text-blue-800',
    '商品审核': 'bg-purple-100 text-purple-800',
    '商品下架': 'bg-orange-100 text-orange-800',
    '商品上架': 'bg-green-100 text-green-800'
  }
  return map[action] || 'bg-gray-100 text-gray-800'
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>
