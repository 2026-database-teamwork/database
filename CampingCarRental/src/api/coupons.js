import api from './index'

export const getMyCoupons = (username) => {
  return api.get('/api/coupons/my', {
    params: { username: username }
  })
}
