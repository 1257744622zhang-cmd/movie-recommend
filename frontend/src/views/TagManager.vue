<template>
  <div class="tag-manager">
    <div class="header">
      <h1>标签管理</h1>
      <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog = true">新建标签</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索标签名称"
        prefix-icon="el-icon-search"
        clearable
        @input="handleSearch"
        style="width: 300px"
      />
      <el-radio-group v-model="sortBy" size="small" style="margin-left: 20px;">
        <el-radio-button label="usage">按热度</el-radio-button>
        <el-radio-button label="name">按名称</el-radio-button>
        <el-radio-button label="time">按时间</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 标签列表 -->
    <div v-loading="loading" class="tag-list">
      <el-empty v-if="filteredTags.length === 0" description="暂无标签" />
      <el-row v-else :gutter="20">
        <el-col v-for="tag in filteredTags" :key="tag.id" :xs="12" :sm="8" :md="6" :lg="4">
          <el-card class="tag-card" shadow="hover">
            <div class="tag-header">
              <el-tag :style="{ backgroundColor: tag.color, borderColor: tag.color, color: '#fff' }" size="medium">{{ tag.name }}</el-tag>
              <el-dropdown @command="(cmd) => handleTagCommand(cmd, tag)">
                <i class="el-icon-more tag-more"></i>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
            <div class="tag-info">
              <p v-if="tag.description" class="tag-desc">{{ tag.description }}</p>
              <p class="tag-usage">
                <i class="el-icon-film"></i> 使用 {{ tag.usageCount || 0 }} 次
              </p>
              <p class="tag-time">创建于 {{ formatDate(tag.createTime) }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 新建标签对话框 -->
    <el-dialog title="新建标签" :visible.sync="showCreateDialog" width="400px">
      <el-form :model="newTag" :rules="tagRules" ref="createForm" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="newTag.name" placeholder="请输入标签名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="newTag.color" show-alpha :predefine="predefineColors"></el-color-picker>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="newTag.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createTag">创建</el-button>
      </div>
    </el-dialog>

    <!-- 编辑标签对话框 -->
    <el-dialog title="编辑标签" :visible.sync="showEditDialog" width="400px">
      <el-form :model="editTag" :rules="tagRules" ref="editForm" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editTag.name" placeholder="请输入标签名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="editTag.color" show-alpha :predefine="predefineColors"></el-color-picker>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editTag.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateTag">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { tagService } from '../services/tag'

export default {
  name: 'TagManager',
  data() {
    return {
      tags: [],
      loading: false,
      searchKeyword: '',
      sortBy: 'usage',
      showCreateDialog: false,
      showEditDialog: false,
      newTag: {
        name: '',
        color: '#409EFF',
        description: ''
      },
      editTag: {
        id: null,
        name: '',
        color: '#409EFF',
        description: ''
      },
      tagRules: {
        name: [
          { required: true, message: '请输入标签名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ]
      },
      predefineColors: [
        '#ff4500',
        '#ff8c00',
        '#ffd700',
        '#90ee90',
        '#00ced1',
        '#1e90ff',
        '#c71585',
        '#ff69b4',
        '#da70d6',
        '#9370db',
        '#409EFF',
        '#67C23A',
        '#E6A23C',
        '#F56C6C',
        '#909399'
      ]
    }
  },
  computed: {
    filteredTags() {
      let result = this.tags
      
      // 搜索过滤
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(tag => tag.name.toLowerCase().includes(keyword))
      }
      
      // 排序
      if (this.sortBy === 'usage') {
        result = [...result].sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0))
      } else if (this.sortBy === 'name') {
        result = [...result].sort((a, b) => a.name.localeCompare(b.name))
      } else if (this.sortBy === 'time') {
        result = [...result].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      }
      
      return result
    }
  },
  created() {
    this.loadTags()
  },
  methods: {
    async loadTags() {
      this.loading = true
      try {
        this.tags = await tagService.getAllTags()
      } catch (e) {
        this.$message.error(e.message || '加载标签失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      // 实时搜索，无需额外操作，由computed处理
    },
    async createTag() {
      this.$refs.createForm.validate(async (valid) => {
        if (!valid) return
        try {
          await tagService.createTag(
            this.newTag.name,
            this.newTag.color,
            this.newTag.description
          )
          this.$message.success('创建成功')
          this.showCreateDialog = false
          this.newTag = { name: '', color: '#409EFF', description: '' }
          await this.loadTags()
        } catch (e) {
          this.$message.error(e.message || '创建失败')
        }
      })
    },
    handleTagCommand(command, tag) {
      if (command === 'edit') {
        this.editTag = { ...tag }
        this.showEditDialog = true
      } else if (command === 'delete') {
        this.$confirm(`确定要删除标签 "${tag.name}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            await tagService.deleteTag(tag.id)
            this.$message.success('删除成功')
            await this.loadTags()
          } catch (e) {
            this.$message.error(e.message || '删除失败')
          }
        }).catch(() => {})
      }
    },
    async updateTag() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return
        try {
          await tagService.updateTag(
            this.editTag.id,
            this.editTag.name,
            this.editTag.color,
            this.editTag.description
          )
          this.$message.success('更新成功')
          this.showEditDialog = false
          await this.loadTags()
        } catch (e) {
          this.$message.error(e.message || '更新失败')
        }
      })
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleDateString('zh-CN')
    }
  }
}
</script>

<style scoped>
.tag-manager {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.tag-list {
  min-height: 400px;
}

.tag-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.tag-card:hover {
  transform: translateY(-5px);
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.tag-more {
  cursor: pointer;
  padding: 5px;
  color: #909399;
}

.tag-more:hover {
  color: #409EFF;
}

.tag-info {
  font-size: 12px;
  color: #606266;
}

.tag-desc {
  margin: 0 0 10px 0;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tag-usage {
  margin: 5px 0;
  color: #409EFF;
}

.tag-time {
  margin: 5px 0 0 0;
  color: #C0C4CC;
  font-size: 11px;
}
</style>
