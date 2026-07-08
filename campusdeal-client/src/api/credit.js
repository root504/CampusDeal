import request from './index'

export const creditApi = {
  getMyCredit() {
    return request.get('/credits/my')
  },
  getMyRecords(params) {
    return request.get('/credits/my/records', { params })
  },
  getRules() {
    return request.get('/credits/rules')
  }
}
