import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),   // @ 代表 src 目录，写导入路径更短
    },
  },
  server: {
    port: 5173,
    proxy: {
      // 前端所有 /api 开头的请求，自动转发给后端 8080 端口（解决跨域问题）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
