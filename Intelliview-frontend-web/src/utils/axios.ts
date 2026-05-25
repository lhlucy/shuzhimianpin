import axios from 'axios'

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 创建axios实例
const service = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    return res
  },
  error => {
    if (error?.code === 'ECONNABORTED') {
      error.message = '请求超时：后端响应时间过长或服务未正常启动'
    } else if (error?.message === 'Network Error') {
      error.message = `网络连接失败：当前无法访问后端服务 ${apiBaseUrl}`
    } else if (error?.response?.data?.message) {
      error.message = error.response.data.message
    }
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

export default service
