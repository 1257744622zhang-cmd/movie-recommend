<template>
  <div class="behavior-logs">
    <h2>用户行为日志</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalLogs }}</div>
            <div class="stat-label">总行为数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ stats.activeUsers }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" v-for="(count, type) in stats.typeCount" :key="type">
        <el-card>
          <div class="stat-item">
            <div class="stat-value">{{ count }}</div>
            <div class="stat-label">{{ getBehaviorTypeLabel(type) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="行为类型" clearable @change="loadLogs">
        <el-option label="点击" value="CLICK" />
        <el-option label="浏览" value="VIEW" />
        <el-option label="评分" value="RATING" />
        <el-option label="收藏" value="COLLECTION" />
        <el-option label="搜索" value="SEARCH" />
        <el-option label="笔记" value="NOTE" />
      </el-select>
      <el-button type="primary" @click="loadLogs">刷新</el-button>
    </div>

    <!-- 日志列表 -->
    <el-card>
      <div slot="header">
        <span>详细日志 ({{ logs.length }}条)</span>
      </div>
      
      <div v-if="loading" style="text-align: center; padding: 40px;">
        加载中...
      </div>
      
      <div v-else-if="logs.length === 0" style="text-align: center; padding: 40px; color: #999;">
        暂无数据
      </div>
      
      <table v-else class="log-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>行为</th>
            <th>电影ID</th>
            <th>电影名称</th>
            <th>详情</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td>{{ log.id }}</td>
            <td>{{ log.userId }}</td>
            <td>
              <span :class="'tag tag-' + getBehaviorTypeType(log.behaviorType)">
                {{ getBehaviorTypeLabel(log.behaviorType) }}
              </span>
            </td>
            <td>
              <span v-if="log.behaviorType === 'SEARCH'">-</span>
              <span v-else>{{ log.targetId }}</span>
            </td>
            <td>
              <span v-if="log.behaviorType === 'SEARCH'">-</span>
              <span v-else>{{ movieNames[log.targetId] || '加载中...' }}</span>
            </td>
            <td>{{ getDetailText(log) }}</td>
            <td>{{ formatTime(log.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </el-card>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      @current-change="handlePageChange"
      :current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      class="pagination"
    />
  </div>
</template>

<script>
import api from '../../services/api'
import movieService from '../../services/movie'

export default {
  name: 'BehaviorLogs',
  data() {
    return {
      logs: [],
      movieNames: {}, // 存储电影ID到名称的映射
      stats: {
        totalLogs: 0,
        activeUsers: 0,
        typeCount: {}
      },
      loading: false,
      page: 1,
      size: 20,
      total: 0,
      filterType: ''
    }
  },
  created() {
    this.loadLogs()
    this.loadStats()
  },
  methods: {
    async loadLogs() {
      this.loading = true
      try {
        const params = {
          page: this.page - 1,
          size: this.size
        }
        
        // 只有当filterType不为空时才添加behaviorType参数
        if (this.filterType) {
          params.behaviorType = this.filterType
        }
        
        const res = await api.get('/admin/behavior/logs', { params })
        const responseData = res.data || res
        this.logs = responseData.content || []
        this.total = responseData.totalElements || 0
        
        // 加载电影名称
        this.loadMovieNames()
      } catch (e) {
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    async loadMovieNames() {
      const movieIds = [...new Set(this.logs
        .filter(log => log.behaviorType !== 'SEARCH')
        .map(log => log.targetId))]
      
      console.log('需要加载的电影ID:', movieIds)
      
      const newMovieNames = { ...this.movieNames }
      
      for (const id of movieIds) {
        if (!newMovieNames[id]) {
          try {
            const res = await movieService.getMovieById(id)
            // 电影数据可能在 res 或 res.data 中
            const movieData = res.data || res
            if (movieData && movieData.title) {
              newMovieNames[id] = movieData.title
            } else {
              newMovieNames[id] = `电影${id}`
            }
          } catch (e) {
            newMovieNames[id] = `电影${id}`
          }
        }
      }
      
      console.log('电影名称映射:', newMovieNames)
      this.movieNames = newMovieNames
    },
    async loadStats() {
      try {
        const res = await api.get('/admin/behavior/stats')
        const statsData = res.data.data || res.data
        this.stats = statsData
      } catch (e) {
        console.error('加载统计失败', e)
      }
    },
    handlePageChange(page) {
      this.page = page
      this.loadLogs()
    },
    getBehaviorTypeLabel(type) {
      const labels = {
        'CLICK': '点击',
        'VIEW': '浏览',
        'RATING': '评分',
        'COLLECTION': '收藏',
        'SEARCH': '搜索',
        'NOTE': '笔记',
        'WATCHLIST': '想看',
        'WATCHING': '在看',
        'WATCHED': '已看'
      }
      return labels[type] || type
    },
    getBehaviorTypeType(type) {
      const types = {
        'CLICK': 'primary',
        'VIEW': 'success',
        'RATING': 'warning',
        'COLLECTION': 'danger',
        'SEARCH': 'info',
        'NOTE': 'default',
        'WATCHLIST': 'warning',
        'WATCHING': 'primary',
        'WATCHED': 'success'
      }
      return types[type] || 'default'
    },
    getDetailText(log) {
      if (!log.targetInfo) return '-'
      
      // 翻译收藏状态
      if (log.targetInfo.startsWith('action:')) {
        const action = log.targetInfo.replace('action:', '')
        const statusMap = {
          'WANT_TO_WATCH': '想看',
          'WATCHING': '在看',
          'WATCHED': '已看',
          'REMOVE': '取消收藏'
        }
        return statusMap[action] || action
      }
      
      // 翻译评分
      if (log.targetInfo.startsWith('score:')) {
        const score = log.targetInfo.replace('score:', '')
        return score === 'null' ? '评分未记录' : `${score}分`
      }
      
      // 搜索关键词
      if (log.behaviorType === 'SEARCH') {
        return `搜索: ${log.targetInfo}`
      }
      
      return log.targetInfo
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.behavior-logs {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.log-table {
  width: 100%;
  border-collapse: collapse;
}

.log-table th,
.log-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.log-table th {
  background: #f5f7fa;
  font-weight: bold;
}

.log-table tr:hover {
  background: #f5f7fa;
}

.tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-primary { background: #ecf5ff; color: #409eff; }
.tag-success { background: #f0f9eb; color: #67c23a; }
.tag-warning { background: #fdf6ec; color: #e6a23c; }
.tag-danger { background: #fef0f0; color: #f56c6c; }
.tag-info { background: #f4f4f5; color: #909399; }
.tag-default { background: #f4f4f5; color: #606266; }

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
