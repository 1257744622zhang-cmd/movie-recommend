<template>
  <div class="search-page">
    <el-form inline class="search-form">
      <el-form-item>
        <el-input v-model="keyword" placeholder="片名/导演/演员" clearable @keyup.enter="search" style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-select v-model="genre" placeholder="类型" clearable style="width: 120px">
          <el-option label="剧情" value="剧情" />
          <el-option label="喜剧" value="喜剧" />
          <el-option label="爱情" value="爱情" />
          <el-option label="科幻" value="科幻" />
          <el-option label="悬疑" value="悬疑" />
          <el-option label="动画" value="动画" />
          <el-option label="动作" value="动作" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
      </el-form-item>
    </el-form>
    
    <div v-loading="loading">
      <movie-card
        v-for="m in currentPageMovies"
        :key="m.id"
        :movie="m"
        @click="handleMovieClick(m.id)"
        @watch-status="(status) => handleWatchStatus(m.id, status)"
        @toggle-collect="toggleCollect(m.id)"
        @dislike="dislikeMovie(m.id)"
        @edit-movie="handleEditMovie"
        @delete-movie="handleDeleteMovie"
      />
      <el-empty v-if="!loading && movies.length === 0" description="暂无搜索结果" />
      <el-pagination
        v-if="movies.length > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="movies.length"
        style="margin-top: 20px; text-align: center;"
      />
    </div>
    
    <!-- 编辑电影对话框 -->
    <el-dialog :title="editMovieId ? '编辑电影' : '添加电影'" :visible.sync="movieDialogVisible" width="500px">
      <el-form :model="movieForm" label-width="80px">
        <el-form-item label="片名"><el-input v-model="movieForm.title" /></el-form-item>
        <el-form-item label="导演"><el-input v-model="movieForm.director" /></el-form-item>
        <el-form-item label="主演"><el-input v-model="movieForm.actors" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="movieForm.genre" placeholder="选择类型">
            <el-option label="剧情" value="剧情" />
            <el-option label="喜剧" value="喜剧" />
            <el-option label="爱情" value="爱情" />
            <el-option label="科幻" value="科幻" />
            <el-option label="悬疑" value="悬疑" />
            <el-option label="动画" value="动画" />
            <el-option label="动作" value="动作" />
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
import movieService from '../services/movie'
import collectionService from '../services/collection'
import folderService from '../services/folder'
import watchStatusService from '../services/watchStatus'
import { dislikeMovie as dislikeMovieService } from '../services/feedback'
import behaviorService from '../services/behavior'
import MovieCard from '../components/MovieCard.vue'
import axios from 'axios'
import eventBus from '../utils/eventBus'

export default {
  components: {
    MovieCard
  },
  data() {
    return {
      keyword: '',
      genre: '',
      movies: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      user: JSON.parse(localStorage.getItem('user') || 'null'),
      defaultFolderId: null,
      // 编辑电影相关
      editMovieId: null,
      movieForm: { title: '', director: '', actors: '', genre: '', releaseYear: 2020, region: '', description: '' },
      movieDialogVisible: false
    }
  },
  created() {
    this.search()
    if (this.user) {
      this.loadDefaultFolder()
    }
    // 监听电影更新事件
    this.movieUpdatedListener = (updatedMovie) => {
      const index = this.movies.findIndex(m => m.id === updatedMovie.id)
      if (index !== -1) {
        this.movies[index] = { ...this.movies[index], ...updatedMovie }
      }
      this.$forceUpdate()
    }
    eventBus.$on('movie-updated', this.movieUpdatedListener)
    
    // 监听电影删除事件
    this.movieDeletedListener = (movieId) => {
      this.movies = this.movies.filter(m => m.id !== movieId)
    }
    eventBus.$on('movie-deleted', this.movieDeletedListener)
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
  computed: {
    currentPageMovies() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.movies.slice(start, end)
    }
  },
  watch: {
    genre() {
      this.currentPage = 1
      this.search()
    },
    keyword() {
      this.currentPage = 1
      this.search()
    }
  },
  methods: {
    // 处理从MovieCard传来的编辑事件
    handleEditMovie(movie) {
      console.log('Search.vue - 收到编辑电影事件:', movie)
      this.showMovieDialog(movie)
    },
    // 处理从MovieCard传来的删除事件
    handleDeleteMovie(movie) {
      console.log('Search.vue - 收到删除电影事件:', movie)
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
      console.log('Search.vue - 显示编辑对话框:', movie)
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
    async search() {
      this.loading = true
      try {
        let results = []
        if (this.keyword) {
          results = await movieService.searchMovies(this.keyword)
        } else if (this.genre) {
          results = await movieService.filterMovies({ genre: this.genre })
        } else {
          results = await movieService.getHotMovies()
        }
        this.movies = results || []
        
        // 记录浏览行为 - 每部电影卡片显示后记录一次
        if (this.user) {
          this.movies.forEach(movie => {
            behaviorService.logView(movie.id)
          })
        }
        
        // 如果用户已登录，加载每个电影的收藏状态和观看状态
        if (this.user) {
          for (const movie of this.movies) {
            // 检查收藏状态
            try {
              const collected = await collectionService.isMovieCollected(movie.id)
              // 处理直接返回布尔值的情况
              movie.isCollected = typeof collected === 'boolean' ? collected : (collected.success ? collected.data : false)
            } catch (e) {
              console.error('获取收藏状态失败:', e)
              movie.isCollected = false
            }
            
            // 检查观看状态
            try {
              const watchStatus = await watchStatusService.getWatchStatus(movie.id)
              // 处理不同的返回格式
              movie.watchStatus = watchStatus.status || (typeof watchStatus === 'string' ? watchStatus : 'NONE')
            } catch (e) {
              console.error('获取观看状态失败:', e)
              movie.watchStatus = 'NONE'
            }
          }
        }
        
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
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
        await dislikeMovieService(movieId)
        this.$message.success('已标记为不喜欢')
        const movie = this.movies.find(m => m.id === movieId)
        if (movie) {
          movie.isDisliked = true
          movie.isCollected = false
          movie.watchStatus = 'NONE'
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
          await collectionService.removeFromCollection(movieId)
          this.$message.success('已取消收藏')
          movie.isCollected = false
          // 记录取消收藏行为
          behaviorService.logCollection(movieId, 'REMOVE')
        } else {
          await collectionService.addToCollection(movieId, { status: 'COLLECTED', collectionName: '默认收藏夹' })
          if (this.defaultFolderId) {
            try {
              await folderService.moveMovieToFolder(this.defaultFolderId, movieId)
            } catch (e) {
              console.log('电影已在默认收藏夹中')
            }
          }
          this.$message.success('已收藏')
          movie.isCollected = true
          // 记录收藏行为
          behaviorService.logCollection(movieId, 'COLLECTED')
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
          await watchStatusService.removeWatchStatus(movieId)
          movie.watchStatus = 'NONE'
          this.$message.success('已清除标记')
        } else {
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
    }
  }
}
</script>

<style scoped>
.search-form { margin-bottom: 20px; max-width: 1200px; margin-left: auto; margin-right: auto; padding: 0 20px; }
.search-page > div:not(.search-form) {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}
</style>
