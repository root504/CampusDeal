import request from './index'

export const addressApi = {
  getList() {
    return request.get('/addresses')
  },
  getDetail(id) {
    return request.get(`/addresses/${id}`)
  },
  create(data) {
    return request.post('/addresses', data)
  },
  update(id, data) {
    return request.put(`/addresses/${id}`, data)
  },
  delete(id) {
    return request.delete(`/addresses/${id}`)
  },
  setDefault(id) {
    return request.put(`/addresses/${id}/default`)
  }
}
