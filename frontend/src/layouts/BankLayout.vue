<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBankStatsApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ref } from 'vue'

/**
 * 银行侧后台布局：左侧边栏 + 顶栏 + 内容区
 * 仅银行角色可进入，其他角色被送回首页
 */
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pendingCount = ref(0)

const menus = [
  { label: '📊 工作台', path: '/bank' },
  { label: '🧠 智能匹配农户', path: '/bank/match' },
  { label: '✅ 贷款审批', path: '/bank/approve' },
]

onMounted(async () => {
  // 非银行角色不允许进入后台
  if (userStore.role !== 'BANK') {
    ElMessage.warning('只有银行账号可以进入融资管理后台')
    router.push('/')
    return
  }
  try {
    const res = await getBankStatsApi()
    pendingCount.value = res.data?.pendingCount || 0
  } catch (e) {
    // 拦截器已提示
  }
})

function go(path) {
  router.push(path)
}

function logout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

const isBank = computed(() => userStore.role === 'BANK')
</script>

<template>
  <div class="bank-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="side-logo" @click="router.push('/')">
        🏦 融小通
        <div class="side-sub">银行融资管理后台</div>
      </div>
      <div
        v-for="m in menus"
        :key="m.path"
        class="side-item"
        :class="{ active: route.path === m.path }"
        @click="go(m.path)"
      >
        {{ m.label }}
        <el-badge
          v-if="m.path === '/bank/approve' && pendingCount > 0"
          :value="pendingCount"
          class="side-badge"
        />
      </div>
      <div class="side-foot">
        <div class="bank-name">{{ userStore.realName }}</div>
        <el-button size="small" text @click="logout">退出登录</el-button>
      </div>
    </aside>

    <!-- 内容区 -->
    <main class="bank-main" v-if="isBank">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.bank-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f4f1;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #1f3a2d;
  color: #cfd8cf;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}
.side-logo {
  padding: 22px 20px;
  color: #fff;
  font-size: 19px;
  font-weight: 700;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.side-sub {
  font-size: 12px;
  font-weight: 400;
  color: #9db5a4;
  margin-top: 4px;
}
.side-item {
  padding: 14px 22px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  display: flex;
  align-items: center;
}
.side-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.side-item.active {
  background: rgba(76, 175, 80, 0.18);
  color: #7ecb83;
  font-weight: 600;
  border-left-color: #4caf50;
}
.side-badge {
  margin-left: 8px;
}
.side-foot {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.bank-name {
  color: #fff;
  font-size: 13px;
  margin-bottom: 6px;
}
.bank-main {
  flex: 1;
  padding: 24px;
  min-width: 0;
}
</style>
