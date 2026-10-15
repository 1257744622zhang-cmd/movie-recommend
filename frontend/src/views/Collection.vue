<template>
  <div class="collection">
    <h1>我的收藏</h1>
    
    <!-- 状态筛选 -->
    <el-tabs v-model="activeTab" class="status-tabs">
      <el-tab-pane label="全部" name="ALL"></el-tab-pane>
      <el-tab-pane label="想看" name="WATCHLIST"></el-tab-pane>
      <el-tab-pane label="在看" name="WATCHING"></el-tab-pane>
      <el-tab-pane label="已看" name="WATCHED"></el-tab-pane>
      <el-tab-pane label="不推荐" name="DISLIKE"></el-tab-pane>
      <el-tab-pane label="收藏夹管理" name="FOLDERS"></el-tab-pane>
    </el-tabs>

    <!-- 收藏夹管理内容 -->
    <div v-if="activeTab === 'FOLDERS'">
      <collection-manager />
    </div>

    <!-- 收藏列表内容 -->
    <div v-else>
      <el-row v-loading="loading" :gutter="20">
        <el-col v-for="movie in movieList" :key="movie.id" :xs="12" :sm="8" :md="6">
          <movie-card
            :movie="movie"
            :compact="true"
            @click="handleMovieClick(movie.id)"
            @watch-status="(status) => handleWatchStatus(movie.id, status)"
            @toggle-collect="toggleCollect(movie.id)"
            @dislike="dislikeMovie(movie.id)"
          />
        </el-col>
      </el-row>
      <el-empty v-if="!loading && movieList.length === 0" description="暂无收藏" />
    </div>
  </div>
</template>

<script>
import collectionService from '../services/collection'
import movieService from '../services/movie'
import folderService from '../services/folder'
import watchStatusService from '../services/watchStatus'
import { dislikeMovie, getDislikedMovies } from '../services/feedback'
import behaviorService from '../services/behavior'
import CollectionManager from './CollectionManager.vue'
import MovieCard from '../components/MovieCard.vue'
import eventBus from '../utils/eventBus'

