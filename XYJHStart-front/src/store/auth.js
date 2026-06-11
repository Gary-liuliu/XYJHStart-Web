import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../utils/api'
import { clearAuthState, getToken, saveToken } from '../utils/authToken'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken() || null)

  async function login(username, password) {
    try {
      const jwtToken = await api.post('/api/admin/users/login', {
        username,
        password
      })

      token.value = jwtToken
      saveToken(jwtToken)
      api.defaults.headers.common.Authorization = 'Bearer ' + jwtToken
      console.log('登录成功:', jwtToken)
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  function logout(router = null) {
    token.value = null
    clearAuthState(api)

    if (router) {
      router.push('/login')
    }
  }

  const isAuthenticated = () => !!token.value

  return { token, login, logout, isAuthenticated }
})
