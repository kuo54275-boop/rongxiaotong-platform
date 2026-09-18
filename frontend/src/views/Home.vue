<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHomeApi } from '@/api'

/**
 * 首页：平台门户，包含六大板块
 * 1. 新闻轮播 2. 金融产品展示 3. 专家一览 4. 专家问答展示 5. 农业知识展示 6. 助农电商入口
 */
const router = useRouter()
const home = ref({})
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getHomeApi()
    home.value = res.data || {}
  } catch (e) {
    // 请求失败时拦截器已弹出提示
  } finally {
    loading.value = false
  }
})

/** 农产品交易模块还没上线，其余模块直接跳转 */
function todo() {
  ElMessage.info('农产品交易模块开发中，敬请期待！')
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- ========== 1. 新闻轮播 ========== -->
    <el-carousel v-if="home.news?.length" height="360px" class="carousel">
      <el-carousel-item v-for="n in home.news" :key="n.id">
        <div class="banner" :style="{ backgroundImage: `url(${n.cover})` }">
          <div class="banner-text">
            <h2>{{ n.title }}</h2>
            <p>{{ n.summary }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- ========== 2. 金融产品展示 ========== -->
    <section class="section">
      <div class="section-head">
        <h3>🏦 金融产品</h3>
        <span class="more" @click="router.push('/finance')">进入融资服务 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="8" v-for="p in home.loanProducts" :key="p.id">
          <el-card shadow="hover" class="loan-card" @click="router.push('/finance')">
            <div class="loan-name">{{ p.name }}</div>
            <div class="loan-rate">年利率 <b>{{ p.rate }}%</b></div>
            <div class="loan-info">
              额度 {{ (p.minAmount / 10000).toFixed(1) }}~{{ (p.maxAmount / 10000).toFixed(1) }} 万元
              · 最长 {{ p.termMonths }} 个月
            </div>
            <div class="loan-desc">{{ p.description }}</div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- ========== 3. 专家一览 ========== -->
    <section class="section">
      <div class="section-head">
        <h3>👨‍🌾 专家一览</h3>
        <span class="more" @click="router.push('/expert/team')">进入专家助力 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="e in home.experts" :key="e.id">
          <el-card shadow="hover" class="expert-card" @click="router.push('/expert/team')">
            <img :src="e.avatar" class="expert-avatar" alt="专家头像" />
            <div class="expert-name">{{ e.realName }}</div>
            <div class="expert-title">{{ e.title }}</div>
            <div class="expert-specialty">{{ e.specialty }}</div>
            <div class="expert-rating">
              <el-rate :model-value="e.rating" disabled size="small" />
              <span class="rating-num">{{ e.rating }}</span>
              <span class="service-count">已服务 {{ e.serviceCount }} 次</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- ========== 4. 专家问答展示 ========== -->
    <section class="section">
      <div class="section-head">
        <h3>💬 专家问答</h3>
        <span class="more" @click="router.push('/expert/questions')">去提问 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="12" v-for="q in home.questions" :key="q.id">
          <el-card shadow="hover" class="qa-card" @click="router.push('/expert/questions')">
            <div class="qa-q">
              <span class="qa-tag">问</span>
              <span class="qa-title">{{ q.title }}</span>
            </div>
            <div class="qa-a">
              <span class="qa-tag answer">答</span>
              <span class="qa-content">{{ q.answerContent }}</span>
            </div>
            <div class="qa-meta">
              {{ q.farmerName }} 提问 · {{ q.answerExpertName }} 回答 · {{ q.createTime }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- ========== 5. 农业知识展示 ========== -->
    <section class="section">
      <div class="section-head">
        <h3>📚 农业知识</h3>
        <span class="more" @click="router.push('/expert/knowledge')">更多知识 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="a in home.articles" :key="a.id">
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

    <!-- ========== 6. 助农电商入口 ========== -->
    <section class="section">
      <div class="section-head">
        <h3>🛒 助农电商</h3>
        <span class="more" @click="todo('农产品交易')">进入农产品交易 &gt;</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="6" v-for="p in home.products" :key="p.id">
          <el-card shadow="hover" class="product-card" :body-style="{ padding: '0' }">
            <img :src="p.cover" class="product-cover" alt="商品图" />
            <div class="product-body">
              <div class="product-name">{{ p.name }}</div>
              <div class="product-price">
                <span class="price">¥{{ p.price }}</span>
                <span class="unit">/{{ p.unit }}</span>
                <span class="farmer">{{ p.farmerName }} 家</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<style scoped>
.carousel {
  border-radius: 8px;
  overflow: hidden;
}
.banner {
  height: 360px;
  background-size: cover;
  background-position: center;
  position: relative;
}
.banner-text {
  position: absolute;
  left: 60px;
  bottom: 50px;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}
.banner-text h2 {
  font-size: 30px;
  margin-bottom: 10px;
}
.banner-text p {
  font-size: 15px;
  opacity: 0.9;
}

.section {
  margin-top: 36px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-head h3 {
  font-size: 20px;
  color: #303133;
}
.more {
  color: #3d8c40;
  cursor: pointer;
  font-size: 14px;
}
.more:hover {
  text-decoration: underline;
}

/* 贷款产品卡 */
.loan-card {
  border-top: 3px solid #4caf50;
}
.loan-name {
  font-size: 17px;
  font-weight: 600;
}
.loan-rate {
  color: #f56c6c;
  font-size: 15px;
  margin: 10px 0 6px;
}
.loan-rate b {
  font-size: 22px;
}
.loan-info {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.loan-desc {
  color: #606266;
  font-size: 13px;
  line-height: 20px;
  height: 40px;
  overflow: hidden;
}

/* 专家卡 */
.expert-card {
  text-align: center;
}
.expert-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
}
.expert-name {
  font-size: 16px;
  font-weight: 600;
  margin-top: 10px;
}
.expert-title {
  color: #3d8c40;
  font-size: 13px;
  margin-top: 4px;
}
.expert-specialty {
  color: #909399;
  font-size: 12px;
  margin: 6px 0 8px;
  height: 18px;
  overflow: hidden;
}
.expert-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.rating-num {
  color: #e6a23c;
  font-weight: 600;
  font-size: 13px;
}
.service-count {
  color: #c0c4cc;
  font-size: 12px;
}

/* 问答卡 */
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

/* 知识卡 */
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

/* 商品卡 */
.product-cover {
  width: 100%;
  height: 160px;
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
  font-size: 18px;
  font-weight: 700;
}
.unit {
  color: #909399;
  font-size: 12px;
}
.farmer {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}
</style>
