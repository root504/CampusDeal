<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link to="/" class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
            <span class="text-white font-bold text-sm">CD</span>
          </div>
          <span class="text-xl font-bold text-gray-900">CampusDeal</span>
        </router-link>

        <!-- Nav Links -->
        <nav class="hidden md:flex items-center space-x-8">
          <router-link to="/" class="text-gray-700 hover:text-primary-600 transition-colors">首页</router-link>
          <router-link to="/products" class="text-gray-700 hover:text-primary-600 transition-colors">逛一逛</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/favorites" class="text-gray-700 hover:text-primary-600 transition-colors">收藏</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/orders" class="text-gray-700 hover:text-primary-600 transition-colors">订单</router-link>
          <router-link v-if="userStore.isLoggedIn" to="/cart" class="text-gray-700 hover:text-primary-600 transition-colors relative">
            购物车
            <span v-if="cartStore.totalCount > 0" class="absolute -top-2 -right-4 bg-accent-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              {{ cartStore.totalCount }}
            </span>
          </router-link>
        </nav>

        <!-- User Actions -->
        <div class="flex items-center space-x-4">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/publish" class="btn-primary text-sm py-2 px-4">发布</router-link>
            <router-link to="/messages" class="relative p-2 text-gray-700 hover:text-primary-600 transition-colors">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
              </svg>
              <span v-if="chatStore.unreadCount > 0" class="absolute -top-1 -right-2 bg-red-500 text-white text-xs rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1 leading-none">
                {{ chatStore.unreadCount > 99 ? '99+' : chatStore.unreadCount }}
              </span>
            </router-link>
            <router-link to="/profile" class="flex items-center space-x-2 text-gray-700 hover:text-primary-600 transition-colors">
              <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                <span class="text-primary-600 font-medium text-sm">{{ userStore.userInfo?.username?.charAt(0) || 'U' }}</span>
              </div>
            </router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="text-gray-700 hover:text-primary-600 transition-colors">登录</router-link>
            <router-link to="/register" class="btn-primary text-sm py-2 px-4">注册</router-link>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useChatStore } from '@/stores/chat'
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const chatStore = useChatStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.fetchCart()
    chatStore.fetchUnreadCount()
  }
})

// 每次路由切换时刷新未读消息数
watch(() => route.path, () => {
  if (userStore.isLoggedIn) {
    chatStore.fetchUnreadCount()
  }
})
</script>
