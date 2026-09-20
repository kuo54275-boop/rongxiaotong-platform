<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyLoansApi } from '@/api'

/**
 * 我的融资：贷款申请列表
 */
const router = useRouter()
const list = ref([])
const loading = ref(false)

const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyLoansApi()
    list.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <div class="head">
      <h3 class="page-title">🏦 我的融资</h3>
      <el-button type="primary" @click="router.push('/finance')">➕ 申请新贷款</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe empty-text="还没有融资申请记录">
      <el-table-column prop="productName" label="贷款产品" width="140" />
      <el-table-column label="申请金额">
        <template #default="{ row }">{{ row.amount }} 元</template>
      </el-table-column>
      <el-table-column label="期限">
        <template #default="{ row }">{{ row.term }} 个月</template>
      </el-table-column>
      <el-table-column prop="purpose" label="用途" min-width="160" show-overflow-tooltip />
      <el-table-column label="联合贷款人" width="110">
        <template #default="{ row }">{{ row.partnerName || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
            {{ statusMap[row.status]?.text || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="170" />
    </el-table>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
  flex: 1;
  margin-bottom: 20px;
}
</style>
