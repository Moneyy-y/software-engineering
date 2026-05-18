import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({ baseURL: '', timeout: 15000 })

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.success) return data.data
    ElMessage.error(data.message || '请求失败')
    if (data.status === 1002 || data.status === 1003) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(data)
  },
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.message
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('nickname')
      ElMessage.error(msg || '登录已失效，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(msg || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
