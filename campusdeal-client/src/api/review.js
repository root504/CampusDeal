import request from './index'

export const reviewApi = {
  /** 查询某商品的评价列表 */
  getProductReviews(productId, page = 1, size = 20) {
    return request.get(`/reviews/product/${productId}`, { params: { page, size } })
  },

  /** 检查订单是否已评价 */
  checkReviewed(orderId) {
    return request.get('/reviews/check', { params: { orderId } })
  },

  /** 提交评价（通过订单接口） */
  submitReview(orderId, data) {
    return request.post(`/orders/${orderId}/review`, data)
  },

  /** 查询用户是否有该商品的待评价订单 */
  getReviewableOrder(productId) {
    return request.get('/orders/reviewable', { params: { productId } })
  },

  /** 查询某卖家收到的评价总人数 */
  getSellerReviewCount(sellerId) {
    return request.get(`/reviews/seller/${sellerId}/count`)
  }
}
