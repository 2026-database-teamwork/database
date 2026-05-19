import api from './index';

export const login = (data) => {
  return api.post('/api/auth/login', data);
};

export const join = (data) => {
  return api.post('/api/auth/join', data);
};
