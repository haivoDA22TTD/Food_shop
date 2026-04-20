import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'https://api-gateway-4tdc.onrender.com'

const axiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth-storage')
    if (token) {
      const { state } = JSON.parse(token)
      if (state?.token) {
        config.headers.Authorization = `Bearer ${state.token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

export default axiosInstance
