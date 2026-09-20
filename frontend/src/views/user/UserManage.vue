<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsersApi, getRoleCountsApi, toggleUserStatusApi } from '@/api'

/**
 * 用户管理（管理员）：用户列表、搜索、启用/禁用
 */
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const keyword = ref('')
const loading = ref(false)
const counts = ref([])

const roleNameMap = {
  FARMER: '农户',
  BANK: '银行',
  BUYER: '买家',
  EXPERT: '技术专家',
  ADMIN: '管理员',
}

onMounted(() => {
  load()
  loadCounts()
})

async function load() {
  loading.value = true
  try {
    const res = await getAdminUsersApi({ page: page.value, size, keyword: keyword.value.trim() || undefined })
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function loadCounts() {
  try {
    const res = await getRoleCountsApi()
    counts.value = res.data || []
  } catch (e) {
    // 忽略统计失败
  }
}

function search() {
  page.value = 1
  load()
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.realName || row.username}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await toggleUserStatusApi(row.id, newStatus)
  ElMessage.success(`已${action}`)
  load()
}
</script>

<template>
  <div>
    <h3 class="page-title">🛠️ 用户管理</h3>

    <div class="counts">
      <div class="count-item"><b>{{ counts[0] || 0 }}</b> 农户</div>
      <div class="count-item"><b>{{ counts[1] || 0 }}</b> 买家</div>
      <div class="count-item"><b>{{ counts[2] || 0 }}</b> 专家</div>
      <div class="count-item"><b>{{ counts[3] || 0 }}</b> 银行</div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索账号或姓名"
        clearable
        style="width: 240px"
        @keyup.enter="search"
      />
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="username" label="账号" width="130" />
      <el-table-column label="姓名" width="160">
        <template #default="{ row }">{{ row.realName || '—' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="success">{{ roleNameMap[row.role] || row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="电话" width="140">
        <template #default="{ row }">{{ row.phone || '—' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="load"
      />
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.counts {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.count-item {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 8px 18px;
  font-size: 13px;
  color: #606266;
}
.count-item b {
  color: #3d8c40;
  font-size: 18px;
  margin-right: 4px;
}
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
