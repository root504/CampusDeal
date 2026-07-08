<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
    <div class="w-full max-w-sm">
      <div class="text-center mb-8">
        <div class="w-14 h-14 bg-gray-900 rounded-xl flex items-center justify-center mx-auto mb-3">
          <span class="text-white font-bold text-xl">CD</span>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">管理员登录</h1>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 space-y-5">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
          <input v-model="username" type="text" class="input-field" placeholder="请输入管理员用户名" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
          <input v-model="password" type="password" class="input-field" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </div>
        <button @click="handleLogin" class="btn-primary w-full bg-gray-900 hover:bg-gray-800" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    alert('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await adminApi.login({ username: username.value, password: password.value })
    if (res.data?.code === 200) {
      const data = res.data.data
      // 兼容对象和字符串两种格式
      let adminInfo = {}
      let token = null
      if (typeof data === 'object' && data !== null) {
        token = data.accessToken || data.token
        adminInfo = data
      } else if (typeof data === 'string') {
        token = data
        adminInfo = { username: username.value }
      }
      if (token) {
        adminStore.setAuth(token, adminInfo)
      }
      router.push('/admin/dashboard')
    } else {
      alert(res.data?.message || '登录失败')
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '登录失败，请检查网络连接'
    alert(msg)
  } finally {
    loading.value = false
  }
}
</script>
