<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteArticleApi,
  getArticlesApi,
  getMyArticlesApi,
  saveArticleApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 农业知识库：分类浏览 + 分页；专家登录后可管理（发布/编辑/删除）
 */
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const categories = ['全部', '种植技术', '养殖技术', '病虫害防治', '政策法规']
const activeCategory = ref('全部')
const articles = ref([])
const total = ref(0)
const page = ref(1)
const size = 8
const loading = ref(false)

// 专家管理
const tab = ref('browse')        // browse 浏览 / manage 我的知识管理
const myArticles = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, title: '', category: '种植技术', cover: '', content: '' })
const coverOptions = [
  { label: '绿野 1', value: '/api/images/article1.svg' },
  { label: '绿野 2', value: '/api/images/article2.svg' },
  { label: '大棚', value: '/api/images/article3.svg' },
  { label: '养殖', value: '/api/images/article4.svg' },
]

onMounted(() => {
  // 从个人中心"知识管理"进来时直接打开管理模式
  if (route.query.tab === 'manage' && userStore.role === 'EXPERT') {
    tab.value = 'manage'
    loadMyArticles()
  }
  loadList()
})

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (activeCategory.value !== '全部') {
      params.category = activeCategory.value
    }
    const res = await getArticlesApi(params)
    articles.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function changeCategory(cat) {
  activeCategory.value = cat
  page.value = 1
  loadList()
}

async function loadMyArticles() {
  const res = await getMyArticlesApi()
  myArticles.value = res.data || []
}

function openEdit(row) {
  form.id = row ? row.id : null
  form.title = row?.title || ''
  form.category = row?.category || '种植技术'
  form.cover = row?.cover || coverOptions[0].value
  form.content = row?.content || ''
  dialogVisible.value = true
}

async function save() {
  if (!form.title || !form.content) {
    return ElMessage.warning('请填写标题和内容')
  }
  saving.value = true
  try {
    await saveArticleApi(form)
    ElMessage.success(form.id ? '修改成功' : '发布成功')
    dialogVisible.value = false
    loadMyArticles()
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return   // 用户取消
  }
  await deleteArticleApi(row.id)
  ElMessage.success('删除成功')
  loadMyArticles()
  loadList()
}
</script>

<template>
  <div class="page-container">
    <!-- 分类导航 -->
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
      <span v-if="userStore.role === 'EXPERT'" class="manage-switch">
        <el-radio-group v-model="tab" size="small" @change="tab === 'manage' && loadMyArticles()">
          <el-radio-button value="browse">知识库</el-radio-button>
          <el-radio-button value="manage">我的知识管理</el-radio-button>
        </el-radio-group>
      </span>
    </div>

    <!-- 浏览模式 -->
    <div v-if="tab === 'browse'" v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="6" v-for="a in articles" :key="a.id">
          <el-card shadow="hover" class="article-card" :body-style="{ padding: '0' }"
                   @click="router.push(`/expert/knowledge/${a.id}`)">
            <img :src="a.cover" class="article-cover" alt="封面" />
            <div class="article-body">
              <el-tag size="small" type="success">{{ a.category }}</el-tag>
              <div class="article-title">{{ a.title }}</div>
              <div class="article-meta">{{ a.expertName }} · {{ a.views }} 次浏览 · {{ a.createTime }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!articles.length" description="该分类下暂无知识" />

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

    <!-- 专家管理模式 -->
    <div v-else>
      <div class="manage-bar">
        <el-button type="primary" @click="openEdit(null)">➕ 发布新知识</el-button>
      </div>
      <el-table :data="myArticles" border stripe empty-text="还没有发布过知识">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="views" label="浏览量" width="90" />
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识' : '发布知识'" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="知识标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 200px">
            <el-option v-for="c in categories.filter((x) => x !== '全部')" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <el-select v-model="form.cover" style="width: 100%">
            <el-option v-for="c in coverOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
          <img v-if="form.cover" :src="form.cover" class="cover-preview" alt="封面预览" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入知识正文内容"
            maxlength="5000"
            show-word-limit
          />
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

.article-card {
  cursor: pointer;
  margin-bottom: 16px;
}
.article-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  display: block;
}
.article-body {
  padding: 12px;
}
.article-title {
  font-size: 14px;
  font-weight: 600;
  margin: 8px 0 6px;
  height: 40px;
  overflow: hidden;
}
.article-meta {
  color: #c0c4cc;
  font-size: 12px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.manage-bar {
  margin-bottom: 14px;
}
.cover-preview {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  margin-top: 8px;
}
</style>
