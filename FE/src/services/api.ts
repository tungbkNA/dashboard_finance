import axios from 'axios'
import { useToast } from 'primevue/usetoast'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor placeholder
api.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
)

// Response interceptor — global error toast (T032)
api.interceptors.response.use(
  (response) => response,
  (error) => {
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
