<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteArticleApi,
  deleteDemandApi,
  deleteProductApi,
  getMyArticlesApi,
  getMyDemandsApi,
  getMyProductsApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 我的发布：按角色聚合展示
 * 农户 → 商品 / 买家 → 求购需求 / 专家 → 知识文章
 */
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const demands = ref([])
const articles = ref([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    if (userStore.role === 'FARMER') {
      const res = await getMyProductsApi()
      products.value = res.data || []
    } else if (userStore.role === 'BUYER') {
      const res = await getMyDemandsApi()
      demands.value = res.data || []
    } else if (userStore.role === 'EXPERT') {
      const res = await getMyArticlesApi()
      articles.value = res.data || []
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function removeProduct(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.name}》吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteProductApi(row.id)
  ElMessage.success('删除成功')
  load()
}

async function removeDemand(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteDemandApi(row.id)
  ElMessage.success('删除成功')
  load()
}

async function removeArticle(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteArticleApi(row.id)
  ElMessage.success('删除成功')
  load()
}
</script>

<template>
  <div v-loading="loading">
    <h3 class="page-title">📝 我的发布</h3>

    <!-- 农户：商品 -->
    <template v-if="userStore.role === 'FARMER'">
      <el-table :data="products" border stripe empty-text="还没有发布过商品">
        <el-table-column label="商品" min-width="180">
          <template #default="{ row }">
            <div class="cell-product">
              <img :src="row.cover" class="cell-cover" alt="图" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
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
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="removeProduct(row)">删除</el-button>
            <el-button size="small" type="primary" link @click="router.push('/market')">去管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 买家：求购需求 -->
    <template v-else-if="userStore.role === 'BUYER'">
      <el-table :data="demands" border stripe empty-text="还没有发布过求购需求">
        <el-table-column prop="title" label="需求" min-width="200" show-overflow-tooltip />
        <el-table-column label="数量" width="120">
          <template #default="{ row }">{{ row.quantity }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="removeDemand(row)">删除</el-button>
            <el-button size="small" type="primary" link @click="router.push('/market/demands')">去管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 专家：知识文章 -->
    <template v-else-if="userStore.role === 'EXPERT'">
      <el-table :data="articles" border stripe empty-text="还没有发布过知识文章">
        <el-table-column prop="title" label="文章" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="views" label="浏览量" width="90" />
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="removeArticle(row)">删除</el-button>
            <el-button size="small" type="primary" link @click="router.push('/expert/knowledge')">去管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 银行/管理员 -->
    <el-empty v-else description="当前角色没有发布内容" />
  </div>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.cell-product {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cell-cover {
  width: 44px;
  height: 34px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
