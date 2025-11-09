import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// 1. 导入 Element Plus 的 CSS (非常重要，否则样式会丢失)
import 'element-plus/dist/index.css'

// 2. 导入我们刚刚创建的 router
import router from './router'

// 3. 导入 Pinia
import { createPinia } from 'pinia'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 4. 创建 Pinia 实例
const pinia = createPinia()

// 5. 创建 Vue 应用实例
const app = createApp(App)
// 全局注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 6. 挂载 Pinia 和 Router
app.use(pinia)  // 挂载 Pinia
app.use(router) // 挂载 Router

// 7. 挂载根组件到页面
app.mount('#app')