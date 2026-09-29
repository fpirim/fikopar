import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect on an expired session, not on a failed login (the page shows that error inline).
    const isLoginRequest = error.config?.url?.includes('/auth/login')
    const hadToken = Boolean(localStorage.getItem('token'))
    if (error.response?.status === 401 && hadToken && !isLoginRequest) {
      localStorage.removeItem('token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default api
