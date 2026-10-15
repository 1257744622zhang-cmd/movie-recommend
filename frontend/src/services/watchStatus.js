import api from './api'

const watchStatusService = {
  // 设置观看状态
  setWatchStatus: async (movieId, status) => {
    return await api.post(`/watch-status/${movieId}`, { status })
  },

  // 获取观看状态
  getWatchStatus: async (movieId) => {
    return await api.get(`/watch-status/${movieId}`)
  },

  // 清除观看状态
  removeWatchStatus: async (movieId) => {
    return await api.delete(`/watch-status/${movieId}`)
  },

  // 获取用户所有观看状态
  getUserWatchStatuses: async () => {
    return await api.get('/watch-status/list')
  }
}

export default watchStatusService
