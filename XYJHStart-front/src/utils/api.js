import axios from 'axios'
import { clearAuthState, getToken, isTokenExpired } from './authToken'

const api = axios.create({
  baseURL: '/',
  timeout: 10000,
})

function isLoginRequest(url = '') {
  return url.includes('/login')
}

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.assign('/login')
  }
}

function handleUnauthorized() {
  clearAuthState(api)
  redirectToLogin()
}

api.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code !== 0) {
      if ([401, 403].includes(res.code) && !isLoginRequest(response.config.url)) {
        handleUnauthorized()
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res.data
  },
  (error) => {
    console.error('API Error: ' + error)

    if ([401, 403].includes(error.response?.status) && !isLoginRequest(error.config?.url)) {
      handleUnauthorized()
    }

    return Promise.reject(error)
  }
)

api.interceptors.request.use(
  (config) => {
    const token = getToken()

    if (token && isTokenExpired(token)) {
      handleUnauthorized()
      return Promise.reject(new axios.CanceledError('Token expired'))
    }

    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }

    return config
  },
  (error) => Promise.reject(error)
)

const token = getToken()
if (token && !isTokenExpired(token)) {
  api.defaults.headers.common.Authorization = 'Bearer ' + token
} else if (token) {
  clearAuthState(api)
}

export default api
