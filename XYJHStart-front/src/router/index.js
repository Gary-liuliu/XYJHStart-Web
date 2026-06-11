import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import AppLayout from '../components/AppLayout.vue'
import Home from '../views/Home.vue'
import ManageLicenses from '../views/ManageLicenses.vue'
import PendingLicenses from '../views/PendingLicenses.vue'
import ManageAdmins from '../views/ManageAdmins.vue'
import RenewLicenses from '../views/RenewLicenses.vue'
import { clearAuthState, getToken, isTokenExpired } from '../utils/authToken'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: Home
      },
      {
        path: 'pending-licenses',
        name: 'PendingLicenses',
        component: PendingLicenses
      },
      {
        path: 'manage-licenses',
        name: 'ManageLicenses',
        component: ManageLicenses
      },
      {
        path: 'manage-admins',
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

router.beforeEach((to, from, next) => {
  const token = getToken()
  const hasValidToken = token && !isTokenExpired(token)

  if (token && !hasValidToken) {
    clearAuthState()
  }

  if (to.meta.requiresAuth && !hasValidToken) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && hasValidToken) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
