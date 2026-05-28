import api from './index';

export const getCarRepairHistory = (carId) => {
  return api.get(`/api/repair/history/${carId}`);
};
