<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- Filters -->
      <div class="mb-6 space-y-4">
        <!-- Search -->
        <div class="flex items-center space-x-4">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索商品..."
            class="input-field flex-1 max-w-md"
            @keyup.enter="handleSearch"
          />
          <button @click="handleSearch" class="btn-primary">搜索</button>
        </div>

        <!-- Sort -->
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-500">排序：</span>
          <button
            v-for="s in sorts"
            :key="s.value"
            @click="sortBy = s.value; loadProducts(true)"
            class="px-3 py-1.5 rounded-full text-sm transition-colors"
            :class="sortBy === s.value ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          >
            {{ s.label }}
          </button>
        </div>

        <!-- Category filter -->
        <div class="flex items-center space-x-2 flex-wrap">
          <span class="text-sm text-gray-500">分类：</span>
          <button
            @click="categoryCode = ''; loadProducts(true)"
            class="px-3 py-1.5 rounded-full text-sm transition-colors"
            :class="!categoryCode ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          >
            全部
          </button>
          <button
            v-for="cat in categories"
            :key="cat.code"
            @click="categoryCode = cat.code; loadProducts(true)"
            class="px-3 py-1.5 rounded-full text-sm transition-colors"
            :class="categoryCode === cat.code ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
          >
            {{ cat.name }}
          </button>
        </div>
      </div>

      <!-- Product Grid -->
      <ProductGrid :products="products" />

      <!-- Load More -->
      <div v-if="hasMore" class="text-center mt-8">
        <button @click="loadMore" class="btn-secondary" :disabled="loading">
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>

      <div v-if="!loading && products.length === 0" class="text-center py-12 text-gray-400">
        未找到相关商品
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { productApi } from '@/api/product'

const route = useRoute()

const categories = ref([])
const codeToId = ref({})

const sorts = [
  { label: '最新', value: 'created_at,desc' },
  { label: '最热', value: 'favorite_count,desc' },
  { label: '价格升序', value: 'price,asc' },
  { label: '价格降序', value: 'price,desc' }
]

const keyword = ref(route.query.keyword || '')
const sortBy = ref(route.query.sort || 'created_at,desc')
const categoryCode = ref(route.query.category || '')

const products = ref([])
const page = ref(1)
const loading = ref(false)
const hasMore = ref(true)

async function loadProducts(reset = false) {
  if (loading.value) return
  if (reset) {
    page.value = 1
    products.value = []
    hasMore.value = true
  }
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 20,
      sort: sortBy.value
    }
    if (keyword.value) params.keyword = keyword.value
    if (categoryCode.value && codeToId.value[categoryCode.value]) {
      params.categoryId = codeToId.value[categoryCode.value]
    }

    const res = await productApi.getProducts(params)
    const data = res.data?.data || {}
    const records = data.list || []

    if (reset || page.value === 1) {
      products.value = records
    } else {
      products.value = [...products.value, ...records]
    }

    hasMore.value = records.length >= 20
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  loadProducts(false)
}

function handleSearch() {
  loadProducts(true)
}

onMounted(async () => {
  // Load categories from API to get code→id mapping
  try {
    const res = await productApi.getCategories()
    if (res.data?.code === 200) {
      const cats = res.data.data || []
      categories.value = cats.map(c => ({ name: c.name, code: c.code }))
      cats.forEach(c => { codeToId.value[c.code] = c.id })
    }
  } catch {
    // fallback to hardcoded if API fails
  }
  loadProducts(true)
})
</script>
