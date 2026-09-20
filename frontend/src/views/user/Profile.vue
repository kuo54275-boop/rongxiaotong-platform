<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getInfoApi, updateUserProfileApi, uploadImageApi } from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 基础信息：头像上传 + 姓名电话修改
 */
const userStore = useUserStore()

const form = reactive({ realName: '', phone: '', avatar: '' })
const saving = ref(false)
const uploading = ref(false)

onMounted(async () => {
  try {
    const res = await getInfoApi()
    form.realName = res.data.realName || ''
    form.phone = res.data.phone || ''
    form.avatar = res.data.avatar || ''
  } catch (e) {
    // 拦截器已提示
  }
})

/** 头像上传（自定义请求，上传后仅预览，点保存才生效） */
async function handleAvatar(options) {
  uploading.value = true
  try {
    const res = await uploadImageApi(options.file)
    form.avatar = res.data
    ElMessage.success('头像上传成功，记得点保存生效')
  } catch (e) {
    // 拦截器已提示
  } finally {
    uploading.value = false
  }
}

async function save() {
  if (!form.realName) {
    return ElMessage.warning('姓名不能为空')
  }
  saving.value = true
  try {
    await updateUserProfileApi(form)
    await userStore.fetchInfo()   // 刷新顶部导航的头像和名字
    ElMessage.success('保存成功')
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <h3 class="page-title">👤 基础信息</h3>

    <div class="avatar-row">
      <img :src="form.avatar" class="avatar-preview" alt="头像" />
      <div>
        <el-upload
          :show-file-list="false"
          accept="image/*"
          :http-request="handleAvatar"
        >
          <el-button :loading="uploading">上传新头像</el-button>
        </el-upload>
        <div class="tip">支持 jpg / png / gif / webp / svg，点击保存后生效</div>
      </div>
    </div>

    <el-form :model="form" label-width="90px" class="profile-form">
      <el-form-item label="登录账号">
        <el-input :model-value="userStore.userInfo?.username" disabled />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.realName" placeholder="真实姓名 / 单位名称" maxlength="30" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="联系电话" maxlength="20" />
      </el-form-item>
      <el-form-item label="注册时间">
        <el-input :model-value="userStore.userInfo?.createTime" disabled />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存修改</el-button>
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
.avatar-row {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
}
.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid #ecf7ed;
}
.tip {
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 6px;
}
.profile-form {
  max-width: 440px;
}
</style>
