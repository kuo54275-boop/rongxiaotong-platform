<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticlesApi, getExpertsApi, getQuestionsApi } from '@/api'

/**
 * 专家助力模块门户：农业知识、专家问答、专家团队三大入口 + 数据展示
 */
const router = useRouter()
const articles = ref([])
const questions = ref([])
const experts = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [a, q, e] = await Promise.all([
      getArticlesApi({ page: 1, size: 4 }),
      getQuestionsApi({ page: 1, size: 4 }),
      getExpertsApi(),
    ])
    articles.value = a.data?.list || []
    questions.value = q.data?.list || []
    experts.value = e.data || []
  } catch (err) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
})

const entries = [
  { icon: '📚', title: '农业知识库', desc: '种植养殖技术、病虫害防治、政策法规', path: '/expert/knowledge', color: '#4caf50' },
  { icon: '💬', title: '专家问答', desc: '有问题问专家，专家在线解答', path: '/expert/questions', color: '#409eff' },
  { icon: '👨‍🌾', title: '专家团队', desc: '资深农技专家，支持在线咨询与预约', path: '/expert/team', color: '#e6a23c' },
]
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 模块横幅 -->
    <div class="page-banner">
      <h2>🌱 专家助力</h2>
      <p>农业知识在线学习，农技难题专家答疑，支持在线咨询与上门预约</p>
    </div>

    <!-- 三大入口 -->
    <el-row :gutter="16">
      <el-col :span="8" v-for="en in entries" :key="en.path">
        <div class="entry-card" :style="{ borderTopColor: en.color }" @click="router.push(en.path)">
          <div class="entry-icon">{{ en.icon }}</div>
          <div class="entry-title">{{ en.title }}</div>
          <div class="entry-desc">{{ en.desc }}</div>
          <div class="entry-go">进入 &gt;</div>
        </div>
      </el-col>
    </el-row>

    <!-- 最新知识 -->
    <section class="section">
      <div class="section-head">
        <h3>📖 最新知识</h3>
        <span class="more" @click="router.push('/expert/knowledge')">更多 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="a in articles" :key="a.id">
          <el-card shadow="hover" class="article-card" :body-style="{ padding: '0' }"
                   @click="router.push(`/expert/knowledge/${a.id}`)">
            <img :src="a.cover" class="article-cover" alt="封面" />
            <div class="article-body">
              <el-tag size="small" type="success">{{ a.category }}</el-tag>
              <div class="article-title">{{ a.title }}</div>
              <div class="article-meta">{{ a.expertName }} · {{ a.views }} 次浏览</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 最新问答 -->
    <section class="section">
      <div class="section-head">
        <h3>💡 最新问答</h3>
        <span class="more" @click="router.push('/expert/questions')">更多 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="12" v-for="q in questions" :key="q.id">
          <el-card shadow="hover" class="qa-card" @click="router.push('/expert/questions')">
            <div class="qa-q">
              <span class="qa-tag">问</span>
              <span class="qa-title">{{ q.title }}</span>
            </div>
            <div class="qa-a">
              <span class="qa-tag answer">答</span>
              <span class="qa-content">{{ q.answerContent || '等待专家回答中…' }}</span>
            </div>
            <div class="qa-meta">{{ q.farmerName }} 提问 · {{ q.createTime }}</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 专家团队 -->
    <section class="section">
      <div class="section-head">
        <h3>🌟 专家团队</h3>
        <span class="more" @click="router.push('/expert/team')">找专家 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="e in experts" :key="e.id">
          <el-card shadow="hover" class="expert-card" @click="router.push('/expert/team')">
            <img :src="e.avatar" class="expert-avatar" alt="专家头像" />
            <div class="expert-name">{{ e.realName }}</div>
            <div class="expert-title">{{ e.title }}</div>
            <div class="expert-specialty">{{ e.specialty }}</div>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<style scoped>
.page-banner {
  background: linear-gradient(135deg, #43a047, #1b5e20);
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

.entry-card {
  background: #fff;
  border-radius: 10px;
  border-top: 4px solid;
  padding: 24px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 16px;
}
.entry-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.entry-icon {
  font-size: 34px;
}
.entry-title {
  font-size: 17px;
  font-weight: 700;
  margin: 10px 0 6px;
}
.entry-desc {
  color: #909399;
  font-size: 13px;
  margin-bottom: 10px;
}
.entry-go {
  color: #3d8c40;
  font-size: 13px;
}

.section {
  margin-top: 28px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.section-head h3 {
  font-size: 18px;
}
.more {
  color: #3d8c40;
  cursor: pointer;
  font-size: 13px;
}
.more:hover {
  text-decoration: underline;
}

.article-card {
  cursor: pointer;
}
.article-cover {
  width: 100%;
  height: 130px;
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

.qa-card {
  cursor: pointer;
}
.qa-q,
.qa-a {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.qa-a {
  margin-top: 10px;
}
.qa-tag {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qa-tag.answer {
  background: #4caf50;
}
.qa-title {
  font-weight: 600;
  font-size: 14px;
}
.qa-content {
  color: #606266;
  font-size: 13px;
  line-height: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.qa-meta {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 10px;
}

.expert-card {
  text-align: center;
  cursor: pointer;
}
.expert-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
}
.expert-name {
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
}
.expert-title {
  color: #3d8c40;
  font-size: 13px;
  margin-top: 4px;
}
.expert-specialty {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
  height: 18px;
  overflow: hidden;
}
</style>
