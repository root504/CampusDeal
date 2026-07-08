import request from './index'

export const cartApi = {
  getCart() {
    return request.get('/cart')
  },
  addToCart(data) {
    return request.post('/cart', data)
  },
  updateQuantity(id, data) {
    return request.put(`/cart/${id}`, data)
  },
  toggleSelect(id) {
    return request.put(`/cart/${id}/select`)
  },
  selectAll(selected) {
    return request.put('/cart/select-all', { selectAll: selected })
  },
  removeItem(id) {
    return request.delete(`/cart/${id}`)
  },
  removeBatch(ids) {
    return request.delete('/cart/batch', { data: { productIds: ids } })
  }
}
