// 权限检查混入
export default {
  methods: {
    // 检查是否登录，未登录则提示并跳转
    requireAuth(message = '请先登录') {
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      if (!user) {
        this.$message.info(message)
        this.$router.push('/login')
        return false
      }
      return true
    },
    
    // 游客模式提示（不强制跳转）
    guestTip(message = '登录后可享受更多功能') {
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      if (!user) {
        this.$message.info(message)
      }
      return !!user
    }
  }
}
