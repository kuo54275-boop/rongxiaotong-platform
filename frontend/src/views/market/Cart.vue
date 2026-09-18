<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createOrderApi,
  getAddressListApi,
  getCartApi,
  removeCartApi,
  saveAddressApi,
  updateCartQuantityApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 购物车：改数量、删除、结算（选收货地址 → 生成订单）
 */
const router = useRouter()
const userStore = useUserStore()

const items = ref([])
const loading = ref(false)
const selected = ref([])          // 选中的购物车条目 id

// 结算弹窗
const checkoutVisible = ref(false)
const addresses = ref([])
const selectedAddress = ref(null)
const submitting = ref(false)
const addrFormVisible = ref(false)
const addrForm = reactive({ receiver: '', phone: '', region: '', detail: '', isDefault: 0 })

const totalAmount = computed(() => {
  return items.value
    .filter((i) => selected.value.includes(i.id))
    .reduce((sum, i) => sum + i.subtotal, 0)
    .toFixed(2)
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getCartApi()
    items.value = res.data || []
    selected.value = items.value.map((i) => i.id)   // 默认全选
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function onSelectionChange(val) {
  selected.value = val
}

async function changeQuantity(row, qty) {
  try {
    await updateCartQuantityApi(row.id, qty)
    load()
  } catch (e) {
    // 失败时重新加载恢复原值
    load()
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定移除《${row.productName}》吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await removeCartApi(row.id)
  ElMessage.success('已移除')
  load()
}

/** 打开结算弹窗：加载地址列表 */
async function openCheckout() {
  if (!selected.value.length) {
    return ElMessage.warning('请先勾选要结算的商品')
  }
  const res = await getAddressListApi()
  addresses.value = res.data || []
  const def = addresses.value.find((a) => a.isDefault === 1)
  selectedAddress.value = def ? def.id : addresses.value[0]?.id || null
  checkoutVisible.value = true
}

async function submitOrder() {
  if (!selectedAddress.value) {
    return ElMessage.warning('请选择收货地址，或新增一个')
  }
  submitting.value = true
  try {
    await createOrderApi({ addressId: selectedAddress.value })
    ElMessage.success('下单成功！请到订单页付款')
    checkoutVisible.value = false
    router.push('/market/orders')
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function saveAddr() {
  if (!addrForm.receiver || !addrForm.phone || !addrForm.region || !addrForm.detail) {
    return ElMessage.warning('请完整填写收货信息')
  }
  await saveAddressApi(addrForm)
  ElMessage.success('地址已保存')
  addrFormVisible.value = false
  const res = await getAddressListApi()
  addresses.value = res.data || []
  selectedAddress.value = addresses.value[addresses.value.length - 1]?.id || null
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-banner">
      <h2>🛒 我的购物车</h2>
      <p>勾选商品 → 结算 → 选择收货地址 → 生成订单</p>
    </div>

    <template v-if="items.length">
      <el-table :data="items" @selection-change="onSelectionChange" border stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="t-product" @click="router.push(`/market/product/${row.productId}`)">
              <img :src="row.cover" class="t-cover" alt="图" />
              <div>
                <div class="t-name">{{ row.productName }}</div>
                <div class="t-farmer">{{ row.farmerName }} 家 · 库存 {{ row.stock }} {{ row.unit }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="110">
          <template #default="{ row }">¥{{ row.price }}/{{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="数量" width="170">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              :max="row.stock"
              size="small"
              controls-position="right"
              @change="(v) => changeQuantity(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="110">
          <template #default="{ row }">
            <span class="t-subtotal">¥{{ row.subtotal }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="remove(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="checkout-bar">
        <span>已选 {{ selected.length }} 件商品</span>
        <div class="checkout-right">
          <span class="total-label">合计：</span>
          <span class="total-num">¥{{ totalAmount }}</span>
          <el-button type="primary" size="large" @click="openCheckout">去结算</el-button>
        </div>
      </div>
    </template>

    <el-empty v-else description="购物车空空如也，去商城逛逛吧">
      <el-button type="primary" @click="router.push('/market')">去逛逛</el-button>
    </el-empty>

    <!-- 结算弹窗 -->
    <el-dialog v-model="checkoutVisible" title="确认订单" width="560px">
      <div class="addr-title">选择收货地址</div>
      <el-radio-group v-model="selectedAddress" class="addr-list">
        <el-radio v-for="a in addresses" :key="a.id" :value="a.id" class="addr-item" border>
          <div>
            <b>{{ a.receiver }}</b> {{ a.phone }}
            <el-tag v-if="a.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
          <div class="addr-text">{{ a.region }} {{ a.detail }}</div>
        </el-radio>
      </el-radio-group>
      <el-button link type="primary" @click="addrFormVisible = true">➕ 新增收货地址</el-button>

      <div class="addr-form" v-if="addrFormVisible">
        <el-form :model="addrForm" label-width="70px">
          <el-form-item label="收货人">
            <el-input v-model="addrForm.receiver" placeholder="收货人姓名" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="addrForm.phone" placeholder="联系电话" />
          </el-form-item>
          <el-form-item label="地区">
            <el-input v-model="addrForm.region" placeholder="省市区，如：河南省郑州市金水区" />
          </el-form-item>
          <el-form-item label="详细地址">
            <el-input v-model="addrForm.detail" placeholder="街道门牌号" />
          </el-form-item>
          <el-form-item label="设为默认">
            <el-switch v-model="addrForm.isDefault" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="saveAddr">保存地址</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="checkout-total">应付金额：<b>¥{{ totalAmount }}</b></div>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">提交订单</el-button>
      </template>
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

.t-product {
  display: flex;
  gap: 10px;
  align-items: center;
  cursor: pointer;
}
.t-cover {
  width: 56px;
  height: 42px;
  object-fit: cover;
  border-radius: 6px;
}
.t-name {
  font-size: 14px;
  font-weight: 600;
}
.t-farmer {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 2px;
}
.t-subtotal {
  color: #f56c6c;
  font-weight: 700;
}

.checkout-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}
.checkout-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.total-label {
  color: #909399;
}
.total-num {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}

.addr-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 10px;
}
.addr-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  margin-bottom: 10px;
}
.addr-item {
  height: auto;
  padding: 8px 12px;
  margin-right: 0;
  width: 100%;
}
.addr-text {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
.addr-form {
  margin-top: 12px;
  border-top: 1px dashed #e4e7ed;
  padding-top: 12px;
}
.checkout-total {
  text-align: right;
  margin-top: 14px;
  font-size: 14px;
}
.checkout-total b {
  color: #f56c6c;
  font-size: 20px;
}
</style>
