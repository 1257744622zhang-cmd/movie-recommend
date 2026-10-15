<template>
  <div class="register-box">
    <el-card>
      <h2>注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <!-- 观影偏好设置 -->
        <el-form-item label="电影类型偏好">
          <el-select v-model="form.genrePreferences" multiple placeholder="请选择喜欢的电影类型">
            <el-option label="剧情" value="剧情" />
            <el-option label="喜剧" value="喜剧" />
            <el-option label="爱情" value="爱情" />
            <el-option label="科幻" value="科幻" />
            <el-option label="悬疑" value="悬疑" />
            <el-option label="动作" value="动作" />
            <el-option label="恐怖" value="恐怖" />
            <el-option label="动画" value="动画" />
            <el-option label="纪录片" value="纪录片" />
            <el-option label="战争" value="战争" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="导演偏好">
          <el-select v-model="form.directorPreferences" multiple placeholder="请选择喜欢的导演（可选）">
            <el-option label="张艺谋" value="张艺谋" />
            <el-option label="冯小刚" value="冯小刚" />
            <el-option label="陈凯歌" value="陈凯歌" />
            <el-option label="李安" value="李安" />
            <el-option label="吴宇森" value="吴宇森" />
            <el-option label="王家卫" value="王家卫" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="演员偏好">
          <el-select v-model="form.actorPreferences" multiple placeholder="请选择喜欢的演员（可选）">
            <el-option label="巩俐" value="巩俐" />
            <el-option label="章子怡" value="章子怡" />
            <el-option label="梁朝伟" value="梁朝伟" />
            <el-option label="刘德华" value="刘德华" />
            <el-option label="周润发" value="周润发" />
            <el-option label="周星驰" value="周星驰" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">注册</el-button>
          <el-button @click="$router.push('/login')">去登录</el-button>
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
      form: {
        username: '',
        password: '',
        genrePreferences: [],
        directorPreferences: [],
        actorPreferences: []
      },
      loading: false
    }
  },
  methods: {
    async submit() {
      this.loading = true
      try {
        const user = await authService.register(this.form)
        storage.set('user', user)
        this.$message.success('注册成功')
        this.$router.push('/home')
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-box { max-width: 400px; margin: 80px auto; }
h2 { margin-bottom: 20px; }
</style>
