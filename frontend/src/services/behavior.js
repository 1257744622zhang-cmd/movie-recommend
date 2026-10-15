import api from './api'

const behaviorService = {
  // 记录用户行为
  logBehavior: async (behaviorType, targetId, targetInfo = '') => {
    try {
      const params = new URLSearchParams({
        behaviorType,
        targetId: targetId.toString(),
        targetInfo
      })
      console.log('上报行为:', behaviorType, targetId, targetInfo)
      const res = await api.post(`/behavior/log?${params.toString()}`)
      console.log('行为上报结果:', res)
      return res
    } catch (e) {
      console.error('行为上报失败:', e)
      throw e
    }
  },

  // 记录点击行为
  logClick: async (movieId) => {
    return await behaviorService.logBehavior('CLICK', movieId)
  },

  // 记录浏览行为
  logView: async (movieId) => {
    return await behaviorService.logBehavior('VIEW', movieId)
  },

  // 记录评分行为
  logRating: async (movieId, score) => {
    return await behaviorService.logBehavior('RATING', movieId, `score:${score}`)
  },

  // 记录收藏行为
  logCollection: async (movieId, action) => {
    return await behaviorService.logBehavior('COLLECTION', movieId, `action:${action}`)
  },

  // 记录搜索行为
  logSearch: async (keyword) => {
    return await behaviorService.logBehavior('SEARCH', 0, keyword)
  },

  // 记录笔记行为
  logNote: async (movieId) => {
    return await behaviorService.logBehavior('NOTE', movieId)
  },

  // 获取用户行为日志
  getUserLogs: async () => {
    return await api.get('/behavior/logs')
  }
}

export default behaviorService
