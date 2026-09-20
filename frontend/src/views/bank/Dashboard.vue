<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getBankStatsApi } from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 银行工作台：统计数字 + 快捷入口（每 10 秒自动刷新统计）
 */
const router = useRouter()
const userStore = useUserStore()

const stats = ref({})
const loading = ref(true)
let timer = null

onMounted(async () => {
  await load()
  timer = setInterval(load, 10000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

async function load() {
  try {
    const res = await getBankStatsApi()
    stats.value = res.data || {}
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

const cards = [
  { key: 'pendingCount', label: '待审批申请', icon: '⏳', color: '#e6a23c' },
  { key: 'approvedCount', label: '已通过', icon: '✅', color: '#4caf50' },
  { key: 'rejectedCount', label: '已拒绝', icon: '❌', color: '#f56c6c' },
  { key: 'matchCount', label: '已匹配农户', icon: '🧠', color: '#409eff' },
  { key: 'productCount', label: '在售贷款产品', icon: '🏦', color: '#909399' },
]
</script>

<template>
  <div v-loading="loading">
    <div class="welcome">
      <h2>你好，{{ userStore.realName }}</h2>
      <p>这里是银行融资管理后台，可智能匹配农户并处理贷款审批</p>
    </div>

    <div class="cards">
      <div v-for="c in cards" :key="c.key" class="card">
        <div class="card-icon" :style="{ color: c.color }">{{ c.icon }}</div>
        <div>
          <div class="card-num">{{ stats[c.key] ?? 0 }}</div>
          <div class="card-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <div class="quick">
      <div class="quick-title">快捷操作</div>
      <div class="quick-btns">
        <el-button type="primary" size="large" @click="router.push('/bank/match')">
          🧠 智能匹配农户
        </el-button>
        <el-button type="warning" size="large" @click="router.push('/bank/approve')">
          ⏳ 处理待审批申请
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.welcome {
  background: linear-gradient(135deg, #2e5d43, #1f3a2d);
  border-radius: 10px;
  color: #fff;
  padding: 24px 28px;
  margin-bottom: 20px;
}
.welcome h2 {
  font-size: 22px;
  margin-bottom: 6px;
}
.welcome p {
  font-size: 13px;
  opacity: 0.85;
}

.cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 24px;
}
.card {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-icon {
  font-size: 30px;
}
.card-num {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}
.card-label {
  color: #909399;
  font-size: 12px;
}

.quick {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
}
.quick-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 14px;
}
.quick-btns {
  display: flex;
  gap: 12px;
}
</style>
