<template>
  <el-card :class="['movie-card', { 'compact': compact }]" shadow="hover" @click.native="$emit('click')">
    <!-- 收藏状态标签 -->
    <el-tag v-if="movie.isCollected" type="warning" size="small" class="collect-tag">
      <i class="el-icon-star-on"></i> 已收藏
    </el-tag>
    <!-- 观看状态标签 -->
    <el-tag v-if="movie.watchStatus && movie.watchStatus !== 'NONE'" :type="getStatusType(movie.watchStatus)" size="small" class="status-tag">
      {{ getStatusLabel(movie.watchStatus) }}
    </el-tag>
    
    <div class="card-content">
      <div class="poster">{{ movie.title?.slice(0, 2) || '电影' }}</div>
      <div class="movie-info">
        <h3 class="title">{{ movie.title }}</h3>
        <div v-if="!compact" class="meta">
          <p>导演：{{ movie.director || '未知' }}</p>
          <p>主演：{{ movie.actors || '未知' }}</p>
          <p>类型：{{ movie.genre }} | 年份：{{ movie.releaseYear }} | 地区：{{ movie.region || '未知' }}</p>
        </div>
        <div v-else class="meta-compact">{{ movie.genre }} · {{ movie.releaseYear }}</div>
        <div v-if="!compact" class="description">{{ movie.description || '暂无描述' }}</div>
        <div class="rating">
          <span class="score">{{ displayRating }}</span>
          <el-rate :value="displayRating / 2" :max="5" disabled />
        </div>
      </div>
    </div>
    <div class="actions">
      <!-- 编辑按钮（仅管理员可见） - 放在操作栏最前面 -->
      <el-button
        v-if="isAdmin"
        size="small"
        type="primary"
        icon="el-icon-edit"
        @click.stop="handleEdit"
        style="margin-right: 8px;"
      >
        编辑电影
      </el-button>
      <!-- 删除按钮（仅管理员可见） -->
      <el-button
        v-if="isAdmin"
        size="small"
        type="danger"
        icon="el-icon-delete"
        @click.stop="handleDelete"
        style="margin-right: 8px;"
      >
        删除
      </el-button>
      <!-- 观看状态下拉 -->
      <el-dropdown @command="(status) => $emit('watch-status', status)" size="small" style="margin-right: 8px;" @click.native.stop>
        <el-button size="small" :type="movie.watchStatus && movie.watchStatus !== 'NONE' ? 'success' : 'info'" class="watch-btn">
          {{ movie.watchStatus && movie.watchStatus !== 'NONE' ? getStatusLabel(movie.watchStatus) : '标记' }}<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="WATCHLIST">想看</el-dropdown-item>
          <el-dropdown-item command="WATCHING">在看</el-dropdown-item>
          <el-dropdown-item command="WATCHED">已看</el-dropdown-item>
          <el-dropdown-item v-if="movie.watchStatus && movie.watchStatus !== 'NONE'" command="NONE" divided>清除标记</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <!-- 收藏按钮 -->
      <el-button
        size="small"
        :type="movie.isCollected ? 'warning' : 'primary'"
        :icon="movie.isCollected ? 'el-icon-star-on' : 'el-icon-star-off'"
        class="collect-btn"
        @click.stop="$emit('toggle-collect')"
      >
        {{ movie.isCollected ? '已收藏' : '收藏' }}
      </el-button>
      <el-button
        size="small"
        :type="movie.isDisliked ? 'info' : 'danger'"
        :icon="movie.isDisliked ? 'el-icon-close' : 'el-icon-delete'"
        @click.stop="$emit('dislike')"
        class="dislike-btn"
      >
        {{ movie.isDisliked ? '已不喜欢' : '不喜欢' }}
      </el-button>
    </div>
  </el-card>
</template>

<script>
import { getStatusConfig, getStatusType, getStatusLabel, getStatusIcon } from '../constants/collectionStatus'
import { Permission } from '../utils/permission'
import ratingService from '../services/rating'

export default {
  name: 'MovieCard',
  props: {
    movie: {
      type: Object,
      required: true
    },
    compact: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      realTimeRating: null
    }
  },
  computed: {
    displayRating() {
      if (this.realTimeRating) {
        return this.realTimeRating
      }
      return '暂无评分'
    },
    isAdmin() {
      return Permission.isAdmin()
    }
  },
  created() {
    this.loadRealTimeRating()
  },
  watch: {
    'movie.id': {
      handler() {
        this.loadRealTimeRating()
      }
    }
  },
  methods: {
    handleEdit() {
      console.log('MovieCard - 编辑按钮被点击，电影:', this.movie)
      this.$emit('edit-movie', this.movie)
    },
    handleDelete() {
      console.log('MovieCard - 删除按钮被点击，电影:', this.movie)
      this.$emit('delete-movie', this.movie)
    },
    getStatusType(status) {
      return getStatusType(status || 'COLLECTED')
    },
    getStatusLabel(status) {
      return getStatusLabel(status || 'COLLECTED')
    },
    getStatusIcon(status) {
      return getStatusIcon(status || 'COLLECTED')
    },
    getStatusColor(status) {
      const config = getStatusConfig(status || 'COLLECTED')
      return config.color
    },
    async loadRealTimeRating() {
      if (this.movie && this.movie.id) {
        try {
          const stats = await ratingService.getRatingStats(this.movie.id)
          if (stats && stats.averageRating) {
            this.realTimeRating = stats.averageRating
          }
        } catch (e) {
          console.error('加载实时评分失败:', e)
        }
      }
    },
    // 更新电影状态
    updateMovieStatus(updates) {
      this.movie = { ...this.movie, ...updates }
    }
  }
}
</script>

<style scoped>
.movie-card { cursor: pointer; margin-bottom: 20px; position: relative; width: 85%; margin-left: auto; margin-right: auto; min-height: 300px; display: flex; flex-direction: column; }
.movie-card .collect-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
}
.movie-card .status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
}
/* 管理员编辑按钮 - 放在卡片右上角 */
.admin-edit-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}
.movie-card .card-content {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  min-height: 180px;
  height: auto;
  position: relative;
}
.movie-card .poster {
  width: 120px;
  min-height: 180px;
  height: 180px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  border-radius: 4px;
  flex-shrink: 0;
  flex-grow: 0;
  flex-basis: 120px;
}
.movie-card .movie-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.movie-card .movie-info .rating {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 0;
}
.movie-card .movie-info .title {
  font-weight: bold;
  font-size: 18px;
  margin: 0 0 10px 0;
}
.movie-card .movie-info .meta {
  margin-bottom: 10px;
}
.movie-card .movie-info .meta p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}
.movie-card .movie-info .meta-compact {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.movie-card .movie-info .description {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
  line-height: 1.4;
}
.movie-card.compact .card-content {
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
}
.movie-card.compact {
  height: 280px;
  display: flex;
  flex-direction: column;
}
.movie-card.compact .poster {
  width: 100%;
  height: 150px;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.movie-card.compact .movie-info {
  width: 100%;
  text-align: left;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.movie-card.compact .movie-info .title {
  margin: 0 0 4px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.movie-card.compact .movie-info .rating {
  margin-bottom: 8px;
}
.movie-card .movie-info .rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.movie-card .movie-info .rating .score {
  font-size: 16px;
  font-weight: bold;
  color: #606266;
}
.movie-card .movie-info .rating .el-rate {
  font-size: 14px;
}
.movie-card .actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.movie-card .actions .el-button {
  padding: 4px 12px;
  font-size: 12px;
  margin-left: 8px;
}

.movie-card .actions .el-button:first-child {
  margin-left: 0;
}
</style>
