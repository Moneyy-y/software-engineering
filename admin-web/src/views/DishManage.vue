<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">新增菜品</el-button>
      <el-input
        v-model="keyword"
        placeholder="搜索菜品名称"
        clearable
        style="width:220px"
        @clear="load"
        @keyup.enter="load"
      />
      <el-button @click="load">搜索</el-button>
    </div>
    <el-table :data="dishes" style="margin-top:16px">
      <el-table-column label="封面" width="70">
        <template #default="{ row }">
          <el-avatar v-if="row.coverImage" :src="row.coverImage" size="small" />
          <span v-else style="color:#ccc">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="菜品名" />
      <el-table-column prop="price" label="价格" width="80" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ normalizeCategory(row.category) }}</template>
      </el-table-column>
      <el-table-column prop="avgScore" label="评分" width="80" />
      <el-table-column prop="saleCount" label="销量" width="80" />
      <el-table-column prop="shopName" label="食堂" />
      <el-table-column label="操作" width="140" align="center">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row.dishId)">下架</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      v-model="visible"
      :title="form.dishId ? '编辑菜品' : '新增菜品'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜品名称" clearable />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="食堂" prop="shopId">
          <el-select v-model="form.shopId" placeholder="选择食堂" style="width:100%">
            <el-option v-for="s in shops" :key="s.shopId" :label="s.name" :value="s.shopId" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图">
          <div class="upload-wrapper">
            <el-avatar v-if="form.coverImage" :src="form.coverImage" :size="80" shape="square" style="margin-right:12px" />
            <el-upload
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :http-request="uploadCover"
              accept="image/*"
            >
              <el-button type="primary" size="small" :loading="coverUploading">
                {{ form.coverImage ? '更换封面' : '上传封面' }}
              </el-button>
            </el-upload>
            <el-button v-if="form.coverImage" size="small" @click="form.coverImage = ''" style="margin-left:8px">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '../utils/request'
import { listSelectableShops, listStalls } from '../api/shop'
import { ElMessage } from 'element-plus'
import { DISH_CATEGORIES, normalizeCategory, toStallCategory } from '../constants/dishCategories'

const dishes = ref([])
const shops = ref([])
const categories = DISH_CATEGORIES
const keyword = ref('')
const visible = ref(false)
const form = ref({})
const formRef = ref(null)
const coverUploading = ref(false)

const rules = {
  name: [
    { required: true, message: '请输入菜品名称', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value && /^\s+$/.test(value)) {
          callback(new Error('菜品名称不能为纯空格'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入价格'))
        } else if (Number(value) < 0) {
          callback(new Error('价格不能小于 0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  shopId: [
    { required: true, message: '请选择食堂', trigger: 'change' }
  ]
}

onMounted(() => { load(); loadShops() })

async function loadShops() {
  shops.value = await listSelectableShops() || []
}

async function load() {
  const params = { page: 1, size: 100 }
  const kw = keyword.value?.trim()
  if (kw) params.keyword = kw
  const res = await request.get('/api/dish/list', { params })
  dishes.value = res.records || []
}

async function openDialog(row) {
  await loadShops()
  if (row) {
    form.value = {
      ...row,
      category: normalizeCategory(row.category),
      shopId: row.shopId
    }
  } else {
    form.value = {
      name: '',
      price: null,
      category: '',
      shopId: null,
      status: 1
    }
  }
  visible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function resetForm() {
  formRef.value?.resetFields()
}

/**
 * 根据食堂与分类匹配对应档口 ID
 * @param {number} shopId
 * @param {string} category
 * @returns {Promise<number|null>}
 */
async function resolveStallId(shopId, category) {
  const stalls = ((await listStalls(shopId)) || []).filter(s => s.status === 1)
  const stallCategory = toStallCategory(category)
  const matched = stalls.find(s =>
    s.category === stallCategory ||
    s.category === category ||
    s.name === category
  )
  return matched?.stallId ?? stalls[0]?.stallId ?? null
}

function beforeCoverUpload(file) {
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

async function uploadCover(options) {
  coverUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    const data = await request.post('/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.value.coverImage = data.url
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const stallId = await resolveStallId(form.value.shopId, form.value.category)
  if (!stallId) {
    ElMessage.error('该食堂下没有可用档口，请先在食堂档口管理中创建')
    return
  }
  const payload = {
    ...form.value,
    name: form.value.name.trim(),
    stallId,
    category: toStallCategory(form.value.category)
  }
  delete payload.shopId
  await request.post('/api/admin/dish/save', payload)
  visible.value = false
  ElMessage.success('保存成功')
  load()
}

async function remove(id) {
  await request.post('/api/admin/dish/delete', null, { params: { dishId: id } })
  ElMessage.success('已下架')
  load()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.table-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
}
.upload-wrapper { display: flex; align-items: center; }
</style>
