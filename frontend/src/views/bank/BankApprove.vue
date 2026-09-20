<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveApplicationApi, getBankApplicationsApi, rejectApplicationApi } from '@/api'

/**
 * 银行侧：贷款审批（待审批 / 已通过 / 已拒绝）
 */
const tab = ref('PENDING')
const list = ref([])
const loading = ref(false)

const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getBankApplicationsApi(tab.value)
    list.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function changeTab(t) {
  tab.value = t
  load()
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(
      `确定通过「${row.farmerName}」的 ${row.amount} 元贷款申请吗？`,
      '审批通过',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }
  await approveApplicationApi(row.id)
  ElMessage.success('已通过，农户会收到通知')
  load()
}

async function reject(row) {
  try {
    await ElMessageBox.confirm(
      `确定拒绝「${row.farmerName}」的 ${row.amount} 元贷款申请吗？`,
      '审批拒绝',
      { type: 'warning' },
    )
  } catch (e) {
    return
  }
  await rejectApplicationApi(row.id)
  ElMessage.success('已拒绝，农户会收到通知')
  load()
}
</script>

<template>
  <div>
    <div class="head">
      <h2>✅ 贷款审批</h2>
    </div>

    <el-tabs v-model="tab" @tab-change="changeTab">
      <el-tab-pane label="待审批" name="PENDING" />
      <el-tab-pane label="已通过" name="APPROVED" />
      <el-tab-pane label="已拒绝" name="REJECTED" />
    </el-tabs>

    <el-table v-loading="loading" :data="list" border stripe empty-text="暂无数据">
      <el-table-column label="申请农户" width="120">
        <template #default="{ row }">
          <div>
            <div class="f-name">{{ row.farmerName }}</div>
            <div class="f-phone">{{ row.farmerPhone }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="贷款产品" width="130" />
      <el-table-column label="申请金额" width="110">
        <template #default="{ row }">
          <b class="amount">{{ row.amount }}</b> 元
        </template>
      </el-table-column>
      <el-table-column label="期限" width="90">
        <template #default="{ row }">{{ row.term }} 个月</template>
      </el-table-column>
      <el-table-column prop="purpose" label="贷款用途" min-width="150" show-overflow-tooltip />
      <el-table-column label="联合贷款人" width="100">
        <template #default="{ row }">{{ row.partnerName || '—' }}</template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(row)">拒绝</el-button>
          </template>
          <span v-else class="done-tip">{{ row.approveTime }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.head {
  margin-bottom: 10px;
}
.head h2 {
  font-size: 20px;
}
.f-name {
  font-weight: 600;
  font-size: 14px;
}
.f-phone {
  color: #c0c4cc;
  font-size: 12px;
}
.amount {
  color: #f56c6c;
  font-size: 15px;
}
.done-tip {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
