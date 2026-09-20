<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getContactsApi,
  getConsultStatsApi,
  getNotifyListApi,
  markAllNotifyReadApi,
  markNotifyReadApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 前台整体布局：顶部导航（含消息铃铛）+ 页面主体 + 页脚
 * 登录后铃铛每 10 秒轮询一次：未读消息 / 待处理预约 / 待回答问题，有红点提醒
 */
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = [
  { label: '首页', path: '/' },
  { label: '融资服务', path: '/finance' },
  { label: '专家助力', path: '/expert' },
  { label: '农产品交易', path: '/market' },
  { label: '个人中心', path: '/user' },
]

// 通知数据
const stats = ref({ unreadMessages: 0, pendingAppointments: 0, todoQuestions: 0, unreadNotifications: 0 })
const contacts = ref([])
const notifications = ref([])
const unreadContacts = computed(() => contacts.value.filter((c) => c.unread > 0))
const unreadNotifications = computed(() => notifications.value.filter((n) => n.isRead === 0))
const bellBadge = computed(
  () =>
    stats.value.unreadMessages +
    stats.value.pendingAppointments +
    stats.value.todoQuestions +
    stats.value.unreadNotifications,
)
let timer = null

onMounted(() => {
  tick()
  timer = setInterval(tick, 10000)   // 每 10 秒轮询一次通知
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

async function tick() {
  if (!userStore.isLogin) {
    return
  }
  try {
    const [s, c, n] = await Promise.all([
      getConsultStatsApi(),
      getContactsApi(),
      getNotifyListApi(),
    ])
    stats.value = s.data || stats.value
    contacts.value = c.data || []
    notifications.value = n.data || []
  } catch (e) {
    // 登录过期时拦截器会处理跳转
  }
}

/** 点击系统通知：标记已读 + 跳转到对应页面 */
async function onNotifyClick(n) {
  if (n.isRead === 0) {
    try {
      await markNotifyReadApi(n.id)
    } catch (e) {
      // 忽略
    }
  }
  const routeMap = {
    ORDER_NEW: '/market/orders',
    ORDER_PAID: '/market/orders',
    ORDER_SHIPPED: '/market/orders',
    QUESTION_ANSWERED: '/expert/questions',
    APPOINTMENT_HANDLED: '/expert/appointments',
    LOAN_APPROVED: '/user/finance',
    LOAN_REJECTED: '/user/finance',
  }
  router.push(routeMap[n.type] || '/')
  tick()
}

async function readAllNotify() {
  await markAllNotifyReadApi()
  tick()
}

function onMenu(path) {
  // 已完成的模块直接跳转，未完成的先提示
  if (path === '/' || path === '/finance' || path === '/expert' || path === '/market') {
    router.push(path)
    return
  }
  if (path === '/user') {
    if (!userStore.isLogin) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    router.push('/user')
    return
  }
  ElMessage.info('该模块正在开发中，敬请期待！')
}

/** 铃铛里的通知项点击 */
function onNotify(item) {
  if (item === 'appointments') {
    router.push('/expert/appointments')
  } else if (item === 'questions') {
    router.push('/expert/questions')
  } else if (typeof item === 'number' || typeof item === 'string') {
    router.push(`/expert/chat/${item}`)
  }
}

/** 用户头像下拉菜单 */
function onUserCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (cmd === 'info') {
    router.push('/user')
  } else if (cmd === 'appointments') {
    router.push('/expert/appointments')
  } else if (cmd === 'knowledge') {
    router.push('/expert/knowledge')
  } else if (cmd === 'finance') {
    router.push('/finance')
  }
}
</script>

<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/')">
          <span class="logo-icon">🌾</span>
          <span class="logo-text">融小通</span>
        </div>

        <nav class="menu">
          <span
            v-for="m in menus"
            :key="m.path"
            class="menu-item"
            :class="{ active: m.path === route.path }"
            @click="onMenu(m.path)"
          >
            {{ m.label }}
          </span>
        </nav>

        <div class="user-area">
          <template v-if="userStore.isLogin">
            <!-- 消息铃铛：未读消息 / 待处理预约 / 待回答问题 -->
            <el-popover placement="bottom-end" :width="320" trigger="click">
              <template #reference>
                <span class="bell">
                  <el-badge :value="bellBadge" :hidden="bellBadge === 0" :max="99">
                    <span class="bell-icon">🔔</span>
                  </el-badge>
                </span>
              </template>
              <div class="notify-panel">
                <div class="notify-title">通知中心</div>

                <!-- 未读消息 -->
                <template v-if="unreadContacts.length">
                  <div class="notify-group">💬 未读消息</div>
                  <div
                    v-for="c in unreadContacts"
                    :key="c.userId"
                    class="notify-item"
                    @click="onNotify(c.userId)"
                  >
                    <img :src="c.avatar" class="notify-avatar" alt="头像" />
                    <div class="notify-text">
                      <div class="notify-name">{{ c.realName }}</div>
                      <div class="notify-msg">{{ c.lastMessage }}</div>
                    </div>
                    <el-badge :value="c.unread" />
                  </div>
                </template>

                <!-- 专家：待处理预约 -->
                <template v-if="userStore.role === 'EXPERT' && stats.pendingAppointments > 0">
                  <div class="notify-group">📅 预约处理</div>
                  <div class="notify-item" @click="onNotify('appointments')">
                    <span class="notify-dot">●</span>
                    有 {{ stats.pendingAppointments }} 条预约待处理
                  </div>
                </template>

                <!-- 专家：待回答问题 -->
                <template v-if="userStore.role === 'EXPERT' && stats.todoQuestions > 0">
                  <div class="notify-group">❓ 问题回答</div>
                  <div class="notify-item" @click="onNotify('questions')">
                    <span class="notify-dot">●</span>
                    有 {{ stats.todoQuestions }} 个问题待回答
                  </div>
                </template>

                <!-- 系统通知：新订单 / 已发货 / 回答 / 预约状态 -->
                <template v-if="unreadNotifications.length">
                  <div class="notify-group">
                    🔔 系统通知
                    <span class="read-all" @click="readAllNotify">全部已读</span>
                  </div>
                  <div
                    v-for="n in unreadNotifications"
                    :key="n.id"
                    class="notify-item"
                    @click="onNotifyClick(n)"
                  >
                    <div class="notify-text">
                      <div class="notify-name">{{ n.title }}</div>
                      <div class="notify-msg">{{ n.content }}</div>
                      <div class="notify-time">{{ n.createTime }}</div>
                    </div>
                    <span class="notify-dot">●</span>
                  </div>
                </template>

                <!-- 全部已读 -->
                <div v-if="bellBadge === 0" class="notify-empty">暂无新通知</div>
              </div>
            </el-popover>

            <!-- 用户下拉 -->
            <el-dropdown @command="onUserCommand">
              <span class="user-info">
                <img :src="userStore.avatar" class="avatar" alt="头像" />
                {{ userStore.realName }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info">个人中心</el-dropdown-item>
                  <template v-if="userStore.role === 'EXPERT'">
                    <el-dropdown-item command="appointments">📅 预约管理</el-dropdown-item>
                    <el-dropdown-item command="knowledge">📚 知识管理</el-dropdown-item>
                  </template>
                  <template v-if="userStore.role === 'FARMER'">
                    <el-dropdown-item command="appointments">📅 我的预约</el-dropdown-item>
                    <el-dropdown-item command="finance">🏦 我的融资</el-dropdown-item>
                  </template>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="small" @click="router.push('/login')">
              登录 / 注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 页面主体：不同页面替换这里 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-title">融小通农业综合服务平台 · 助力乡村振兴</div>
      <div class="footer-sub">融资授信 · 农产品购销 · 农技服务 一站式解决方案</div>
      <div class="footer-sub">Copyright © 2026 RongXiaoTong. All rights reserved.</div>
    </footer>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  width: 1200px;
  max-width: 100%;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  margin-right: 40px;
}
.logo-icon {
  font-size: 26px;
}
.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #3d8c40;
}
.menu {
  flex: 1;
  display: flex;
  gap: 8px;
}
.menu-item {
  padding: 8px 16px;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}
