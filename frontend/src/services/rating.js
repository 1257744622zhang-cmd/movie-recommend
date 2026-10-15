import api from './api'

const ratingService = {
  // 添加评分 - 支持0-10分，0.5分精度
  addRating: async (movieId, rating, note) => {
    const params = new URLSearchParams({ movieId, rating })
    if (note) params.append('note', note)
    return await api.post(`/rating/add?${params.toString()}`)
  },

  // 获取用户所有评分
  getUserRatings: async () => {
    return await api.get('/rating/user')
  },

  // 获取电影的所有评分
  getMovieRatings: async (movieId) => {
    return await api.get(`/rating/movie?movieId=${movieId}`)
  },

  // 获取电影平均分
  getAverageRating: async (movieId) => {
    return await api.get(`/rating/average?movieId=${movieId}`)
  },

  // 获取评分统计信息（包含分布）
  getRatingStats: async (movieId) => {
    return await api.get(`/rating/stats?movieId=${movieId}`)
  },

  // 获取评分分布
  getRatingDistribution: async (movieId) => {
    return await api.get(`/rating/distribution?movieId=${movieId}`)
  },

  // 获取当前用户对某电影的评分
  getMyRating: async (movieId) => {
    return await api.get(`/rating/check?movieId=${movieId}`)
  }
}

export default ratingService
