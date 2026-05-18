<template>
  <el-card>
    <el-button type="primary" @click="openDialog()">新增菜品</el-button>
    <el-table :data="dishes" style="margin-top:16px">
      <el-table-column prop="name" label="菜品名" />
      <el-table-column prop="price" label="价格" width="80" />
      <el-table-column prop="category" label="分类" width="120" />
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
    <el-dialog v-model="visible" :title="form.dishId ? '编辑菜品' : '新增菜品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="档口">
          <el-select v-model="form.stallId" placeholder="选择档口" style="width:100%">
            <el-option v-for="s in stalls" :key="s.stallId" :label="s.name" :value="s.stallId" />
          </el-select>
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
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const dishes = ref([])
const stalls = ref([])
const visible = ref(false)
const form = ref({})

onMounted(() => { load(); loadStalls() })

async function loadStalls() {
  const shops = await request.get('/api/admin/shop/list') || []
  const all = []
  for (const shop of shops) {
    const list = await request.get('/api/admin/stall/list', { params: { shopId: shop.shopId } }) || []
    all.push(...list)
  }
  stalls.value = all
}

async function load() {
  const res = await request.get('/api/dish/list', { params: { page: 1, size: 100 } })
  dishes.value = res.records || []
}

function openDialog(row) {
  form.value = row ? { ...row } : { name: '', price: 10, category: '快餐便当', stallId: 1, status: 1 }
  visible.value = true
}

async function save() {
  await request.post('/api/admin/dish/save', form.value)
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
.table-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: nowrap;
}
</style>
