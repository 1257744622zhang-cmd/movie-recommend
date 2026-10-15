import api from './api'

const folderService = {
  // 创建收藏夹
  createFolder: async (name, description, permission = 'PRIVATE') => {
    const params = new URLSearchParams({ name })
    if (description) params.append('description', description)
    if (permission) params.append('permission', permission)
    return await api.post(`/folder/create?${params.toString()}`)
  },

  // 重命名收藏夹
  renameFolder: async (id, newName) => {
    return await api.put(`/folder/${id}/rename?newName=${encodeURIComponent(newName)}`)
  },

  // 删除收藏夹
  deleteFolder: async (id, moveToDefault = true) => {
    return await api.delete(`/folder/${id}?moveToDefault=${moveToDefault}`)
  },

  // 获取用户所有收藏夹
  getUserFolders: async () => {
    return await api.get('/folder/list')
  },

  // 获取收藏夹中的电影
  getFolderMovies: async (id) => {
    return await api.get(`/folder/${id}/movies`)
  },

  // 移动电影到收藏夹
  moveMovieToFolder: async (folderId, movieId) => {
    return await api.post(`/folder/${folderId}/move?movieId=${movieId}`)
  },

  // 批量移动电影到收藏夹
  batchMoveMoviesToFolder: async (folderId, movieIds) => {
    return await api.post(`/folder/${folderId}/batch-move`, movieIds)
  },

  // 获取收藏夹电影数量
  getFolderMovieCount: async (id) => {
    return await api.get(`/folder/${id}/count`)
  },

  // 设置收藏夹权限
  setFolderPermission: async (id, permission) => {
    return await api.put(`/folder/${id}/permission?permission=${permission}`)
  },

  // 获取公开的收藏夹列表
  getPublicFolders: async () => {
    return await api.get('/folder/public')
  },

  // 获取收藏夹详情（带权限检查）
  getFolderDetail: async (id) => {
    return await api.get(`/folder/${id}/detail`)
  }
}

export default folderService
