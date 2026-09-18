<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addCartApi, getProductApi } from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 商品详情页：查看详情、加入购物车
 */
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const quantity = ref(1)
const adding = ref(false)

onMounted(async () => {
  try {
    const res = await getProductApi(route.params.id)
    product.value = res.data
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
})

async function addCart() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (userStore.role !== 'BUYER') {
    ElMessage.warning('只有买家可以购买商品')
    return
  }
  adding.value = true
  try {
    await addCartApi(product.value.id, quantity.value)
    ElMessage.success('已加入购物车！')
  } catch (e) {
    // 拦截器已提示
  } finally {
    adding.value = false
  }
}

async function buyNow() {
  await addCart()
  router.push('/market/cart')
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="breadcrumb" @click="router.push('/market')">← 返回商城</div>
    <el-card v-if="product" class="detail-card">
      <div class="detail-layout">
        <img :src="product.cover" class="detail-cover" alt="商品图" />
        <div class="detail-info">
          <h2 class="p-name">{{ product.name }}</h2>
          <div class="p-tags">
            <el-tag type="success" size="small">{{ product.category }}</el-tag>
            <el-tag v-if="product.status === 1" type="info" size="small">现货</el-tag>
          </div>
          <div class="p-price-box">
            <span class="p-price">¥{{ product.price }}</span>
            <span class="p-unit">/ {{ product.unit }}</span>
          </div>
          <div class="p-rows">
            <div class="p-row"><span class="label">供货农户</span><b>{{ product.farmerName }}</b></div>
            <div class="p-row"><span class="label">产 地</span><b>{{ product.farmerAddress || '—' }}</b></div>
            <div class="p-row"><span class="label">库 存</span><b>{{ product.stock }} {{ product.unit }}</b></div>
            <div class="p-row"><span class="label">发布时间</span><b>{{ product.createTime }}</b></div>
          </div>
          <div class="p-buy">
            <span class="label">购买数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" controls-position="right" />
          </div>
          <div class="p-actions">
            <el-button size="large" :loading="adding" @click="addCart">🛒 加入购物车</el-button>
            <el-button size="large" type="primary" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>
      <div class="p-desc-title">商品介绍</div>
      <div class="p-desc">{{ product.description }}</div>
    </el-card>
  </div>
</template>

<style scoped>
.breadcrumb {
  color: #909399;
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 14px;
}
.breadcrumb:hover {
  color: #3d8c40;
}
.detail-card {
  padding: 8px;
}
.detail-layout {
  display: flex;
  gap: 32px;
}
.detail-cover {
  width: 380px;
  height: 285px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
}
.detail-info {
  flex: 1;
}
.p-name {
  font-size: 22px;
  color: #303133;
}
.p-tags {
  margin: 10px 0;
  display: flex;
  gap: 8px;
}
.p-price-box {
  background: #fff5f5;
  border-radius: 8px;
  padding: 14px 18px;
  margin: 12px 0 16px;
}
.p-price {
  color: #f56c6c;
  font-size: 30px;
  font-weight: 700;
}
.p-unit {
  color: #909399;
  font-size: 14px;
}
.p-rows {
  margin-bottom: 14px;
}
.p-row {
  display: flex;
  gap: 12px;
  line-height: 28px;
  font-size: 14px;
  color: #303133;
}
.p-row .label {
  color: #909399;
  width: 64px;
  flex-shrink: 0;
}
.p-buy {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.p-buy .label {
  color: #909399;
  font-size: 14px;
}
.p-actions {
  display: flex;
  gap: 12px;
}
.p-desc-title {
  font-size: 16px;
  font-weight: 700;
  margin: 28px 0 10px;
}
.p-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
