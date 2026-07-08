import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    const adminToken = localStorage.getItem('adminToken')
    // 管理员路由优先使用 adminToken；普通路由使用 user token
    if (config.url?.startsWith('/admin')) {
      if (adminToken) {
        config.headers['Authorization'] = `Bearer ${adminToken}`
      }
    } else if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    // 401：token 过期
    if (res.code === 401) {
      const isAdminRequest = response.config.url?.startsWith('/admin')
      handleTokenExpired(isAdminRequest)
      return Promise.reject(new Error('认证已过期，请重新登录'))
    }
    // 非 200：后端业务异常（GlobalExceptionHandler 返回 HTTP 200 + code != 200）
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 写操作成功后自动刷新页面，确保数据实时同步
    const method = response.config.method?.toLowerCase()
    if (['post', 'put', 'delete', 'patch'].includes(method)) {
      // 排除查询性质的 POST（如分页查询导出等）
      const url = response.config.url || ''
      if (!url.includes('/export') && !url.includes('/refresh')) {
        setTimeout(() => {
          window.location.reload()
        }, 300)
      }
    }
    return response
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const isAdminRequest = error.config?.url?.startsWith('/admin')
      if (status === 401) {
        handleTokenExpired(isAdminRequest)
      } else if (status === 403) {
        // 封禁响应：后端返回 code=403 且 message 含"封禁"时，强制用户退出
        const msg = error.response.data?.message || ''
        if (!isAdminRequest && msg.includes('封禁')) {
          ElMessage.error(msg || '账号已被封禁，请联系管理员')
          clearUserAuth()
        } else {
          ElMessage.error(msg || '没有权限执行此操作')
        }
      } else if (status >= 500) {
        ElMessage.error('服务器异常，请稍后重试')
      }
    } else {
      ElMessage.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

async function handleTokenExpired(isAdminRequest) {
  if (isAdminRequest) {
    // 管理员 token 过期：清除管理员登录态
    clearAdminAuth()
    return
  }

  // 用户 token 过期：尝试刷新
  const refreshToken = sessionStorage.getItem('refreshToken')
  if (!refreshToken) {
    clearUserAuth()
    return
  }
  try {
    const res = await axios.post('/api/v1/auth/refresh', { refreshToken })
    if (res.data.code === 200) {
      sessionStorage.setItem('token', res.data.data.accessToken)
      return
    }
  } catch {
    // refresh failed
  }
  clearUserAuth()
}

function clearUserAuth() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('refreshToken')
  sessionStorage.removeItem('userInfo')
  sessionStorage.removeItem('userRole')
  window.location.href = '/login'
}

function clearAdminAuth() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  localStorage.removeItem('adminRole')
  // 仅当当前页面是管理员路由时才跳转，避免影响正在浏览普通页面的用户
  if (window.location.pathname.startsWith('/admin')) {
    window.location.href = '/login?loginType=admin'
  }
}

export default service
