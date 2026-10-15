<template>
  <div class="admin">
    <h1>管理员控制台</h1>
    
    <!-- 电影管理 -->
    <div class="section">
      <h2>电影管理</h2>
      <div class="tab-header">
        <el-button type="primary" @click="showMovieDialog()">添加电影</el-button>
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleImportSuccess"
          accept=".csv"
          style="margin-left: 10px;">
        </el-upload>
      </div>
      
      <!-- SVR评分填充功能 -->
      <div class="svr-section" style="margin-top: 20px; padding: 20px; background: #f5f7fa; border-radius: 8px;">
        <h3>SVR评分矩阵填充</h3>
        <p style="color: #606266; margin-bottom: 15px;">使用支持向量回归算法填充稀疏的评分矩阵，提升推荐算法性能</p>
        <el-button type="success" @click="handleFillRatingMatrix" :loading="fillingMatrix">
          {{ fillingMatrix ? '填充中...' : '执行评分填充' }}
        </el-button>
        
        <!-- SVR填充统计结果 -->
        <div v-if="fillResult" style="margin-top: 20px;">
          <div style="background: #f0f9eb; padding: 15px; border-radius: 8px; margin-bottom: 15px;">
            <p style="color: #67c23a; font-weight: bold; margin-bottom: 10px;">
              {{ fillResult.status === 'success' ? '✓ 评分矩阵填充成功！' : fillResult.status }}
            </p>
            <p v-if="fillResult.executionTimeMs" style="color: #909399; font-size: 13px;">
              执行耗时: {{ (fillResult.executionTimeMs / 1000).toFixed(2) }} 秒
            </p>
          </div>
          
          <!-- 对比表格 -->
          <el-table :data="svrComparisonData" border style="width: 100%; background: white;">
            <el-table-column label="指标" prop="metric" width="180"></el-table-column>
            <el-table-column label="填充前" prop="before" width="140"></el-table-column>
            <el-table-column label="填充后" prop="after" width="140"></el-table-column>
            <el-table-column label="变化" prop="change"></el-table-column>
          </el-table>
        </div>
      </div>
      
      <div v-if="movies.length === 0" style="text-align: center; padding: 40px; color: #999;">
        暂无电影数据，请点击"添加电影"按钮添加
      </div>
      <el-table v-else :data="movies" style="width: 100%; margin-top: 20px;" :key="movies.length">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="片名"></el-table-column>
        <el-table-column prop="genre" label="类型" width="120"></el-table-column>
        <el-table-column prop="director" label="导演" width="120"></el-table-column>
        <el-table-column prop="releaseYear" label="年份" width="80"></el-table-column>
        <el-table-column prop="avgRating" label="评分" width="80"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click.native="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click.native="deleteMovie(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 行为日志 -->
    <div class="section" style="margin-top: 40px;">
      <h2>用户行为日志</h2>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin: 20px 0;">
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
        style="margin-top: 20px; text-align: right;"
      />
    </div>

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
import adminService from '../services/admin'
import api from '../services/api'
import movieService from '../services/movie'
import recommendService from '../services/recommend'
import axios from 'axios'

export default {
  data() {
    return {
      movieDialogVisible: false,
      editMovieId: null,
      genres: ['剧情', '喜剧', '动作', '爱情', '科幻', '悬疑', '恐怖', '动画', '纪录片', '战争', '犯罪', '奇幻'],
      movieForm: { title: '', director: '', actors: '', genre: '', releaseYear: 2020, region: '', description: '' },
      movies: [],
      uploadUrl: '/api/admin/movie/batch-import',
      uploadHeaders: {},
      
      // SVR功能相关
      fillingMatrix: false,
      fillResult: null,
      svrComparisonData: [],
      
      // 行为日志相关
      logs: [],
      movieNames: {},
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
    this.loadMovies();
    this.loadLogs();
    this.loadStats();
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    this.uploadHeaders = {
      'Authorization': 'Bearer ' + (user.token || ''),
      'X-User-Role': user.role || ''
    };
  },
  methods: {
    async loadMovies() {
      try {
        console.log('开始加载电影数据...')
        const res = await axios.get('/api/admin/movie/all', {
          headers: this.uploadHeaders
        })
        console.log('电影数据响应:', res.data)
        if (res.data.success) {
          this.movies = res.data.data || []
          console.log('加载的电影列表:', this.movies)
        }
      } catch (e) {
        console.error('加载电影失败:', e)
        this.$message.error('加载电影数据失败: ' + (e.message || '未知错误'))
      }
    },
    handleEdit(row) {
      console.log('handleEdit 被调用, row:', row)
      this.showMovieDialog(row)
    },
    showMovieDialog(row) {
      console.log('showMovieDialog 被调用, 参数 row:', row)
      this.editMovieId = row ? row.id : null
      if (row) {
        this.movieForm = { 
          title: row.title || '', 
          director: row.director || '', 
          actors: row.actors || '', 
          genre: row.genre || '', 
          releaseYear: row.releaseYear || 2020, 
          region: row.region || '', 
          description: row.description || '' 
        }
      } else {
        this.movieForm = { title: '', director: '', actors: '', genre: '', releaseYear: 2020, region: '', description: '' }
      }
      console.log('当前编辑的电影ID:', this.editMovieId)
      console.log('当前表单数据:', this.movieForm)
      this.movieDialogVisible = true
    },
    async submitMovie() {
      console.log('submitMovie 被调用, editMovieId:', this.editMovieId, '表单数据:', this.movieForm)
      try {
        if (this.editMovieId) {
          const res = await axios.put('/api/admin/movie/' + this.editMovieId, this.movieForm, { headers: this.uploadHeaders })
          console.log('更新响应:', res.data)
          this.$message.success('更新成功')
        } else {
          const res = await axios.post('/api/admin/movie', this.movieForm, { headers: this.uploadHeaders })
          console.log('添加响应:', res.data)
          this.$message.success('添加成功')
        }
        this.movieDialogVisible = false
        this.loadMovies()
      } catch (e) {
        console.error('提交失败:', e)
        this.$message.error('操作失败: ' + (e.response?.data?.message || e.message || '未知错误'))
      }
    },
    async deleteMovie(id) {
      try {
        await this.$confirm('确定要删除这部电影吗?')
        await axios.delete('/api/admin/movie/' + id, { headers: this.uploadHeaders })
        this.$message.success('删除成功')
        this.loadMovies()
      } catch (e) {
        if (e !== 'cancel') {
          this.$message.error(e.message)
        }
      }
    },
    handleImportSuccess(response) {
      if (response.success) {
        this.$message.success(response.message)
        this.loadMovies()
      } else {
        this.$message.error(response.message)
      }
    },
    
    // SVR评分填充功能
    async handleFillRatingMatrix() {
      this.fillingMatrix = true
      this.fillResult = null
      this.svrComparisonData = []
      
      try {
        console.log('开始执行SVR评分矩阵填充...')
        const result = await recommendService.fillRatingMatrix()
        console.log('SVR填充结果:', result)
        
        this.fillResult = result
        
        // 构建对比数据
        if (result.status === 'success') {
          this.svrComparisonData = this.buildSVRComparisonData(result)
          this.$message.success('SVR评分填充完成！')
        } else {
          this.$message.error('SVR评分填充失败: ' + result.status)
        }
        
      } catch (e) {
        console.error('SVR评分填充失败:', e)
        this.$message.error('SVR评分填充失败')
      } finally {
        this.fillingMatrix = false
      }
    },
    
    // 构建SVR对比数据
    buildSVRComparisonData(result) {
      const data = []
      
      data.push({
        metric: '用户数量',
        before: result.originalUserCount,
        after: result.filledUserCount,
        change: result.filledUserCount - result.originalUserCount
      })
      
      data.push({
        metric: '电影数量',
        before: result.originalMovieCount,
        after: result.filledMovieCount,
        change: result.filledMovieCount - result.originalMovieCount
      })
      
      data.push({
        metric: '评分数量',
        before: result.originalRatingCount,
        after: result.filledRatingCount,
        change: `+${result.newPredictedCount}`
      })
      
      data.push({
        metric: '稀疏度',
        before: `${(result.originalSparsity * 100).toFixed(2)}%`,
        after: `${(result.filledSparsity * 100).toFixed(2)}%`,
        change: `${((result.originalSparsity - result.filledSparsity) * 100).toFixed(2)}% 降低`
      })
      
      data.push({
        metric: '数据密度',
        before: `${((1 - result.originalSparsity) * 100).toFixed(2)}%`,
        after: `${((1 - result.filledSparsity) * 100).toFixed(2)}%`,
        change: `${((result.originalSparsity - result.filledSparsity) * 100).toFixed(2)}% 提升`
      })
      
      return data
    },
    
    // 行为日志相关
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
      if (!time) return '';
      return new Date(time).toLocaleString('zh-CN');
    }
  }
}
</script>

<style scoped>
.admin {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.admin h1 {
  margin-bottom: 30px;
}

.section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.section h2 {
  margin-top: 0;
}

.tab-header {
  display: flex;
  align-items: center;
}

.filter-bar {
  margin: 20px 0;
  display: flex;
  gap: 10px;
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
</style>
