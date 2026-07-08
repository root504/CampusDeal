<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- User Info Card -->
      <div class="card mb-6">
        <div class="flex items-center space-x-4">
          <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
            <span class="text-primary-600 font-bold text-2xl">{{ userStore.userInfo?.username?.charAt(0) || 'U' }}</span>
          </div>
          <div class="flex-1">
            <div class="flex items-center space-x-2">
              <h2 class="text-xl font-bold text-gray-900">{{ userStore.userInfo?.username || '用户' }}</h2>
              <span v-if="userStore.userInfo?.isVerified" class="badge bg-green-100 text-green-800">已认证</span>
            </div>
            <p class="text-sm text-gray-500 mt-1">{{ userStore.userInfo?.school || '未设置学校' }} · {{ userStore.userInfo?.campus || '' }}</p>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-3 gap-4 mb-6">
        <div class="card text-center cursor-pointer hover:shadow-md transition-shadow" @click="$router.push('/my-products')">
          <p class="text-2xl font-bold text-primary-600">{{ stats.published }}</p>
          <p class="text-xs text-gray-500 mt-1">已发布</p>
        </div>
        <div class="card text-center cursor-pointer hover:shadow-md transition-shadow" @click="$router.push('/orders?role=seller&status=completed&readonly=true')">
          <p class="text-2xl font-bold text-accent-600">{{ stats.sold }}</p>
          <p class="text-xs text-gray-500 mt-1">已售出</p>
        </div>
        <div class="card text-center cursor-pointer hover:shadow-md transition-shadow" @click="$router.push('/credit-detail')">
          <p class="text-2xl font-bold text-green-600">{{ userStore.userInfo?.creditScore || 100 }}</p>
          <p class="text-xs text-gray-500 mt-1">信用分</p>
        </div>
      </div>

      <!-- Menu -->
      <div class="space-y-2">
        <router-link to="/settings" class="card flex items-center justify-between !p-4 hover:bg-gray-50">
          <span class="text-gray-700">设置</span>
          <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
        <router-link to="/orders" class="card flex items-center justify-between !p-4 hover:bg-gray-50">
          <span class="text-gray-700">我的订单</span>
          <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
        <router-link to="/favorites" class="card flex items-center justify-between !p-4 hover:bg-gray-50">
          <span class="text-gray-700">我的收藏</span>
          <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
        <router-link to="/messages" class="card flex items-center justify-between !p-4 hover:bg-gray-50">
          <span class="text-gray-700">我的消息</span>
          <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
        <button @click="userStore.logout()" class="card w-full flex items-center justify-between !p-4 hover:bg-red-50 text-red-600">
          <span>退出登录</span>
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
        </button>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  published: 0,
  sold: 0
})

const fetchStats = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await userApi.getUserStats()
    if (res.data.code === 200) {
      stats.published = res.data.data.publishedCount
      stats.sold = res.data.data.soldCount
    }
  } catch {
    // keep default 0 on error
  }
}

onMounted(fetchStats)
onActivated(fetchStats)
</script>
