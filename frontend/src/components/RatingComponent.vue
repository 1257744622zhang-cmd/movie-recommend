<template>
  <div class="rating-component">
    <!-- 评分展示 + 分布并排 -->
    <div class="rating-overview">
      <!-- 左侧：平均评分 -->
      <div class="score-panel">
        <div class="score-label">评分</div>
        <div class="score-main">
          <span class="score-number">{{ formattedAverage }}</span>
        </div>
        <div class="score-stars" v-if="ratingStats && ratingStats.totalCount && ratingStats.totalCount > 0">
          <el-rate
            :value="(ratingStats.averageRating || 0) / 2"
            :max="5"
            disabled
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          />
        </div>
        <div class="score-count">
          {{ (!this.ratingStats || !this.ratingStats.totalCount || this.ratingStats.totalCount === 0) ? '0人评分' : `${this.ratingStats.totalCount}人评分` }}
        </div>
      </div>

      <!-- 右侧：评分分布 -->
      <div class="distribution-panel" v-if="ratingStats && ratingStats.distribution">
        <div class="distribution-bars">
          <div
            v-for="item in starDistribution"
            :key="item.star"
            class="distribution-bar"
          >
            <div class="bar-stars">
              <i v-for="n in 5" :key="n" 
                 :class="getStarClass(n, item.star)"
                 class="star-icon"></i>
            </div>
            <div class="bar-wrapper">
              <div
                class="bar-fill"
                :style="{ width: item.percent + '%' }"
              />
            </div>
            <span class="bar-percent">{{ item.percent.toFixed(2) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户评分区域 -->
    <div v-if="user" class="user-rating">
      <div class="rating-row">
        <span class="rating-label">我的评分</span>
        <div class="rating-control">
          <el-rate
            v-model="myRating"
            :max="5"
            allow-half
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            @change="onRatingChange"
            class="user-rate-stars"
          />
          <span class="rating-value">{{ displayRating }}</span>
          <span class="rating-text" v-if="myRating">{{ ratingText }}</span>
        </div>
      </div>
      <el-input
        v-model="ratingNote"
        type="textarea"
        :rows="2"
        placeholder="写下你的观影感受（可选）"
        maxlength="500"
        show-word-limit
        class="rating-note"
        size="small"
      />
      <div class="submit-row">
        <el-button
          type="primary"
          size="small"
          @click="submitRating"
          :loading="submitting"
        >
          {{ hasRated ? '更新评分' : '提交评分' }}
        </el-button>
      </div>
    </div>

    <!-- 评分列表 -->
    <div v-if="ratings && ratings.length > 0" class="rating-list">
      <div class="section-title">最新评分</div>
      <div
        v-for="rating in ratings.slice(0, 5)"
        :key="rating.id"
        class="rating-item"
      >
        <div class="rating-header">
          <el-avatar :size="36" :src="'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          <div class="user-info">
            <div class="username">{{ rating.username || '匿名用户' }}</div>
            <div class="rating-meta">
              <el-rate
                :value="rating.rating / 2"
                :max="5"
                disabled
                class="user-stars"
              />
              <span class="rating-score">{{ rating.rating }}分</span>
              <span class="rating-time">{{ formatTime(rating.createdAt) }}</span>
            </div>
          </div>
        </div>
        <p v-if="rating.note" class="rating-content">{{ rating.note }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import ratingService from '../services/rating'

export default {
  name: 'RatingComponent',
  props: {
    movieId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || 'null'),
      myRating: 0,
      ratingNote: '',
      hasRated: false,
      submitting: false,
      ratingStats: null,
      ratings: []
    }
  },
  computed: {
    formattedAverage() {
      if (this.ratingStats && this.ratingStats.totalCount && this.ratingStats.totalCount > 0) {
        return this.ratingStats.averageRating ? this.ratingStats.averageRating.toFixed(1) : '0.0'
      }
      return '暂无评分'
    },
    displayRating() {
      if (!this.myRating) return '未评分'
      return (this.myRating * 2).toFixed(1)
    },
    ratingText() {
      if (!this.myRating) return ''
      const texts = {
        0.5: '很差', 1: '很差',
        1.5: '较差', 2: '较差',
        2.5: '一般', 3: '一般',
        3.5: '还行', 4: '还行',
        4.5: '推荐', 5: '推荐'
      }
      return texts[this.myRating] || ''
    },
    starDistribution() {
      if (!this.ratingStats || !this.ratingStats.distribution) return []
      const dist = this.ratingStats.distribution
      const total = this.ratingStats.totalCount || 1
      
      const ranges = [
        { star: 5, scores: [10, 9] },
        { star: 4, scores: [8, 7] },
        { star: 3, scores: [6, 5] },
        { star: 2, scores: [4, 3] },
        { star: 1, scores: [2, 1, 0] }
      ]
      
      return ranges.map(r => {
        let count = 0
        r.scores.forEach(score => {
          count += dist[score] || 0
        })
        return {
          star: r.star,
          count,
          percent: (count / total) * 100
        }
      })
    }
  },
  created() {
    this.loadRatingData()
    if (this.user) {
      this.loadMyRating()
    }
  },
  methods: {
    async loadRatingData() {
      try {
        this.ratingStats = await ratingService.getRatingStats(this.movieId)
        this.ratings = await ratingService.getMovieRatings(this.movieId)
      } catch (e) {
        console.error('加载评分数据失败:', e)
      }
    },
    async loadMyRating() {
      try {
        const myRatingData = await ratingService.getMyRating(this.movieId)
        if (myRatingData && myRatingData.rating) {
          this.myRating = myRatingData.rating / 2
          this.ratingNote = myRatingData.note || ''
          this.hasRated = true
        }
      } catch (e) {
        console.error('加载我的评分失败:', e)
      }
    },
    onRatingChange(val) {
      this.myRating = val
    },
    async submitRating() {
      if (this.myRating <= 0) {
        this.$message.warning('请先选择评分')
        return
      }
      this.submitting = true
      try {
        const ratingScore = this.myRating * 2
        const res = await ratingService.addRating(this.movieId, ratingScore, this.ratingNote)
        if (res.success) {
          this.$message.success(this.hasRated ? '评分已更新' : '评分成功')
          this.hasRated = true
          await this.loadRatingData()
          // 立即更新myScore并触发行为日志记录
          this.$emit('rating-updated', ratingScore)
        } else {
          this.$message.error(res.message || '评分失败')
        }
      } catch (e) {
        this.$message.error(e.message || '评分失败')
      } finally {
        this.submitting = false
      }
    },
    getStarClass(n, starValue) {
      if (n <= starValue) {
        return 'el-icon-star-on'
      } else {
        return 'el-icon-star-off'
      }
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleDateString('zh-CN')
    }
  }
}
</script>

<style scoped>
.rating-component {
  padding: 16px 0;
}

.rating-overview {
  display: flex;
  gap: 40px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 16px;
}

.score-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  padding-right: 40px;
  border-right: 1px solid #e0e0e0;
}

