<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePasswordApi } from '@/api'
import { useUserStore } from '@/stores/user'

/**
 * 修改密码：成功后自动退出，用新密码重新登录
 */
const router = useRouter()
const userStore = useUserStore()

const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const saving = ref(false)

async function save() {
  if (!form.oldPassword || !form.newPassword) {
    return ElMessage.warning('请填写原密码和新密码')
  }
  if (form.newPassword.length < 6) {
    return ElMessage.warning('新密码长度不能少于 6 位')
  }
  if (form.newPassword !== form.confirm) {
    return ElMessage.warning('两次输入的新密码不一致')
  }
  saving.value = true
  try {
    await changePasswordApi({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码修改成功，请用新密码重新登录')
    userStore.logout()
    router.push('/login')
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <h3 class="page-title">🔒 修改密码</h3>
    <el-form :model="form" label-width="90px" class="pwd-form">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" placeholder="请输入当前使用的密码" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" placeholder="至少 6 位" show-password />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.confirm" type="password" placeholder="再次输入新密码" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">修改密码</el-button>
      </el-form-item>
    </el-form>
    <div class="tip">💡 修改成功后需要重新登录</div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}
.pwd-form {
  max-width: 400px;
}
.tip {
  color: #909399;
  font-size: 13px;
}
</style>
