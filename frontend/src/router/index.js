import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表：定义网址和页面的对应关系
 * 例如访问 / → 首页，访问 /login → 登录页
 */
const routes = [
  // 登录页（独立页面，没有顶部导航）
  {
    path: '/login',
    component: () => import('@/views/Login.vue'),
  },
  // 前台页面（统一带顶部导航和页脚）
  {
    path: '/',
    component: () => import('@/layouts/FrontLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/Home.vue') },
      { path: 'finance', name: 'finance', component: () => import('@/views/finance/FinanceHome.vue') },
      // 专家助力模块
      { path: 'expert', name: 'expert', component: () => import('@/views/expert/ExpertHome.vue') },
      { path: 'expert/knowledge', name: 'knowledge', component: () => import('@/views/expert/KnowledgeList.vue') },
      { path: 'expert/knowledge/:id', name: 'knowledgeDetail', component: () => import('@/views/expert/KnowledgeDetail.vue') },
      { path: 'expert/questions', name: 'questions', component: () => import('@/views/expert/QuestionList.vue') },
      { path: 'expert/team', name: 'expertTeam', component: () => import('@/views/expert/ExpertList.vue') },
      { path: 'expert/chat/:userId', name: 'chat', component: () => import('@/views/expert/Chat.vue') },
      { path: 'expert/appointments', name: 'appointments', component: () => import('@/views/expert/Appointments.vue') },
      // 后续模块的路由在这里逐个添加
    ],
  },
  // 访问不存在的页面 → 回到首页
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),   // 切换页面后回到顶部
})

// 全局路由守卫：标记了 requiresAuth 的页面必须登录才能访问
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return '/login'
  }
})

export default router
