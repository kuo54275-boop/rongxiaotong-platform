<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelOrderApi,
  getMyOrdersApi,
  getSellerOrdersApi,
  payOrderApi,
  shipOrderApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 订单页：
 * 买家视角 → 我的订单（付款 / 取消）
 * 农户视角 → 供货订单（发货）
 */
const userStore = useUserStore()

const orders = ref([])
const loading = ref(false)

const statusMap = {
  UNPAID: { text: '待付款', type: 'warning' },
  SHIPPED: { text: '待发货', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const api = userStore.role === 'FARMER' ? getSellerOrdersApi : getMyOrdersApi
    const res = await api()
    orders.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function pay(row) {
  try {
    await ElMessageBox.confirm(`确认支付 ¥${row.totalAmount} 吗？（演示环境为模拟付款）`, '付款', {
      type: 'warning',
    })
  } catch (e) {
    return
  }
  await payOrderApi(row.id)
  ElMessage.success('付款成功，等待农户发货！')
  load()
}

async function ship(row) {
  try {
    await ElMessageBox.confirm('确认发货吗？', '发货', { type: 'warning' })
  } catch (e) {
    return
  }
  await shipOrderApi(row.id)
  ElMessage.success('发货成功！')
  load()
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？商品库存会自动恢复。', '取消订单', { type: 'warning' })
  } catch (e) {
    return
  }
  await cancelOrderApi(row.id)
  ElMessage.success('订单已取消')
  load()
}
</script>

<template>
  <div class="page-container">
    <div class="page-banner">
      <h2 v-if="userStore.role === 'FARMER'">📦 供货订单</h2>
      <h2 v-else>📦 我的订单</h2>
      <p v-if="userStore.role === 'FARMER'">买家下单后在这里发货</p>
      <p v-else>订单状态流转：待付款 → 待发货 → 已完成</p>
    </div>

    <div v-loading="loading">
      <el-card v-for="o in orders" :key="o.id" shadow="hover" class="order-card">
        <div class="o-head">
          <div>
            <span class="o-no">订单号：{{ o.orderNo }}</span>
            <span class="o-time">{{ o.createTime }}</span>
          </div>
          <el-tag :type="statusMap[o.status]?.type || 'info'" size="small">
            {{ statusMap[o.status]?.text || o.status }}
          </el-tag>
        </div>

        <!-- 订单明细 -->
        <div class="o-items">
          <div v-for="it in o.items" :key="it.id" class="o-item">
            <img :src="it.cover" class="o-cover" alt="图" />
            <div class="o-item-main">
              <div class="o-item-name">{{ it.productName }}</div>
              <div class="o-item-sub">¥{{ it.price }}/{{ it.unit }} × {{ it.quantity }}</div>
            </div>
            <div class="o-item-subtotal">¥{{ it.subtotal }}</div>
          </div>
        </div>

        <div class="o-foot">
          <div class="o-info">
            <template v-if="userStore.role === 'FARMER'">
              <span>买家：<b>{{ o.buyerName }}</b></span>
            </template>
            <template v-else>
              <span>农户：<b>{{ o.farmerName }}</b></span>
            </template>
            <span class="o-addr">收货地址：{{ o.addressText }}</span>
          </div>
          <div class="o-actions">
            <span class="o-total">合计 <b>¥{{ o.totalAmount }}</b></span>
            <template v-if="userStore.role === 'FARMER'">
              <el-button v-if="o.status === 'SHIPPED'" type="primary" @click="ship(o)">发 货</el-button>
            </template>
            <template v-else>
              <el-button v-if="o.status === 'UNPAID'" type="danger" @click="cancel(o)">取消订单</el-button>
              <el-button v-if="o.status === 'UNPAID'" type="primary" @click="pay(o)">付 款</el-button>
            </template>
          </div>
        </div>
      </el-card>
      <el-empty v-if="!orders.length" description="还没有订单" />
    </div>
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

.order-card {
  margin-bottom: 14px;
}
.o-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}
.o-no {
  font-weight: 600;
  font-size: 14px;
  margin-right: 14px;
}
.o-time {
  color: #c0c4cc;
  font-size: 12px;
}

.o-items {
  padding: 10px 0;
}
.o-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}
.o-cover {
  width: 52px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
}
.o-item-main {
  flex: 1;
}
.o-item-name {
  font-size: 14px;
  font-weight: 600;
}
.o-item-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
.o-item-subtotal {
  color: #f56c6c;
  font-weight: 600;
  font-size: 14px;
}

.o-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f2f5;
  padding-top: 10px;
}
.o-info {
  font-size: 13px;
  color: #606266;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.o-addr {
  color: #909399;
  font-size: 12px;
}
.o-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.o-total {
  font-size: 14px;
  color: #606266;
}
.o-total b {
  color: #f56c6c;
  font-size: 20px;
}
</style>
