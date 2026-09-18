<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  applyLoanApi,
  getLoanProductsApi,
  getMyLoansApi,
  matchPartnersApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 融资服务页：
 * 1. 贷款产品一览
 * 2. 贷款申请（第一步填申请信息 → 第二步智能匹配联合贷款人 → 提交）
 * 3. 我的融资申请列表
 */
const router = useRouter()
const userStore = useUserStore()

const products = ref([])
const myLoans = ref([])
const loading = ref(false)

// 申请弹窗状态
const dialogVisible = ref(false)
const step = ref(1)          // 1 填写申请 / 2 选择联合贷款人
const submitting = ref(false)
const matching = ref(false)
const currentProduct = ref(null)
const form = reactive({ amount: null, term: null, purpose: '' })
const partners = ref([])
const selectedPartner = ref(null)

const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await getLoanProductsApi()
    products.value = res.data || []
    // 农户登录后顺带加载自己的申请列表
    if (userStore.isLogin && userStore.role === 'FARMER') {
      const my = await getMyLoansApi()
      myLoans.value = my.data || []
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

/** 点击"申请贷款"：校验登录和角色，打开弹窗 */
function openApply(p) {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再申请贷款')
    router.push('/login')
    return
  }
  if (userStore.role !== 'FARMER') {
    ElMessage.warning('只有农户可以申请贷款')
    return
  }
  currentProduct.value = p
  form.amount = p.minAmount
  form.term = Math.min(p.termMonths, 12)
  form.purpose = ''
  step.value = 1
  selectedPartner.value = null
  partners.value = []
  dialogVisible.value = true
}

/** 第一步 → 第二步：触发智能匹配 */
async function nextStep() {
  const p = currentProduct.value
  if (!form.amount || form.amount < p.minAmount || form.amount > p.maxAmount) {
    return ElMessage.warning(`申请金额须在 ${p.minAmount} ~ ${p.maxAmount} 元之间`)
  }
  if (!form.term || form.term <= 0 || form.term > p.termMonths) {
    return ElMessage.warning(`申请期限须在 1 ~ ${p.termMonths} 个月之间`)
  }
  step.value = 2
  matching.value = true
  try {
    const res = await matchPartnersApi()
    partners.value = res.data || []
  } catch (e) {
    step.value = 1
  } finally {
    matching.value = false
  }
}

