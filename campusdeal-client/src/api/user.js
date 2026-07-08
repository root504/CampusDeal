import request from './index'

export const userApi = {
  updateProfile(data) {
    return request.put('/user/profile', data)
  },
  changePassword(data) {
    return request.put('/user/password', data)
  },
  changePhone(data) {
    return request.put('/user/phone', data)
  },
  deactivate(password) {
    return request.put('/user/deactivate', { password })
  },
  getUserInfo(id) {
    return request.get(`/user/${id}`)
  },
  getUserStats() {
    return request.get('/user/stats')
  }
}
