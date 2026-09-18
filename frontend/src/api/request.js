import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * axios 实例：所有接口请求的统一出口
 * 1. 请求自动带上登录令牌 token
 * 2. 响应统一解包 { code, msg, data }，出错自动弹提示
 */
const request = axios.create({
  baseURL: '/api',      // vite 会把它代理到后端 http://localhost:8080
  timeout: 10000,
})

// 请求拦截器：每次请求前自动携带 token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.token = token
  }
  return config
})

// 响应拦截器：统一处理返回结果
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res   // 成功：把 { code, msg, data } 返回给调用方
    }
    if (res.code === 401) {
      // 登录过期：清除本地登录信息，跳转登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg))
  },
  (error) => {
    ElMessage.error('网络异常，请检查后端是否已启动')
    return Promise.reject(error)
  },
)

export default request
