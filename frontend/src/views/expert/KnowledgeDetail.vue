<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleApi } from '@/api'

/**
 * 知识详情页
 */
const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getArticleApi(route.params.id)
    article.value = res.data
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <el-card v-if="article" class="detail-card">
      <div class="breadcrumb" @click="router.push('/expert/knowledge')">
        ← 返回知识库
      </div>
      <h1 class="title">{{ article.title }}</h1>
      <div class="meta">
        <el-tag size="small" type="success">{{ article.category }}</el-tag>
        <span>{{ article.expertName }} · {{ article.createTime }} · {{ article.views }} 次浏览</span>
      </div>
      <img v-if="article.cover" :src="article.cover" class="cover" alt="封面" />
      <div class="content">{{ article.content }}</div>
    </el-card>
  </div>
</template>

<style scoped>
.detail-card {
  padding: 12px 8px;
}
.breadcrumb {
  color: #909399;
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 16px;
}
.breadcrumb:hover {
  color: #3d8c40;
}
.title {
  font-size: 26px;
  color: #303133;
  margin-bottom: 12px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #909399;
  font-size: 13px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}
.cover {
  width: 100%;
  border-radius: 8px;
  margin: 20px 0;
}
.content {
  font-size: 15px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;   /* 保留换行 */
}
</style>
