<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelAppointmentApi,
  getExpertAppointmentsApi,
  getMyAppointmentsApi,
  updateAppointmentStatusApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 预约管理：
 * 农户视角 → 我的预约（可取消）
 * 专家视角 → 收到的预约（可确认 / 完成 / 取消）
 */
const userStore = useUserStore()

const list = ref([])
const loading = ref(false)

const statusMap = {
  PENDING: { text: '待确认', type: 'warning' },
  CONFIRMED: { text: '已确认', type: 'primary' },
  DONE: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const api = userStore.role === 'EXPERT' ? getExpertAppointmentsApi : getMyAppointmentsApi
    const res = await api()
    list.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function handleStatus(row, status) {
  const actionMap = { CONFIRMED: '确认该预约', DONE: '标记预约已完成', CANCELLED: '取消该预约' }
  try {
    await ElMessageBox.confirm(`确定要${actionMap[status]}吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await updateAppointmentStatusApi(row.id, status)
  ElMessage.success('处理成功')
  load()
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm('确定取消这个预约吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await cancelAppointmentApi(row.id)
  ElMessage.success('已取消预约')
  load()
}
</script>

<template>
  <div class="page-container">
    <div class="page-banner">
      <h2>📅 预约管理</h2>
      <p v-if="userStore.role === 'EXPERT'">处理农户的预约申请</p>
      <p v-else>查看我预约专家的进度</p>
    </div>

    <el-table v-loading="loading" :data="list" border stripe empty-text="暂无预约记录">
      <el-table-column prop="topic" label="预约主题" min-width="170" show-overflow-tooltip />
      <template v-if="userStore.role === 'EXPERT'">
        <el-table-column label="预约农户" width="110">
          <template #default="{ row }">{{ row.farmerName }}</template>
        </el-table-column>
        <el-table-column label="联系电话" width="130">
          <template #default="{ row }">{{ row.farmerPhone }}</template>
        </el-table-column>
      </template>
      <template v-else>
        <el-table-column label="专家" width="130">
          <template #default="{ row }">{{ row.expertName }}（{{ row.expertTitle }}）</template>
        </el-table-column>
      </template>
      <el-table-column label="期望时间" width="170">
        <template #default="{ row }">{{ row.appointTime }}</template>
      </el-table-column>
      <el-table-column prop="description" label="情况说明" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="userStore.role === 'EXPERT'">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="primary"
              @click="handleStatus(row, 'CONFIRMED')"
            >
              确认
            </el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              size="small"
              type="success"
              @click="handleStatus(row, 'DONE')"
            >
              完成
            </el-button>
            <el-button
              v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'"
              size="small"
              type="danger"
              @click="handleStatus(row, 'CANCELLED')"
            >
              取消
            </el-button>
          </template>
          <template v-else>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="cancel(row)"
            >
              取消预约
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page-banner {
  background: linear-gradient(135deg, #43a047, #1b5e20);
  border-radius: 10px;
  color: #fff;
  padding: 24px 32px;
  margin-bottom: 24px;
}
.page-banner h2 {
  font-size: 22px;
  margin-bottom: 4px;
}
.page-banner p {
  font-size: 13px;
  opacity: 0.9;
}
</style>
