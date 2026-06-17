import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'https://api-gateway-4tdc.onrender.com'

const axiosInstance = axios.create({
  baseURL: API_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

axiosInstance.interceptors.request.use(
  (config) => {
    const authStorage = localStorage.getItem('auth-storage')
    if (authStorage) {
      try {
        const { state } = JSON.parse(authStorage)
        if (state?.token) {
          config.headers.Authorization = `Bearer ${state.token}`
        }
      } catch (error) {
        // Prevent malformed legacy storage from breaking all API calls.
        console.warn('Invalid auth-storage format, clearing it.', error)
        localStorage.removeItem('auth-storage')
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: handle 401 (token expired) gracefully
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired - clear auth state and redirect to login
      localStorage.removeItem('auth-storage')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default axiosInstance