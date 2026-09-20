<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfileApi, updateProfileApi } from '@/api'

/**
 * 专家信息管理：职称、擅长领域、介绍、从业年限
 */
const form = reactive({ title: '', specialty: '', intro: '', years: null })
const extra = ref({ rating: 0, serviceCount: 0 })
const saving = ref(false)

onMounted(async () => {
  try {
    const res = await getMyProfileApi()
    if (res.data) {
      form.title = res.data.title || ''
      form.specialty = res.data.specialty || ''
      form.intro = res.data.intro || ''
      form.years = res.data.years ?? null
      extra.value = { rating: res.data.rating || 0, serviceCount: res.data.serviceCount || 0 }
    }
  } catch (e) {
    // 拦截器已提示
  }
})

async function save() {
  if (!form.title || !form.specialty) {
    return ElMessage.warning('请填写职称和擅长领域')
  }
  saving.value = true
  try {
    await updateProfileApi(form)
    ElMessage.success('专家信息已更新')
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <h3 class="page-title">🎓 专家信息管理</h3>

    <div class="stats">
      <div class="stat-card">
        <div class="stat-num">⭐ {{ extra.rating }}</div>
        <div class="stat-label">当前评分</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ extra.serviceCount }}</div>
        <div class="stat-label">累计服务次数</div>
      </div>
    </div>

    <el-form :model="form" label-width="90px" class="form">
      <el-form-item label="职称">
        <el-input v-model="form.title" placeholder="如：高级农艺师" maxlength="20" />
      </el-form-item>
      <el-form-item label="擅长领域">
        <el-input v-model="form.specialty" placeholder="如：小麦种植、病虫害防治（用顿号分隔）" maxlength="50" />
      </el-form-item>
      <el-form-item label="从业年限">
        <el-input-number v-model="form.years" :min="0" :max="60" controls-position="right" />
        <span class="tip">年</span>
      </el-form-item>
      <el-form-item label="专家介绍">
        <el-input
          v-model="form.intro"
          type="textarea"
          :rows="5"
          placeholder="介绍你的从业经历、擅长方向、服务成果等，展示在专家团队页"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.stats {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
}
.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 14px 24px;
  text-align: center;
}
.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #3d8c40;
}
.stat-label {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
.form {
  max-width: 520px;
}
.tip {
  margin-left: 8px;
  color: #909399;
}
</style>
