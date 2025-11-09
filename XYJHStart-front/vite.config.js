import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// --- 下面是新增的代码 ---
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
// --- 新增代码结束 ---

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  server: {
    proxy: {
      // 通过相对路径 /api 转发到后端
      '/api': {
        target: 'http://localhost:9191',
        changeOrigin: true,
      },
    },
  },
})