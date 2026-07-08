import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAdminStore = defineStore('admin', () => {
  const adminToken = ref(localStorage.getItem('adminToken') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('adminInfo') || 'null'))

  function setAuth(token, info) {
    adminToken.value = token
    adminInfo.value = info
    localStorage.setItem('adminToken', token)
    localStorage.setItem('adminInfo', JSON.stringify(info))
    localStorage.setItem('adminRole', 'admin')
  }

  function clearAuth() {
    adminToken.value = ''
    adminInfo.value = null
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
    localStorage.removeItem('adminRole')
    // 注意：不再清除 token / refreshToken / userInfo / userRole，确保多账号会话隔离
  }

  const isAdmin = () => !!adminToken.value

  return { adminToken, adminInfo, setAuth, clearAuth, isAdmin }
})
