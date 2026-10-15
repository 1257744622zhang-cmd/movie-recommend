import api from './api'

const collectionService = {
  addToCollection: async (movieId, options = {}) => {
    const params = new URLSearchParams({ movieId })
    if (options.status) params.append('status', options.status)
    if (options.collectionName) params.append('collectionName', options.collectionName)
    if (options.permission) params.append('permission', options.permission)
    return await api.post(`/collection/add?${params.toString()}`)
  },
  removeFromCollection: async (movieId) => {
    return await api.delete(`/collection/remove?movieId=${movieId}`)
  },
  updateCollectionStatus: async (movieId, status) => {
    return await api.put(`/collection/status?movieId=${movieId}&status=${status}`)
  },
  getUserCollections: async () => {
    return await api.get('/collection/user')
  },
  getUserCollectionByStatus: async (status) => {
    return await api.get(`/collection/user/status?status=${status}`)
  },
  getUserCollectionByCollectionName: async (collectionName) => {
    return await api.get(`/collection/user/collection?collectionName=${collectionName}`)
  },
  isMovieCollected: async (movieId) => {
    return await api.get(`/collection/check?movieId=${movieId}`)
  }
}

export default collectionService
