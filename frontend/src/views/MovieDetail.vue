<template>
  <div class="detail" v-loading="loading">
    <el-card v-if="movie">
      <el-row :gutter="24">
        <el-col :span="6">
          <div class="poster">{{ movie.title.slice(0, 2) }}</div>
        </el-col>
        <el-col :span="18">
          <h1>{{ movie.title }}</h1>
          <p><strong>导演：</strong>{{ movie.director }}</p>
          <p><strong>主演：</strong>{{ movie.actors }}</p>
          <p><strong>类型：</strong>{{ movie.genre }} | <strong>年份：</strong>{{ movie.releaseYear }} | <strong>地区：</strong>{{ movie.region }}</p>
          <p class="desc">{{ movie.description }}</p>

          <!-- 电影标签 -->
          <div class="movie-tags" v-if="user">
            <div class="tags-header">
              <span class="tags-title">标签</span>
              <el-button size="mini" type="text" @click="openTagDialog">
                <i class="el-icon-edit"></i> 编辑标签
              </el-button>
            </div>
            <div class="tags-list">
              <el-tag
                v-for="tag in movieTags"
                :key="tag.id"
                :style="{ backgroundColor: tag.color, borderColor: tag.color, color: '#fff' }"
                size="small"
                class="movie-tag"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="movieTags.length === 0" class="no-tags">暂无标签</span>
            </div>
          </div>

          <!-- 操作按钮区域 -->
          <div class="action-area">
            <!-- 编辑按钮（仅管理员可见） -->
            <el-button v-if="isAdmin" type="primary" size="small" @click="showMovieDialog" style="margin-right: 12px">
              <i class="el-icon-edit"></i> 编辑电影
            </el-button>
            <!-- 观看状态 -->
            <template v-if="user">
              <el-dropdown @command="handleWatchStatusCommand" style="margin-right: 12px">
                <el-button :type="movie.watchStatus && movie.watchStatus !== 'NONE' ? 'success' : 'info'" size="small">
                  <i :class="getWatchStatusIcon()"></i>
                  {{ getWatchStatusText() }}<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="WATCHLIST"><i class="el-icon-star-off"></i> 想看</el-dropdown-item>
                  <el-dropdown-item command="WATCHING"><i class="el-icon-video-play"></i> 在看</el-dropdown-item>
                  <el-dropdown-item command="WATCHED"><i class="el-icon-check"></i> 已看</el-dropdown-item>
                  <el-dropdown-item v-if="movie.watchStatus && movie.watchStatus !== 'NONE'" command="NONE" divided><i class="el-icon-close"></i> 清除标记</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <!-- 收藏按钮 -->
              <el-dropdown @command="handleCollectionCommand">
                <el-button :type="movie.isCollected ? 'warning' : 'primary'" size="small">
                  <i :class="getCollectionIcon()"></i>
                  {{ getCollectionText() }}
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="COLLECTED"><i class="el-icon-star-on"></i> 收藏</el-dropdown-item>
                  <el-dropdown-item v-if="movie.isCollected" command="REMOVE" divided><i class="el-icon-close"></i> 取消收藏</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-button :type="movie.isDisliked ? 'danger' : 'info'" size="small" @click="toggleDislike" style="margin-left: 12px">
                <i v-if="movie.isDisliked" class="el-icon-delete"></i>
                <i v-else class="el-icon-delete"></i>
                {{ movie.isDisliked ? '已不喜欢' : '标记不喜欢' }}
              </el-button>
            </template>
          </div>
        </el-col>
      </el-row>

      <!-- 评分组件区域 -->
      <rating-component
        :movie-id="movie.id"
        :average-rating="movie.avgRating"
        :rating-count="movie.ratingCount"
        @rating-updated="onRatingUpdated"
        class="rating-section"
      />

      <!-- 笔记区域 -->
      <div class="notes-section" v-if="user">
        <div class="section-header">
          <h3><i class="el-icon-edit-outline"></i> 我的笔记</h3>
          <el-button type="primary" size="small" @click="openNoteDialog()">
            <i class="el-icon-plus"></i> 写笔记
          </el-button>
        </div>
        
        <div v-if="movieNotes.length > 0" class="notes-list">
          <el-card
            v-for="note in movieNotes"
            :key="note.id"
            class="note-item"
            :body-style="{ padding: '15px' }"
          >
            <div class="note-header">
              <span class="note-title">{{ note.title }}</span>
              <div class="note-actions">
                <el-button type="text" size="small" @click="openNoteDialog(note)">编辑</el-button>
                <el-button type="text" size="small" @click="deleteNote(note.id)">删除</el-button>
              </div>
            </div>
            <p class="note-content">{{ note.content }}</p>
            <div class="note-meta">
              <el-tag size="mini" :type="note.isPublic ? 'success' : 'warning'">
                {{ note.isPublic ? '公开' : '私密' }}
              </el-tag>
              <span class="note-time">{{ formatNoteTime(note.updatedAt) }}</span>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无笔记，快来写下你的观影感受吧" :image-size="80" />
      </div>
    </el-card>

    <!-- 标签编辑对话框 -->
    <el-dialog title="编辑电影标签" :visible.sync="showTagDialog" width="500px">
      <div class="tag-edit-content">
        <div class="current-tags">
          <h4>当前标签</h4>
          <div class="selected-tags">
            <el-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              :style="{ backgroundColor: tag.color, borderColor: tag.color, color: '#fff' }"
              closable
              @close="removeTag(tag.id)"
              class="selected-tag"
            >
              {{ tag.name }}
            </el-tag>
            <span v-if="selectedTags.length === 0" class="no-selected">暂无标签，请从下方选择</span>
          </div>
        </div>
        
        <div class="available-tags">
          <h4>可选标签</h4>
          <el-input
            v-model="tagSearchKeyword"
            placeholder="搜索标签"
            prefix-icon="el-icon-search"
            size="small"
            style="margin-bottom: 10px"
          />
          <div class="tag-cloud">
            <el-tag
              v-for="tag in filteredAvailableTags"
              :key="tag.id"
              :style="{ backgroundColor: tag.color, borderColor: tag.color, color: '#fff' }"
              size="small"
              class="available-tag"
              @click="addTag(tag)"
            >
              {{ tag.name }}
            </el-tag>
            <span v-if="filteredAvailableTags.length === 0" class="no-available-tags">
              {{ allTags.length === 0 ? '暂无标签，请创建新标签' : '没有匹配的标签' }}
            </span>
          </div>
        </div>
        
        <div class="create-tag">
          <h4>创建新标签</h4>
          <el-input
            v-model="newTagName"
            placeholder="输入新标签名称"
            size="small"
            style="width: 200px; margin-right: 10px"
          />
          <el-color-picker v-model="newTagColor" size="small" style="margin-right: 10px" />
          <el-button type="primary" size="small" @click="createNewTag">创建</el-button>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTags">保存</el-button>
      </div>
    </el-dialog>

    <!-- 笔记编辑对话框 -->
    <el-dialog
      :title="editingNote.id ? '编辑笔记' : '写笔记'"
      :visible.sync="showNoteDialog"
      width="600px"
    >
      <el-form :model="editingNote" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editingNote.title" placeholder="请输入标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editingNote.category" placeholder="选择分类">
            <el-option label="随笔" value="GENERAL" />
            <el-option label="影评" value="REVIEW" />
            <el-option label="分析" value="ANALYSIS" />
            <el-option label="摘录" value="QUOTE" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="editingNote.content"
            type="textarea"
            :rows="6"
            placeholder="写下你的观影感受..."
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="权限">
          <el-switch
            v-model="editingNote.isPublic"
            active-text="公开"
            inactive-text="私密"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showNoteDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingNote" @click="saveNote">保存</el-button>
      </div>
    </el-dialog>

    <!-- 编辑电影对话框 -->
    <el-dialog title="编辑电影" :visible.sync="movieDialogVisible" width="500px">
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
        <el-form-item label="简介"><el-input v-model="movieForm.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="movieDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMovie">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import movieService from '../services/movie'
