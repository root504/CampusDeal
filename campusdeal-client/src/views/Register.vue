<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="w-14 h-14 bg-primary-600 rounded-xl flex items-center justify-center mx-auto mb-3">
          <span class="text-white font-bold text-xl">CD</span>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">注册 CampusDeal</h1>
      </div>

      <div class="card space-y-5">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">昵称</label>
          <input v-model="username" type="text" class="input-field" placeholder="请输入昵称" />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">手机号</label>
          <input v-model="phone" type="text" class="input-field" placeholder="请输入手机号" maxlength="11" />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">学校</label>
          <input v-model="school" type="text" class="input-field" placeholder="请输入学校名称" />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">校区</label>
          <input v-model="campus" type="text" class="input-field" placeholder="请输入校区（如南湖校区）" />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
          <input v-model="password" type="password" class="input-field" placeholder="请输入密码" />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">确认密码</label>
          <input v-model="confirmPassword" type="password" class="input-field" placeholder="请确认密码" />
        </div>

        <button @click="handleRegister" class="btn-primary w-full" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <div class="text-center text-sm text-gray-500">
          已有账号？
          <router-link to="/login" class="text-primary-600 hover:text-primary-700">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const phone = ref('')
const username = ref('')
const school = ref('')
const campus = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)

async function handleRegister() {
  if (!phone.value || !password.value || !username.value) {
    alert('请填写所有必填字段')
    return
  }
  if (password.value !== confirmPassword.value) {
    alert('两次密码输入不一致')
    return
  }
  loading.value = true
  try {
    const res = await authApi.register({
      phone: phone.value,
      username: username.value,
      school: school.value,
      campus: campus.value,
      password: password.value
    })
    if (res.data?.code === 200) {
      userStore.setAuth({
        accessToken: res.data.data.accessToken,
        refreshToken: res.data.data.refreshToken,
        userInfo: { ...res.data.data, id: res.data.data.userId },
        role: res.data.data.role || 'user'
      })
      router.push('/')
    } else {
      alert(res.data?.message || '注册失败')
    }
  } catch {
    alert('注册失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>
