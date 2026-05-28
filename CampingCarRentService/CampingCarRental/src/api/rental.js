import api from './index'

export const createRental = (data) => {
  return api.post('/api/rental/rent', data)
}

export const getMyRentals = () => {
  return api.get('/api/rental/history/my')
}

export const getCarRentals = (carId) => {
  return api.get(`/api/rental/history/car/${carId}`)
}

export const getUserInfo = (username) => {
  return api.get(`/api/member/${username}`)
}

export const getMyCoupons = (license) => {
  return api.post('/api/coupon/my', license, {
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}

