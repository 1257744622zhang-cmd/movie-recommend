import api from './api'

const noteService = {
  // 创建笔记
  createNote: async (movieId, title, content, category = 'GENERAL', isPublic = false) => {
    const params = new URLSearchParams({ movieId, title, content })
    if (category) params.append('category', category)
    if (isPublic !== undefined) params.append('isPublic', isPublic)
    return await api.post(`/notes/create?${params.toString()}`)
  },

  // 更新笔记
  updateNote: async (noteId, data) => {
    const params = new URLSearchParams()
    if (data.title !== undefined) params.append('title', data.title)
    if (data.content !== undefined) params.append('content', data.content)
    if (data.category !== undefined) params.append('category', data.category)
    if (data.isPublic !== undefined) params.append('isPublic', data.isPublic)
    if (data.isFavorite !== undefined) params.append('isFavorite', data.isFavorite)
    return await api.post(`/notes/update/${noteId}?${params.toString()}`)
  },

  // 删除笔记
  deleteNote: async (noteId) => {
    return await api.post(`/notes/delete/${noteId}`)
  },

  // 获取用户所有笔记
  getUserNotes: async () => {
    return await api.get('/notes/list')
  },

  // 获取某电影的笔记
  getNotesByMovie: async (movieId) => {
    return await api.get(`/notes/movie/${movieId}`)
  },

  // 获取某电影的公开笔记
  getPublicNotesByMovie: async (movieId) => {
    return await api.get(`/notes/public/movie/${movieId}`)
  },

  // 获取收藏的笔记
  getFavoriteNotes: async () => {
    return await api.get('/notes/favorites')
  },

  // 获取某分类的笔记
  getNotesByCategory: async (category) => {
    return await api.get(`/notes/category/${category}`)
  },

  // 获取笔记详情
  getNoteDetail: async (noteId) => {
    return await api.get(`/notes/detail/${noteId}`)
  },

  // 获取用户笔记总数
  getUserNoteCount: async () => {
    return await api.get('/notes/count')
  },

  // 获取某电影的笔记数量
  getNoteCountByMovie: async (movieId) => {
    return await api.get(`/notes/count/movie/${movieId}`)
  }
}

export default noteService
