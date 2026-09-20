<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getLoanProductsApi,
  getMatchRecordsApi,
  markContactedApi,
  matchFarmersApi,
} from '@/api'

/**
 * 银行侧：智能匹配农户
 * 选择贷款产品 → 算法按 信用分40% + 种植规模30% + 历史履约30% 推荐 Top10
 */
const products = ref([])
const selectedProduct = ref(null)
const results = ref([])
const records = ref([])
const matching = ref(false)
const tab = ref('match')          // match 匹配结果 / records 匹配记录
const lastProductName = ref('')

onMounted(async () => {
  const res = await getLoanProductsApi()
  products.value = res.data || []
  loadRecords()
})

async function doMatch() {
  if (!selectedProduct.value) {
    return ElMessage.warning('请先选择贷款产品')
  }
  matching.value = true
  try {
    const res = await matchFarmersApi(selectedProduct.value)
    results.value = res.data || []
    lastProductName.value = products.value.find((p) => p.id === selectedProduct.value)?.name || ''
    ElMessage.success(`匹配完成，共推荐 ${results.value.length} 位农户`)
    loadRecords()
  } catch (e) {
    // 拦截器已提示
  } finally {
    matching.value = false
  }
}

async function loadRecords() {
  try {
    const res = await getMatchRecordsApi()
    records.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  }
}

async function contact(vo) {
  if (!vo.recordId) {
    return
  }
  await markContactedApi(vo.recordId)
  ElMessage.success(`已标记联系 ${vo.realName}`)
  loadRecords()
}
</script>

<template>
  <div>
    <div class="head">
      <h2>🧠 智能匹配农户</h2>
      <div class="head-right">
        <el-select v-model="selectedProduct" placeholder="选择贷款产品" style="width: 220px">
          <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button type="primary" :loading="matching" @click="doMatch">开始匹配</el-button>
      </div>
    </div>

    <div class="algo-tip">
      🧮 匹配算法：<b>信用分（40%）</b> + <b>种植规模（30%）</b> + <b>历史履约（30%）</b>，
      综合打分排序，推荐最适合该产品的农户
    </div>

    <el-tabs v-model="tab">
      <!-- 匹配结果 -->
      <el-tab-pane label="匹配结果" name="match">
        <el-empty v-if="!results.length" description="选择产品后点击「开始匹配」，算法将推荐最适合的农户" />
        <el-table v-else :data="results" border stripe>
          <el-table-column label="排名" width="70">
            <template #default="{ $index }">
              <el-tag :type="$index === 0 ? 'success' : 'info'" size="small">Top{{ $index + 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="农户" width="140">
            <template #default="{ row }">
              <div class="farmer-cell">
                <div class="farmer-name">{{ row.realName }}</div>
                <div class="farmer-phone">{{ row.phone }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="地区" width="170">
            <template #default="{ row }">{{ row.address }}</template>
          </el-table-column>
          <el-table-column label="经营情况" width="130">
            <template #default="{ row }">{{ row.crop || '—' }} · {{ row.landArea }} 亩</template>
          </el-table-column>
          <el-table-column label="信用分" width="80">
            <template #default="{ row }">
              <b :class="row.creditScore >= 85 ? 'good' : 'normal'">{{ row.creditScore }}</b>
            </template>
          </el-table-column>
          <el-table-column label="综合得分" width="110">
            <template #default="{ row }">
              <span class="score">{{ row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column label="推荐理由" min-width="180">
            <template #default="{ row }">{{ row.reason }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="contact(row)">📞 标记已联系</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 匹配记录 -->
      <el-tab-pane :label="`匹配记录（${records.length}）`" name="records">
        <el-table :data="records" border stripe empty-text="还没有匹配记录">
          <el-table-column label="农户" width="120">
            <template #default="{ row }">{{ row.realName }}</template>
          </el-table-column>
          <el-table-column label="联系电话" width="140">
            <template #default="{ row }">{{ row.phone || '—' }}</template>
          </el-table-column>
          <el-table-column label="匹配产品" width="150">
            <template #default="{ row }">{{ row.reason }}</template>
          </el-table-column>
          <el-table-column label="匹配分" width="90">
            <template #default="{ row }">{{ row.score }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.address === '已联系' ? 'success' : 'info'" size="small">
                {{ row.address }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.head h2 {
  font-size: 20px;
}
.head-right {
  display: flex;
  gap: 10px;
}
.algo-tip {
  background: #eef7ef;
  color: #3d8c40;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 13px;
  margin-bottom: 14px;
}
.farmer-cell .farmer-name {
  font-weight: 600;
  font-size: 14px;
}
.farmer-cell .farmer-phone {
  color: #c0c4cc;
  font-size: 12px;
}
.score {
  color: #4caf50;
  font-size: 20px;
  font-weight: 700;
}
.good {
  color: #4caf50;
}
.normal {
  color: #e6a23c;
}
</style>
