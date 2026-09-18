<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteDemandApi,
  getDemandsApi,
  getMyDemandsApi,
  saveDemandApi,
  updateDemandStatusApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 求购大厅：买家发布/管理采购需求；农户查看需求并联系买家
 */
const router = useRouter()
const userStore = useUserStore()

const tab = ref('hall')            // hall 求购大厅 / mine 我的需求
const demands = ref([])
const total = ref(0)
const page = ref(1)
const size = 8
const loading = ref(false)

// 发布/编辑弹窗
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null,
  title: '',
  category: '粮食',
  quantity: null,
  unit: '斤',
  budget: null,
  deadline: '',
  description: '',
})

// 联系买家弹窗（农户）
const contactVisible = ref(false)
const contact = ref(null)

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    if (tab.value === 'mine') {
      const res = await getMyDemandsApi()
      demands.value = res.data || []
      total.value = demands.value.length
      return
    }
    const res = await getDemandsApi({ page: page.value, size })
    demands.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function changeTab(t) {
  tab.value = t
  page.value = 1
  if (t === 'mine' && !userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadList()
}

function openEdit(row) {
  form.id = row ? row.id : null
  form.title = row?.title || ''
  form.category = row?.category || '粮食'
  form.quantity = row?.quantity ?? null
  form.unit = row?.unit || '斤'
  form.budget = row?.budget ?? null
  form.deadline = row?.deadline || ''
  form.description = row?.description || ''
  dialogVisible.value = true
}

async function save() {
  if (!form.title || form.quantity === null) {
    return ElMessage.warning('请填写需求标题和数量')
  }
  saving.value = true
  try {
    await saveDemandApi(form)
    ElMessage.success(form.id ? '修改成功' : '发布成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateDemandStatusApi(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '需求已重新开启' : '需求已结束')
  loadList()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDemandApi(row.id)
  ElMessage.success('删除成功')
  loadList()
}

/** 农户查看买家联系方式 */
function openContact(d) {
  contact.value = d
  contactVisible.value = true
}
</script>

<template>
  <div class="page-container">
    <div class="page-banner">
      <h2>📋 求购大厅</h2>
      <p>买家发布采购需求，农户查看并联系买家，产销对接更直接</p>
    </div>

    <!-- 切换栏 -->
    <div class="top-bar">
      <el-radio-group v-model="tab" @change="changeTab">
        <el-radio-button value="hall">求购大厅</el-radio-button>
        <el-radio-button v-if="userStore.role === 'BUYER'" value="mine">我的需求</el-radio-button>
      </el-radio-group>
      <el-button v-if="userStore.role === 'BUYER'" type="primary" @click="openEdit(null)">
        ✍️ 发布求购需求
      </el-button>
    </div>

    <!-- 需求列表 -->
    <div v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="8" v-for="d in demands" :key="d.id">
          <el-card shadow="hover" class="demand-card">
            <div class="d-head">
              <span class="d-title">{{ d.title }}</span>
              <el-tag :type="d.status === 1 ? 'success' : 'info'" size="small">
                {{ d.status === 1 ? '进行中' : '已结束' }}
              </el-tag>
            </div>
            <div class="d-tags">
              <el-tag size="small" type="warning">{{ d.category }}</el-tag>
              <el-tag size="small" type="info">数量 {{ d.quantity }} {{ d.unit }}</el-tag>
              <el-tag v-if="d.budget" size="small" type="danger">预算 ¥{{ d.budget }}</el-tag>
            </div>
            <div class="d-desc">{{ d.description || '（无补充说明）' }}</div>
            <div class="d-meta">
              <span>{{ d.buyerName }} · 期望 {{ d.deadline || '面议' }} 前供货</span>
            </div>
            <div class="d-actions">
              <template v-if="tab === 'hall'">
                <el-button
                  v-if="userStore.role === 'FARMER'"
                  size="small"
                  type="primary"
                  @click="openContact(d)"
                >
                  📞 联系买家
                </el-button>
                <el-button
                  v-else-if="!userStore.isLogin"
                  size="small"
                  type="primary"
                  @click="router.push('/login')"
                >
                  登录后查看
                </el-button>
              </template>
              <template v-else>
                <el-button size="small" type="primary" link @click="openEdit(d)">编辑</el-button>
                <el-button size="small" :type="d.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(d)">
                  {{ d.status === 1 ? '结束需求' : '重新开启' }}
                </el-button>
                <el-button size="small" type="danger" link @click="remove(d)">删除</el-button>
              </template>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!demands.length" description="暂无求购需求" />

      <div v-if="tab === 'hall'" class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 发布/编辑需求弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑需求' : '发布求购需求'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="需求标题">
          <el-input v-model="form.title" placeholder="如：求购优质小麦 5000 斤" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="品类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in ['水果', '蔬菜', '粮食', '禽畜', '水产', '其他']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="单位">
          <el-select v-model="form.unit" style="width: 180px">
            <el-option v-for="u in ['斤', '袋', '箱', '瓶', '枚', '件', '吨']" :key="u" :label="u" :value="u" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算">
          <el-input-number v-model="form.budget" :min="0" :step="100" controls-position="right" style="width: 180px" />
          <span class="form-tip">元（可不填）</span>
        </el-form-item>
        <el-form-item label="期望时间">
          <el-date-picker v-model="form.deadline" type="date" placeholder="期望供货时间" style="width: 180px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="需求详情">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="品质要求、包装要求等" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">发布</el-button>
      </template>
    </el-dialog>

    <!-- 联系买家弹窗 -->
    <el-dialog v-model="contactVisible" title="联系买家" width="420px">
      <div v-if="contact" class="contact-box">
        <div class="c-row"><span>买家</span><b>{{ contact.buyerName }}</b></div>
        <div class="c-row"><span>联系电话</span><b class="c-phone">{{ contact.buyerPhone || '未填写' }}</b></div>
        <div class="c-row"><span>需求</span><b>{{ contact.title }}（{{ contact.quantity }} {{ contact.unit }}）</b></div>
        <div class="c-tip">💡 建议先电话沟通品质、价格、物流等细节，达成意向后线下交易或平台下单</div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-banner {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border-radius: 10px;
  color: #fff;
  padding: 24px 32px;
  margin-bottom: 20px;
}
.page-banner h2 {
  font-size: 22px;
  margin-bottom: 4px;
}
.page-banner p {
  font-size: 13px;
  opacity: 0.9;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}

.demand-card {
  margin-bottom: 16px;
}
.d-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.d-title {
  font-size: 15px;
  font-weight: 700;
}
.d-tags {
  display: flex;
  gap: 6px;
  margin: 10px 0;
  flex-wrap: wrap;
}
.d-desc {
  color: #606266;
  font-size: 13px;
  line-height: 20px;
  height: 40px;
  overflow: hidden;
}
.d-meta {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 8px;
}
.d-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 10px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.contact-box {
  font-size: 14px;
}
.c-row {
  display: flex;
  gap: 12px;
  line-height: 30px;
}
.c-row span {
  color: #909399;
  width: 70px;
  flex-shrink: 0;
}
.c-phone {
  color: #409eff;
  font-size: 18px;
}
.c-tip {
  margin-top: 12px;
  background: #ecf7ed;
  color: #3d8c40;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 20px;
}
</style>
