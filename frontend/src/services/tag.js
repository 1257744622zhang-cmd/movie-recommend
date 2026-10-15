import api from './api'

const tagService = {
  // 获取所有标签
  getAllTags: async () => {
    return await api.get('/tag/list')
  },

  // 获取热门标签
  getHotTags: async (limit = 10) => {
    return await api.get(`/tag/hot?limit=${limit}`)
  },

  // 搜索标签
  searchTags: async (keyword) => {
    return await api.get(`/tag/search?keyword=${encodeURIComponent(keyword)}`)
  },

  // 创建标签
  createTag: async (name, color, description) => {
    return await api.post('/tag/create', { name, color, description })
  },

  // 更新标签
  updateTag: async (id, name, color, description) => {
    return await api.put(`/tag/${id}`, { name, color, description })
  },

  // 删除标签
  deleteTag: async (id) => {
    return await api.delete(`/tag/${id}`)
  },

  // 获取标签详情
  getTagById: async (id) => {
    return await api.get(`/tag/${id}`)
  },

  // 获取标签使用次数
  getTagUsageCount: async (id) => {
    return await api.get(`/tag/${id}/usage`)
  }
}

const movieTagService = {
  // 为电影添加标签
  addTagToMovie: async (movieId, tagId) => {
    return await api.post('/movie-tag/add', { movieId, tagId })
  },

  // 为电影添加多个标签
  addTagsToMovie: async (movieId, tagIds) => {
    return await api.post('/movie-tag/add-batch', { movieId, tagIds })
  },

  // 移除电影的标签
  removeTagFromMovie: async (movieId, tagId) => {
    return await api.delete(`/movie-tag/remove?movieId=${movieId}&tagId=${tagId}`)
  },

  // 获取电影的标签
  getTagsByMovieId: async (movieId) => {
    return await api.get(`/movie-tag/movie/${movieId}`)
  },

  // 根据标签搜索电影
  getMoviesByTagId: async (tagId) => {
    return await api.get(`/movie-tag/tag/${tagId}/movies`)
  },

  // 多标签搜索（AND模式）
  searchMoviesByTagsAnd: async (tagIds) => {
    return await api.post('/movie-tag/search-and', { tagIds })
  },

  // 多标签搜索（OR模式）
  searchMoviesByTagsOr: async (tagIds) => {
    return await api.post('/movie-tag/search-or', { tagIds })
  },

  // 检查电影是否有标签
  hasTag: async (movieId, tagId) => {
    return await api.get(`/movie-tag/check?movieId=${movieId}&tagId=${tagId}`)
  },

  // 获取电影的标签数量
  getMovieTagCount: async (movieId) => {
    return await api.get(`/movie-tag/movie/${movieId}/count`)
  }
}

export { tagService, movieTagService }
export default tagService
