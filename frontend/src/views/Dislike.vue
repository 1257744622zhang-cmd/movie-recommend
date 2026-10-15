<template>
  <div class="dislike">
    <h1>不推荐</h1>
    <el-row v-loading="loading" :gutter="20">
      <el-col v-for="item in dislikedMovies" :key="item.id" :xs="12" :sm="8" :md="6">
        <el-card class="movie-card" shadow="hover" @click.native="$router.push('/movie/' + (item.movieId || item.movie_id))">
          <div class="poster">{{ getMovieTitle(item.movieId || item.movie_id).slice(0, 2) }}</div>
          <div class="title">
            {{ getMovieTitle(item.movieId || item.movie_id) }}
            <i class="el-icon-close disliked"></i>
          </div>
          <div class="meta">{{ getMovieMeta(item.movieId || item.movie_id) }}</div>
          <div class="rating">
            <span class="score">{{ movies[item.movieId || item.movie_id] ? movies[item.movieId || item.movie_id].avgRating : 0 }}</span>
            <el-rate :value="movies[item.movieId || item.movie_id] ? movies[item.movieId || item.movie_id].avgRating / 2 : 0" :max="5" disabled />
          </div>
          <div class="actions">
            <el-button size="small" type="success" icon="el-icon-check" @click.stop="removeDislike(item.movieId || item.movie_id)">取消不推荐</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && dislikedMovies.length === 0" description="暂无不推荐" />
  </div>
</template>

<script>
import { getDislikedMovies, cancelDislike } from '../services/feedback'
import movieService from '../services/movie'

export default {
  data() {
    return {
      dislikedMovies: [],
      movies: {},
      loading: true,
      user: JSON.parse(localStorage.getItem('user') || 'null')
    }
  },
  created() {
    this.loadDislikedMovies()
  },
  methods: {
    async loadDislikedMovies() {
      this.loading = true
      try {
        if (!this.user) {
          this.$message.info('请先登录解锁更多功能')
          this.dislikedMovies = []
          return
        }
        const feedbacks = await getDislikedMovies()
        this.dislikedMovies = feedbacks
        
        const movieIds = [...new Set(feedbacks.map(item => item.movieId || item.movie_id))]
        for (const id of movieIds) {
          try {
            const movie = await movieService.getMovieById(id)
            this.$set(this.movies, id, movie)
          } catch (e) {
            console.error('获取电影详情失败:', e)
          }
        }
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async removeDislike(movieId) {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        console.log('取消不推荐，movieId:', movieId)
        await cancelDislike(movieId)
        this.$message.success('已取消不推荐')
        this.loadDislikedMovies()
      } catch (e) {
        console.error('取消不推荐失败:', e)
        this.$message.error(e.message || '取消不推荐失败')
      }
    },
    getMovieTitle(movieId) {
      return this.movies[movieId] ? this.movies[movieId].title : '未知电影'
    },
    getMovieMeta(movieId) {
      const movie = this.movies[movieId]
      if (!movie) return '未知'
      return `${movie.genre} · ${movie.releaseYear}`
    },
    getMovieRating(movieId) {
      return this.movies[movieId] ? this.movies[movieId].avgRating : 0
    }
  }
}
</script>

<style scoped>
.dislike h1 { margin-bottom: 20px; }
.movie-card { cursor: pointer; margin-bottom: 20px; }
.movie-card .poster {
  height: 120px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 32px; border-radius: 4px; margin-bottom: 10px;
}
.movie-card .title {
  font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 4px;
}
.movie-card .disliked {
  color: #f56c6c;
  font-size: 16px;
}
.movie-card .meta { font-size: 12px; color: #909399; margin-bottom: 4px; }
.movie-card .rating { font-size: 12px; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.movie-card .rating .score { font-size: 14px; font-weight: bold; color: #606266; }
.movie-card .rating .el-rate { font-size: 12px; }
.movie-card .actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.movie-card:hover .actions {
  opacity: 1;
}

.movie-card .actions .el-button {
  padding: 4px 12px;
  font-size: 12px;
}
</style>
