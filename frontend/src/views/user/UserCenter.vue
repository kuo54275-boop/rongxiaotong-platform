<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 个人信息中心：左侧菜单（按角色显示不同子功能）+ 右侧内容区
 */
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const roleNameMap = {
  FARMER: '农户',
  BANK: '银行',
  BUYER: '买家',
  EXPERT: '技术专家',
  ADMIN: '管理员',
}

const role = computed(() => userStore.role)

/** 菜单项：path 为空表示跳到外部页面 */
const menuItems = computed(() => {
  const common = [
    { label: '👤 基础信息', path: '/user' },
    { label: '🔒 修改密码', path: '/user/password' },
    { label: '📍 收货地址', path: '/user/address' },
    { label: '📝 我的发布', path: '/user/posts' },
  ]
  const roleItems = {
    FARMER: [
      { label: '🏦 我的融资', path: '/user/finance' },
      { label: '📦 我的订单', path: '/market/orders' },
      { label: '📅 我的预约', path: '/expert/appointments' },
      { label: '🛒 商品管理', path: '/market?tab=manage' },
    ],
    BUYER: [
      { label: '📦 我的订单', path: '/market/orders' },
      { label: '📋 我的求购', path: '/market/demands?tab=mine' },
    ],
    EXPERT: [
      { label: '🎓 专家信息管理', path: '/user/expert' },
      { label: '📅 预约管理', path: '/expert/appointments' },
      { label: '📚 知识管理', path: '/expert/knowledge?tab=manage' },
    ],
    BANK: [
      { label: '🏦 融资管理后台', path: '/bank' },
    ],
    ADMIN: [
      { label: '🛠️ 用户管理', path: '/user/admin' },
    ],
  }
  return [...common, ...(roleItems[role.value] || [])]
})

function onMenu(path) {
  if (path === '/bank') {
    // 银行后台在后续阶段上线
    router.push('/')
    return
  }
  router.push(path)
}
</script>

<template>
  <div class="page-container">
    <div class="user-layout">
      <!-- 左侧：用户信息 + 菜单 -->
      <div class="left">
        <div class="user-summary">
          <img :src="userStore.avatar" class="summary-avatar" alt="头像" />
          <div class="summary-name">{{ userStore.realName }}</div>
          <el-tag size="small" type="success">{{ roleNameMap[role] || role }}</el-tag>
          <div class="summary-username">@{{ userStore.userInfo?.username }}</div>
        </div>
        <div
          v-for="m in menuItems"
          :key="m.label"
          class="menu-item"
          :class="{ active: route.path === m.path.split('?')[0] }"
          @click="onMenu(m.path)"
        >
          {{ m.label }}
        </div>
      </div>

      <!-- 右侧：内容区 -->
      <div class="right">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.left {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}
.user-summary {
  text-align: center;
  padding: 24px 12px 16px;
  background: linear-gradient(180deg, #ecf7ed 0%, #ffffff 100%);
}
.summary-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  margin-bottom: 8px;
}
.summary-name {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
}
.summary-username {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 4px;
}
.menu-item {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}
.menu-item:hover {
  background: #f5f7fa;
  color: #3d8c40;
}
.menu-item.active {
  background: #ecf7ed;
  color: #3d8c40;
  font-weight: 600;
  border-left-color: #4caf50;
}

.right {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  min-height: 500px;
}
</style>
