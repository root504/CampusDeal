import request from './index'

export const orderApi = {
  getOrders(params) {
    return request.get('/orders', { params })
  },
  getOrder(id) {
    return request.get(`/orders/${id}`)
  },
  createOrder(data) {
    return request.post('/orders', data)
  },
  payOrder(id) {
    return request.put(`/orders/${id}/pay`)
  },
  shipOrder(id) {
    return request.put(`/orders/${id}/ship`)
  },
  receiveOrder(id) {
    return request.put(`/orders/${id}/receive`)
  },
  cancelOrder(id, reason) {
    return request.put(`/orders/${id}/cancel`, { reason })
  },
  reviewOrder(id, data) {
    return request.post(`/orders/${id}/review`, data)
  }
}
