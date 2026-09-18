<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 登录 / 注册页
 */
const router = useRouter()
const userStore = useUserStore()

const tab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({
  username: '',
  password: '',
  confirm: '',
  realName: '',
  phone: '',
  role: 'FARMER',
})

const roles = [
  { value: 'FARMER', label: '农户' },
  { value: 'BANK', label: '银行' },
  { value: 'BUYER', label: '买家' },
  { value: 'EXPERT', label: '技术专家' },
]

async function onLogin() {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning('请输入账号和密码')
  }
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/')
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  if (!registerForm.username || !registerForm.password) {
    return ElMessage.warning('请输入账号和密码')
  }
  if (registerForm.password.length < 6) {
    return ElMessage.warning('密码长度不能少于 6 位')
  }
  if (registerForm.password !== registerForm.confirm) {
    return ElMessage.warning('两次输入的密码不一致')
  }
  if (!registerForm.realName) {
    return ElMessage.warning('请填写真实姓名')
  }
  loading.value = true
  try {
    await userStore.register(registerForm)
    ElMessage.success('注册成功，已自动登录！')
    router.push('/')
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-title">🌾 融小通农业综合服务平台</div>
      <div class="login-subtitle">融资授信 · 农产品购销 · 农技服务 一站式解决方案</div>

      <el-tabs v-model="tab" stretch class="login-tabs">
        <!-- ===== 登录 ===== -->
        <el-tab-pane label="登 录" name="login">
          <el-form :model="loginForm" @keyup.enter="onLogin">
            <el-form-item>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入账号"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit"
              :loading="loading"
              @click="onLogin"
            >
              登 录
            </el-button>
          </el-form>
          <div class="tips">
            演示账号：farmer01 / buyer01 / expert01 / bank01，密码均为 123456
          </div>
        </el-tab-pane>

        <!-- ===== 注册 ===== -->
        <el-tab-pane label="注 册" name="register">
          <el-form :model="registerForm" label-width="76px">
            <el-form-item label="身份">
              <el-radio-group v-model="registerForm.role">
                <el-radio-button v-for="r in roles" :key="r.value" :value="r.value">
                  {{ r.label }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="账号">
              <el-input v-model="registerForm.username" placeholder="登录账号，如 farmer05" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少 6 位"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input
                v-model="registerForm.confirm"
                type="password"
                placeholder="再次输入密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="registerForm.realName" placeholder="真实姓名 / 单位名称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="registerForm.phone" placeholder="联系电话" />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit"
              :loading="loading"
              @click="onRegister"
            >
              注册并登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
  padding: 20px;
}
.login-box {
  width: 440px;
  background: #fff;
  border-radius: 12px;
  padding: 36px 40px 30px;
  box-shadow: 0 10px 40px rgba(61, 140, 64, 0.25);
}
.login-title {
  font-size: 22px;
  font-weight: 700;
  color: #3d8c40;
  text-align: center;
}
.login-subtitle {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin: 8px 0 20px;
}
.submit {
  width: 100%;
  margin-top: 4px;
}
.tips {
  margin-top: 14px;
  color: #c0c4cc;
  font-size: 12px;
  text-align: center;
  line-height: 20px;
}
</style>
