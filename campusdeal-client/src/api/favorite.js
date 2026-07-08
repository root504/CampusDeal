import request from './index'

export const favoriteApi = {
  getFavorites() {
    return request.get('/favorites')
  },
  addFavorite(productId) {
    return request.post('/favorites', { productId })
  },
  removeFavorite(productId) {
    return request.delete(`/favorites/${productId}`)
  },
  checkFavorite(productId) {
    return request.get(`/favorites/check/${productId}`)
  }
}
