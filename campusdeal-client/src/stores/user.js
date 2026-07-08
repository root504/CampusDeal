import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem('token') || '')
  const refreshToken = ref(sessionStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || 'null'))
  const role = ref(sessionStorage.getItem('userRole') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'admin')

  function setAuth(data) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo
    role.value = data.role || 'user'
    sessionStorage.setItem('token', data.accessToken)
    sessionStorage.setItem('refreshToken', data.refreshToken)
    sessionStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    sessionStorage.setItem('userRole', data.role || 'user')
  }

  async function checkAuth() {
    if (!token.value) return
    try {
      const res = await authApi.getMe()
      if (res.data.code === 200) {
        userInfo.value = { ...res.data.data, id: res.data.data.id }
        sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      }
    } catch {
      clearAuth()
    }
  }

  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    role.value = ''
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('userInfo')
    sessionStorage.removeItem('userRole')
    // 注意：不再清除 adminToken / adminInfo，确保多账号会话隔离
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
    clearAuth()
    router.push('/')
  }

  return {
    token,
    refreshToken,
    userInfo,
    role,
    isLoggedIn,
    isAdmin,
    setAuth,
    checkAuth,
    updateUserInfo,
    clearAuth,
    logout
  }
})
