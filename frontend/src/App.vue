<template>
  <div id="app">
    <el-container class="layout-container">
      <!-- 侧边栏 -->
      <el-aside v-if="!isAuthPage" width="220px" class="sidebar">
        <div class="sidebar-header">
          <div class="logo">
            <i class="el-icon-film"></i>
            <span>电影推荐</span>
          </div>
        </div>
        
        <div class="sidebar-menu">
          <div class="menu-section">
            <div class="section-title">发现</div>
            <router-link to="/home" class="menu-item" :class="{ active: $route.path === '/home' }">
              <i class="el-icon-s-home"></i>
              <span>首页</span>
            </router-link>
            <router-link to="/search" class="menu-item" :class="{ active: $route.path === '/search' }">
              <i class="el-icon-search"></i>
              <span>搜索</span>
            </router-link>
          </div>
          
          <div v-if="user" class="menu-section">
            <div class="section-title">我的</div>
            <router-link to="/collection" class="menu-item" :class="{ active: $route.path === '/collection' }">
              <i class="el-icon-star-on"></i>
              <span>收藏</span>
            </router-link>
            <router-link to="/notes" class="menu-item" :class="{ active: $route.path === '/notes' }">
              <i class="el-icon-edit-outline"></i>
              <span>笔记</span>
            </router-link>
            <router-link to="/tags" class="menu-item" :class="{ active: $route.path === '/tags' }">
              <i class="el-icon-price-tag"></i>
              <span>标签</span>
            </router-link>
            <router-link to="/user/preference" class="menu-item" :class="{ active: $route.path === '/user/preference' }">
              <i class="el-icon-data-line"></i>
              <span>爱好分析</span>
            </router-link>
            <router-link to="/recommend/analysis" class="menu-item" :class="{ active: $route.path === '/recommend/analysis' }">
              <i class="el-icon-data-analysis"></i>
              <span>推荐算法分析</span>
            </router-link>
          </div>
          
          <div v-if="user && user.role === 'ADMIN'" class="menu-section">
            <div class="section-title">系统</div>
            <router-link to="/admin" class="menu-item" :class="{ active: $route.path === '/admin' }">
              <i class="el-icon-s-tools"></i>
              <span>管理</span>
            </router-link>
          </div>
        </div>
        
        <!-- 用户信息卡片 -->
        <div v-if="user" class="user-card">
          <el-avatar :size="40" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
          <div class="user-info">
            <div class="username">{{ user.username }}</div>
            <div class="user-role">{{ user.role === 'ADMIN' ? '管理员' : '用户' }}</div>
          </div>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <i class="el-icon-more"></i>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        
        <div v-else class="login-btn-wrapper">
          <el-button type="primary" class="login-btn" @click="$router.push('/login')">
            <i class="el-icon-user"></i> 登录
          </el-button>
        </div>
      </el-aside>
      
      <!-- 主内容区 -->
    <el-main class="main-content">
      <keep-alive>
        <router-view />
      </keep-alive>
    </el-main>
    </el-container>
  </div>
</template>

<script>
import { storage } from './utils'

export default {
  data() {
    return {
      user: storage.get('user')
    }
  },
  computed: {
    isAuthPage() {
      return ['/login', '/register'].includes(this.$route.path)
    }
  },
  methods: {
    logout() {
      storage.remove('user')
      this.user = null
      this.$router.push('/login')
    },
    handleUserCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/user/profile')
          break
        case 'password':
          this.$router.push('/user/change-password')
          break
        case 'logout':
          this.logout()
          break
      }
    },
    updateUserInfo() {
      this.user = storage.get('user')
    }
  },
  watch: {
    $route() {
      this.user = storage.get('user')
    }
  }
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { 
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background: #f0f2f5;
}
#app { min-height: 100vh; }

.layout-container {
  min-height: 100vh;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 4px 0 20px rgba(0,0,0,0.15);
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.logo i {
  font-size: 28px;
  color: #409EFF;
}

.sidebar-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.menu-section {
  margin-bottom: 24px;
}

.section-title {
  color: rgba(255,255,255,0.4);
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 0 12px;
  margin-bottom: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 4px 0;
  border-radius: 10px;
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.menu-item:hover {
  background: rgba(255,255,255,0.08);
  color: #fff;
}

.menu-item.active {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(64,158,255,0.4);
}

.menu-item i {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.menu-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 用户卡片 */
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin: 12px;
  background: rgba(255,255,255,0.05);
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.1);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.username {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  color: rgba(255,255,255,0.5);
  font-size: 12px;
  margin-top: 2px;
}

.user-card .el-icon-more {
  color: rgba(255,255,255,0.5);
  cursor: pointer;
  padding: 4px;
  transition: color 0.3s;
}

.user-card .el-icon-more:hover {
  color: #fff;
}

/* 登录按钮 */
.login-btn-wrapper {
  padding: 16px;
}

.login-btn {
  width: 100%;
  border-radius: 10px;
  height: 44px;
  font-weight: 500;
}

/* 主内容区 */
.main-content {
  margin-left: 220px;
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255,255,255,0.2);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(255,255,255,0.3);
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }
  
  .sidebar-header {
    padding: 16px 12px;
  }
  
  .logo span,
  .section-title,
  .menu-item span,
  .user-info {
    display: none;
  }
  
  .menu-item {
    justify-content: center;
    padding: 16px;
  }
  
  .main-content {
    margin-left: 64px;
  }
}
</style>
