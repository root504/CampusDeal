<template>
  <div class="flex min-h-screen bg-gray-100">
    <AdminSidebar @logout="handleLogout" />
    <div class="flex-1 flex flex-col">
      <header class="bg-white shadow-sm px-6 py-4 flex items-center justify-between">
        <h1 class="text-lg font-semibold text-gray-900">{{ pageTitle }}</h1>
        <div class="flex items-center space-x-3">
          <span class="text-sm text-gray-500">{{ adminName }}</span>
        </div>
      </header>
      <main class="flex-1 p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AdminSidebar from '@/components/layout/AdminSidebar.vue'
import { useAdminStore } from '@/stores/admin'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()
const userStore = useUserStore()

const adminName = computed(() => {
  return adminStore.adminInfo?.username || adminStore.adminInfo?.username || '管理员'
})

const pageTitle = computed(() => {
  const titles = {
    '/admin/dashboard': '数据概览',
    '/admin/products': '商品管理',
    '/admin/orders': '订单管理',
    '/admin/users': '用户管理',
    '/admin/announcements': '公告管理',
    '/admin/logs': '系统日志',
    '/admin/messages': '消息管理',
    '/admin/credits': '基本配置',
    '/admin/credit-records': '信誉分明细',
    '/admin/credit-rules': '信誉分规则',
    '/admin/order-rules': '订单规则',
    '/admin/sensitive-words': '敏感词过滤',
    '/admin/banners': '轮播图管理'
  }
  return titles[route.path] || '后台管理'
})

function handleLogout() {
  adminStore.clearAuth()
  userStore.clearAuth()
  router.push('/login')
}
</script>
