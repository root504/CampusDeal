import request from './index'

export const bannerApi = {
  getBanners() {
    return request.get('/banners')
  }
}
