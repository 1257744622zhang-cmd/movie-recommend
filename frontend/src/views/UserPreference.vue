<template>
  <div class="user-preference">
    <h1>用户偏好分析</h1>
    <div v-loading="loading" class="analysis-container">
      <el-row :gutter="20">
        <!-- 左侧分析内容 -->
        <el-col :span="16">
          <!-- 类型偏好 -->
          <el-card class="analysis-card">
            <template slot="header">
              <div class="card-header">
                <span>类型偏好</span>
              </div>
            </template>
            <div class="chart-container">
              <el-empty v-if="!loading && preferences.genres.length === 0" description="暂无数据" />
              <div v-else ref="genreChart" class="chart"></div>
            </div>
          </el-card>
          
          <!-- 导演偏好 -->
          <el-card class="analysis-card">
            <template slot="header">
              <div class="card-header">
                <span>导演偏好</span>
              </div>
            </template>
            <div class="chart-container">
              <el-empty v-if="!loading && preferences.directors.length === 0" description="暂无数据" />
              <div v-else ref="directorChart" class="chart"></div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧分析内容 -->
        <el-col :span="8">
          <!-- 最近行为 -->
          <el-card class="analysis-card">
            <template slot="header">
              <div class="card-header">
                <span>最近行为</span>
              </div>
            </template>
            <div class="recent-activities">
              <el-empty v-if="!loading && recentActivities.length === 0" description="暂无数据" />
              <el-timeline v-else>
                <el-timeline-item 
                  v-for="(activity, index) in recentActivities" 
                  :key="index"
                  :timestamp="formatTime(activity.timestamp)"
                  :type="getActivityType(activity.behavior)"
                  :color="getActivityColor(activity.behavior)"
                >
                  {{ getActivityDescription(activity) }}
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-card>
          
          <!-- 行为统计 -->
          <el-card class="analysis-card">
            <template slot="header">
              <div class="card-header">
                <span>行为统计</span>
              </div>
            </template>
            <div class="behavior-stats">
              <el-statistic v-for="(stat, key) in behaviorStats" :key="key" :title="getStatTitle(key)" :value="stat" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import behaviorService from '../services/behavior'
import movieService from '../services/movie'
import * as echarts from 'echarts'

