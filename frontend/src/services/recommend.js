import api from './api'

const recommendService = {
  getRecommendations: async () => {
    return await api.get('/recommend/user')
  },
  getNewUserRecommendations: async () => {
    return await api.get('/recommend/new-user')
  },
  refreshRecommendations: async () => {
    return await api.post('/recommend/refresh')
  },
  addFeedback: async (movieId, feedbackType, score) => {
    const params = new URLSearchParams({ movieId, feedbackType })
    if (score) params.append('score', score)
    return await api.post(`/recommend-feedback/add?${params.toString()}`)
  },
  // 新增：获取协同过滤推荐
  getCFRecommendations: async () => {
    return await api.get('/recommend/cf')
  },
  // 新增：获取自编码器推荐
  getAutoencoderRecommendations: async () => {
    return await api.get('/recommend/autoencoder')
  },
  // 新增：获取混合推荐
  getHybridRecommendations: async () => {
    return await api.get('/recommend/hybrid')
  },
  // 新增：获取用户画像
  getUserProfile: async () => {
    return await api.get('/recommend/profile')
  },
  // 新增：获取用户聚类
  getUserCluster: async () => {
    return await api.get('/recommend/cluster')
  },
  // 新增：填充评分矩阵
  fillRatingMatrix: async () => {
    return await api.post('/recommend/fill-matrix')
  },
  // 新增：获取算法性能
  getAlgorithmPerformance: async () => {
    return await api.get('/recommend/performance')
  }
}

export default recommendService
