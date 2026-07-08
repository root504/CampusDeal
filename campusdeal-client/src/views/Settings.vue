<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">设置</h1>

      <div class="bg-white rounded-xl shadow-sm overflow-hidden divide-y divide-gray-100">
        <!-- 修改个人信息 -->
        <router-link
          to="/settings/profile"
          class="flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 bg-blue-100 rounded-lg flex items-center justify-center">
              <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-900">修改个人信息</p>
              <p class="text-xs text-gray-400">昵称、学校、校区、年级</p>
            </div>
          </div>
          <svg class="w-5 h-5 text-gray-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>

        <!-- 修改注册信息 -->
        <router-link
          to="/settings/account"
          class="flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 bg-green-100 rounded-lg flex items-center justify-center">
              <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-900">修改注册信息</p>
              <p class="text-xs text-gray-400">手机号、登录密码</p>
            </div>
          </div>
          <svg class="w-5 h-5 text-gray-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>

        <!-- 收货地址 -->
        <router-link
          to="/settings/address"
          class="flex items-center justify-between px-5 py-4 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 bg-orange-100 rounded-lg flex items-center justify-center">
              <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-900">收货地址</p>
              <p class="text-xs text-gray-400">管理收货地址</p>
            </div>
          </div>
          <svg class="w-5 h-5 text-gray-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </div>

      <!-- 注销账号 -->
      <div class="mt-8 bg-white rounded-xl shadow-sm overflow-hidden">
        <button
          @click="showDeactivateDialog = true"
          class="w-full flex items-center justify-between px-5 py-4 hover:bg-red-50 transition-colors"
        >
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 bg-red-100 rounded-lg flex items-center justify-center">
              <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-red-600">注销账号</p>
              <p class="text-xs text-gray-400">注销后7天内可恢复，超期将永久删除</p>
            </div>
          </div>
          <svg class="w-5 h-5 text-gray-300 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>

      <!-- 注销确认弹窗 -->
      <div
        v-if="showDeactivateDialog"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
        @click.self="showDeactivateDialog = false"
      >
        <div class="bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 p-6 animate-slide-up">
          <h3 class="text-lg font-semibold text-gray-900 mb-2">确认注销账号</h3>
          <p class="text-sm text-gray-500 mb-4">
            注销后你的账号将进入7天冷静期，期间可联系客服恢复。超过7天后账号将被永久删除，所有数据不可恢复。
          </p>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">请输入密码确认</label>
            <input
              v-model="deactivatePassword"
              type="password"
              class="input-field w-full"
              placeholder="输入当前密码"
            />
          </div>
          <div class="flex space-x-3">
            <button @click="showDeactivateDialog = false" class="btn-secondary flex-1">取消</button>
            <button
              @click="confirmDeactivate"
              :disabled="deactivating || !deactivatePassword"
              class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700 disabled:opacity-50 transition-colors"
            >
              {{ deactivating ? '处理中...' : '确认注销' }}
            </button>
          </div>
        </div>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()

const showDeactivateDialog = ref(false)
const deactivatePassword = ref('')
const deactivating = ref(false)

async function confirmDeactivate() {
  if (!deactivatePassword.value) {
    ElMessage.warning('请输入密码')
    return
  }
  deactivating.value = true
  try {
    await userApi.deactivate(deactivatePassword.value)
    ElMessage.success('账号已申请注销，7天内可联系客服恢复')
    showDeactivateDialog.value = false
    userStore.clearAuth()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '注销失败，请检查密码')
  } finally {
    deactivating.value = false
  }
}
</script>
