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

// ===== 专家助力 =====
// 公开浏览
export const getArticlesApi = (params) => request.get('/public/articles', { params })
export const getArticleApi = (id) => request.get(`/public/article/${id}`)
export const getExpertsApi = () => request.get('/public/experts')
export const getExpertApi = (userId) => request.get(`/public/expert/${userId}`)
export const getQuestionsApi = (params) => request.get('/public/questions', { params })
export const getQuestionApi = (id) => request.get(`/public/question/${id}`)
// 知识库（专家维护）
export const getMyArticlesApi = () => request.get('/expert/article/my')
export const saveArticleApi = (data) => request.post('/expert/article/save', data)
export const deleteArticleApi = (id) => request.delete(`/expert/article/${id}`)
// 专家信息
export const getMyProfileApi = () => request.get('/expert/profile')
export const updateProfileApi = (data) => request.put('/expert/profile', data)
// 问答
export const askQuestionApi = (data) => request.post('/expert/question/ask', data)
export const answerQuestionApi = (id, data) => request.post(`/expert/question/${id}/answer`, data)
export const getTodoQuestionsApi = () => request.get('/expert/question/todo')
export const getMyQuestionsApi = () => request.get('/expert/question/my')
// 预约
export const createAppointmentApi = (data) => request.post('/appointment/create', data)
export const getMyAppointmentsApi = () => request.get('/appointment/my')
export const getExpertAppointmentsApi = () => request.get('/appointment/expert')
export const updateAppointmentStatusApi = (id, status) =>
  request.put(`/appointment/${id}/status`, null, { params: { status } })
export const cancelAppointmentApi = (id) => request.put(`/appointment/${id}/cancel`)
// 在线咨询
export const sendMessageApi = (receiverId, content) =>
  request.post('/consult/send', null, { params: { receiverId, content } })
export const getMessagesApi = (withUserId) =>
  request.get('/consult/list', { params: { with: withUserId } })
export const getContactsApi = () => request.get('/consult/contacts')
export const getConsultStatsApi = () => request.get('/consult/stats')

// 用户公开信息
export const getBasicUserApi = (id) => request.get(`/public/user/${id}/basic`)
