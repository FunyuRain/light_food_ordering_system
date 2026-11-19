import axios from 'axios'
import { showTopError } from '@/utils/errorToast'

const TOKEN_KEY = 'lf_user_token'
const PROFILE_KEY = 'lf_user_profile'

const service = axios.create({
  baseURL: '/',
  timeout: 15000
})

function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(PROFILE_KEY)
}

function redirectToLogin() {
  if (!window.location.hash.startsWith('#/login')) {
    window.location.hash = '#/login'
  }
}

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && payload.code === 1) {
      return payload.data
    }
    const message = payload?.message || '操作失败'
    if (payload?.code === 401 || payload?.code === 403) {
      clearSession()
      redirectToLogin()
    }
    showTopError(message)
    return Promise.reject(new Error(message))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || (status === 401 ? '登录已过期，请重新登录' : '网络开小差了，请稍后再试')
    if (status === 401 || status === 403) {
      clearSession()
      redirectToLogin()
    }
    showTopError(message)
    return Promise.reject(error)
  }
)

export default service
