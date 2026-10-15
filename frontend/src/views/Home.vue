<template>
  <div class="home">
    <div class="header">
      <h1>电影推荐系统</h1>
      <div class="algorithm-switch">
        <el-radio-group v-model="selectedAlgorithm" @change="switchAlgorithm">
          <el-radio label="hybrid">混合推荐</el-radio>
          <el-radio label="cf">协同过滤</el-radio>
          <el-radio label="autoencoder">自编码器</el-radio>
        </el-radio-group>
      </div>
    </div>
    <p v-if="!user" class="tip">登录后可获得个性化推荐</p>
    
    <div v-loading="loading">
      <movie-card
        v-for="m in movies"
        :key="m.id"
        :movie="m"
        @click="handleMovieClick(m.id)"
        @watch-status="(status) => handleWatchStatus(m.id, status)"
        @toggle-collect="toggleCollect(m.id)"
        @dislike="dislikeMovie(m.id)"
        @edit-movie="handleEditMovie"
        @delete-movie="handleDeleteMovie"
      />
      <el-empty v-if="!loading && movies.length === 0" description="暂无推荐" />
    </div>
    
    <!-- 右侧可折叠挂件 -->
    <div class="recommendation-sidebar" :class="{ expanded: sidebarExpanded, collapsed: !sidebarExpanded }">
      <!-- 展开状态 -->
      <div v-if="sidebarExpanded" class="sidebar-content" :style="{ background: getSidebarBackground() }">
        <div class="sidebar-header">
          <h4>{{ getSidebarTitle() }}</h4>
          <el-button type="text" icon="el-icon-s-fold" @click="sidebarExpanded = false" />
        </div>
        <div class="sidebar-body">
          <div class="params-list">
            <div class="param-item" v-for="(value, key) in algorithmParams" :key="key">
              <span class="param-name">{{ key }}</span>
              <div class="param-bar">
                <div class="param-bar-fill" :style="{width: (value * 10) + '%', background: getParamBarColor(key)}"></div>
              </div>
              <span class="param-value">{{ value.toFixed(1) }}</span>
            </div>
          </div>
          <div class="recommendation-note">
            <p class="note-text">{{ algorithmNote }}</p>
          </div>
        </div>
      </div>
      
      <!-- 折叠状态 - 只显示按钮 -->
      <div v-else class="sidebar-collapsed">
        <el-tooltip content="展开推荐参数" placement="left">
          <el-button type="primary" icon="el-icon-s-unfold" circle @click="sidebarExpanded = true" />
        </el-tooltip>
      </div>
    </div>
    
    <!-- 编辑电影对话框 -->
    <el-dialog :title="editMovieId ? '编辑电影' : '添加电影'" :visible.sync="movieDialogVisible" width="500px">
      <el-form :model="movieForm" label-width="80px">
        <el-form-item label="片名"><el-input v-model="movieForm.title" /></el-form-item>
        <el-form-item label="导演"><el-input v-model="movieForm.director" /></el-form-item>
        <el-form-item label="主演"><el-input v-model="movieForm.actors" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="movieForm.genre" placeholder="选择类型">
            <el-option v-for="g in genres" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份"><el-input-number v-model="movieForm.releaseYear" :min="1900" /></el-form-item>
        <el-form-item label="地区"><el-input v-model="movieForm.region" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="movieForm.description" type="textarea" rows="4" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="movieDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMovie">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import recommendService from '../services/recommend'
import collectionService from '../services/collection'
import folderService from '../services/folder'
import watchStatusService from '../services/watchStatus'
import { dislikeMovie, cancelDislike } from '../services/feedback'
import behaviorService from '../services/behavior'
import axios from 'axios'
import MovieCard from '../components/MovieCard.vue'
import eventBus from '../utils/eventBus'

