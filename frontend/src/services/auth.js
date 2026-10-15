import api from './api'

const authService = {
  login: async (credentials) => {
    return await api.post('/auth/login', credentials)
  },
  register: async (userData) => {
    return await api.post('/auth/register', userData)
  },
  getProfile: async () => {
    return await api.get('/user/profile')
  },
  updateProfile: async (profileData) => {
    return await api.put('/user/profile', profileData)
  },
  changePassword: async (passwordData) => {
    return await api.post('/user/change-password', passwordData)
  }
}

export default authService
