<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <template #header>
          <span>食堂/商铺</span>
          <el-button type="primary" size="small" style="float:right" @click="openShop()">新增</el-button>
        </template>
        <el-table :data="shops" highlight-current-row @current-change="onShopSelect">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型" width="80">
            <template #default="{ row }">{{ row.type === 0 ? '食堂' : '商铺' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="openShop(row)">编辑</el-button>
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
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="openStall(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>
  <el-dialog v-model="shopVisible" title="食堂/商铺" width="480px">
    <el-form :model="shopForm" label-width="80px">
      <el-form-item label="名称"><el-input v-model="shopForm.name" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="shopForm.type"><el-option :value="0" label="食堂" /><el-option :value="1" label="周边商铺" /></el-select>
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
  <el-dialog v-model="stallVisible" title="档口" width="400px">
    <el-form :model="stallForm" label-width="80px">
      <el-form-item label="名称"><el-input v-model="stallForm.name" /></el-form-item>
      <el-form-item label="品类"><el-input v-model="stallForm.category" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="stallVisible = false">取消</el-button>
      <el-button type="primary" @click="saveStall">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const shops = ref([])
const stalls = ref([])
const currentShop = ref(null)
const shopVisible = ref(false)
const stallVisible = ref(false)
const shopForm = ref({})
const stallForm = ref({})

onMounted(loadShops)

async function loadShops() {
  shops.value = await request.get('/api/admin/shop/list') || []
}

async function loadStalls() {
  if (!currentShop.value) return
  stalls.value = await request.get('/api/admin/stall/list', { params: { shopId: currentShop.value.shopId } }) || []
}

function onShopSelect(row) {
  currentShop.value = row
  loadStalls()
}

function openShop(row) {
  shopForm.value = row ? { ...row } : { name: '', type: 0, address: '', status: 1 }
  shopVisible.value = true
}

async function saveShop() {
  await request.post('/api/admin/shop/save', shopForm.value)
  shopVisible.value = false
  ElMessage.success('已保存')
  loadShops()
}

function openStall(row) {
  stallForm.value = row ? { ...row } : { shopId: currentShop.value.shopId, name: '', category: '', status: 1 }
  stallVisible.value = true
}

async function saveStall() {
  await request.post('/api/admin/stall/save', stallForm.value)
  stallVisible.value = false
  ElMessage.success('已保存')
  loadStalls()
}
</script>
