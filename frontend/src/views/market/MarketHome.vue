<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteProductApi,
  getCartApi,
  getMyProductsApi,
  getProductsApi,
  saveProductApi,
  updateProductStatusApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 农产品交易主页：商品浏览（搜索/分类/分页）+ 农户商品管理 + 购物车/求购入口
 */
const router = useRouter()
const userStore = useUserStore()

const categories = ['全部', '水果', '蔬菜', '粮食', '禽畜', '水产', '其他']
const activeCategory = ref('全部')
const keyword = ref('')
const products = ref([])
const total = ref(0)
const page = ref(1)
const size = 8
const loading = ref(false)
const cartCount = ref(0)

// 农户商品管理
const tab = ref('browse')          // browse 商城 / manage 商品管理
const myProducts = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null,
  name: '',
  category: '粮食',
  price: null,
  stock: null,
  unit: '斤',
  cover: '',
  description: '',
})
const coverOptions = [
  '/api/images/p-wheat.svg',
  '/api/images/p-flour.svg',
  '/api/images/p-corn.svg',
  '/api/images/p-sweetcorn.svg',
  '/api/images/p-peanut.svg',
  '/api/images/p-egg.svg',
  '/api/images/p-garlic.svg',
  '/api/images/p-honey.svg',
]
const unitOptions = ['斤', '袋', '箱', '瓶', '枚', '件']

onMounted(async () => {
  await loadList()
  if (userStore.role === 'BUYER') {
    try {
      const res = await getCartApi()
      cartCount.value = (res.data || []).length
    } catch (e) {
      // 忽略
    }
  }
})

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (activeCategory.value !== '全部') {
      params.category = activeCategory.value
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    const res = await getProductsApi(params)
    products.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  loadList()
}

function changeCategory(cat) {
  activeCategory.value = cat
  page.value = 1
  loadList()
}

async function loadMyProducts() {
  const res = await getMyProductsApi()
  myProducts.value = res.data || []
}

function openEdit(row) {
  form.id = row ? row.id : null
  form.name = row?.name || ''
  form.category = row?.category || '粮食'
  form.price = row?.price ?? null
  form.stock = row?.stock ?? null
  form.unit = row?.unit || '斤'
  form.cover = row?.cover || coverOptions[0]
  form.description = row?.description || ''
  dialogVisible.value = true
}

