import api from './index'

export const createRental = (data) => {
  return api.post('/api/rental/rent', data)
}

// data로 username 값을 받아 params에 매핑합니다.
export const getMyRentals = (username) => {
  return api.get('/api/rental/history/my', {
    params: { username: username }
  })
}

export const getCarRentals = (carId) => {
  return api.get(`/api/rental/history/car/${carId}`)
}
