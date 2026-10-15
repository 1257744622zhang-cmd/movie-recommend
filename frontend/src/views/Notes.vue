<template>
  <div class="notes-page">
    <div class="page-header">
      <h1>我的笔记</h1>
      <div class="stats">
        <el-tag type="info">共 {{ totalNotes }} 篇笔记</el-tag>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <el-radio-group v-model="currentFilter" size="small" @change="handleFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="favorite">收藏</el-radio-button>
        <el-radio-button label="GENERAL">随笔</el-radio-button>
        <el-radio-button label="REVIEW">影评</el-radio-button>
        <el-radio-button label="ANALYSIS">分析</el-radio-button>
        <el-radio-button label="QUOTE">摘录</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 笔记列表 -->
    <div v-loading="loading" class="notes-list">
      <el-empty v-if="filteredNotes.length === 0" description="暂无笔记" />
      
      <el-card
        v-for="note in filteredNotes"
        :key="note.id"
        class="note-card"
        :body-style="{ padding: '20px' }"
      >
        <div class="note-header">
          <h3 class="note-title">{{ note.title }}</h3>
          <div class="note-actions">
            <el-button
              type="text"
              :icon="note.isFavorite ? 'el-icon-star-on' : 'el-icon-star-off'"
              :class="{ 'is-favorite': note.isFavorite }"
              @click="toggleFavorite(note)"
            />
            <el-button type="text" icon="el-icon-edit" @click="editNote(note)" />
            <el-button type="text" icon="el-icon-delete" @click="deleteNote(note)" />
          </div>
        </div>
        
        <div class="note-meta">
          <el-tag size="mini" type="info">{{ getCategoryLabel(note.category) }}</el-tag>
          <el-tag v-if="note.isPublic" size="mini" type="success">公开</el-tag>
          <el-tag v-else size="mini" type="warning">私密</el-tag>
          <span class="note-time">{{ formatTime(note.updatedAt) }}</span>
        </div>
        
        <p class="note-content">{{ note.content }}</p>
        
        <div class="note-footer">
          <el-button type="text" size="small" @click="viewMovie(note.movieId)">
            <i class="el-icon-film" /> 查看电影
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 编辑笔记对话框 -->
    <el-dialog
      :title="editingNote.id ? '编辑笔记' : '新建笔记'"
      :visible.sync="showEditDialog"
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
            placeholder="写下你的想法..."
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
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveNote">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import noteService from '../services/note'

export default {
  name: 'Notes',
  data() {
    return {
      notes: [],
      loading: false,
      currentFilter: 'all',
      totalNotes: 0,
      showEditDialog: false,
      saving: false,
      editingNote: {
        id: null,
        title: '',
        content: '',
        category: 'GENERAL',
        isPublic: false
      }
    }
  },
  computed: {
    filteredNotes() {
      if (this.currentFilter === 'all') {
        return this.notes
      } else if (this.currentFilter === 'favorite') {
        return this.notes.filter(note => note.isFavorite)
      } else {
        return this.notes.filter(note => note.category === this.currentFilter)
      }
    }
  },
  created() {
    this.loadNotes()
  },
  methods: {
    async loadNotes() {
      this.loading = true
      try {
        const res = await noteService.getUserNotes()
        if (res.success) {
          this.notes = res.data || []
          this.totalNotes = this.notes.length
        }
      } catch (e) {
        this.$message.error('加载笔记失败')
      } finally {
        this.loading = false
      }
    },
    handleFilterChange() {
      // 筛选逻辑在 computed 中处理
    },
    getCategoryLabel(category) {
      const labels = {
        'GENERAL': '随笔',
        'REVIEW': '影评',
        'ANALYSIS': '分析',
        'QUOTE': '摘录'
      }
      return labels[category] || '随笔'
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },
    editNote(note) {
      this.editingNote = { ...note }
      this.showEditDialog = true
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
      
      this.saving = true
      try {
        if (this.editingNote.id) {
          await noteService.updateNote(this.editingNote.id, {
            title: this.editingNote.title,
            content: this.editingNote.content,
            category: this.editingNote.category,
            isPublic: this.editingNote.isPublic
          })
          this.$message.success('笔记更新成功')
        }
        this.showEditDialog = false
        this.loadNotes()
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.saving = false
      }
    },
    async toggleFavorite(note) {
      try {
        await noteService.updateNote(note.id, { isFavorite: !note.isFavorite })
        note.isFavorite = !note.isFavorite
        this.$message.success(note.isFavorite ? '已收藏' : '已取消收藏')
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    deleteNote(note) {
      this.$confirm('确定要删除这篇笔记吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await noteService.deleteNote(note.id)
          this.$message.success('删除成功')
          this.loadNotes()
        } catch (e) {
          this.$message.error('删除失败')
        }
      })
    },
    viewMovie(movieId) {
      this.$router.push(`/movie/${movieId}`)
    }
  }
}
</script>

<style scoped>
.notes-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
}

.filter-tabs {
  margin-bottom: 20px;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.note-card {
  transition: box-shadow 0.3s;
}

.note-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.note-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.note-actions {
  display: flex;
  gap: 8px;
}

.note-actions .is-favorite {
  color: #f7ba2a;
}

.note-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.note-time {
  color: #909399;
  font-size: 13px;
}

.note-content {
  color: #606266;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-footer {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}
</style>
