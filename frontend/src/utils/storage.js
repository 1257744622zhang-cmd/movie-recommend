// 本地存储工具函数

const storage = {
  get: (key) => {
    try {
      return JSON.parse(localStorage.getItem(key))
    } catch (error) {
      console.error('Error getting data from localStorage:', error)
      return null
    }
  },
  set: (key, value) => {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error('Error setting data to localStorage:', error)
    }
  },
  remove: (key) => {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error('Error removing data from localStorage:', error)
    }
  },
  clear: () => {
    try {
      localStorage.clear()
    } catch (error) {
      console.error('Error clearing localStorage:', error)
    }
  }
}

export default storage
