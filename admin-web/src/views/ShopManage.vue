<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <template #header>
          <span>食堂/商铺</span>
          <el-button type="primary" size="small" style="float:right" @click="openShop()">新增</el-button>
        </template>
        <el-table :data="shops" highlight-current-row @current-change="onShopSelect">
          <el-table-column label="Logo" width="70">
            <template #default="{ row }">
              <el-avatar v-if="row.logo" :src="row.logo" size="small" />
              <span v-else style="color:#ccc">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型" width="80">
            <template #default="{ row }">{{ row.type === 0 ? '食堂' : '商铺' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="openShop(row)">编辑</el-button>
              <el-button
                v-if="row.status === 1"
                size="small"
                type="danger"
                @click="removeShop(row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header>
          <span>档口（{{ currentShop?.name || '请先选择食堂' }}）</span>
          <el-button type="primary" size="small" style="float:right" :disabled="!currentShop" @click="openStall()">新增档口</el-button>
        </template>
        <el-table :data="stalls">
          <el-table-column prop="name" label="档口名" />
          <el-table-column prop="category" label="品类" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="openStall(row)">编辑</el-button>
              <el-button
                v-if="row.status === 1"
                size="small"
                type="danger"
                @click="removeStall(row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
  <el-dialog v-model="shopVisible" title="食堂/商铺" width="520px" @close="resetShopForm">
    <el-form ref="shopFormRef" :model="shopForm" :rules="shopRules" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="shopForm.name" placeholder="请输入名称" clearable />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="shopForm.type" placeholder="请选择类型" style="width:100%">
          <el-option :value="0" label="食堂" />
          <el-option :value="1" label="周边商铺" />
        </el-select>
      </el-form-item>
      <el-form-item label="Logo">
        <div class="upload-wrapper">
          <el-avatar v-if="shopForm.logo" :src="shopForm.logo" :size="80" shape="square" style="margin-right:12px" />
          <el-upload
            :show-file-list="false"
            :before-upload="beforeShopLogoUpload"
            :http-request="uploadShopLogo"
            accept="image/*"
          >
            <el-button type="primary" size="small" :loading="shopLogoUploading">
              {{ shopForm.logo ? '更换Logo' : '上传Logo' }}
            </el-button>
          </el-upload>
          <el-button v-if="shopForm.logo" size="small" @click="shopForm.logo = ''" style="margin-left:8px">移除</el-button>
        </div>
      </el-form-item>
      <el-form-item label="地址"><el-input v-model="shopForm.address" /></el-form-item>
      <el-form-item label="经度"><el-input v-model="shopForm.lng" /></el-form-item>
      <el-form-item label="纬度"><el-input v-model="shopForm.lat" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="shopVisible = false">取消</el-button>
      <el-button type="primary" @click="saveShop">保存</el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="stallVisible" title="档口" width="400px" @close="resetStallForm">
    <el-form ref="stallFormRef" :model="stallForm" :rules="stallRules" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="stallForm.name" placeholder="请输入档口名称" clearable />
      </el-form-item>
      <el-form-item label="品类">
        <el-input v-model="stallForm.category" placeholder="如：快餐便当" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="stallVisible = false">取消</el-button>
      <el-button type="primary" @click="saveStall">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAllShops,
  listStalls,
  saveShop as saveShopApi,
  deleteShop,
  saveStall as saveStallApi,
  deleteStall
} from '../api/shop'
import request from '../utils/request'

const shops = ref([])
const stalls = ref([])
const currentShop = ref(null)
const shopVisible = ref(false)
const stallVisible = ref(false)
const shopForm = reactive({ name: '', type: 0, address: '', logo: '', lng: '', lat: '', status: 1, shopId: null })
const stallForm = reactive({ name: '', category: '', shopId: null, status: 1, stallId: null })
const shopFormRef = ref(null)
const stallFormRef = ref(null)
const shopLogoUploading = ref(false)

const shopRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value && /^\s+$/.test(value)) callback(new Error('名称不能为纯空格'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ]
}

