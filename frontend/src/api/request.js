import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) {
    config.headers['X-User-Id'] = user.id
    config.headers['X-User-Role'] = user.role
    if (user.token) {
      config.headers['Authorization'] = `Bearer ${user.token}`
    }
  }
  return config
})

api.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || err.message
    return Promise.reject(new Error(msg))
  }
)

export default api