import ratingService from '../services/rating'
import collectionService from '../services/collection'
import folderService from '../services/folder'
import watchStatusService from '../services/watchStatus'
import { tagService, movieTagService } from '../services/tag'
import { dislikeMovie, cancelDislike } from '../services/feedback'
import { getStatusConfig, getStatusIcon, getStatusLabel } from '../constants/collectionStatus'
import { Permission } from '../utils/permission'
import RatingComponent from '../components/RatingComponent.vue'
import noteService from '../services/note'
import behaviorService from '../services/behavior'
import eventBus from '../utils/eventBus'
import axios from 'axios'

export default {
  components: {
    RatingComponent
  },
  data() {
    return {
      movie: null,
      loading: true,
      user: JSON.parse(localStorage.getItem('user') || 'null'),
      defaultFolderId: null,
      // 标签相关
      showTagDialog: false,
      movieTags: [],
      allTags: [],
      selectedTags: [],
      tagSearchKeyword: '',
      newTagName: '',
      newTagColor: '#409EFF',
      // 笔记相关
      movieNotes: [],
      showNoteDialog: false,
      editingNote: {
        id: null,
        title: '',
        content: '',
        category: 'GENERAL',
        isPublic: false
      },
      savingNote: false,
      // 电影编辑相关
      movieDialogVisible: false,
      genres: ['剧情', '喜剧', '动作', '爱情', '科幻', '悬疑', '恐怖', '动画', '纪录片', '战争', '犯罪', '奇幻'],
      movieForm: {
        title: '',
        director: '',
        actors: '',
        genre: '',
        releaseYear: 2020,
        region: '',
        description: ''
      }
    }
  },
  computed: {
    filteredAvailableTags() {
      const selectedIds = this.selectedTags.map(t => t.id)
      let tags = this.allTags.filter(tag => !selectedIds.includes(tag.id))
      
      if (this.tagSearchKeyword.trim()) {
        const keyword = this.tagSearchKeyword.toLowerCase()
        console.log('搜索关键词:', keyword)
        console.log('过滤前标签数:', tags.length)
        tags = tags.filter(tag => {
          const match = tag.name && tag.name.toLowerCase().includes(keyword)
          console.log(`标签 "${tag.name}" 匹配 "${keyword}":`, match)
          return match
        })
        console.log('过滤后标签数:', tags.length)
      }
      
      return tags
    },
    isAdmin() {
      return Permission.isAdmin()
    }
  },
  created() {
    this.fetch()
    if (this.user) {
      this.loadDefaultFolder()
    }
  },
  watch: {
    // 监听路由变化
    '$route': {
      handler(to, from) {
        if (to.params.id && to.params.id !== from.params.id) {
          this.fetch()
        }
      }
    },
    movie: {
      immediate: true,
      handler(newVal) {
        if (this.user && newVal && newVal.id) {
          // 记录浏览行为
          behaviorService.logView(newVal.id)
          // 加载笔记
          this.loadMovieNotes()
        }
      }
    }
  },
  methods: {
    // 显示编辑对话框
    showMovieDialog() {
      this.movieForm = {
        title: this.movie.title || '',
        director: this.movie.director || '',
        actors: this.movie.actors || '',
        genre: this.movie.genre || '',
        releaseYear: this.movie.releaseYear || 2020,
        region: this.movie.region || '',
        description: this.movie.description || ''
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
        
        await axios.put('/api/admin/movie/' + this.movie.id, this.movieForm, { headers })
        this.$message.success('更新成功')
        this.movieDialogVisible = false
        
        // 更新本地数据
        this.movie = {
          ...this.movie,
          ...this.movieForm
        }
        
        // 发送事件通知其他组件
        eventBus.$emit('movie-updated', {
          id: this.movie.id,
          ...this.movieForm
        })
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    async fetch() {
      this.loading = true
      try {
        this.movie = await movieService.getMovieById(this.$route.params.id)
        // 加载电影标签
        if (this.user) {
          await this.loadMovieTags()
          // 加载收藏状态
          try {
            const collected = await collectionService.isMovieCollected(this.movie.id)
            this.movie.isCollected = typeof collected === 'boolean' ? collected : (collected.success ? collected.data : false)
          } catch (e) {
            console.error('获取收藏状态失败:', e)
            this.movie.isCollected = false
          }
          // 加载观看状态
          try {
            const watchStatus = await watchStatusService.getWatchStatus(this.movie.id)
            this.movie.watchStatus = watchStatus.status || (typeof watchStatus === 'string' ? watchStatus : 'NONE')
          } catch (e) {
            console.error('获取观看状态失败:', e)
            this.movie.watchStatus = 'NONE'
          }
        }
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async onRatingUpdated(score) {
      // 评分更新后刷新电影数据
      await this.fetch()
      // 记录评分行为
      if (this.user && this.movie) {
        const ratingScore = score || this.movie.myScore
        if (ratingScore) {
          behaviorService.logRating(this.movie.id, ratingScore)
        }
        // 发送评分更新事件
        eventBus.$emit('movie-updated', {
          id: this.movie.id,
          avgRating: this.movie.avgRating,
          ratingCount: this.movie.ratingCount
        })
      }
    },
    async loadMovieTags() {
      try {
        this.movieTags = await movieTagService.getTagsByMovieId(this.movie.id)
      } catch (e) {
        console.error('加载电影标签失败:', e)
        this.movieTags = []
      }
    },
    async loadAllTags() {
      try {
        const tags = await tagService.getAllTags()
        console.log('加载到的标签:', tags)
        this.allTags = tags || []
      } catch (e) {
        console.error('加载所有标签失败:', e)
        this.allTags = []
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
    // 处理观看状态命令
    async handleWatchStatusCommand(command) {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        if (command === 'NONE') {
          await watchStatusService.removeWatchStatus(this.movie.id)
          this.$message.success('已清除标记')
          this.movie.watchStatus = 'NONE'
        } else {
          await watchStatusService.setWatchStatus(this.movie.id, command)
          this.$message.success(`已标记为"${getStatusLabel(command)}"`)
          this.movie.watchStatus = command
          // 记录观看状态变更行为
          behaviorService.logBehavior(command, this.movie.id, `status:${command}`)
        }
        this.fetch()
        // 发送观看状态更新事件
        eventBus.$emit('movie-updated', {
          id: this.movie.id,
          watchStatus: this.movie.watchStatus
        })
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    // 处理收藏命令
    async handleCollectionCommand(command) {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        if (command === 'REMOVE') {
          await collectionService.removeFromCollection(this.movie.id)
          this.$message.success('已取消收藏')
          this.movie.isCollected = false
          // 记录取消收藏行为
          behaviorService.logCollection(this.movie.id, 'REMOVE')
        } else if (command === 'COLLECTED') {
          await collectionService.addToCollection(this.movie.id, { status: 'COLLECTED', collectionName: '默认收藏夹' })
          // 移动到默认收藏夹
          if (this.defaultFolderId) {
            try {
              await folderService.moveMovieToFolder(this.defaultFolderId, this.movie.id)
            } catch (e) {
              console.log('电影已在默认收藏夹中')
            }
          }
          this.$message.success('已收藏')
          this.movie.isCollected = true
          // 记录收藏行为
          behaviorService.logCollection(this.movie.id, 'COLLECTED')
        }
        // 发送收藏状态更新事件
        eventBus.$emit('movie-updated', {
          id: this.movie.id,
          isCollected: this.movie.isCollected
        })
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    getWatchStatusIcon() {
      if (!this.movie.watchStatus || this.movie.watchStatus === 'NONE') return 'el-icon-video-play'
      return getStatusIcon(this.movie.watchStatus)
    },
    getWatchStatusText() {
      if (!this.movie.watchStatus || this.movie.watchStatus === 'NONE') return '标记'
      return getStatusLabel(this.movie.watchStatus)
    },
    getCollectionIcon() {
      return this.movie.isCollected ? 'el-icon-star-on' : 'el-icon-star-off'
    },
    getCollectionText() {
      return this.movie.isCollected ? '已收藏' : '收藏'
    },
    async toggleDislike() {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      try {
        if (this.movie.isDisliked) {
          await cancelDislike(this.movie.id)
          this.$message.success('已取消不喜欢')
          this.movie.isDisliked = false
        } else {
          await dislikeMovie(this.movie.id)
          this.$message.success('已标记为不喜欢')
          this.movie.isDisliked = true
        }
        // 发送不喜欢状态更新事件
        eventBus.$emit('movie-updated', {
          id: this.movie.id,
          isDisliked: this.movie.isDisliked
        })
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    // 标签编辑方法
    async openTagDialog() {
      if (!this.user) {
        this.$message.info('请先登录解锁更多功能')
        return
      }
      this.selectedTags = [...this.movieTags]
      await this.loadAllTags()
      this.showTagDialog = true
    },
    addTag(tag) {
      if (!this.selectedTags.find(t => t.id === tag.id)) {
        this.selectedTags.push(tag)
      }
    },
    removeTag(tagId) {
      this.selectedTags = this.selectedTags.filter(t => t.id !== tagId)
    },
    async createNewTag() {
      if (!this.newTagName.trim()) {
        this.$message.warning('请输入标签名称')
        return
      }
      try {
        const newTag = await tagService.createTag(
          this.newTagName.trim(),
          this.newTagColor,
          ''
        )
        this.$message.success('标签创建成功')
        this.allTags.push(newTag)
        this.selectedTags.push(newTag)
        this.newTagName = ''
        this.newTagColor = '#409EFF'
      } catch (e) {
        this.$message.error(e.message || '创建标签失败')
      }
    },
    async saveTags() {
      try {
        const currentTagIds = this.movieTags.map(t => t.id)
        const selectedTagIds = this.selectedTags.map(t => t.id)
        
        // 删除的标签
        const tagsToRemove = currentTagIds.filter(id => !selectedTagIds.includes(id))
        for (const tagId of tagsToRemove) {
          await movieTagService.removeTagFromMovie(this.movie.id, tagId)
        }
        
        // 新增的标签
        const tagsToAdd = selectedTagIds.filter(id => !currentTagIds.includes(id))
        for (const tagId of tagsToAdd) {
          await movieTagService.addTagToMovie(this.movie.id, tagId)
        }
        
        this.$message.success('标签保存成功')
        this.showTagDialog = false
        await this.loadMovieTags()
      } catch (e) {
        this.$message.error(e.message || '保存标签失败')
      }
    },
    // 笔记相关方法
    async loadMovieNotes() {
      if (!this.movie || !this.movie.id) {
        return
      }
      try {
        const res = await noteService.getNotesByMovie(this.movie.id)
        if (res.success) {
          this.movieNotes = res.data || []
        }
      } catch (e) {
        console.error('加载笔记失败:', e)
      }
    },
    openNoteDialog(note = null) {
      if (note) {
        this.editingNote = { ...note }
      } else {
        this.editingNote = {
          id: null,
          title: '',
          content: '',
          category: 'GENERAL',
          isPublic: false
        }
      }
      this.showNoteDialog = true
    },
    async saveNote() {
      if (!this.editingNote.title.trim()) {
        this.$message.warning('请输入标题')
        return
      }
      if (!this.editingNote.content.trim()) {
        this.$message.warning('请输入内容')
        return
      }

      this.savingNote = true
      try {
        if (this.editingNote.id) {
          await noteService.updateNote(this.editingNote.id, {
            title: this.editingNote.title,
            content: this.editingNote.content,
            category: this.editingNote.category,
            isPublic: this.editingNote.isPublic
          })
          this.$message.success('笔记更新成功')
        } else {
          await noteService.createNote(
            this.movie.id,
            this.editingNote.title,
            this.editingNote.content,
            this.editingNote.category,
            this.editingNote.isPublic
          )
          this.$message.success('笔记创建成功')
        }
        this.showNoteDialog = false
        this.loadMovieNotes()
        // 记录笔记行为
        behaviorService.logNote(this.movie.id)
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.savingNote = false
      }
    },
    async deleteNote(noteId) {
      this.$confirm('确定要删除这篇笔记吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await noteService.deleteNote(noteId)
          this.$message.success('删除成功')
          this.loadMovieNotes()
        } catch (e) {
          this.$message.error('删除失败')
        }
      })
    },
    formatNoteTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.detail { max-width: 900px; margin: 0 auto; }
.poster {
  height: 200px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 48px; border-radius: 8px;
}
h1 { margin-bottom: 16px; }
p { margin: 8px 0; color: #606266; }
.desc { line-height: 1.6; }
.action-area { margin-top: 20px; display: flex; align-items: center; }
.rating-section { margin-top: 30px; border-top: 1px solid #EBEEF5; }

/* 标签样式 */
.movie-tags {
  margin: 15px 0;
  padding: 10px 0;
  border-top: 1px solid #EBEEF5;
  border-bottom: 1px solid #EBEEF5;
}

.tags-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.tags-title {
  font-weight: bold;
  color: #606266;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.movie-tag {
  cursor: pointer;
}

.no-tags {
  color: #909399;
  font-size: 12px;
}

/* 标签编辑对话框样式 */
.tag-edit-content h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.current-tags {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.selected-tag {
  cursor: pointer;
}

.no-selected {
  color: #909399;
  font-size: 12px;
}

.available-tags {
  margin-bottom: 20px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 150px;
  overflow-y: auto;
  padding: 10px;
  background: #F5F7FA;
  border-radius: 4px;
}

.available-tag {
  cursor: pointer;
  transition: transform 0.2s;
}

.available-tag:hover {
  transform: scale(1.1);
}

.no-available-tags {
  color: #909399;
  font-size: 12px;
  width: 100%;
  text-align: center;
  padding: 20px 0;
}

.create-tag {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 笔记区域样式 */
.notes-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  color: #303133;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.note-item {
  transition: box-shadow 0.3s;
}

.note-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.note-title {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}

.note-actions {
  display: flex;
  gap: 8px;
}

.note-content {
  color: #606266;
  line-height: 1.6;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.note-time {
  color: #909399;
  font-size: 12px;
}
</style>