const stallRules = {
  name: [
    { required: true, message: '请输入档口名称', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value && /^\s+$/.test(value)) callback(new Error('名称不能为纯空格'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

onMounted(loadShops)

async function loadShops() {
  shops.value = await listAllShops() || []
  if (currentShop.value) {
    const found = shops.value.find(s => s.shopId === currentShop.value.shopId)
    currentShop.value = found || null
    if (currentShop.value) loadStalls()
    else stalls.value = []
  }
}

async function loadStalls() {
  if (!currentShop.value) return
  stalls.value = await listStalls(currentShop.value.shopId) || []
}

function onShopSelect(row) {
  currentShop.value = row
  loadStalls()
}

function resetShopForm() {
  shopFormRef.value?.resetFields()
}

function resetStallForm() {
  stallFormRef.value?.resetFields()
}

function openShop(row) {
  if (row) {
    Object.assign(shopForm, {
      shopId: row.shopId,
      name: row.name,
      type: row.type,
      address: row.address || '',
      logo: row.logo || '',
      lng: row.lng,
      lat: row.lat,
      status: row.status ?? 1
    })
  } else {
    Object.assign(shopForm, {
      shopId: null,
      name: '',
      type: null,
      address: '',
      logo: '',
      lng: '',
      lat: '',
      status: 1
    })
  }
  shopVisible.value = true
  nextTick(() => shopFormRef.value?.clearValidate())
}

function openStall(row) {
  if (row) {
    Object.assign(stallForm, {
      stallId: row.stallId,
      shopId: row.shopId,
      name: row.name,
      category: row.category || '',
      status: row.status ?? 1
    })
  } else {
    Object.assign(stallForm, {
      stallId: null,
      shopId: currentShop.value.shopId,
      name: '',
      category: '',
      status: 1
    })
  }
  stallVisible.value = true
  nextTick(() => stallFormRef.value?.clearValidate())
}

function beforeShopLogoUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function uploadShopLogo(options) {
  shopLogoUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const data = await request.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    shopForm.logo = data.url
    ElMessage.success('Logo 上传成功')
  } catch {
    ElMessage.error('Logo 上传失败')
  } finally {
    shopLogoUploading.value = false
  }
}

function buildShopPayload() {
  const payload = {
    name: shopForm.name.trim(),
    type: shopForm.type,
    address: shopForm.address,
    logo: shopForm.logo,
    lng: shopForm.lng,
    lat: shopForm.lat,
    status: shopForm.status ?? 1
  }
  if (shopForm.shopId) payload.shopId = shopForm.shopId
  return payload
}

function isDuplicateShopName(name, excludeId) {
  return shops.value.some(s => s.status === 1 && s.name === name && s.shopId !== excludeId)
}

function isDuplicateStallName(name, excludeId) {
  return stalls.value.some(s => s.status === 1 && s.name === name && s.stallId !== excludeId)
}

async function saveShop() {
  const valid = await shopFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const name = shopForm.name.trim()
  if (isDuplicateShopName(name, shopForm.shopId)) {
    ElMessage.error('食堂名称已存在')
    return
  }
  try {
    await saveShopApi(buildShopPayload())
    shopVisible.value = false
    ElMessage.success('已保存')
    loadShops()
  } catch {
    // 错误信息由 request 拦截器提示
  }
}

async function saveStall() {
  const valid = await stallFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const name = stallForm.name.trim()
  if (isDuplicateStallName(name, stallForm.stallId)) {
    ElMessage.error('该食堂下档口名称已存在')
    return
  }
  const payload = {
    shopId: stallForm.shopId,
    name,
    category: stallForm.category?.trim() || '',
    status: stallForm.status ?? 1
  }
  if (stallForm.stallId) payload.stallId = stallForm.stallId
  try {
    await saveStallApi(payload)
    stallVisible.value = false
    ElMessage.success('已保存')
    loadStalls()
  } catch {
    // 错误信息由 request 拦截器提示
  }
}

async function removeShop(row) {
  if (!row?.shopId) {
    ElMessage.error('食堂ID无效，请刷新页面后重试')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除「${row.name}」？删除后菜品管理和小程序将不再显示该食堂。`,
      '删除食堂',
      { type: 'warning' }
    )
    await deleteShop(row)
    ElMessage.success('已删除')
    if (currentShop.value?.shopId === row.shopId) {
      currentShop.value = null
      stalls.value = []
    }
    loadShops()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      // 错误信息由 request 拦截器提示
    }
  }
}

async function removeStall(row) {
  if (!row?.stallId) {
    ElMessage.error('档口ID无效，请刷新页面后重试')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除档口「${row.name}」？`, '删除档口', { type: 'warning' })
    await deleteStall(row)
    ElMessage.success('已删除')
    loadStalls()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      // 错误信息由 request 拦截器提示
    }
  }
}
</script>

<style scoped>
.upload-wrapper { display: flex; align-items: center; }
</style>