.menu-item:hover {
  color: #3d8c40;
  background: #ecf7ed;
}
.menu-item.active {
  color: #3d8c40;
  font-weight: 600;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 18px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
  outline: none;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

/* 铃铛 */
.bell {
  cursor: pointer;
  outline: none;
  display: inline-flex;
  align-items: center;
}
.bell-icon {
  font-size: 20px;
}
.notify-panel {
  max-height: 400px;
  overflow-y: auto;
}
.notify-title {
  padding: 10px 16px;
  font-weight: 700;
  font-size: 14px;
  border-bottom: 1px solid #f0f2f5;
}
.notify-group {
  padding: 8px 16px 4px;
  font-size: 12px;
  color: #909399;
}
.notify-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
}
.notify-item:hover {
  background: #f5f7fa;
}
.notify-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
}
.notify-text {
  flex: 1;
  min-width: 0;
}
.notify-name {
  font-size: 13px;
  font-weight: 600;
}
.notify-msg {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}
.notify-dot {
  color: #f56c6c;
  margin-right: 6px;
  flex-shrink: 0;
}
.notify-time {
  color: #c0c4cc;
  font-size: 11px;
  margin-top: 2px;
}
.read-all {
  float: right;
  color: #3d8c40;
  cursor: pointer;
  font-size: 12px;
}
.notify-empty {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 20px 0;
}

/* 主体 */
.main {
  flex: 1;
}

/* 页脚 */
.footer {
  background: #2c3e34;
  color: #cfe3d4;
  text-align: center;
  padding: 28px 16px;
  margin-top: 40px;
}
.footer-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
}
.footer-sub {
  font-size: 13px;
  line-height: 22px;
  opacity: 0.8;
}
</style>
