<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1">
      <!-- Banner 轮播图 -->
      <section class="relative bg-gradient-to-r from-primary-600 to-primary-800 text-white">
        <div class="relative h-80 overflow-hidden">
          <template v-if="banners.length > 0">
            <div
              v-for="(b, i) in banners"
              :key="b.id"
              class="absolute inset-0 transition-opacity duration-700"
              :class="i === current ? 'opacity-100' : 'opacity-0'"
            >
              <router-link :to="b.linkUrl || '/products'" class="block w-full h-full">
                <img :src="b.imageUrl" class="w-full h-full object-cover" alt="banner" />
              </router-link>
            </div>
          </template>
          <div v-else class="absolute inset-0 flex items-center justify-center">
            <div class="max-w-2xl text-center px-4">
              <h1 class="text-4xl font-bold mb-4">校园二手，好物流转</h1>
              <p class="text-lg text-primary-100 mb-8">安全便捷的闲置物品交易平台，让每一件好物都找到新主人</p>
              <div class="flex space-x-4 justify-center">
                <router-link to="/products" class="bg-white text-primary-600 px-6 py-3 rounded-lg font-medium hover:bg-gray-100 transition-colors">
                  开始探索
                </router-link>
                <router-link to="/publish" class="border border-white text-white px-6 py-3 rounded-lg font-medium hover:bg-white/10 transition-colors">
                  发布商品
                </router-link>
              </div>
            </div>
          </div>

          <!-- 左右箭头 -->
          <button
            v-if="banners.length > 1"
            @click="prev"
            class="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/30 hover:bg-black/50 text-white flex items-center justify-center transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <button
            v-if="banners.length > 1"
            @click="next"
            class="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-black/30 hover:bg-black/50 text-white flex items-center justify-center transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>

          <!-- 底部指示器 -->
          <div v-if="banners.length > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2">
            <button
              v-for="(b, i) in banners"
              :key="'dot-' + b.id"
              @click="goTo(i)"
              class="w-3 h-3 rounded-full transition-colors"
              :class="i === current ? 'bg-white' : 'bg-white/40 hover:bg-white/60'"
            />
          </div>
        </div>
      </section>

      <!-- Categories -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h2 class="text-2xl font-bold mb-6">分类浏览</h2>
        <div class="grid grid-cols-4 sm:grid-cols-4 lg:grid-cols-8 gap-4">
          <router-link
            v-for="cat in categories"
            :key="cat.code"
            :to="`/products?category=${cat.code}`"
            class="flex flex-col items-center p-4 bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow"
          >
            <span class="text-3xl mb-2">{{ cat.icon }}</span>
            <span class="text-xs text-gray-600 text-center">{{ cat.name }}</span>
          </router-link>
        </div>
      </section>

      <!-- Hot Products -->
      <section class="bg-gray-50 py-12">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold">热门好物</h2>
            <router-link to="/products?sort=hot" class="text-primary-600 hover:text-primary-700 text-sm">查看更多</router-link>
          </div>
          <ProductGrid :products="hotProducts" />
        </div>
      </section>

      <!-- Latest Products -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-2xl font-bold">最新上架</h2>
          <router-link to="/products?sort=latest" class="text-primary-600 hover:text-primary-700 text-sm">查看更多</router-link>
        </div>
        <ProductGrid :products="latestProducts" />
      </section>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { productApi } from '@/api/product'
import { bannerApi } from '@/api/banner'

const categories = [
  { name: '教材书籍', code: 'textbook', icon: '📚' },
  { name: '数码电子', code: 'electronics', icon: '💻' },
  { name: '服饰鞋包', code: 'clothing', icon: '👗' },
  { name: '生活日用', code: 'daily', icon: '🏠' },
  { name: '运动户外', code: 'sport', icon: '⚽' },
  { name: '乐器文具', code: 'instrument', icon: '🎸' },
  { name: '美妆护肤', code: 'beauty', icon: '💄' },
  { name: '其他好物', code: 'other', icon: '🎁' }
]

const hotProducts = ref([])
const latestProducts = ref([])
const banners = ref([])
const current = ref(0)
let timer = null

onMounted(async () => {
  try {
    const [hotRes, latestRes, bannerRes] = await Promise.all([
      productApi.getHot(),
      productApi.getLatest(),
      bannerApi.getBanners()
    ])
    hotProducts.value = hotRes.data?.data?.list || []
    latestProducts.value = latestRes.data?.data?.list || []
    banners.value = bannerRes.data?.data || []
    if (banners.value.length > 1) startAutoPlay()
  } catch {
    // ignore
  }
})

onUnmounted(() => {
  clearInterval(timer)
})

function startAutoPlay() {
  clearInterval(timer)
  timer = setInterval(() => {
    current.value = (current.value + 1) % banners.value.length
  }, 3000)
}

function prev() {
  current.value = (current.value - 1 + banners.value.length) % banners.value.length
  startAutoPlay()
}

function next() {
  current.value = (current.value + 1) % banners.value.length
  startAutoPlay()
}

function goTo(i) {
  current.value = i
  startAutoPlay()
}
</script>
