// 权限检查工具
export const Permission = {
  // 是否登录
  isAuthenticated() {
    return !!localStorage.getItem('user')
  },
  
  // 获取当前用户
  getCurrentUser() {
    return JSON.parse(localStorage.getItem('user') || 'null')
  },
  
  // 是否是管理员
  isAdmin() {
    const user = this.getCurrentUser()
    return user && user.role === 'ADMIN'
  },
  
  // 检查权限
  hasRole(role) {
    const user = this.getCurrentUser()
    return user && user.role === role
  },
  
  // 设置登录后跳转路径
  setRedirectPath(path) {
    localStorage.setItem('redirectPath', path)
  },
  
  // 获取并清除跳转路径
  getAndClearRedirectPath() {
    const path = localStorage.getItem('redirectPath')
    localStorage.removeItem('redirectPath')
    return path
  }
}
