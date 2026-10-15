<template>
  <div class="login-box">
    <el-card>
      <h2>登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
          <el-button @click="guestBrowse">游客浏览</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { authService } from '../services'
import { storage } from '../utils'

export default {
  data() {
    return {
      form: { username: '', password: '' },
      loading: false
    }
  },
  methods: {
    async submit() {
      this.loading = true
      try {
        const user = await authService.login(this.form)
        storage.set('user', user)
        this.$message.success('登录成功')
        
        // 检查是否有保存的跳转路径
        const redirectPath = localStorage.getItem('redirectPath')
        if (redirectPath) {
          localStorage.removeItem('redirectPath')
          this.$router.push(redirectPath)
        } else {
          this.$router.push('/home')
        }
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    guestBrowse() {
      storage.remove('user')
      this.$router.push('/home')
    }
  }
}
</script>

<style scoped>
.login-box { max-width: 400px; margin: 80px auto; }
h2 { margin-bottom: 20px; }
</style>
