<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">我的收藏</h1>

      <div v-if="favorites.length > 0">
        <ProductGrid :products="favorites" />
      </div>

      <div v-else class="text-center py-12 text-gray-400">
        <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
        <p class="text-lg">暂无收藏</p>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { favoriteApi } from '@/api/favorite'

const favorites = ref([])

onMounted(async () => {
  try {
    const res = await favoriteApi.getFavorites()
    favorites.value = (res.data?.data?.list || []).map(f => f.product || f)
  } catch {
    // ignore
  }
})
</script>
