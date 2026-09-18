<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  answerQuestionApi,
  askQuestionApi,
  getConsultStatsApi,
  getExpertsApi,
  getMyQuestionsApi,
  getQuestionApi,
  getQuestionsApi,
  getTodoQuestionsApi,
} from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 专家问答：农户提问、专家回答、待回答列表、我的提问
 */
const router = useRouter()
const userStore = useUserStore()

const tab = ref('all')          // all 全部 / todo 待回答(专家) / mine 我的提问(农户)
const todoCount = ref(0)        // 待回答问题数（红点）
const questions = ref([])
const total = ref(0)
const page = ref(1)
const size = 6
const loading = ref(false)
const experts = ref([])

// 提问弹窗
const askVisible = ref(false)
const asking = ref(false)
const askForm = reactive({ title: '', content: '', expertId: null })

// 详情弹窗
const detailVisible = ref(false)
const detail = ref(null)

// 回答弹窗
const answerVisible = ref(false)
const answering = ref(false)
const answerForm = reactive({ questionId: null, content: '' })

onMounted(async () => {
  await loadList()
  try {
    const res = await getExpertsApi()
    experts.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  }
})

async function loadList() {
  loading.value = true
  try {
    if (tab.value === 'mine') {
      const res = await getMyQuestionsApi()
      questions.value = res.data || []
      total.value = questions.value.length
      return
    }
    if (tab.value === 'todo') {
      const res = await getTodoQuestionsApi()
      questions.value = res.data || []
      total.value = questions.value.length
      // 刷新红点数字
      try {
        const s = await getConsultStatsApi()
        todoCount.value = s.data?.todoQuestions || 0
      } catch (e) {
        // 忽略统计失败
      }
      return
    }
    const res = await getQuestionsApi({ page: page.value, size })
    questions.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function changeTab(t) {
  tab.value = t
  page.value = 1
  if (t === 'mine' && !userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadList()
}

function openAsk() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再提问')
    router.push('/login')
    return
  }
  if (userStore.role !== 'FARMER') {
    ElMessage.warning('只有农户可以提问')
    return
  }
  askForm.title = ''
  askForm.content = ''
  askForm.expertId = null
  askVisible.value = true
}

async function ask() {
  if (!askForm.title || !askForm.content) {
    return ElMessage.warning('请填写问题标题和描述')
  }
  asking.value = true
  try {
    await askQuestionApi(askForm)
    ElMessage.success('提问成功，等待专家回答！')
    askVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    asking.value = false
  }
}

async function openDetail(id) {
  const res = await getQuestionApi(id)
  detail.value = res.data
  detailVisible.value = true
}

function openAnswer(q) {
  if (userStore.role !== 'EXPERT') {
    ElMessage.warning('只有技术专家可以回答')
    return
  }
  answerForm.questionId = q.id
  answerForm.content = ''
  answerVisible.value = true
}

async function submitAnswer() {
  if (!answerForm.content) {
    return ElMessage.warning('请填写回答内容')
  }
  answering.value = true
  try {
    await answerQuestionApi(answerForm.questionId, { content: answerForm.content })
    ElMessage.success('回答成功！')
    answerVisible.value = false
    // 刷新详情和列表
    if (detailVisible.value) {
      openDetail(answerForm.questionId)
    }
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    answering.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <el-radio-group v-model="tab" @change="changeTab">
        <el-radio-button value="all">全部问答</el-radio-button>
        <el-badge v-if="userStore.role === 'EXPERT'" :value="todoCount" :hidden="todoCount === 0" :offset="[6, -4]">
          <el-radio-button value="todo">待回答</el-radio-button>
        </el-badge>
        <el-radio-button v-if="userStore.role === 'FARMER'" value="mine">我的提问</el-radio-button>
      </el-radio-group>
      <el-button v-if="userStore.role === 'FARMER'" type="primary" @click="openAsk">
        ✍️ 我要提问
      </el-button>
    </div>

    <!-- 问答列表 -->
    <div v-loading="loading">
      <el-card v-for="q in questions" :key="q.id" shadow="hover" class="q-card" @click="openDetail(q.id)">
        <div class="q-head">
          <span class="q-tag">问</span>
          <span class="q-title">{{ q.title }}</span>
          <el-tag v-if="q.status === 1" size="small" type="success">已回答</el-tag>
          <el-tag v-else size="small" type="warning">待回答</el-tag>
        </div>
        <div class="q-body">{{ q.content }}</div>
        <div v-if="q.answerContent" class="q-answer">
          <span class="q-tag answer">答</span>
          <span>{{ q.answerContent }}</span>
        </div>
        <div class="q-meta">
          <span>{{ q.farmerName }} 提问 · {{ q.createTime }}</span>
          <span v-if="q.answerExpertName" class="answerer">{{ q.answerExpertName }} 回答</span>
          <el-button
            v-if="userStore.role === 'EXPERT' && q.status === 0"
            size="small"
            type="primary"
            @click.stop="openAnswer(q)"
          >
            去回答
          </el-button>
        </div>
      </el-card>
      <el-empty v-if="!questions.length" description="暂无问答" />

      <div v-if="tab === 'all'" class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 提问弹窗 -->
    <el-dialog v-model="askVisible" title="向专家提问" width="560px">
      <el-form :model="askForm" label-width="80px">
        <el-form-item label="问题标题">
          <el-input v-model="askForm.title" placeholder="一句话概括问题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input
            v-model="askForm.content"
            type="textarea"
            :rows="5"
            placeholder="详细描述你遇到的问题，如作物品种、症状、环境等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="指定专家">
          <el-select v-model="askForm.expertId" placeholder="不选则由所有专家可见" clearable style="width: 100%">
            <el-option
              v-for="e in experts"
              :key="e.userId"
              :label="`${e.realName}（${e.title}）`"
              :value="e.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="askVisible = false">取消</el-button>
        <el-button type="primary" :loading="asking" @click="ask">提交问题</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="问答详情" width="640px">
      <template v-if="detail">
        <div class="d-q">
          <div class="d-title">❓ {{ detail.title }}</div>
          <div class="d-meta">{{ detail.farmerName }} 提问 · {{ detail.createTime }}</div>
          <div class="d-content">{{ detail.content }}</div>
        </div>
        <div class="d-a-list">
          <div v-for="a in detail.answers" :key="a.id" class="d-a">
            <img :src="a.expertAvatar" class="d-avatar" alt="专家" />
            <div class="d-a-main">
              <div class="d-a-name">{{ a.expertName }} <span class="d-a-time">{{ a.createTime }}</span></div>
              <div class="d-a-content">{{ a.content }}</div>
            </div>
          </div>
          <div v-if="!detail.answers?.length" class="d-empty">还没有专家回答，等待中…</div>
        </div>
        <div v-if="userStore.role === 'EXPERT'" class="d-actions">
          <el-button type="primary" @click="openAnswer(detail)">我来回答</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 回答弹窗 -->
    <el-dialog v-model="answerVisible" title="回答问题" width="560px">
      <el-input
        v-model="answerForm.content"
        type="textarea"
        :rows="7"
        placeholder="请输入专业、具体的回答内容"
        maxlength="1000"
        show-word-limit
      />
      <template #footer>
        <el-button @click="answerVisible = false">取消</el-button>
        <el-button type="primary" :loading="answering" @click="submitAnswer">提交回答</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}

.q-card {
  margin-bottom: 14px;
  cursor: pointer;
}
.q-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.q-tag {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.q-tag.answer {
  background: #4caf50;
}
.q-title {
  font-size: 15px;
  font-weight: 600;
  flex: 1;
}
.q-body {
  color: #606266;
  font-size: 13px;
  margin: 10px 0;
  line-height: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.q-answer {
  background: #f0f9eb;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  color: #303133;
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.q-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 10px;
}
.answerer {
  color: #3d8c40;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 详情 */
.d-q {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 14px;
}
.d-title {
  font-size: 16px;
  font-weight: 700;
}
.d-meta {
  color: #c0c4cc;
  font-size: 12px;
  margin: 6px 0 10px;
}
.d-content {
  color: #303133;
  font-size: 14px;
  line-height: 1.8;
}
.d-a-list {
  margin-top: 14px;
  max-height: 300px;
  overflow-y: auto;
}
.d-a {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}
.d-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
}
.d-a-name {
  font-size: 13px;
  font-weight: 600;
  color: #3d8c40;
}
.d-a-time {
  color: #c0c4cc;
  font-weight: 400;
  font-size: 12px;
  margin-left: 8px;
}
.d-a-content {
  font-size: 14px;
  line-height: 1.8;
  margin-top: 4px;
}
.d-empty {
  color: #c0c4cc;
  text-align: center;
  padding: 20px;
}
.d-actions {
  text-align: right;
  margin-top: 10px;
}
</style>
