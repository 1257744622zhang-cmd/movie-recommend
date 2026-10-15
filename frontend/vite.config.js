import { defineConfig } from 'vite'
// 导入 Vue 2 插件
import { createVuePlugin } from 'vite-plugin-vue2'
import path from 'path'

// 修复 __dirname 在 ESModule 中的兼容问题
import { fileURLToPath } from 'url'
const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

export default defineConfig({
  // 正确调用 Vue 2 插件
  plugins: [createVuePlugin()],
  resolve: {
    alias: {
      // 只保留常用的 @ 别名（指向 src），删除所有 core-js 相关别名
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})