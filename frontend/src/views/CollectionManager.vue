<template>
  <div class="collection-manager">
    <div class="header">
      <h1>收藏夹管理</h1>
      <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog = true">新建收藏夹</el-button>
    </div>

    <div class="content">
      <!-- 左侧收藏夹列表 -->
      <div class="folder-sidebar">
        <div class="folder-list">
          <div
            v-for="folder in folders"
            :key="folder.id"
            :class="['folder-item', { active: selectedFolder?.id === folder.id }]"
            @click="selectFolder(folder)"
          >
            <i :class="folder.isDefault ? 'el-icon-s-home' : 'el-icon-folder'"></i>
            <span class="folder-name">{{ folder.name }}</span>
            <span class="folder-count">({{ folder.movieCount || 0 }})</span>
            <el-tag v-if="folder.permission === 'PUBLIC'" size="mini" type="success">公开</el-tag>
            <el-dropdown v-if="!folder.isDefault" @command="(cmd) => handleFolderCommand(cmd, folder)" @click.stop>
              <i class="el-icon-more folder-more"></i>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="permission">
                  {{ folder.permission === 'PUBLIC' ? '设为私有' : '设为公开' }}
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 右侧电影列表 -->
      <div class="movie-content">
        <div v-if="selectedFolder" class="folder-header">
          <div class="folder-title-row">
            <h2>{{ selectedFolder.name }}</h2>
            <div class="folder-permission">
              <el-tag v-if="selectedFolder.permission === 'PUBLIC'" type="success" size="small">
                <i class="el-icon-view"></i> 公开
              </el-tag>
              <el-tag v-else type="info" size="small">
                <i class="el-icon-lock"></i> 私有
              </el-tag>
              <el-button 
                v-if="!selectedFolder.isDefault" 
                size="mini" 
                type="text" 
                @click="toggleFolderPermission(selectedFolder)"
              >
                {{ selectedFolder.permission === 'PUBLIC' ? '设为私有' : '设为公开' }}
              </el-button>
            </div>
          </div>
          <p v-if="selectedFolder.description" class="folder-desc">{{ selectedFolder.description }}</p>
          <div class="batch-actions" v-if="selectedMovies.length > 0">
            <span>已选择 {{ selectedMovies.length }} 项</span>
            <el-dropdown @command="handleBatchMove">
              <el-button size="small" type="primary">
                移动到<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item
                  v-for="folder in folders.filter(f => f.id !== selectedFolder.id)"
                  :key="folder.id"
                  :command="folder.id"
                >
                  {{ folder.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-button size="small" type="danger" @click="batchRemove">从收藏夹移除</el-button>
          </div>
        </div>

        <el-row v-loading="loading" :gutter="20">
          <el-col v-for="movie in movies" :key="movie.id" :xs="12" :sm="8" :md="6" :lg="4">
            <el-card class="movie-card" shadow="hover" :body-style="{ padding: '10px' }">
              <div class="movie-checkbox" @click.stop>
                <el-checkbox v-model="selectedMovies" :label="movie.id">&nbsp;</el-checkbox>
              </div>
              <div class="poster" @click="$router.push('/movie/' + movie.id)">
                {{ movie.title?.slice(0, 2) || '电影' }}
              </div>
              <div class="movie-info" @click="$router.push('/movie/' + movie.id)">
                <div class="title" :title="movie.title">{{ movie.title }}</div>
                <div class="meta">{{ movie.genre }} · {{ movie.releaseYear }}</div>
                <div class="rating">
                  <el-rate :value="(movie.avgRating || 0) / 2" :max="5" disabled show-score 
                           :score-template="String(movie.avgRating || 0)" />
                </div>
              </div>
              <div class="movie-actions">
                <el-button-group>
                  <el-button size="mini" type="primary" icon="el-icon-right" @click="openMoveDialog(movie)">移动</el-button>
                  <el-button size="mini" type="danger" icon="el-icon-delete" @click="removeMovie(movie)">移除</el-button>
                </el-button-group>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="!loading && movies.length === 0 && selectedFolder" description="该收藏夹暂无电影" />
      </div>
    </div>

    <!-- 创建收藏夹对话框 -->
    <el-dialog title="新建收藏夹" :visible.sync="showCreateDialog" width="400px">
      <el-form :model="newFolder" :rules="folderRules" ref="createForm" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="newFolder.name" placeholder="请输入收藏夹名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="newFolder.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="权限">
          <el-radio-group v-model="newFolder.permission">
            <el-radio label="PRIVATE">私有</el-radio>
            <el-radio label="PUBLIC">公开</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createFolder">创建</el-button>
      </div>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog title="重命名收藏夹" :visible.sync="showRenameDialog" width="400px">
      <el-form :model="renameForm" :rules="folderRules" ref="renameForm" label-width="80px">
        <el-form-item label="新名称" prop="name">
          <el-input v-model="renameForm.name" placeholder="请输入新名称" maxlength="50" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" @click="renameFolder">确认</el-button>
      </div>
    </el-dialog>

    <!-- 移动到收藏夹对话框 -->
    <el-dialog title="移动到收藏夹" :visible.sync="showMoveDialog" width="400px">
      <div class="move-folder-list">
        <div
          v-for="folder in folders.filter(f => f.id !== selectedFolder?.id)"
          :key="folder.id"
          :class="['move-folder-item', { selected: targetFolderId === folder.id }]"
          @click="targetFolderId = folder.id"
        >
          <i :class="folder.isDefault ? 'el-icon-s-home' : 'el-icon-folder'"></i>
          <span>{{ folder.name }}</span>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="showMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmMove" :disabled="!targetFolderId">移动</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import folderService from '../services/folder'
import collectionService from '../services/collection'

export default {
  name: 'CollectionManager',
  data() {
    return {
      folders: [],
      selectedFolder: null,
      movies: [],
      loading: false,
      selectedMovies: [],
      showCreateDialog: false,
      showRenameDialog: false,
      showMoveDialog: false,
      newFolder: {
        name: '',
        description: '',
        permission: 'PRIVATE'
      },
      renameForm: {
        folder: null,
        name: ''
      },
      targetFolderId: null,
      currentMovie: null,
      folderRules: {
        name: [
          { required: true, message: '请输入收藏夹名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadFolders()
  },
  methods: {
    async loadFolders() {
      try {
        this.folders = await folderService.getUserFolders()
        // 加载每个收藏夹的电影数量
        for (let folder of this.folders) {
          try {
            const countRes = await folderService.getFolderMovieCount(folder.id)
            folder.movieCount = countRes.count
          } catch (e) {
            folder.movieCount = 0
          }
        }
        // 默认选中第一个收藏夹
        if (this.folders.length > 0 && !this.selectedFolder) {
          this.selectFolder(this.folders[0])
        }
      } catch (e) {
        this.$message.error(e.message || '加载收藏夹失败')
      }
    },
    async selectFolder(folder) {
      this.selectedFolder = folder
      this.selectedMovies = []
      this.loading = true
      try {
        this.movies = await folderService.getFolderMovies(folder.id)
      } catch (e) {
        this.$message.error(e.message || '加载电影失败')
        this.movies = []
      } finally {
        this.loading = false
      }
    },
    async createFolder() {
      this.$refs.createForm.validate(async (valid) => {
        if (!valid) return
        try {
          await folderService.createFolder(
            this.newFolder.name,
            this.newFolder.description,
            this.newFolder.permission
          )
          this.$message.success('创建成功')
          this.showCreateDialog = false
          this.newFolder = { name: '', description: '', permission: 'PRIVATE' }
          await this.loadFolders()
        } catch (e) {
          this.$message.error(e.message || '创建失败')
        }
      })
    },
    handleFolderCommand(command, folder) {
      if (command === 'rename') {
        this.renameForm.folder = folder
        this.renameForm.name = folder.name
        this.showRenameDialog = true
      } else if (command === 'permission') {
        // 切换权限
        const newPermission = folder.permission === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC'
        const actionText = newPermission === 'PUBLIC' ? '公开' : '私有'
        this.$confirm(`确定要将收藏夹 "${folder.name}" 设为${actionText}吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            await folderService.setFolderPermission(folder.id, newPermission)
            this.$message.success(`已设为${actionText}`)
            await this.loadFolders()
          } catch (e) {
            this.$message.error(e.message || '设置失败')
          }
        }).catch(() => {})
      } else if (command === 'delete') {
        this.$confirm(`确定要删除收藏夹 "${folder.name}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          try {
            await folderService.deleteFolder(folder.id, true)
            this.$message.success('删除成功')
            if (this.selectedFolder?.id === folder.id) {
              this.selectedFolder = null
              this.movies = []
            }
            await this.loadFolders()
          } catch (e) {
            this.$message.error(e.message || '删除失败')
          }
        }).catch(() => {})
      }
    },
    async renameFolder() {
      this.$refs.renameForm.validate(async (valid) => {
        if (!valid) return
        try {
          await folderService.renameFolder(this.renameForm.folder.id, this.renameForm.name)
          this.$message.success('重命名成功')
          this.showRenameDialog = false
          await this.loadFolders()
        } catch (e) {
          this.$message.error(e.message || '重命名失败')
        }
      })
    },
    openMoveDialog(movie) {
      this.currentMovie = movie
      this.targetFolderId = null
      this.showMoveDialog = true
    },
    removeMovie(movie) {
      this.$confirm(`确定要从收藏夹移除 "${movie.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await collectionService.removeFromCollection(movie.id)
          this.$message.success('移除成功')
          await this.selectFolder(this.selectedFolder)
          await this.loadFolders()
        } catch (e) {
          this.$message.error(e.message || '移除失败')
        }
      }).catch(() => {})
    },
    async confirmMove() {
      if (!this.targetFolderId || !this.currentMovie) return
      try {
        await folderService.moveMovieToFolder(this.targetFolderId, this.currentMovie.id)
        this.$message.success('移动成功')
        this.showMoveDialog = false
        this.currentMovie = null
        this.targetFolderId = null
        await this.selectFolder(this.selectedFolder)
        await this.loadFolders()
      } catch (e) {
        this.$message.error(e.message || '移动失败')
      }
    },
    // 切换收藏夹权限
    async toggleFolderPermission(folder) {
      const newPermission = folder.permission === 'PUBLIC' ? 'PRIVATE' : 'PUBLIC'
      const actionText = newPermission === 'PUBLIC' ? '公开' : '私有'
      this.$confirm(`确定要将收藏夹 "${folder.name}" 设为${actionText}吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await folderService.setFolderPermission(folder.id, newPermission)
          this.$message.success(`已设为${actionText}`)
          // 更新当前选中的收藏夹
          if (this.selectedFolder && this.selectedFolder.id === folder.id) {
            this.selectedFolder.permission = newPermission
          }
          await this.loadFolders()
        } catch (e) {
          this.$message.error(e.message || '设置失败')
        }
      }).catch(() => {})
    },
    async handleBatchMove(targetFolderId) {
      if (!targetFolderId || this.selectedMovies.length === 0) return
      try {
        await folderService.batchMoveMoviesToFolder(targetFolderId, this.selectedMovies)
        this.$message.success('批量移动成功')
        this.selectedMovies = []
        await this.selectFolder(this.selectedFolder)
        await this.loadFolders()
      } catch (e) {
        this.$message.error(e.message || '批量移动失败')
      }
    },
    async batchRemove() {
      if (this.selectedMovies.length === 0) return
      this.$confirm(`确定要从收藏夹移除选中的 ${this.selectedMovies.length} 部电影吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          for (const movieId of this.selectedMovies) {
            await collectionService.removeFromCollection(movieId)
          }
          this.$message.success('批量移除成功')
          this.selectedMovies = []
          await this.selectFolder(this.selectedFolder)
          await this.loadFolders()
        } catch (e) {
          this.$message.error(e.message || '批量移除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.collection-manager {
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

.content {
  display: flex;
  gap: 20px;
  min-height: 600px;
}

.folder-sidebar {
  width: 250px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
}

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.folder-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.folder-item:hover {
  background: #f5f7fa;
}

.folder-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.folder-item i {
  margin-right: 10px;
  font-size: 18px;
}

.folder-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-count {
  color: #909399;
  font-size: 12px;
  margin-left: 5px;
}

.folder-more {
  padding: 5px;
  opacity: 0;
  transition: opacity 0.3s;
}

.folder-item:hover .folder-more {
  opacity: 1;
}

.movie-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.folder-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.folder-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.folder-title-row h2 {
  margin: 0;
}

.folder-permission {
  display: flex;
  align-items: center;
  gap: 10px;
}

.folder-desc {
  color: #909399;
  margin: 0 0 15px 0;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.movie-card {
  margin-bottom: 15px;
  position: relative;
  transition: transform 0.3s;
}

.movie-card:hover {
  transform: translateY(-5px);
}

.movie-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
}

.poster {
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
}

.movie-info {
  cursor: pointer;
}

.movie-info .title {
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 5px;
}

.movie-info .meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.movie-info .rating {
  margin-bottom: 5px;
}

.movie-actions {
  text-align: center;
}

.move-folder-list {
  max-height: 300px;
  overflow-y: auto;
}

.move-folder-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 5px;
}

.move-folder-item:hover {
  background: #f5f7fa;
}

.move-folder-item.selected {
  background: #ecf5ff;
  color: #409eff;
}

.move-folder-item i {
  margin-right: 10px;
  font-size: 18px;
}
</style>
