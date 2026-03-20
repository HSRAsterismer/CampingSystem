import axios from 'axios'

const TOKEN_KEY = 'camping-system-token'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.success) {
      return payload.data
    }
    return Promise.reject(new Error(payload?.message || '请求失败'))
  },
  (error) => {
    const errorMessage = error.response?.data?.message || error.message || '网络异常，请稍后重试'
    return Promise.reject(new Error(errorMessage))
  },
)

export { TOKEN_KEY }
export default http
