<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteAddressApi, getAddressListApi, saveAddressApi } from '@/api'

/**
 * 收货地址管理：列表 + 新增/编辑/删除/设默认
 */
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, receiver: '', phone: '', region: '', detail: '', isDefault: 0 })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getAddressListApi()
    list.value = res.data || []
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  form.id = row ? row.id : null
  form.receiver = row?.receiver || ''
  form.phone = row?.phone || ''
  form.region = row?.region || ''
  form.detail = row?.detail || ''
  form.isDefault = row?.isDefault || 0
  dialogVisible.value = true
}

async function save() {
  if (!form.receiver || !form.phone || !form.region || !form.detail) {
    return ElMessage.warning('请完整填写收货人、电话和地址')
  }
  saving.value = true
  try {
    await saveAddressApi(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function setDefault(row) {
  await saveAddressApi({ ...row, isDefault: 1 })
  ElMessage.success('已设为默认地址')
  load()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除这个收货地址吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteAddressApi(row.id)
  ElMessage.success('删除成功')
  load()
}
</script>

<template>
  <div>
    <div class="head">
      <h3 class="page-title">📍 收货地址</h3>
      <el-button type="primary" @click="openEdit(null)">➕ 新增地址</el-button>
    </div>

    <div v-loading="loading">
      <el-card v-for="a in list" :key="a.id" shadow="hover" class="addr-card">
        <div class="addr-main">
          <div class="addr-line1">
            <b>{{ a.receiver }}</b> {{ a.phone }}
            <el-tag v-if="a.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
          <div class="addr-line2">{{ a.region }} {{ a.detail }}</div>
        </div>
        <div class="addr-actions">
          <el-button v-if="a.isDefault !== 1" size="small" link type="primary" @click="setDefault(a)">
            设为默认
          </el-button>
          <el-button size="small" link type="primary" @click="openEdit(a)">编辑</el-button>
          <el-button size="small" link type="danger" @click="remove(a)">删除</el-button>
        </div>
      </el-card>
      <el-empty v-if="!list.length" description="还没有收货地址，下单前先添加一个吧" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="form.receiver" placeholder="收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="所在地区">
          <el-input v-model="form.region" placeholder="如：河南省郑州市金水区" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" placeholder="街道、门牌号等" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
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
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
  flex: 1;
  margin-bottom: 20px;
}
.addr-card {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.addr-line1 {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.addr-line2 {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}
.addr-actions {
  display: flex;
  gap: 4px;
}
</style>
