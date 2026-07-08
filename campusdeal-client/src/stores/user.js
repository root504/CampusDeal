import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import router from '@/router'
import { userStorage } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref(userStorage.get('token') || '')
  const refreshToken = ref(userStorage.get('refreshToken') || '')
  const userInfo = ref(JSON.parse(userStorage.get('userInfo') || 'null'))
  const role = ref(userStorage.get('userRole') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'admin')

  function setAuth(data) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo
    role.value = data.role || 'user'
    userStorage.set('token', data.accessToken)
    userStorage.set('refreshToken', data.refreshToken)
    userStorage.set('userInfo', JSON.stringify(data.userInfo))
    userStorage.set('userRole', data.role || 'user')
  }

  async function checkAuth() {
    if (!token.value) return
    try {
      const res = await authApi.getMe()
      if (res.data.code === 200) {
        userInfo.value = { ...res.data.data, id: res.data.data.id }
        userStorage.set('userInfo', JSON.stringify(userInfo.value))
      }
    } catch {
      clearAuth()
    }
  }

  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    userStorage.set('userInfo', JSON.stringify(userInfo.value))
  }

  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    role.value = ''
    userStorage.clear()
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
