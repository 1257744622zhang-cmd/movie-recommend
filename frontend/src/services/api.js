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
    // 后端返回的是纯文本错误消息
    const msg = err.response?.data || err.message
    console.error('API错误:', err.response?.status, msg)
    return Promise.reject(new Error(msg))
  }
)

export default api
