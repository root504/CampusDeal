<template>
  <aside class="w-64 bg-gray-900 text-gray-300 min-h-screen flex-shrink-0">
    <div class="p-6 border-b border-gray-800">
      <div class="flex items-center space-x-2">
        <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
          <span class="text-white font-bold text-sm">CD</span>
        </div>
        <span class="text-lg font-bold text-white">后台管理</span>
      </div>
    </div>
    <nav class="p-4 space-y-1">
      <template v-for="item in menuItems" :key="item.path">
        <!-- 有子菜单的父级 -->
        <template v-if="item.children">
          <button
            @click="toggleExpand(item.path)"
            class="flex items-center w-full space-x-3 px-4 py-3 rounded-lg transition-colors"
            :class="isParentActive(item) ? 'bg-primary-600/50 text-white' : 'hover:bg-gray-800 hover:text-white'"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
            </svg>
            <span class="flex-1 text-left">{{ item.label }}</span>
            <svg
              class="w-4 h-4 transition-transform duration-200"
              :class="expandedMenus.has(item.path) ? 'rotate-90' : ''"
              fill="none" stroke="currentColor" viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
          <!-- 子菜单项 -->
          <div v-show="expandedMenus.has(item.path)" class="ml-4 space-y-1 border-l border-gray-700 pl-3">
            <router-link
              v-for="child in item.children"
              :key="child.path"
              :to="child.path"
              class="flex items-center space-x-3 px-4 py-2.5 rounded-lg transition-colors text-sm"
              :class="isActive(child.path) ? 'bg-primary-600 text-white' : 'hover:bg-gray-800 hover:text-white'"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="child.icon" />
              </svg>
              <span>{{ child.label }}</span>
            </router-link>
          </div>
        </template>
        <!-- 无子菜单的普通项 -->
        <router-link
          v-else
          :to="item.path"
          class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors"
          :class="isActive(item.path) ? 'bg-primary-600 text-white' : 'hover:bg-gray-800 hover:text-white'"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
          </svg>
          <span>{{ item.label }}</span>
        </router-link>
      </template>
    </nav>
    <div class="absolute bottom-0 w-64 p-4 border-t border-gray-800">
      <button @click="$emit('logout')" class="flex items-center space-x-3 w-full px-4 py-3 rounded-lg hover:bg-gray-800 hover:text-white transition-colors">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
        <span>退出登录</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

defineEmits(['logout'])

const route = useRoute()

const expandedMenus = ref(new Set())

const menuItems = [
  {
    path: '/admin/dashboard',
    label: '数据概览',
    icon: 'M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zm10 0a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z'
  },
  {
    path: '/admin/products',
    label: '商品管理',
    icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4'
  },
  {
    path: '/admin/orders',
    label: '订单管理',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2'
  },
  {
    path: '/admin/users',
    label: '用户管理',
    icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z'
  },
  {
    path: '/admin/announcements',
    label: '公告管理',
    icon: 'M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z'
  },
  {
    path: '/admin/logs',
    label: '系统日志',
    icon: 'M4 7v10c0 2 1 3 3 3h10c2 0 3-1 3-3V7M4 7c0-2 1-3 3-3h10c2 0 3 1 3 3M4 7h16M9 12h6'
  },
  {
    path: '/admin/messages',
    label: '消息管理',
    icon: 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z'
  },
  {
    path: '/admin/basic-config',
    label: '基本配置',
    icon: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z'
  }
]

function isActive(path) {
  return route.path === path
}

function isParentActive(item) {
  if (route.path === item.path) return true
  if (item.children) {
    return item.children.some(c => route.path === c.path)
  }
  return false
}

function toggleExpand(path) {
  const s = new Set(expandedMenus.value)
  if (s.has(path)) {
    s.delete(path)
  } else {
    s.add(path)
  }
  expandedMenus.value = s
}

// 当路由进入子菜单项时自动展开父级
watch(() => route.path, (newPath) => {
  for (const item of menuItems) {
    if (item.children && item.children.some(c => c.path === newPath)) {
      const s = new Set(expandedMenus.value)
      s.add(item.path)
      expandedMenus.value = s
      break
    }
  }
}, { immediate: true })
</script>
