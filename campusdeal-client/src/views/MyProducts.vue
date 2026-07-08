<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">我的发布</h1>

      <!-- Status Filter Tabs -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="activeTab = tab.value; page = 1; fetchProducts()"
          class="px-4 py-2 rounded-full text-sm transition-colors"
          :class="activeTab === tab.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>

      <!-- Empty -->
      <div v-else-if="products.length === 0" class="text-center py-12">
        <p class="text-gray-400 mb-4">暂无发布的商品，去发布一个吧</p>
        <router-link to="/publish" class="btn-primary inline-block">发布商品</router-link>
      </div>

      <!-- Product List -->
      <div v-else class="space-y-4">
        <div
          v-for="product in products"
          :key="product.id"
          class="card flex gap-4"
        >
          <img
            :src="product.coverImage"
            class="w-24 h-24 rounded-lg object-cover flex-shrink-0"
            @error="onImgError"
          />
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between gap-2">
              <h3 class="font-semibold text-gray-900 truncate">{{ product.title }}</h3>
              <span class="text-sm font-bold text-red-500 flex-shrink-0">&yen;{{ product.price }}</span>
            </div>
            <p class="text-xs text-gray-400 mt-1">{{ formatTime(product.createdAt) }}</p>
            <div class="flex items-center gap-2 mt-2">
              <!-- Status Badge -->
              <span
                class="text-xs px-2 py-0.5 rounded-full"
                :class="statusBadgeClass(product.status)"
              >
                {{ statusLabel(product.status) }}
              </span>
              <!-- Reject Reason -->
              <span v-if="product.status === 2 && product.auditRemark" class="text-xs text-red-500 truncate flex-1">
                驳回理由：{{ product.auditRemark }}
              </span>
            </div>
            <!-- Actions -->
            <div class="flex gap-2 mt-3">
              <button
                @click="editProduct(product.id)"
                class="text-xs px-3 py-1 rounded border border-primary-300 text-primary-600 hover:bg-primary-50 transition-colors"
              >
                编辑
              </button>
              <button
                v-if="product.status === 2 && product.appealStatus === 0"
                @click="openAppealDialog(product)"
                class="text-xs px-3 py-1 rounded border border-yellow-300 text-yellow-600 hover:bg-yellow-50 transition-colors"
              >
                申诉
              </button>
              <button
                v-if="product.status === 1"
                @click="confirmDelist(product)"
                class="text-xs px-3 py-1 rounded border border-orange-300 text-orange-500 hover:bg-orange-50 transition-colors"
              >
                下架
              </button>
              <button
                @click="confirmDelete(product)"
                class="text-xs px-3 py-1 rounded border border-red-300 text-red-500 hover:bg-red-50 transition-colors"
              >
                删除
              </button>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 pt-4">
          <button
            :disabled="page <= 1"
            @click="page--; fetchProducts()"
            class="px-3 py-1 text-sm rounded border border-gray-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            上一页
          </button>
          <span class="text-sm text-gray-500">第 {{ page }} / {{ totalPages }} 页</span>
          <button
            :disabled="page >= totalPages"
            @click="page++; fetchProducts()"
            class="px-3 py-1 text-sm rounded border border-gray-300 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            下一页
          </button>
        </div>
      </div>
    </main>
    <AppFooter />

    <!-- 申诉弹窗 -->
    <div v-if="appealVisible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="appealVisible = false">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4 max-h-[85vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-4">商品申诉</h3>
        <p class="text-sm text-gray-500 mb-4">驳回理由：{{ appealTarget?.auditRemark || '无' }}</p>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">商品标题</label>
            <input v-model="appealForm.title" type="text" class="input-field w-full" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">描述</label>
            <textarea v-model="appealForm.description" rows="3" class="input-field w-full"></textarea>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">价格</label>
              <input v-model.number="appealForm.price" type="number" step="0.01" class="input-field w-full" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">原价</label>
              <input v-model.number="appealForm.originalPrice" type="number" step="0.01" class="input-field w-full" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              申诉理由 <span class="text-red-500">*</span>
            </label>
            <textarea v-model="appealForm.appealReason" rows="3" class="input-field w-full" placeholder="请说明申诉理由..."></textarea>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-5">
          <button @click="appealVisible = false" class="btn-secondary">取消</button>
          <button @click="submitAppeal" class="btn-primary" :disabled="appealSubmitting">
            {{ appealSubmitting ? '提交中...' : '提交申诉' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { productApi } from '@/api/product'

const router = useRouter()
const route = useRoute()

const tabs = [
  { label: '全部', value: null },
  { label: '待审核', value: 0 },
  { label: '已审核', value: 1 },
  { label: '已驳回', value: 2 },
  { label: '已下架', value: 3 }
]

const activeTab = ref(null)
const products = ref([])
const loading = ref(false)
const page = ref(1)
const totalPages = ref(1)

function statusLabel(status) {
  const map = { 0: '待审核', 1: '已审核', 2: '已驳回', 3: '已下架' }
  return map[status] || '未知'
}

function statusBadgeClass(status) {
  const map = {
    0: 'bg-yellow-100 text-yellow-800',
    1: 'bg-green-100 text-green-800',
    2: 'bg-red-100 text-red-800',
    3: 'bg-gray-100 text-gray-600'
  }
  return map[status] || 'bg-gray-100 text-gray-600'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function onImgError(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" fill="%23ddd"><rect width="96" height="96"/><text x="50%" y="50%" dominant-baseline="central" text-anchor="middle" fill="%23999" font-size="12">无图片</text></svg>'
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (activeTab.value !== null) {
      params.status = activeTab.value
    }
    const res = await productApi.getMyProducts(params)
    if (res.data?.code === 200) {
      const data = res.data.data
      products.value = data.list || []
      totalPages.value = Math.max(1, Math.ceil((data.total || 0) / 20))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function editProduct(id) {
  router.push(`/publish?id=${id}`)
}

async function confirmDelete(product) {
  try {
    await ElMessageBox.confirm(`确定要删除商品「${product.title}」吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await productApi.deleteProduct(product.id)
    if (res.data?.code === 200) {
      ElMessage.success('删除成功')
      fetchProducts()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
}

async function confirmDelist(product) {
  try {
    await ElMessageBox.confirm(
      `确定要下架商品「${product.title}」吗？下架后商品将不再展示。`,
      '确认下架',
      {
        confirmButtonText: '下架',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await productApi.updateProduct(product.id, { status: 3 })
    if (res.data?.code === 200) {
      ElMessage.success('下架成功')
      fetchProducts()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '下架失败')
    }
  }
}

// ========== 申诉相关 ==========
const appealVisible = ref(false)
const appealTarget = ref(null)
const appealSubmitting = ref(false)
const appealForm = ref({
  title: '',
  description: '',
  price: null,
  originalPrice: null,
  appealReason: ''
})

function openAppealDialog(product) {
  appealTarget.value = product
  appealForm.value = {
    title: product.title || '',
    description: product.description || '',
    price: product.price || null,
    originalPrice: product.originalPrice || null,
    appealReason: ''
  }
  appealVisible.value = true
}

async function submitAppeal() {
  if (!appealForm.value.appealReason.trim()) {
    ElMessage.warning('请填写申诉理由')
    return
  }
  appealSubmitting.value = true
  try {
    const res = await productApi.submitAppeal(appealTarget.value.id, appealForm.value)
    if (res.data?.code === 200) {
      ElMessage.success('申诉已提交')
      appealVisible.value = false
      fetchProducts()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '申诉提交失败')
  } finally {
    appealSubmitting.value = false
  }
}

onMounted(async () => {
  await fetchProducts()
  const appealId = route.query.appeal
  if (appealId) {
    const p = products.value.find(item => String(item.id) === String(appealId))
    if (p) openAppealDialog(p)
  }
})
</script>
