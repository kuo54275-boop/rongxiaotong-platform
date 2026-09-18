<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createAppointmentApi, getExpertsApi } from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 专家团队：专家一览 + 详情（在线咨询、预约专家）
 */
const router = useRouter()
const userStore = useUserStore()

const experts = ref([])
const loading = ref(true)

// 详情弹窗
const detailVisible = ref(false)
const current = ref(null)

// 预约弹窗
const appointVisible = ref(false)
const appointing = ref(false)
const appointForm = reactive({
  expertId: null,
  topic: '',
  description: '',
  appointTime: '',
  phone: '',
})

onMounted(async () => {
  try {
    const res = await getExpertsApi()
    experts.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
})

function openDetail(e) {
  current.value = e
  detailVisible.value = true
}

/** 在线咨询：农户与专家对话 */
function goChat(e) {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再咨询专家')
    router.push('/login')
    return
  }
  if (userStore.role !== 'FARMER') {
    ElMessage.warning('只有农户可以在线咨询专家')
    return
  }
  router.push(`/expert/chat/${e.userId}`)
}

/** 预约专家 */
function openAppoint(e) {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再预约专家')
    router.push('/login')
    return
  }
  if (userStore.role !== 'FARMER') {
    ElMessage.warning('只有农户可以预约专家')
    return
  }
  appointForm.expertId = e.userId
  appointForm.topic = ''
  appointForm.description = ''
  appointForm.appointTime = ''
  appointForm.phone = userStore.userInfo?.phone || ''
  appointVisible.value = true
}

async function submitAppoint() {
  if (!appointForm.topic) {
    return ElMessage.warning('请填写预约主题')
  }
  if (!appointForm.appointTime) {
    return ElMessage.warning('请选择期望上门时间')
  }
  appointing.value = true
  try {
    await createAppointmentApi(appointForm)
    ElMessage.success('预约提交成功，等待专家确认！')
    appointVisible.value = false
    detailVisible.value = false
  } catch (e) {
    // 拦截器已提示
  } finally {
    appointing.value = false
  }
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-banner">
      <h2>👨‍🌾 专家团队</h2>
      <p>平台特邀资深农技专家，提供知识分享、在线答疑与上门指导服务</p>
    </div>

    <el-row :gutter="16">
      <el-col :span="8" v-for="e in experts" :key="e.id">
        <el-card shadow="hover" class="expert-card" @click="openDetail(e)">
          <div class="e-head">
            <img :src="e.avatar" class="e-avatar" alt="专家头像" />
            <div>
              <div class="e-name">{{ e.realName }}</div>
              <div class="e-title">{{ e.title }}</div>
            </div>
            <div class="e-rating">
              <el-rate :model-value="e.rating" disabled size="small" />
              <div class="e-rating-num">{{ e.rating }} 分</div>
            </div>
          </div>
          <div class="e-specialty">
            <el-tag v-for="s in e.specialty?.split('、')" :key="s" size="small" type="success" class="spec-tag">
              {{ s }}
            </el-tag>
          </div>
          <div class="e-intro">{{ e.intro }}</div>
          <div class="e-foot">
            <span>从业 {{ e.years }} 年 · 服务 {{ e.serviceCount }} 次</span>
            <span class="e-detail-link">查看详情 &gt;</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 专家详情弹窗 -->
    <el-dialog v-model="detailVisible" width="560px">
      <template v-if="current">
        <div class="d-head">
          <img :src="current.avatar" class="d-avatar" alt="专家头像" />
          <div>
            <div class="d-name">{{ current.realName }} <el-tag size="small" type="success">{{ current.title }}</el-tag></div>
            <div class="d-sub">擅长：{{ current.specialty }}</div>
            <div class="d-sub">
              从业 {{ current.years }} 年 · 评分 {{ current.rating }} · 已服务 {{ current.serviceCount }} 次
            </div>
          </div>
        </div>
        <div class="d-intro-title">专家介绍</div>
        <div class="d-intro">{{ current.intro }}</div>
        <div class="d-actions">
          <el-button @click="goChat(current)">💬 在线咨询</el-button>
          <el-button type="primary" @click="openAppoint(current)">📅 预约专家</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 预约弹窗 -->
    <el-dialog v-model="appointVisible" title="预约专家" width="520px" append-to-body>
      <el-form :model="appointForm" label-width="90px">
        <el-form-item label="预约主题">
          <el-input v-model="appointForm.topic" placeholder="如：小麦病虫害防治指导" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="情况说明">
          <el-input
            v-model="appointForm.description"
            type="textarea"
            :rows="4"
            placeholder="简单描述现场情况，方便专家准备"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="期望时间">
          <el-date-picker
            v-model="appointForm.appointTime"
            type="datetime"
            placeholder="选择期望上门时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="appointForm.phone" placeholder="方便专家联系你" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appointVisible = false">取消</el-button>
        <el-button type="primary" :loading="appointing" @click="submitAppoint">提交预约</el-button>
      </template>
    </el-dialog>
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

.expert-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.e-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.e-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}
.e-name {
  font-size: 16px;
  font-weight: 700;
}
.e-title {
  color: #3d8c40;
  font-size: 13px;
  margin-top: 2px;
}
.e-rating {
  margin-left: auto;
  text-align: right;
}
.e-rating-num {
  color: #e6a23c;
  font-size: 12px;
}
.e-specialty {
  margin: 12px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.e-intro {
  color: #909399;
  font-size: 13px;
  line-height: 20px;
  height: 60px;
  overflow: hidden;
}
.e-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  color: #c0c4cc;
  font-size: 12px;
}
.e-detail-link {
  color: #3d8c40;
}

/* 详情 */
.d-head {
  display: flex;
  gap: 14px;
  align-items: center;
}
.d-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
}
.d-name {
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}
.d-sub {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}
.d-intro-title {
  font-size: 15px;
  font-weight: 600;
  margin: 18px 0 8px;
}
.d-intro {
  color: #303133;
  font-size: 14px;
  line-height: 1.8;
}
.d-actions {
  text-align: right;
  margin-top: 20px;
}
</style>
