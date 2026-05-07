import axios from 'axios'
import { useToast } from 'primevue/usetoast'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor — inject Authorization header from authStore
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor — global error toast + 401 redirect
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      const currentPath = window.location.pathname
      if (currentPath !== '/login') {
        window.location.href = '/login?expired=1'
      }
      return Promise.reject(error)
    }

    const toast = useToast()
    const message: string =
      error.response?.data?.message ?? 'Không thể kết nối đến máy chủ'
    toast.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: message,
      life: 4000
    })
    return Promise.reject(error)
  }
)

export default api