export default {
  name: 'Home',
  components: {
    MovieCard
  },
  data() {
    return {
      selectedAlgorithm: 'hybrid',
      movies: [],
      loading: true,
      user: JSON.parse(localStorage.getItem('user') || 'null'),
      defaultFolderId: null,
      userProfile: null,
      userCluster: null,
      sidebarExpanded: true,
      // 电影编辑相关
      movieDialogVisible: false,
      editMovieId: null,
      genres: ['剧情', '喜剧', '动作', '爱情', '科幻', '悬疑', '恐怖', '动画', '纪录片', '战争', '犯罪', '奇幻'],
      movieForm: { title: '', director: '', actors: '', genre: '', releaseYear: 2020, region: '', description: '' }
    }
  },
  created() {
    this.setDefaultUserProfile()
    this.fetchRecommendations()
    if (this.user) {
      this.loadDefaultFolder()
      this.fetchUserProfile()
    }
    // 添加电影更新事件监听器
    this.movieUpdatedListener = eventBus.$on('movie-updated', (updatedMovie) => {
      const index = this.movies.findIndex(movie => movie.id === updatedMovie.id)
      if (index !== -1) {
        this.movies[index] = { ...this.movies[index], ...updatedMovie }
        // 触发视图更新
        this.$forceUpdate()
      }
    })
    
    // 添加电影删除事件监听器
    this.movieDeletedListener = eventBus.$on('movie-deleted', (movieId) => {
      this.movies = this.movies.filter(movie => movie.id !== movieId)
    })
  },
  beforeDestroy() {
    // 移除事件监听器
    if (this.movieUpdatedListener) {
      eventBus.$off('movie-updated', this.movieUpdatedListener)
    }
    if (this.movieDeletedListener) {
      eventBus.$off('movie-deleted', this.movieDeletedListener)
    }
  },
  methods: {
    // 处理从MovieCard传来的编辑事件
    handleEditMovie(movie) {
      console.log('Home.vue - 收到编辑电影事件:', movie)
      this.showMovieDialog(movie)
    },
    // 处理从MovieCard传来的删除事件
    handleDeleteMovie(movie) {
      console.log('Home.vue - 收到删除电影事件:', movie)
      this.$confirm('确定要删除这部电影吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const user = JSON.parse(localStorage.getItem('user') || '{}')
          const headers = {
            'Authorization': 'Bearer ' + (user.token || ''),
            'X-User-Role': user.role || ''
          }
          await axios.delete('/api/admin/movie/' + movie.id, { headers })
          this.$message.success('删除成功')
          // 从本地列表移除
          this.movies = this.movies.filter(m => m.id !== movie.id)
          // 发送事件通知其他组件
          eventBus.$emit('movie-deleted', movie.id)
        } catch (e) {
          this.$message.error('删除失败: ' + (e.response?.data?.message || e.message))
        }
      }).catch(() => {
        // 用户取消
      })
    },
    // 显示编辑对话框
    showMovieDialog(movie) {
      console.log('Home.vue - 显示编辑对话框:', movie)
      this.editMovieId = movie ? movie.id : null
      if (movie) {
        this.movieForm = { ...movie }
      } else {
        this.movieForm = { title: '', director: '', actors: '', genre: '', releaseYear: 2020, region: '', description: '' }
      }
      this.movieDialogVisible = true
    },
    // 提交电影编辑
    async submitMovie() {
      try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        const headers = {
          'Authorization': 'Bearer ' + (user.token || ''),
          'X-User-Role': user.role || ''
        }
        
        if (this.editMovieId) {
          await axios.put('/api/admin/movie/' + this.editMovieId, this.movieForm, { headers })
          this.$message.success('更新成功')
          // 更新本地列表
          const index = this.movies.findIndex(m => m.id === this.editMovieId)
          if (index !== -1) {
            this.movies[index] = { ...this.movies[index], ...this.movieForm }
          }
          // 发送事件通知其他组件
          eventBus.$emit('movie-updated', this.movieForm)
        } else {
          await axios.post('/api/admin/movie', this.movieForm, { headers })
          this.$message.success('添加成功')
        }
        this.movieDialogVisible = false
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    async fetchRecommendations() {
      this.loading = true
      try {
        if (this.user) {
          switch (this.selectedAlgorithm) {
            case 'hybrid':
              this.movies = await recommendService.getHybridRecommendations()
              break
            case 'cf':
              this.movies = await recommendService.getCFRecommendations()
              break
            case 'autoencoder':
              this.movies = await recommendService.getAutoencoderRecommendations()
              break
            default:
              this.movies = await recommendService.getRecommendations()
          }
        } else {
          this.movies = await recommendService.getNewUserRecommendations()
        }
        
        // 记录浏览行为 - 每部电影卡片显示后记录一次
        if (this.user) {
          this.movies.forEach(movie => {
            behaviorService.logView(movie.id)
          })
        }
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    switchAlgorithm() {
      this.fetchRecommendations()
    },
    async fetchUserProfile() {
      try {
        const profile = await recommendService.getUserProfile()
        
        // 正确处理后端返回的数据结构
        if (profile) {
          const interestFeatures = profile.interestFeatures || {}
          
          // 从interestFeatures提取并构建genrePreferences
          let genrePreferences
          
          if (Object.keys(interestFeatures).length > 0) {
            genrePreferences = this.normalizePreferences(interestFeatures)
          } else {
            // 如果没有数据，使用默认数据
            genrePreferences = this.getDefaultGenrePreferences()
          }
          
          this.userProfile = {
            interestFeatures: interestFeatures,
            genrePreferences: genrePreferences
          }
        } else {
          this.setDefaultUserProfile()
        }
        
        const cluster = await recommendService.getUserCluster()
        this.userCluster = cluster
      } catch (e) {
        console.error('获取用户画像失败，使用默认数据:', e)
        this.setDefaultUserProfile()
      }
    },
    setDefaultUserProfile() {
      // 设置默认用户画像数据
      this.userProfile = {
        interestFeatures: {
          '剧情': 0.3,
          '喜剧': 0.25,
          '动作': 0.2,
          '爱情': 0.15,
          '科幻': 0.1
        },
        genrePreferences: this.getDefaultGenrePreferences()
      }
      this.userCluster = {
        clusterId: 1,
        clusterSize: 128,
        centerFeatures: '剧情, 喜剧, 动作'
      }
    },
    getDefaultGenrePreferences() {
      // 返回默认的类型偏好数据
      return {
        '剧情': 8.5,
        '喜剧': 7.8,
        '动作': 7.2,
        '爱情': 6.9,
        '科幻': 7.5,
        '悬疑': 6.8,
        '犯罪': 6.5,
        '动画': 6.3
      }
    },
    normalizePreferences(preferences) {
      // 将偏好数据标准化到0-10范围
      if (!preferences || Object.keys(preferences).length === 0) {
        return this.getDefaultGenrePreferences()
      }
      
      const values = Object.values(preferences)
      const maxValue = Math.max(...values)
      const minValue = Math.min(...values)
      
      const normalized = {}
      for (const [key, value] of Object.entries(preferences)) {
        // 归一化到0-10范围
        if (maxValue === minValue) {
          normalized[key] = 5.0
        } else {
          normalized[key] = ((value - minValue) / (maxValue - minValue)) * 10
        }
      }
      
      return normalized
    },
    async loadDefaultFolder() {
      try {
        const folders = await folderService.getUserFolders()
        const defaultFolder = folders.find(f => f.isDefault)
        if (defaultFolder) {
          this.defaultFolderId = defaultFolder.id
        }
      } catch (e) {
        console.error('加载默认收藏夹失败:', e)
      }
    },
    async dislikeMovie(movieId) {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        const movie = this.movies.find(m => m.id === movieId)
        if (!movie) return
        
        if (movie.isDisliked) {
          // 取消不喜欢
          await cancelDislike(movieId)
          this.$message.success('已取消不喜欢')
          movie.isDisliked = false
        } else {
          // 标记为不喜欢
          await dislikeMovie(movieId)
          this.$message.success('已标记为不喜欢')
          movie.isDisliked = true
        }
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    // 切换收藏状态
    async toggleCollect(movieId) {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        const movie = this.movies.find(m => m.id === movieId)
        if (!movie) return
        
        if (movie.isCollected) {
          // 取消收藏
          await collectionService.removeFromCollection(movieId)
          this.$message.success('已取消收藏')
          movie.isCollected = false
        } else {
          // 添加收藏
          await collectionService.addToCollection(movieId, { 
            status: 'COLLECTED', 
            collectionName: '默认收藏夹' 
          })
          // 移动到默认收藏夹
          if (this.defaultFolderId) {
            try {
              await folderService.moveMovieToFolder(this.defaultFolderId, movieId)
            } catch (e) {
              console.log('电影已在默认收藏夹中')
            }
          }
          this.$message.success('已收藏')
          movie.isCollected = true
        }
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    // 处理观看状态 - 独立于收藏
    async handleWatchStatus(movieId, status) {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        const movie = this.movies.find(m => m.id === movieId)
        if (!movie) return
        
        if (status === 'NONE') {
          // 清除观看状态
          await watchStatusService.removeWatchStatus(movieId)
          movie.watchStatus = 'NONE'
          this.$message.success('已清除标记')
        } else {
          // 设置观看状态 - 不自动收藏
          await watchStatusService.setWatchStatus(movieId, status)
          movie.watchStatus = status
          this.$message.success(`已标记为"${this.getStatusLabel(status)}"`)
          // 记录观看状态变更行为
          behaviorService.logBehavior(status, movieId, `status:${status}`)
        }
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    // 记录点击行为
    async handleMovieClick(movieId) {
      if (this.user) {
        behaviorService.logClick(movieId)
      }
      this.$router.push('/movie/' + movieId)
    },
    getStatusLabel(status) {
      const labels = {
        'WATCHLIST': '想看',
        'WATCHING': '在看',
        'WATCHED': '已看',
        'COLLECTED': '收藏'
      }
      return labels[status] || status
    },
    getParamBarColor(paramName) {
      // 根据参数名称和当前算法返回不同的颜色
      const colorMaps = {
        hybrid: {
          '用户相似度': 'linear-gradient(90deg, #667eea, #764ba2)',
          '内容相似度': 'linear-gradient(90deg, #f093fb, #f5576c)',
          '流行度权重': 'linear-gradient(90deg, #4facfe, #00f2fe)',
          '多样性因子': 'linear-gradient(90deg, #43e97b, #38f9d7)',
          '时效性分数': 'linear-gradient(90deg, #fa709a, #fee140)'
        },
        cf: {
          '用户相似度': 'linear-gradient(90deg, #ff6a00, #ee0979)',
          '近邻数量': 'linear-gradient(90deg, #667eea, #764ba2)',
          '评分相似度': 'linear-gradient(90deg, #f093fb, #f5576c)',
          '行为相似性': 'linear-gradient(90deg, #4facfe, #00f2fe)',
          '共同评分': 'linear-gradient(90deg, #43e97b, #38f9d7)',
          '信任度': 'linear-gradient(90deg, #fa709a, #fee140)'
        },
        autoencoder: {
          '隐层特征': 'linear-gradient(90deg, #00c6ff, #0072ff)',
          '特征压缩': 'linear-gradient(90deg, #f093fb, #f5576c)',
          '非线性重构': 'linear-gradient(90deg, #667eea, #764ba2)',
          '特征相关性': 'linear-gradient(90deg, #4facfe, #00f2fe)',
          '泛化能力': 'linear-gradient(90deg, #43e97b, #38f9d7)',
          '降噪能力': 'linear-gradient(90deg, #fa709a, #fee140)'
        }
      }
      
      // 返回对应颜色，默认使用紫色渐变
      return colorMaps[this.selectedAlgorithm]?.[paramName] || 'linear-gradient(90deg, #667eea, #764ba2)'
    },
    getSidebarTitle() {
      // 根据算法返回不同的标题
      switch(this.selectedAlgorithm) {
        case 'hybrid':
          return '混合推荐参数'
        case 'cf':
          return '协同过滤参数'
        case 'autoencoder':
          return '自编码器参数'
        default:
          return '推荐参数'
      }
    },
    getSidebarBackground() {
      // 根据算法返回不同的背景渐变色
      switch(this.selectedAlgorithm) {
        case 'hybrid':
          return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        case 'cf':
          return 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        case 'autoencoder':
          return 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
        default:
          return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }
    }
  },
  computed: {
    algorithmParams() {
      // 根据不同算法返回不同的推荐参数
      const userPrefs = this.userProfile?.genrePreferences || this.getDefaultGenrePreferences()
      const sortedPrefs = Object.entries(userPrefs)
        .sort((a, b) => b[1] - a[1])
      
      let params = {}
      
      switch(this.selectedAlgorithm) {
        case 'hybrid':
          // 混合推荐：平衡考虑多种因素
          return {
            '用户相似度': 9.2,
            '内容相似度': 8.7,
            [sortedPrefs[0]?.[0] || '剧情']: sortedPrefs[0]?.[1] || 8.5,
            [sortedPrefs[1]?.[0] || '喜剧']: sortedPrefs[1]?.[1] || 7.8,
            '流行度权重': 7.5,
            '多样性因子': 6.8,
            '时效性分数': 6.3
          }
        case 'cf':
          // 协同过滤：强调用户相似性
          return {
            '用户相似度': 9.8,
            '近邻数量': 8.5,
            '评分相似度': 8.2,
            '行为相似性': 7.9,
            [sortedPrefs[0]?.[0] || '剧情']: sortedPrefs[0]?.[1] * 0.8 || 6.8,
            '共同评分': 5.5,
            '信任度': 5.2
          }
        case 'autoencoder':
          // 自编码器：强调深度学习特征
          return {
            '隐层特征': 9.5,
            '特征压缩': 9.0,
            '非线性重构': 8.5,
            [sortedPrefs[0]?.[0] || '剧情']: sortedPrefs[0]?.[1] * 0.7 || 6.0,
            '特征相关性': 7.2,
            '泛化能力': 6.8,
            '降噪能力': 6.2
          }
        default:
          return {
            [sortedPrefs[0]?.[0] || '剧情']: sortedPrefs[0]?.[1] || 8.5,
            [sortedPrefs[1]?.[0] || '喜剧']: sortedPrefs[1]?.[1] || 7.8,
            [sortedPrefs[2]?.[0] || '动作']: sortedPrefs[2]?.[1] || 7.2,
            [sortedPrefs[3]?.[0] || '爱情']: sortedPrefs[3]?.[1] || 6.9,
            [sortedPrefs[4]?.[0] || '科幻']: sortedPrefs[4]?.[1] || 7.5
          }
      }
    },
    algorithmNote() {
      // 根据不同算法返回不同的推荐说明
      const userPrefs = this.userProfile?.genrePreferences || this.getDefaultGenrePreferences()
      const sortedPrefs = Object.entries(userPrefs)
        .sort((a, b) => b[1] - a[1])
      const topGenres = sortedPrefs.slice(0, 3).map(([genre]) => genre)
      
      switch(this.selectedAlgorithm) {
        case 'hybrid':
          return `混合算法结合${topGenres[0]}偏好、用户相似度和内容特征，为您精准推荐！`
        case 'cf':
          return `协同过滤：分析与您相似的${topGenres[0]}和${topGenres[1]}爱好者行为，为您发现惊喜！`
        case 'autoencoder':
          return `自编码器：深度学习隐藏偏好，发现您可能喜欢的${topGenres[0]}、${topGenres[1]}等！`
        default:
          return `已为您推荐更多${topGenres[0]}、${topGenres[1]}及其他类型的电影！`
      }
    }
  }
}
</script>

<style scoped>
.home .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.home h1 {
  margin: 0;
}

.algorithm-switch {
  margin-top: 10px;
}

.tip { 
  color: #909399; 
  margin-bottom: 20px; 
  font-size: 14px; 
  max-width: 1200px; 
  margin-left: auto; 
  margin-right: auto; 
  padding: 0 20px; 
}

.home > div:not(.header):not(.recommendation-sidebar) {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

/* 右侧可折叠挂件样式 */
.recommendation-sidebar {
  position: fixed;
  right: 20px;
  top: 100px;
  transition: all 0.3s ease;
  z-index: 100;
}

.recommendation-sidebar.expanded {
  width: 280px;
}

.recommendation-sidebar.collapsed {
  width: 56px;
}

.sidebar-content {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  overflow: hidden;
  transition: background 0.3s ease;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.sidebar-header h4 {
  color: white;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.sidebar-header .el-button {
  color: white;
  padding: 4px;
}

.sidebar-header .el-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-body {
  padding: 12px 14px;
}

.params-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 12px;
  max-height: 280px;
  overflow-y: auto;
}

.params-list::-webkit-scrollbar {
  width: 4px;
}

.params-list::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 2px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.param-name {
  min-width: 48px;
  font-weight: 500;
  color: #303133;
  font-size: 12px;
}

.param-bar {
  flex: 1;
  height: 14px;
  background: #f0f2f5;
  border-radius: 7px;
  overflow: hidden;
  position: relative;
}

.param-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 7px;
  transition: width 0.5s ease;
  max-width: 100%;
}

.param-value {
  min-width: 36px;
  text-align: right;
  font-weight: 600;
  color: #667eea;
  font-size: 12px;
}

.recommendation-note {
  margin-top: 12px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 10px 12px;
}

.note-text {
  margin: 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.sidebar-collapsed {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