/** 提交申请（可选联合贷款人） */
async function submit() {
  submitting.value = true
  try {
    await applyLoanApi({
      productId: currentProduct.value.id,
      amount: form.amount,
      term: form.term,
      purpose: form.purpose,
      partnerId: selectedPartner.value,
    })
    ElMessage.success('申请提交成功，等待银行审批！')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- ===== 页面标题 ===== -->
    <div class="page-banner">
      <h2>🏦 融资服务</h2>
      <p>银行惠农产品任你选，智能匹配联合贷款人，让融资更简单</p>
    </div>

    <!-- ===== 贷款产品一览 ===== -->
    <el-row :gutter="16">
      <el-col :span="8" v-for="p in products" :key="p.id">
        <el-card shadow="hover" class="product-card">
          <div class="p-name">{{ p.name }}</div>
          <div class="p-rate">
            <span class="rate-label">年利率</span>
            <span class="rate-num">{{ p.rate }}%</span>
          </div>
          <div class="p-info">
            <div class="info-item">
              <span>贷款额度</span>
              <b>{{ (p.minAmount / 10000).toFixed(1) }}~{{ (p.maxAmount / 10000).toFixed(1) }} 万元</b>
            </div>
            <div class="info-item">
              <span>贷款期限</span>
              <b>最长 {{ p.termMonths }} 个月</b>
            </div>
          </div>
          <div class="p-desc">{{ p.description }}</div>
          <el-button type="primary" class="apply-btn" @click="openApply(p)">
            申请贷款
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 我的融资申请 ===== -->
    <section v-if="userStore.isLogin && userStore.role === 'FARMER'" class="my-loans">
      <div class="section-head">
        <h3>📋 我的融资申请</h3>
      </div>
      <el-table :data="myLoans" border stripe empty-text="还没有申请记录，快去申请第一笔贷款吧">
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
    </section>

    <!-- ===== 申请贷款弹窗（两步） ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="'申请贷款 · ' + (currentProduct?.name || '')"
      width="680px"
      :close-on-click-modal="false"
      @closed="step = 1"
    >
      <el-steps :active="step" finish-status="success" align-center>
        <el-step title="填写申请信息" />
        <el-step title="智能匹配联合贷款人" />
      </el-steps>

      <!-- 第一步：申请表单 -->
      <div v-if="step === 1" class="step-body">
        <el-form :model="form" label-width="90px">
          <el-form-item label="贷款产品">
            <el-input :model-value="currentProduct?.name" disabled />
          </el-form-item>
          <el-form-item label="申请金额">
            <el-input-number
              v-model="form.amount"
              :min="currentProduct?.minAmount"
              :max="currentProduct?.maxAmount"
              :step="1000"
              controls-position="right"
              style="width: 200px"
            />
            <span class="form-tip">元（额度 {{ currentProduct?.minAmount }}~{{ currentProduct?.maxAmount }}）</span>
          </el-form-item>
          <el-form-item label="申请期限">
            <el-select v-model="form.term" style="width: 200px">
              <el-option
                v-for="m in currentProduct?.termMonths"
                :key="m"
                :label="m + ' 个月'"
                :value="m"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="贷款用途">
            <el-input
              v-model="form.purpose"
              type="textarea"
              :rows="3"
              placeholder="请说明贷款用途，如：购置农机具、扩大种植规模等"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div class="step-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="nextStep">下一步：智能匹配 &gt;</el-button>
        </div>
      </div>

      <!-- 第二步：匹配结果 -->
      <div v-else class="step-body" v-loading="matching">
        <div class="match-tip">
          💡 算法根据<b>信用分（40%）、还款能力（30%）、地域相近（20%）、历史合作（10%）</b>为你推荐联合贷款人
        </div>

        <div v-if="partners.length" class="partner-list">
          <div
            v-for="pt in partners"
            :key="pt.userId"
            class="partner-card"
            :class="{ selected: selectedPartner === pt.userId }"
            @click="selectedPartner = pt.userId"
          >
            <div class="partner-head">
              <div class="partner-name">
                {{ pt.realName }}
                <el-tag size="small" type="success">{{ pt.address }}</el-tag>
              </div>
              <div class="partner-score">
                匹配度
                <b>{{ pt.score }}</b>
              </div>
            </div>
            <div class="partner-meta">
              主营 {{ pt.crop || '—' }} · 种植面积 {{ pt.landArea || '—' }} 亩 · 信用分 {{ pt.creditPoint }}
            </div>
            <div class="partner-dims">
              <div class="dim">
                <span>信用</span>
                <el-progress :percentage="pt.creditPoint" :stroke-width="8" color="#4caf50" />
              </div>
              <div class="dim">
                <span>还款能力</span>
                <el-progress :percentage="pt.abilityPoint" :stroke-width="8" color="#409eff" />
              </div>
              <div class="dim">
                <span>地域</span>
                <el-progress :percentage="pt.regionPoint" :stroke-width="8" color="#e6a23c" />
              </div>
              <div class="dim">
                <span>历史合作</span>
                <el-progress :percentage="pt.historyPoint" :stroke-width="8" color="#909399" />
              </div>
            </div>
            <div class="partner-reason">推荐理由：{{ pt.reason }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无可匹配的联合贷款人，可跳过直接提交" :image-size="80" />

        <div class="step-actions">
          <el-button @click="step = 1">上一步</el-button>
          <el-button @click="selectedPartner = null">不选联合人</el-button>
          <el-button type="primary" :loading="submitting" @click="submit">
            {{ selectedPartner ? '提交申请（含联合人）' : '提交申请' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-banner {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border-radius: 10px;
  color: #fff;
  padding: 28px 32px;
  margin-bottom: 24px;
}
.page-banner h2 {
  font-size: 24px;
  margin-bottom: 6px;
}
.page-banner p {
  font-size: 14px;
  opacity: 0.9;
}

/* 产品卡 */
.product-card {
  border-top: 3px solid #4caf50;
  margin-bottom: 16px;
}
.p-name {
  font-size: 18px;
  font-weight: 700;
}
.p-rate {
  margin: 12px 0;
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.rate-label {
  color: #909399;
  font-size: 13px;
}
.rate-num {
  color: #f56c6c;
  font-size: 26px;
  font-weight: 700;
}
.p-info {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px 14px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
  line-height: 24px;
}
.p-desc {
  color: #909399;
  font-size: 13px;
  line-height: 20px;
  margin: 12px 0;
  height: 40px;
  overflow: hidden;
}
.apply-btn {
  width: 100%;
}

/* 我的申请 */
.my-loans {
  margin-top: 12px;
}
.section-head {
  margin-bottom: 14px;
}
.section-head h3 {
  font-size: 18px;
}

/* 弹窗 */
.step-body {
  margin-top: 24px;
}
.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
.step-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 匹配结果 */
.match-tip {
  background: #ecf7ed;
  color: #3d8c40;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
  margin-bottom: 14px;
}
.partner-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.partner-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.partner-card:hover {
  border-color: #b9e3ba;
}
.partner-card.selected {
  border-color: #4caf50;
  background: #ecf7ed;
}
.partner-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.partner-name {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.partner-score {
  color: #909399;
  font-size: 13px;
}
.partner-score b {
  color: #4caf50;
  font-size: 22px;
  margin-left: 4px;
}
.partner-meta {
  color: #909399;
  font-size: 12px;
  margin: 8px 0;
}
.partner-dims {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px 24px;
}
.dim {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}
.dim span {
  flex-shrink: 0;
  width: 58px;
}
.partner-reason {
  color: #3d8c40;
  font-size: 12px;
  margin-top: 10px;
}
</style>