async function save() {
  if (!form.name || form.price === null || form.stock === null) {
    return ElMessage.warning('请填写商品名称、价格和库存')
  }
  saving.value = true
  try {
    await saveProductApi(form)
    ElMessage.success(form.id ? '修改成功' : '发布成功')
    dialogVisible.value = false
    loadMyProducts()
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateProductStatusApi(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  loadMyProducts()
  loadList()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.name}》吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteProductApi(row.id)
  ElMessage.success('删除成功')
  loadMyProducts()
  loadList()
}
</script>

<template>
  <div class="page-container">
    <!-- 顶部横幅 + 搜索 -->
    <div class="top-area">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索农产品，如：玉米"
          class="search-input"
          clearable
          @keyup.enter="search"
        />
        <el-button type="primary" @click="search">搜索</el-button>
      </div>
      <div class="top-btns">
        <el-button @click="router.push('/market/demands')">📋 求购大厅</el-button>
        <el-button v-if="userStore.role === 'BUYER'" @click="router.push('/market/orders')">
          📦 我的订单
        </el-button>
        <el-button v-if="userStore.role === 'FARMER'" @click="router.push('/market/orders')">
          📦 供货订单
        </el-button>
        <el-badge v-if="userStore.role === 'BUYER'" :value="cartCount" :hidden="cartCount === 0">
          <el-button type="primary" @click="router.push('/market/cart')">🛒 购物车</el-button>
        </el-badge>
      </div>
    </div>

    <!-- 分类 + 管理模式切换 -->
    <div class="cat-bar">
      <span
        v-for="c in categories"
        :key="c"
        class="cat-item"
        :class="{ active: activeCategory === c }"
        @click="changeCategory(c)"
      >
        {{ c }}
      </span>
      <span v-if="userStore.role === 'FARMER'" class="manage-switch">
        <el-radio-group v-model="tab" size="small" @change="tab === 'manage' && loadMyProducts()">
          <el-radio-button value="browse">商城</el-radio-button>
          <el-radio-button value="manage">我的商品管理</el-radio-button>
        </el-radio-group>
      </span>
    </div>

    <!-- 商城模式 -->
    <div v-if="tab === 'browse'" v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="6" v-for="p in products" :key="p.id">
          <el-card shadow="hover" class="product-card" :body-style="{ padding: '0' }"
                   @click="router.push(`/market/product/${p.id}`)">
            <img :src="p.cover" class="product-cover" alt="商品图" />
            <div class="product-body">
              <div class="product-name">{{ p.name }}</div>
              <div class="product-price">
                <span class="price">¥{{ p.price }}</span>
                <span class="unit">/{{ p.unit }}</span>
              </div>
              <div class="product-meta">
                <span>{{ p.farmerName }} 家 · 库存 {{ p.stock }} {{ p.unit }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!products.length" description="没有找到相关商品" />

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 农户商品管理 -->
    <div v-else>
      <div class="manage-bar">
        <el-button type="primary" @click="openEdit(null)">➕ 发布新商品</el-button>
      </div>
      <el-table :data="myProducts" border stripe empty-text="还没有发布过商品">
        <el-table-column label="商品" min-width="180">
          <template #default="{ row }">
            <div class="table-product">
              <img :src="row.cover" class="table-cover" alt="图" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="90">
          <template #default="{ row }">¥{{ row.price }}/{{ row.unit }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发布/编辑商品弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '发布商品'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" placeholder="如：红富士苹果" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in categories.filter((x) => x !== '全部')" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0.1" :precision="2" :step="1" controls-position="right" style="width: 180px" />
          <span class="form-tip">元</span>
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" controls-position="right" style="width: 180px" />
        </el-form-item>
        <el-form-item label="单位">
          <el-select v-model="form.unit" style="width: 180px">
            <el-option v-for="u in unitOptions" :key="u" :label="u" :value="u" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图">
          <el-select v-model="form.cover" style="width: 100%">
            <el-option
              v-for="c in coverOptions"
              :key="c"
              :label="c.split('/').pop()"
              :value="c"
            />
          </el-select>
          <img v-if="form.cover" :src="form.cover" class="cover-preview" alt="预览" />
        </el-form-item>
        <el-form-item label="商品介绍">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="介绍商品特点、产地、品质等" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.top-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.search-bar {
  display: flex;
  gap: 8px;
  flex: 1;
  max-width: 480px;
}
.cat-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}
.cat-item {
  padding: 6px 16px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}
.cat-item:hover {
  background: #ecf7ed;
  color: #3d8c40;
}
.cat-item.active {
  background: #4caf50;
  color: #fff;
}
.manage-switch {
  margin-left: auto;
}

.product-card {
  cursor: pointer;
  margin-bottom: 16px;
}
.product-cover {
  width: 100%;
  height: 170px;
  object-fit: cover;
  display: block;
}
.product-body {
  padding: 12px;
}
.product-name {
  font-size: 14px;
  font-weight: 600;
  height: 20px;
  overflow: hidden;
}
.product-price {
  margin-top: 8px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.price {
  color: #f56c6c;
  font-size: 19px;
  font-weight: 700;
}
.unit {
  color: #909399;
  font-size: 12px;
}
.product-meta {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 6px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.manage-bar {
  margin-bottom: 14px;
}
.table-product {
  display: flex;
  align-items: center;
  gap: 8px;
}
.table-cover {
  width: 44px;
  height: 34px;
  object-fit: cover;
  border-radius: 4px;
}
.form-tip {
  margin-left: 8px;
  color: #909399;
}
.cover-preview {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  margin-top: 8px;
}
</style>
