// 收藏状态枚举
export const CollectionStatus = {
  WATCHLIST: { value: 'WATCHLIST', label: '想看', icon: 'el-icon-star-off', color: '#f7ba2a', type: 'warning' },
  WATCHING: { value: 'WATCHING', label: '在看', icon: 'el-icon-video-play', color: '#409EFF', type: 'primary' },
  WATCHED: { value: 'WATCHED', label: '已看', icon: 'el-icon-check', color: '#67C23A', type: 'success' },
  COLLECTED: { value: 'COLLECTED', label: '收藏', icon: 'el-icon-star-on', color: '#f56c6c', type: 'danger' }
}

// 获取状态配置
export const getStatusConfig = (status) => {
  return CollectionStatus[status] || CollectionStatus.COLLECTED
}

// 获取状态标签类型
export const getStatusType = (status) => {
  const config = getStatusConfig(status)
  return config.type
}

// 获取状态标签文本
export const getStatusLabel = (status) => {
  const config = getStatusConfig(status)
  return config.label
}

// 获取状态图标
export const getStatusIcon = (status) => {
  const config = getStatusConfig(status)
  return config.icon
}

// 获取所有状态列表
export const getAllStatuses = () => {
  return Object.values(CollectionStatus)
}