.score-label {
  font-size: 14px;
  color: #409EFF;
  margin-bottom: 8px;
}

.score-main {
  margin-bottom: 8px;
}

.score-number {
  font-size: 48px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
}

.score-stars {
  margin-bottom: 8px;
}

.score-stars :deep(.el-rate__icon) {
  font-size: 16px;
}

.score-count {
  font-size: 12px;
  color: #909399;
}

.distribution-panel {
  flex: 1;
}

.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-stars {
  min-width: 90px;
  display: flex;
  gap: 2px;
}

.star-icon {
  font-size: 12px;
  color: #c0c4cc;
}

.star-icon.el-icon-star-on {
  color: #f7ba2a;
}

.bar-wrapper {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #409EFF;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.bar-percent {
  min-width: 50px;
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.user-rating {
  background: #fff;
  padding: 16px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.rating-label {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
  white-space: nowrap;
}

.rating-control {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-rate-stars :deep(.el-rate__icon) {
  font-size: 24px;
}

.rating-value {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
  min-width: 50px;
}

.rating-text {
  color: #909399;
  font-size: 13px;
}

.rating-note {
  margin-bottom: 12px;
}

.rating-note :deep(.el-textarea__inner) {
  resize: none;
}

.submit-row {
  display: flex;
  justify-content: flex-end;
}

.rating-list {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
}

.rating-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.rating-item:last-child {
  border-bottom: none;
}

.rating-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.user-info {
  flex: 1;
}

.username {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
  margin-bottom: 4px;
}

.rating-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-stars :deep(.el-rate__icon) {
  font-size: 14px;
}

.rating-score {
  color: #f7ba2a;
  font-weight: bold;
  font-size: 13px;
}

.rating-time {
  color: #c0c4cc;
  font-size: 12px;
}

.rating-content {
  color: #606266;
  line-height: 1.6;
  margin: 8px 0 0 48px;
  font-size: 14px;
}
</style>