export default {
  components: {
    CollectionManager,
    MovieCard
  },
  data() {
    return {
      collections: [],
      movies: {},
      loading: true,
      user: JSON.parse(localStorage.getItem('user') || 'null'),
      activeTab: 'ALL',
      defaultFolderId: null
    }
  },
  computed: {
    movieList() {
      // 根据当前选中的标签过滤
      let filteredCollections = this.collections
      
      // 如果选中了想看/在看/已看标签，只显示有对应观看状态的收藏
      if (this.activeTab === 'WATCHLIST' || this.activeTab === 'WATCHING' || this.activeTab === 'WATCHED') {
        filteredCollections = this.collections.filter(item => item.watchStatus === this.activeTab)
      } else if (this.activeTab === 'DISLIKE') {
        // 只显示不推荐的电影
        filteredCollections = this.collections.filter(item => item.isDisliked)
      }
      
      return filteredCollections.map(item => {
        const movie = this.movies[item.movieId] || {}
        return {
          ...movie,
          id: item.movieId,
          isCollected: item.status === 'COLLECTED',
          watchStatus: item.watchStatus || 'NONE',
          isDisliked: item.isDisliked
        }
      })
    }
  },
  created() {
    if (this.user) {
      this.loadDefaultFolder().then(() => {
        this.loadCollections()
      })
    } else {
      this.loadCollections()
    }
    // 添加电影更新事件监听器
    this.movieUpdatedListener = eventBus.$on('movie-updated', (updatedMovie) => {
      const index = this.collections.findIndex(item => item.movieId === updatedMovie.id)
      if (index !== -1) {
        // 更新收藏列表中的电影状态
        if (updatedMovie.isCollected !== undefined) {
          this.collections[index].status = updatedMovie.isCollected ? 'COLLECTED' : 'NONE'
        }
        if (updatedMovie.watchStatus !== undefined) {
          this.collections[index].watchStatus = updatedMovie.watchStatus
        }
        if (updatedMovie.isDisliked !== undefined) {
          this.collections[index].isDisliked = updatedMovie.isDisliked
        }
        // 触发视图更新
        this.$forceUpdate()
      }
      // 更新电影详情
      if (this.movies[updatedMovie.id]) {
        this.movies[updatedMovie.id] = { ...this.movies[updatedMovie.id], ...updatedMovie }
      }
    })
  },
  beforeDestroy() {
    // 移除事件监听器
    if (this.movieUpdatedListener) {
      eventBus.$off('movie-updated', this.movieUpdatedListener)
    }
  },
  watch: {
    activeTab() {
      if (this.activeTab !== 'FOLDERS') {
        this.loadCollections()
      }
    }
  },
  methods: {
    async loadCollections() {
      this.loading = true
      try {
        if (!this.user) {
          this.$message.info('请先登录解锁更多功能')
          this.collections = []
          return
        }
        
        // 获取收藏列表
        let collections = await collectionService.getUserCollections()
        
        // 获取观看状态列表
        const watchStatuses = await watchStatusService.getUserWatchStatuses()
        const watchStatusMap = {}
        watchStatuses.forEach(ws => {
          watchStatusMap[ws.movieId] = ws.status
        })
        
        // 获取不推荐电影列表
        const dislikedFeedbacks = await getDislikedMovies()
        const dislikedMap = {}
        dislikedFeedbacks.forEach(feedback => {
          dislikedMap[feedback.movieId] = true
        })
        
        // 创建收藏电影的映射，方便后续处理
        const collectionMap = {}
        collections.forEach(movie => {
          collectionMap[movie.id] = true
        })
        
        // 合并收藏电影、有观看状态的电影和不推荐电影
        const allMovieIds = new Set([...collections.map(item => item.id), ...watchStatuses.map(item => item.movieId), ...dislikedFeedbacks.map(item => item.movieId)])
        
        this.collections = []
        for (const movieId of allMovieIds) {
          // 检查电影是否在收藏列表中
          const isCollected = collectionMap[movieId] || false
          
          // 获取电影的观看状态
          const watchStatus = watchStatusMap[movieId] || 'NONE'
          
          // 检查电影是否在不推荐列表中
          const isDisliked = dislikedMap[movieId] || false
          
          // 只添加有收藏状态、观看状态或不推荐状态的电影
          if (isCollected || watchStatus !== 'NONE' || isDisliked) {
            this.collections.push({
              id: movieId,
              movieId: movieId,
              status: isCollected ? 'COLLECTED' : 'NONE',
              watchStatus: watchStatus,
              isDisliked: isDisliked
            })
          }
        }
        
        // 获取所有电影的详细信息
        for (const movieId of allMovieIds) {
          try {
            const movie = await movieService.getMovieById(movieId)
            this.$set(this.movies, movieId, movie)
          } catch (e) {
            console.error('获取电影详情失败:', e)
          }
        }
        
        // 确保所有已收藏的电影都在默认收藏夹里
        await this.ensureCollectionsInDefaultFolder(collections)
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async ensureCollectionsInDefaultFolder(collections) {
      if (!this.user || !this.defaultFolderId) return
      
      try {
        // 获取收藏夹内容，检查哪些电影不在默认收藏夹里
        const folders = await folderService.getUserFolders()
        const defaultFolder = folders.find(f => f.isDefault)
        if (!defaultFolder) return
        
        // 获取默认收藏夹里的电影
        const defaultFolderMovies = await folderService.getFolderMovies(this.defaultFolderId)
        const defaultFolderMovieIds = new Set(defaultFolderMovies.map(m => m.id))
        
        // 把所有已收藏但不在默认收藏夹里的电影移动过去
        for (const movie of collections) {
          if (!defaultFolderMovieIds.has(movie.id)) {
            try {
              await folderService.moveMovieToFolder(this.defaultFolderId, movie.id)
              console.log(`已将电影 ${movie.id} 移到默认收藏夹`)
            } catch (e) {
              console.log(`电影 ${movie.id} 已在默认收藏夹中`)
            }
          }
        }
      } catch (e) {
        console.error('确保收藏在默认收藏夹失败:', e)
      }
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
        await dislikeMovie(movieId)
        this.$message.success('已标记为不喜欢')
        this.loadCollections()
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
        // 查找电影对象
        const movieItem = this.collections.find(item => item.movieId === movieId)
        if (!movieItem) return
        
        if (movieItem.status === 'COLLECTED') {
          // 取消收藏
          await collectionService.removeFromCollection(movieId)
          this.$message.success('已取消收藏')
          // 记录取消收藏行为
          behaviorService.logCollection(movieId, 'REMOVE')
        } else {
          // 添加收藏
          await collectionService.addToCollection(movieId, { status: 'COLLECTED', collectionName: '默认收藏夹' })
          if (this.defaultFolderId) {
            try {
              await folderService.moveMovieToFolder(this.defaultFolderId, movieId)
            } catch (e) {
              console.log('电影已在默认收藏夹中')
            }
          }
          this.$message.success('已收藏')
          // 记录收藏行为
          behaviorService.logCollection(movieId, 'COLLECTED')
        }
        this.loadCollections()
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
        if (status === 'NONE') {
          await watchStatusService.removeWatchStatus(movieId)
          this.$message.success('已清除标记')
        } else {
          await watchStatusService.setWatchStatus(movieId, status)
          this.$message.success(`已标记为"${this.getStatusLabel(status)}"`)
          // 记录观看状态变更行为
          behaviorService.logBehavior(status, movieId, `status:${status}`)
        }
        this.loadCollections()
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
.collection h1 { margin-bottom: 20px; max-width: 1200px; margin-left: auto; margin-right: auto; padding: 0 20px; }
.status-tabs { margin-bottom: 20px; max-width: 1200px; margin-left: auto; margin-right: auto; padding: 0 20px; }
.collection > div:not(h1) {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}
</style>