export default {
  data() {
    return {
      loading: true,
      preferences: {
        genres: [],
        directors: [],
        actors: []
      },
      recentActivities: [],
      behaviorStats: {
        clicks: 0,
        views: 0,
        collections: 0,
        watchStatus: 0
      },
      genreChart: null,
      directorChart: null
    }
  },
  created() {
    this.loadUserPreferences()
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
    })
    // 添加窗口大小变化监听器
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.genreChart) {
      this.genreChart.dispose()
    }
    if (this.directorChart) {
      this.directorChart.dispose()
    }
    // 移除窗口大小变化监听器
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleResize() {
      if (this.genreChart) {
        this.genreChart.resize()
      }
      if (this.directorChart) {
        this.directorChart.resize()
      }
    },
    async loadUserPreferences() {
      this.loading = true
      try {
        // 获取用户行为数据
        const response = await behaviorService.getUserLogs()
        console.log('行为数据响应:', response)
        // 确保behaviors是一个数组
        const behaviors = Array.isArray(response) ? response : (response?.data || [])
        console.log('处理后的行为数据:', behaviors)
        
        // 获取电影详情，用于分析类型、导演等
        const movieIds = [...new Set(behaviors.filter(b => b.targetId).map(b => b.targetId))]
        const movies = {}
        
        for (const id of movieIds) {
          try {
            const movie = await movieService.getMovieById(id)
            movies[id] = movie
          } catch (e) {
            console.error('获取电影详情失败:', e)
          }
        }
        
        // 处理最近行为
        this.recentActivities = behaviors.slice(0, 10).map(b => {
          console.log('单个行为数据:', b)
          const movie = movies[b.targetId]
          return {
            behavior: b.behaviorType || '未知行为',
            movieTitle: movie?.title || movie?.name || '未知电影',
            timestamp: b.createdAt || b.timestamp || b.time || b.createTime
          }
        })
        console.log('处理后的最近行为:', this.recentActivities)
        
        // 统计行为类型
        this.behaviorStats = {
          clicks: behaviors.filter(b => b.behaviorType === 'CLICK').length,
          views: behaviors.filter(b => b.behaviorType === 'VIEW').length,
          collections: behaviors.filter(b => b.behaviorType === 'COLLECTION' || b.behaviorType === 'COLLECT').length,
          watchStatus: behaviors.filter(b => ['WATCHLIST', 'WATCHING', 'WATCHED'].includes(b.behaviorType)).length
        }
        
        console.log('行为统计:', this.behaviorStats)
        
        // 分析类型偏好
        const genreCount = {}
        behaviors.forEach(b => {
          const movie = movies[b.targetId]
          if (movie && movie.genre) {
            genreCount[movie.genre] = (genreCount[movie.genre] || 0) + 1
          }
        })
        
        this.preferences.genres = Object.entries(genreCount)
          .map(([genre, count]) => ({ genre, count }))
          .sort((a, b) => b.count - a.count)
          .slice(0, 10)
        console.log('类型偏好:', this.preferences.genres)
        
        // 分析导演偏好
        const directorCount = {}
        behaviors.forEach(b => {
          const movie = movies[b.targetId]
          if (movie && movie.director) {
            directorCount[movie.director] = (directorCount[movie.director] || 0) + 1
          }
        })
        
        this.preferences.directors = Object.entries(directorCount)
          .map(([director, count]) => ({ director, count }))
          .sort((a, b) => b.count - a.count)
          .slice(0, 10)
        console.log('导演偏好:', this.preferences.directors)
        
      } catch (e) {
        console.error('加载用户偏好失败:', e)
        this.$message.error(e.message || '加载用户偏好失败')
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.initCharts()
        })
      }
    },
    initCharts() {
      if (!this.$refs.genreChart || this.preferences.genres.length === 0) return
      
      // 类型偏好图表
      if (this.genreChart) {
        this.genreChart.dispose()
      }
      
      this.genreChart = new echarts.init(this.$refs.genreChart)
      const genreOption = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '1%',
          bottom: '3%',
          top: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.preferences.genres.map(item => item.genre),
          axisLabel: {
            interval: 0,
            rotate: 45,
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '行为次数',
            type: 'bar',
            data: this.preferences.genres.map(item => item.count),
            itemStyle: {
              color: '#667eea'
            },
            barWidth: '60%'
          }
        ]
      }
      this.genreChart.setOption(genreOption)
      
      // 导演偏好图表
      if (this.$refs.directorChart && this.preferences.directors.length > 0) {
        if (this.directorChart) {
          this.directorChart.dispose()
        }
        
        this.directorChart = new echarts.init(this.$refs.directorChart)
        const directorOption = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            left: '3%',
            right: '1%',
            bottom: '3%',
            top: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: this.preferences.directors.map(item => item.director),
            axisLabel: {
              interval: 0,
              rotate: 45,
              fontSize: 12
            }
          },
          yAxis: {
            type: 'value'
          },
          series: [
            {
              name: '行为次数',
              type: 'bar',
              data: this.preferences.directors.map(item => item.count),
              itemStyle: {
                color: '#764ba2'
              },
              barWidth: '60%'
            }
          ]
        }
        this.directorChart.setOption(directorOption)
      }
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleString()
    },
    getActivityType(behavior) {
      switch (behavior) {
        case 'CLICK':
          return 'primary'
        case 'VIEW':
          return 'info'
        case 'COLLECT':
          return 'success'
        case 'WATCHLIST':
        case 'WATCHING':
        case 'WATCHED':
          return 'warning'
        default:
          return 'info'
      }
    },
    getActivityColor(behavior) {
      switch (behavior) {
        case 'CLICK':
          return '#409EFF'
        case 'VIEW':
          return '#909399'
        case 'COLLECT':
          return '#67C23A'
        case 'WATCHLIST':
        case 'WATCHING':
        case 'WATCHED':
          return '#E6A23C'
        default:
          return '#909399'
      }
    },
    getActivityDescription(activity) {
      const behaviorMap = {
        'CLICK': '点击了',
        'VIEW': '浏览了',
        'COLLECT': '收藏了',
        'WATCHLIST': '标记为想看',
        'WATCHING': '标记为在看',
        'WATCHED': '标记为已看'
      }
      return `${behaviorMap[activity.behavior] || activity.behavior}电影《${activity.movieTitle}》`
    },
    getStatTitle(key) {
      const titleMap = {
        'clicks': '点击次数',
        'views': '浏览次数',
        'collections': '收藏次数',
        'watchStatus': '观看状态变更次数'
      }
      return titleMap[key] || key
    }
  }
}
</script>

<style scoped>
.user-preference h1 {
  margin-bottom: 20px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.analysis-container {
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.analysis-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
}

.recent-activities {
  max-height: 400px;
  overflow-y: auto;
}

.behavior-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.behavior-stats .el-statistic {
  text-align: center;
}
</style>