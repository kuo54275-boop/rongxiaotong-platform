import { defineStore } from 'pinia'
import { getInfoApi, loginApi, registerApi } from '@/api'

/**
 * 用户状态：保存登录令牌和用户信息（登录后全局都能用）
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    // 页面刷新后从本地存储恢复登录状态
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
  }),
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.userInfo?.role || '',
    realName: (state) => state.userInfo?.realName || '',
    avatar: (state) => state.userInfo?.avatar || '',
  },
  actions: {
    /** 登录：成功后保存令牌和用户信息 */
    async login(data) {
      const res = await loginApi(data)
      this.saveLogin(res.data)
    },
    /** 注册：注册成功即自动登录 */
    async register(data) {
      const res = await registerApi(data)
      this.saveLogin(res.data)
    },
    /** 拉取最新用户信息（如修改头像后刷新） */
    async fetchInfo() {
      const res = await getInfoApi()
      this.userInfo = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    },
    saveLogin(data) {
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    /** 退出登录：清空本地保存的登录信息 */
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
