import api from './index';

export const getCompaniesByRegion = (region) => {
  return api.get(`/api/company/${region}`);
};

export const getCarsByCompany = (companyId) => {
  return api.get(`/api/company/${companyId}/cars`);
};
