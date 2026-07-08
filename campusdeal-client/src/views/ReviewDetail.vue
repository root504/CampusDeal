<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- 评价概览 -->
      <div class="card !p-6 mb-6">
        <div class="flex items-center space-x-6">
          <div class="text-center">
            <p class="text-4xl font-bold text-gray-900">{{ avgRating }}</p>
            <div class="flex text-yellow-400 mt-1 justify-center">
              <span v-for="i in 5" :key="i" :class="i <= Math.round(avgRating) ? 'text-yellow-400' : 'text-gray-300'">&#9733;</span>
            </div>
          </div>
          <div class="text-sm text-gray-500">
            <p>共 <span class="font-medium text-gray-900">{{ reviews.length }}</span> 人评价</p>
          </div>
        </div>
      </div>

      <!-- 评价列表 -->
      <div v-if="reviews.length > 0" class="space-y-4">
        <div v-for="review in reviews" :key="review.id" class="card !p-4">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center space-x-2">
              <span class="font-medium text-gray-900">{{ review.buyerName || '匿名用户' }}</span>
              <span class="flex text-yellow-400">
                <span v-for="i in 5" :key="i" :class="i <= review.rating ? 'text-yellow-400' : 'text-gray-300'">&#9733;</span>
              </span>
              <span v-if="review.orderProductName" class="text-xs text-gray-500 ml-1">
                订单商品：{{ review.orderProductName }}
              </span>
            </div>
            <span class="text-xs text-gray-400">{{ formatDate(review.createdAt) }}</span>
          </div>
          <p v-if="review.content" class="text-sm text-gray-600 leading-relaxed">{{ review.content }}</p>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="text-center py-20 text-gray-400">
        <p class="text-lg mb-2">暂无评价</p>
        <p class="text-sm">该商品还没有人评价</p>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { reviewApi } from '@/api/review'

const route = useRoute()

const reviews = ref([])

const avgRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const total = reviews.value.reduce((sum, r) => sum + r.rating, 0)
  return (total / reviews.value.length).toFixed(1)
})

onMounted(async () => {
  const productId = route.query.productId
  if (productId) {
    try {
      const res = await reviewApi.getProductReviews(productId)
      reviews.value = res.data?.data?.list || []
    } catch { /* ignore */ }
  }
})

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>
