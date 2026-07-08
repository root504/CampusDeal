import request from './index'

export const productApi = {
  getProducts(params) {
    return request.get('/products', { params })
  },
  getProduct(id) {
    return request.get(`/products/${id}`)
  },
  createProduct(data) {
    return request.post('/products', data)
  },
  updateProduct(id, data) {
    return request.put(`/products/${id}`, data)
  },
  deleteProduct(id) {
    return request.delete(`/products/${id}`)
  },
  getHot() {
    return request.get('/products/hot')
  },
  getLatest() {
    return request.get('/products/latest')
  },
  getSimilar(id) {
    return request.get(`/products/${id}/similar`)
  },
  getCategories() {
    return request.get('/categories')
  },
  getMyProducts(params) {
    return request.get('/products/my', { params })
  },
  getMyProduct(id) {
    return request.get(`/products/my/${id}`)
  },
  submitAppeal(id, data) {
    return request.post(`/products/${id}/appeal`, data)
  }
}
