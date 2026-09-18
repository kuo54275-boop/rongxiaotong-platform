<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 前台整体布局：顶部导航 + 页面主体 + 页脚
 * 除登录页外，所有前台页面都套用这个布局
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

function onMenu(path) {
  if (path === '/') {
    router.push(path)
    return
  }
  ElMessage.info('该模块正在开发中，敬请期待！')
}

function onUserCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (cmd === 'info') {
    ElMessage.info('个人中心开发中，敬请期待！')
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
            :class="{ active: m.path === '/' && route.path === '/' }"
            @click="onMenu(m.path)"
          >
            {{ m.label }}
          </span>
        </nav>

        <div class="user-area">
          <template v-if="userStore.isLogin">
            <el-dropdown @command="onUserCommand">
              <span class="user-info">
                <img :src="userStore.avatar" class="avatar" alt="头像" />
                {{ userStore.realName }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="info">个人中心</el-dropdown-item>
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
