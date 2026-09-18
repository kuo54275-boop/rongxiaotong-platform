import request from './request'

/**
 * 接口定义：一个函数对应后端一个接口
 * 用法示例：const res = await getHomeApi(); console.log(res.data)
 */

// ===== 账号 =====
export const loginApi = (data) => request.post('/auth/login', data)
export const registerApi = (data) => request.post('/auth/register', data)
export const getInfoApi = () => request.get('/auth/info')

// ===== 公开数据 =====
export const getHomeApi = () => request.get('/public/home')
export const getNewsListApi = () => request.get('/public/news/list')

// ===== 融资服务 =====
export const getLoanProductsApi = () => request.get('/public/loan/products')
export const getLoanProductApi = (id) => request.get(`/public/loan/product/${id}`)
export const matchPartnersApi = () => request.get('/loan/partner/match')
export const applyLoanApi = (data) => request.post('/loan/apply', data)
export const getMyLoansApi = () => request.get('/loan/my')
