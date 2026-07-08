<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <div v-if="product" class="grid grid-cols-1 lg:grid-cols-5 gap-8">
        <!-- Left: Images -->
        <div class="lg:col-span-3">
          <ImageCarousel :images="productImages" />
        </div>

        <!-- Right: Info -->
        <div class="lg:col-span-2 space-y-6">
          <h1 class="text-2xl font-bold text-gray-900">{{ product.title }}</h1>

          <div class="flex items-baseline space-x-3">
            <span class="text-3xl font-bold text-accent-600">&yen;{{ product.price }}</span>
            <span v-if="product.originalPrice" class="text-lg text-gray-400 line-through">&yen;{{ product.originalPrice }}</span>
          </div>

          <div class="flex flex-wrap gap-2">
            <span class="badge bg-blue-100 text-blue-800">{{ product.conditionLevel }}</span>
            <span class="badge bg-green-100 text-green-800">{{ product.campus }}</span>
            <span class="badge bg-purple-100 text-purple-800">{{ categoryLabel }}</span>
          </div>

          <div class="flex items-center space-x-4 text-sm text-gray-500">
            <span>{{ product.viewCount || 0 }} 浏览</span>
            <span>{{ product.favoriteCount || 0 }} 收藏</span>
            <span>{{ formatDate(product.createdAt) }}</span>
            <button
              @click="$router.push('/review-detail?productId=' + product.id + '&sellerId=' + (product.sellerId || product.seller?.id))"
              class="text-primary-600 font-medium hover:text-primary-700"
            >
              {{ sellerReviewCount }}人评价
            </button>
          </div>

          <!-- Seller Info -->
          <div class="card !p-4">
            <div class="flex items-center space-x-3">
              <div class="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                <span class="text-primary-600 font-bold text-lg">{{ product.seller?.username?.charAt(0) || 'S' }}</span>
              </div>
              <div>
                <p class="font-medium text-gray-900">{{ product.seller?.username || '卖家' }}</p>
                <p class="text-xs text-gray-500">
                  信用分 {{ product.seller?.creditScore || 100 }}
                  <span v-if="product.seller?.isVerified" class="text-green-600 ml-1">已认证</span>
                </p>
              </div>
            </div>
            <button
              @click="goChat"
              class="w-full mt-3 btn-secondary text-sm py-2"
            >
              私信卖家
            </button>
          </div>

          <!-- Description -->
          <div class="card !p-4">
            <h3 class="font-medium text-gray-900 mb-2">商品描述</h3>
            <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">{{ product.description || '暂无描述' }}</p>
          </div>

          <!-- Actions -->
          <div v-if="isMyProduct && product.status === 2" class="card !p-4 bg-red-50 border border-red-200">
            <p class="text-red-600 font-medium mb-1">该商品已被驳回</p>
            <p class="text-sm text-red-500 mb-3" v-if="product.auditRemark">驳回理由：{{ product.auditRemark }}</p>
            <button
              v-if="product.appealStatus === 0"
              @click="handleAppeal(product)"
              class="px-4 py-2 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 transition-colors text-sm"
            >
              申诉
            </button>
            <p v-else-if="product.appealStatus === 1" class="text-sm text-orange-500">申诉处理中...</p>
            <p v-else-if="product.appealStatus === 2" class="text-sm text-green-500">申诉已通过</p>
            <p v-else-if="product.appealStatus === 3" class="text-sm text-red-500">
              申诉已被驳回<span v-if="product.appealRemark">：{{ product.appealRemark }}</span>
            </p>
          </div>
          <div v-else-if="isMyProduct" class="card !p-4 text-center text-gray-500">
            这是您发布的商品
          </div>
          <div v-else class="flex space-x-3">
            <button @click="toggleFavorite" class="flex-1 btn-secondary" :class="{ 'bg-red-50 text-red-600': isFavorited }">
              {{ isFavorited ? '已收藏' : '收藏' }}
            </button>
            <button @click="addToCart" class="flex-1 btn-accent">加入购物车</button>
            <button @click="buyNow" class="flex-1 btn-primary">立即购买</button>
          </div>
        </div>
      </div>

      <!-- Similar Products -->
      <section v-if="similarProducts.length > 0" class="mt-12">
        <h2 class="text-2xl font-bold mb-6">相似推荐</h2>
        <ProductGrid :products="similarProducts" />
      </section>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import ImageCarousel from '@/components/product/ImageCarousel.vue'
import ProductGrid from '@/components/product/ProductGrid.vue'
import { productApi } from '@/api/product'
import { favoriteApi } from '@/api/favorite'
import { reviewApi } from '@/api/review'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const product = ref(null)
const isFavorited = ref(false)
const similarProducts = ref([])
const sellerReviewCount = ref(0)

const productImages = computed(() => {
  if (!product.value) return []
  const images = product.value.images || []
  if (images.length > 0) return images.map(i => i.url)
  if (product.value.coverImage) return [product.value.coverImage]
  return []
})

const categoryLabel = computed(() => product.value?.category?.name || '')

const isMyProduct = computed(() => {
  if (!userStore.isLoggedIn || !product.value) return false
  const userId = userStore.userInfo?.id
  const sellerId = product.value.sellerId || product.value.seller?.id
  return userId && sellerId && userId === sellerId
})

async function loadProduct(id) {
  try {
    const res = await productApi.getProduct(id)
    product.value = res.data?.data

    const [simRes, favRes] = await Promise.all([
      productApi.getSimilar(id),
      userStore.isLoggedIn ? favoriteApi.checkFavorite(id) : Promise.resolve(null)
    ])
    similarProducts.value = simRes.data?.data?.list || []
    isFavorited.value = favRes?.data?.data || false

    // 获取该卖家评价总人数
    const sellerId = product.value.sellerId || product.value.seller?.id
    if (sellerId) {
      try {
        const countRes = await reviewApi.getSellerReviewCount(sellerId)
        sellerReviewCount.value = countRes.data?.data?.count || 0
      } catch { /* ignore */ }
    }
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadProduct(route.params.id)
})

// 监听路由参数变化，实现相似推荐跳转时自动刷新
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadProduct(newId)
    }
  }
)

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await favoriteApi.removeFavorite(product.value.id)
      isFavorited.value = false
    } else {
      await favoriteApi.addFavorite(product.value.id)
      isFavorited.value = true
    }
  } catch {
    // ignore
  }
}

async function addToCart() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await cartStore.addToCart(product.value.id)
    alert('已加入购物车')
  } catch {
    // ignore
  }
}

function buyNow() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  addToCart().then(() => {
    router.push('/cart')
  })
}

function goChat() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const buyerId = userStore.userInfo?.id
  const sellerId = product.value?.seller?.id || product.value?.sellerId
  if (!buyerId || !sellerId) {
    ElMessage.warning('无法发起聊天，请确认已登录且卖家信息完整')
    return
  }
  if (String(buyerId) === String(sellerId)) {
    ElMessage.warning('不能与自己聊天')
    return
  }
  const ids = [buyerId, sellerId].sort()
  const conversationId = `${ids[0]}_${ids[1]}`
  router.push(`/chat/${conversationId}?productId=${product.value.id}`)
}

function handleAppeal(p) {
  router.push(`/my-products?appeal=${p.id}`)
}
</script>
