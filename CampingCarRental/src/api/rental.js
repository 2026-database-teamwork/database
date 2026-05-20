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
