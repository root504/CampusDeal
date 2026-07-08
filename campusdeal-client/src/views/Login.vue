<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="w-14 h-14 bg-primary-600 rounded-xl flex items-center justify-center mx-auto mb-3">
          <span class="text-white font-bold text-xl">CD</span>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">登录 CampusDeal</h1>
      </div>

      <div class="card space-y-5">
        <!-- 登录入口选择 -->
        <div class="flex rounded-lg bg-gray-100 p-1">
          <button
            @click="loginType = 'user'"
            class="flex-1 py-2 text-sm rounded-md transition-colors"
            :class="loginType === 'user' ? 'bg-white shadow text-gray-900 font-medium' : 'text-gray-500'"
          >普通用户登录</button>
          <button
            @click="loginType = 'admin'"
            class="flex-1 py-2 text-sm rounded-md transition-colors"
            :class="loginType === 'admin' ? 'bg-white shadow text-gray-900 font-medium' : 'text-gray-500'"
          >管理员登录</button>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">
            {{ loginType === 'admin' ? '手机号（管理员）' : '手机号' }}
          </label>
          <input
            v-model="phone"
            type="text"
            class="input-field"
            :placeholder="loginType === 'admin' ? '请输入管理员手机号' : '请输入手机号'"
            maxlength="11"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
          <input v-model="password" type="password" class="input-field" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </div>

        <button @click="handleLogin" class="btn-primary w-full" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <!-- 普通用户才显示注册入口 -->
        <div v-if="loginType === 'user'" class="text-center text-sm text-gray-500">
          还没有账号？
          <router-link to="/register" class="text-primary-600 hover:text-primary-700">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api/auth'
import { adminApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const adminStore = useAdminStore()

const phone = ref('')
const password = ref('')
const loginType = ref('user')
const loading = ref(false)

// 当路由参数指定 loginType=admin 时自动切换到管理员标签页
onMounted(() => {
  if (route.query.loginType === 'admin') {
    loginType.value = 'admin'
  }
})

async function handleLogin() {
  if (!phone.value || !password.value) {
    alert('请填写手机号和密码')
    return
  }
  loading.value = true
  try {
    if (loginType.value === 'admin') {
      // 管理员登录：调用 adminApi.login
      const res = await adminApi.login({ phone: phone.value, password: password.value })
      if (res.data?.code === 200) {
        const data = res.data.data
        // 兼容 token 字符串或对象两种格式
        let token = ''
        let adminInfo = {}
        if (typeof data === 'string') {
          token = data
          adminInfo = { phone: phone.value }
        } else if (typeof data === 'object' && data !== null) {
          token = data.accessToken || data.token || ''
          adminInfo = data
        }
        adminStore.setAuth(token, adminInfo)
        router.push('/admin/dashboard')
      } else {
        alert(res.data?.message || '登录失败')
      }
    } else {
      // 普通用户登录：保持原有逻辑
      const res = await authApi.login({ phone: phone.value, password: password.value })
      if (res.data?.code === 200) {
        const roleVal = res.data.data.role
        const roleStr = roleVal === 1 ? 'admin' : 'user'
        userStore.setAuth({
          accessToken: res.data.data.accessToken,
          refreshToken: res.data.data.refreshToken,
          userInfo: { ...res.data.data, id: res.data.data.userId },
          role: roleStr
        })
        const redirect = route.query.redirect || '/'
        router.push(redirect)
      } else {
        alert(res.data?.message || '登录失败')
      }
    }
  } catch (e) {
    // 管理员登录失败时显示后端返回的 message
    const msg = e?.response?.data?.message || e?.data?.message || '登录失败，请检查网络连接'
    alert(msg)
  } finally {
    loading.value = false
  }
}
</script>
