<template>
  <div class="p-6">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">基本配置</h1>
      <p class="text-sm text-gray-500 mt-1">点击卡片进入对应配置模块</p>
    </div>

    <!-- 6宫格布局：每行3个，共2行 -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      <router-link
        v-for="item in pagedItems"
        :key="item.path"
        :to="item.path"
        class="group flex flex-col items-center justify-center p-8 bg-white rounded-xl border border-gray-200 shadow-sm hover:shadow-md hover:border-primary-400 transition-all cursor-pointer"
      >
        <div class="w-14 h-14 rounded-full bg-primary-50 flex items-center justify-center mb-4 group-hover:bg-primary-100 transition-colors">
          <svg class="w-7 h-7 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
          </svg>
        </div>
        <span class="text-base font-medium text-gray-900 group-hover:text-primary-600 transition-colors">
          {{ item.label }}
        </span>
      </router-link>
    </div>

    <!-- 页数切换按钮（配置项超过6个时显示） -->
    <div v-if="totalPages > 1" class="flex items-center justify-center space-x-2 mt-8">
      <button
        :disabled="currentPage === 1"
        @click="currentPage--"
        class="px-4 py-2 rounded-lg border border-gray-300 text-sm font-medium text-gray-700 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
      >
        上一页
      </button>
      <button
        v-for="page in totalPages"
        :key="page"
        @click="currentPage = page"
        class="w-10 h-10 rounded-lg text-sm font-medium transition-colors"
        :class="currentPage === page ? 'bg-primary-600 text-white' : 'border border-gray-300 text-gray-700 hover:bg-gray-50'"
      >
        {{ page }}
      </button>
      <button
        :disabled="currentPage === totalPages"
        @click="currentPage++"
        class="px-4 py-2 rounded-lg border border-gray-300 text-sm font-medium text-gray-700 disabled:opacity-40 disabled:cursor-not-allowed hover:bg-gray-50 transition-colors"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const PAGE_SIZE = 6

// 配置模块入口（超过6个时自动分页）
const configItems = [
  {
    path: '/admin/credit-rules',
    label: '信用规则',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4'
  },
  {
    path: '/admin/order-rules',
    label: '订单规则',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4'
  },
  {
    path: '/admin/sensitive-words',
    label: '敏感词管理',
    icon: 'M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21'
  },
  {
    path: '/admin/banners',
    label: '轮播图管理',
    icon: 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z'
  }
]

const currentPage = ref(1)
const totalPages = computed(() => Math.ceil(configItems.length / PAGE_SIZE))

const pagedItems = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return configItems.slice(start, start + PAGE_SIZE)
})
</script>
