<template>
  <div class="user-profile">
    <el-card>
      <h2>个人中心</h2>
      
      <div v-loading="loading" class="profile-content">
        <!-- 个人信息展示区域 -->
        <div class="profile-section">
          <h3>基本信息</h3>
          <el-form :model="userInfo" label-width="100px" v-if="!editing">
            <el-form-item label="用户名">
              <span>{{ userInfo.username }}</span>
            </el-form-item>
            <el-form-item label="姓名">
              <span>{{ userInfo.name || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="电话号码">
              <span>{{ userInfo.phone || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="邮箱">
              <span>{{ userInfo.email || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="性别">
              <span>{{ userInfo.gender || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="年龄">
              <span>{{ userInfo.age || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="地址">
              <span>{{ userInfo.address || '未设置' }}</span>
            </el-form-item>
            <el-form-item label="头像">
              <el-avatar :size="80" :src="userInfo.avatar || defaultAvatar" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="startEditing">编辑信息</el-button>
              <el-button @click="$router.push('/user/change-password')">修改密码</el-button>
            </el-form-item>
          </el-form>
          
          <!-- 编辑表单 -->
          <el-form :model="editForm" label-width="100px" v-else>
            <el-form-item label="姓名">
              <el-input v-model="editForm.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="电话号码">
              <el-input v-model="editForm.phone" placeholder="请输入电话号码" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="editForm.gender" placeholder="请选择性别">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="年龄">
              <el-input v-model="editForm.age" type="number" placeholder="请输入年龄" />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="editForm.address" placeholder="请输入地址" />
            </el-form-item>
            <el-form-item label="头像">
              <el-avatar :size="80" :src="editForm.avatar || defaultAvatar" />
              <div style="margin-top: 10px; color: #606266; font-size: 12px;">
                当前头像: {{ editForm.avatar || '未设置' }}
              </div>
              <el-upload
                class="avatar-uploader"
                :action="'/api/user/upload-avatar'"
                :on-success="handleAvatarUploadSuccess"
                :on-error="handleAvatarUploadError"
                :show-file-list="false"
                accept="image/*"
                style="margin-top: 10px;"
              >
                <el-button size="small" type="primary">选择图片</el-button>
              </el-upload>
              <el-input v-model="editForm.avatar" placeholder="头像URL" style="margin-top: 10px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="saveChanges">保存修改</el-button>
              <el-button @click="cancelEditing">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { authService } from '../services'
import { storage } from '../utils'

export default {
  data() {
    return {
      userInfo: {
        id: '',
        username: '',
        name: '',
        phone: '',
        email: '',
        gender: '',
        age: '',
        address: '',
        avatar: ''
      },
      editForm: {},
      editing: false,
      loading: false,
      submitting: false,
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  created() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      this.loading = true
      try {
        const user = await authService.getProfile()
        this.userInfo = user
      } catch (e) {
        this.$message.error('获取个人信息失败: ' + e.message)
      } finally {
        this.loading = false
      }
    },
    startEditing() {
      // 复制用户信息到编辑表单
      this.editForm = { ...this.userInfo }
      this.editing = true
    },
    cancelEditing() {
      this.editing = false
      this.editForm = {}
    },
    async saveChanges() {
      this.submitting = true
      try {
        // 准备更新数据，只包含个人信息字段
        const updateData = {
          name: this.editForm.name,
          phone: this.editForm.phone,
          email: this.editForm.email,
          gender: this.editForm.gender,
          age: this.editForm.age,
          address: this.editForm.address,
          avatar: this.editForm.avatar
        }
        
        await authService.updateProfile(updateData)
        this.$message.success('个人信息更新成功')
        this.editing = false
        await this.loadUserInfo() // 重新加载用户信息
        
        // 更新 localStorage 中的用户信息，确保导航栏头像与个人中心一致
        const currentUser = storage.get('user')
        if (currentUser) {
          // 只更新个人信息字段，保留 token 等其他信息
          currentUser.name = this.userInfo.name
          currentUser.phone = this.userInfo.phone
          currentUser.email = this.userInfo.email
          currentUser.gender = this.userInfo.gender
          currentUser.age = this.userInfo.age
          currentUser.address = this.userInfo.address
          currentUser.avatar = this.userInfo.avatar
          storage.set('user', currentUser)
          
          // 导航到当前页面，触发 App.vue 的 watch 监听器，更新导航栏头像
          this.$router.push(this.$route.path)
        }
      } catch (e) {
        this.$message.error('更新失败: ' + e.message)
      } finally {
        this.submitting = false
      }
    },
    handleAvatarUploadSuccess(response) {
      if (response.success) {
        this.editForm.avatar = response.data.avatarUrl
        this.$message.success('头像上传成功')
        // 强制更新组件
        this.$forceUpdate()
      } else {
        this.$message.error('头像上传失败: ' + response.message)
      }
    },
    handleAvatarUploadError() {
      this.$message.error('头像上传失败，请重试')
    }
  }
}
</script>

<style scoped>
.user-profile {
  max-width: 800px;
  margin: 20px auto;
}

.profile-content {
  margin-top: 20px;
}

.profile-section {
  margin-bottom: 30px;
}

.profile-section h3 {
  margin-bottom: 15px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eaeaea;
}

.el-form-item {
  margin-bottom: 15px;
}

.el-avatar {
  margin-right: 10px;
}
</style>
