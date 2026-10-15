<template>
  <div class="change-password">
    <el-card>
      <h2>修改密码</h2>
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password @input="checkPasswordStrength" />
          <div class="password-strength" v-if="form.newPassword">
            <div class="strength-bar">
              <div class="strength-level" :class="passwordStrengthClass"></div>
            </div>
            <div class="strength-text">{{ passwordStrengthText }}</div>
          </div>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">确认修改</el-button>
          <el-button @click="$router.push('/user/profile')">返回个人中心</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { authService } from '../services'

export default {
  data() {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 8, message: '新密码至少8位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.form.newPassword) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      },
      submitting: false,
      passwordStrength: 0 // 0-3，0弱，1中，2强
    }
  },
  computed: {
    passwordStrengthClass() {
      const classes = ['weak', 'medium', 'strong']
      return classes[this.passwordStrength] || 'weak'
    },
    passwordStrengthText() {
      const texts = ['弱', '中', '强']
      return texts[this.passwordStrength] || '弱'
    }
  },
  methods: {
    checkPasswordStrength() {
      const password = this.form.newPassword
      if (!password) {
        this.passwordStrength = 0
        return
      }
      
      let strength = 0
      // 长度检查
      if (password.length >= 8) strength++
      // 包含字母
      if (/[a-zA-Z]/.test(password)) strength++
      // 包含数字
      if (/[0-9]/.test(password)) strength++
      // 包含特殊字符
      if (/[^a-zA-Z0-9]/.test(password)) strength++
      
      this.passwordStrength = Math.min(strength, 3)
    },
    async submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          this.submitting = true
          try {
            await authService.changePassword(this.form)
            this.$message.success('密码修改成功')
            // 二次确认
            this.$confirm('密码修改成功，是否返回个人中心？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'success'
            }).then(() => {
              this.$router.push('/user/profile')
            }).catch(() => {
              // 取消操作
            })
          } catch (e) {
            this.$message.error('密码修改失败: ' + e.message)
          } finally {
            this.submitting = false
          }
        } else {
          this.$message.error('请填写完整表单')
          return false
        }
      })
    }
  }
}
</script>

<style scoped>
.change-password {
  max-width: 600px;
  margin: 20px auto;
}

.password-strength {
  margin-top: 10px;
}

.strength-bar {
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.strength-level {
  height: 100%;
  transition: width 0.3s ease, background-color 0.3s ease;
  border-radius: 4px;
}

.strength-level.weak {
  width: 25%;
  background-color: #f56c6c;
}

.strength-level.medium {
  width: 50%;
  background-color: #e6a23c;
}

.strength-level.strong {
  width: 100%;
  background-color: #67c23a;
}

.strength-text {
  font-size: 12px;
  color: #909399;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
