import request from './index'

export const authApi = {
  register(data) {
    return request.post('/auth/register', data)
  },
  login(data) {
    return request.post('/auth/login', data)
  },
  refreshToken(refreshToken) {
    return request.post('/auth/refresh', { refreshToken })
  },
  getMe() {
    return request.get('/auth/me')
  },
  updateProfile(data) {
    return request.put('/auth/profile', data)
  },
  logout() {
    return request.post('/auth/logout')
  }
}
