import request from './index'

export const messageApi = {
  getConversations() {
    return request.get('/conversations')
  },
  getMessages(conversationId, params) {
    return request.get(`/conversations/${conversationId}/messages`, { params })
  },
  sendMessage(data) {
    return request.post('/messages', data)
  },
  markRead(conversationId) {
    return request.put(`/messages/read/${conversationId}`)
  },

  markAllRead() {
    return request.put('/messages/read-all')
  },
  getUnreadCount() {
    return request.get('/messages/unread-count')
  },
  getAdminMessages() {
    return request.get('/messages/admin')
  },
  // 获取系统通知（公告 + 审核结果）
  getSystemNotifications() {
    return request.get('/messages/system-notifications')
  },
  // 获取公告列表（用户端）
  getAnnouncements() {
    return request.get('/announcements')
  },
  // 用户发送消息给管理员
  sendAdminMessage(data) {
    return request.post('/messages/admin/send', data)
  }
}
