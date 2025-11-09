import { createRouter, createWebHistory } from 'vue-router'

// 1. 导入你的页面组件
import Login from '../views/Login.vue'

// --- 新增导入 ---
import AppLayout from '../components/AppLayout.vue'
import Home from '../views/Home.vue'
import ManageLicenses from '../views/ManageLicenses.vue'
import PendingLicenses from '../views/PendingLicenses.vue'
import ManageAdmins from '../views/ManageAdmins.vue'
import RenewLicenses from '../views/RenewLicenses.vue'
// --- 新增导入结束 ---


// 2. 【核心修改】: 定义嵌套路由
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/', // 根路径现在指向我们的 AppLayout
    component: AppLayout,
    meta: { requiresAuth: true }, // 保护整个布局
    
    // "children" 数组中的组件，将被渲染到 AppLayout 的 <router-view> 中
    children: [
      {
        path: '', // 默认子路由 (访问 / 时显示)
        name: 'Home',
        component: Home
      },
      {
        path: 'pending-licenses', // 访问 /pending-licenses 时显示
        name: 'PendingLicenses',
        component: PendingLicenses
      },
      {
        path: 'manage-licenses', // 访问 /manage-licenses 时显示
        name: 'ManageLicenses',
        component: ManageLicenses
      },
      {
        path: 'manage-admins', // 访问 /manage-admins 时显示
        name: 'ManageAdmins',
        component: ManageAdmins
      },
        {
        path: 'renew-licenses',
        name: 'RenewLicenses',
        component: RenewLicenses
      }

    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 4. 全局路由守卫 (保持不变，它会正确保护所有 requiresAuth: true 的路由)
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login' });
  } else if (to.name === 'Login' && token) {
    // 【修改】如果已登录，访问 /login 时，跳转到 Home (/)
    next({ name: 'Home' }); 
  } else {
    next();
  }
})

export default router