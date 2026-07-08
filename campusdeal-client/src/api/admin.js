import request from './index'

export const adminApi = {
  // 认证
  login({ phone, password }) {
    return request.post('/admin/auth/login', { phone, password })
  },
  // 仪表盘
  getDashboard() {
    return request.get('/admin/dashboard')
  },
  getTrend() {
    return request.get('/admin/dashboard/trend')
  },
  getCategoryDist() {
    return request.get('/admin/dashboard/category-dist')
  },
  // 商品管理
  getProducts(params) {
    return request.get('/admin/products', { params })
  },
  auditProduct(id, data) {
    return request.put(`/admin/products/${id}/audit`, data)
  },
  approveProduct(id) {
    return request.put(`/admin/products/${id}/approve`)
  },
  rejectProduct(id, reason) {
    return request.put(`/admin/products/${id}/reject`, { reason })
  },
  offShelfProduct(id) {
    return request.put(`/admin/products/${id}/off-shelf`)
  },
  onShelfProduct(id) {
    return request.put(`/admin/products/${id}/on-shelf`)
  },
  updateProduct(id, data) {
    return request.put(`/admin/products/${id}`, data)
  },
  deleteProduct(id) {
    return request.delete(`/admin/products/${id}`)
  },
  softDeleteProduct(id) {
    return request.put(`/admin/products/${id}/soft-delete`)
  },
  approveAppeal(id) {
    return request.put(`/admin/products/${id}/appeal/approve`)
  },
  rejectAppeal(id, remark) {
    return request.put(`/admin/products/${id}/appeal/reject`, { remark })
  },
  // 订单管理
  getOrders(params) {
    return request.get('/admin/orders', { params })
  },
  getOrderStats() {
    return request.get('/admin/orders/stats')
  },
  remindOrder(id, message) {
    return request.put(`/admin/orders/${id}/remind`, { message })
  },
  shipOrder(id) {
    return request.put(`/admin/orders/${id}/ship`)
  },
  receiveOrder(id) {
    return request.put(`/admin/orders/${id}/receive`)
  },
  deleteOrder(id) {
    return request.put(`/admin/orders/${id}/delete`)
  },
  getOrderDetail(id) {
    return request.get(`/admin/orders/${id}`)
  },
  // 用户管理
  getUsers(params) {
    return request.get('/admin/users', { params })
  },
  updateUserStatus(id, status) {
    return request.put(`/admin/users/${id}/status`, { status })
  },
  editUser(id, data) {
    return request.put(`/admin/users/${id}`, data)
  },
  updateUserRole(id, role) {
    return request.put(`/admin/users/${id}/role`, { role })
  },
  // 公告管理
  getAnnouncements(params) {
    return request.get('/admin/announcements', { params })
  },
  createAnnouncement(data) {
    return request.post('/admin/announcements', data)
  },
  updateAnnouncement(id, data) {
    return request.put(`/admin/announcements/${id}`, data)
  },
  deleteAnnouncement(id) {
    return request.delete(`/admin/announcements/${id}`)
  },
  // 系统日志
  getSystemLogs(params) {
    return request.get('/admin/logs', { params })
  },
  clearLogs() {
    return request.delete('/admin/logs')
  },
  // 消息管理
  getUserMessages(params) {
    return request.get('/messages/admin/list', { params })
  },
  replyMessage(data) {
    return request.post('/messages/admin/reply', data)
  },
  getAdminConversations() {
    return request.get('/admin/messages/conversations')
  },
  getAdminAllConversations() {
    return request.get('/admin/messages/all-conversations')
  },
  getAdminChat(userId, params) {
    return request.get(`/admin/messages/chat/${userId}`, { params })
  },
  getAdminUserConversations(userId) {
    return request.get(`/admin/messages/user-conversations/${userId}`)
  },
  getAdminUserChat(userId, conversationId, params) {
    return request.get(`/admin/messages/user-chat/${userId}/${conversationId}`, { params })
  },
  sendNotification(data) {
    return request.post('/admin/messages/send', data)
  },
  sendNotificationToAll(data) {
    return request.post('/admin/messages/send-all', data)
  },
  exportChatRecords(userId, format) {
    return request.get(`/admin/messages/export/${userId}`, { params: { format }, responseType: 'text' })
  },
  getUsersForNotification() {
    return request.get('/admin/messages/users')
  },
  getAdminSystemNotifications(params) {
    return request.get('/admin/messages/system-notifications', { params })
  },
  getUserChatPartners(userId) {
    return request.get(`/admin/messages/user-chat-partners/${userId}`)
  },
  // ========== 信誉分管理 ==========
  getCreditList(params) {
    return request.get('/admin/credits', { params })
  },
  getUserCreditDetail(userId, params) {
    return request.get(`/admin/credits/${userId}/records`, { params })
  },
  getCreditRules() {
    return request.get('/admin/credits/rules')
  },
  createCreditRule(data) {
    return request.post('/admin/credits/rules', data)
  },
  updateCreditRule(id, data) {
    return request.put(`/admin/credits/rules/${id}`, data)
  },
  deleteCreditRule(id) {
    return request.delete(`/admin/credits/rules/${id}`)
  },
  applyCreditRule(id) {
    return request.put(`/admin/credits/rules/${id}/apply`)
  },
  unapplyCreditRule(id) {
    return request.put(`/admin/credits/rules/${id}/unapply`)
  },
  // ========== 订单规则 ==========
  getOrderRules() {
    return request.get('/admin/order-rule')
  },
  createOrderRule(data) {
    return request.post('/admin/order-rule', data)
  },
  updateOrderRule(id, data) {
    return request.put(`/admin/order-rule/${id}`, data)
  },
  deleteOrderRule(id) {
    return request.delete(`/admin/order-rule/${id}`)
  },
  applyOrderRule(id) {
    return request.put(`/admin/order-rule/${id}/apply`)
  },
  unapplyOrderRule(id) {
    return request.put(`/admin/order-rule/${id}/unapply`)
  },
  // ========== 敏感词过滤 ==========
  getSensitiveWords() {
    return request.get('/admin/sensitive-words')
  },
  createSensitiveWord(data) {
    return request.post('/admin/sensitive-words', data)
  },
  updateSensitiveWord(id, data) {
    return request.put(`/admin/sensitive-words/${id}`, data)
  },
  deleteSensitiveWord(id) {
    return request.delete(`/admin/sensitive-words/${id}`)
  },
  applySensitiveWord(id) {
    return request.put(`/admin/sensitive-words/${id}/apply`)
  },
  unapplySensitiveWord(id) {
    return request.put(`/admin/sensitive-words/${id}/unapply`)
  },
  // ========== 轮播图管理 ==========
  getBanners(params) {
    return request.get('/admin/banners', { params })
  },
  createBanner(data) {
    return request.post('/admin/banners', data)
  },
  updateBanner(id, data) {
    return request.put(`/admin/banners/${id}`, data)
  },
  adjustBannerOrder(id, direction) {
    return request.put(`/admin/banners/${id}/order`, { direction })
  },
  deleteBanner(id) {
    return request.delete(`/admin/banners/${id}`)
  },
  applyBanners() {
    return request.post('/admin/banners/apply')
  }
}
