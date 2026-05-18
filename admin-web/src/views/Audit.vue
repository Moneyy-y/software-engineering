<template>
  <el-card>
    <el-form inline>
      <el-form-item label="状态">
        <el-select v-model="status" @change="load">
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="success" :disabled="!selected.length" @click="batchPass">批量通过</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" @selection-change="s => selected = s.map(r => r.reviewId)">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="dishName" label="菜品" />
      <el-table-column prop="score" label="评分" width="80" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="sensitiveHit" label="敏感词" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.sensitiveHit" type="danger">{{ row.sensitiveHit }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="pass(row.reviewId)">通过</el-button>
          <el-button size="small" type="danger" @click="openReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="load" style="margin-top:16px" />
    <el-dialog v-model="rejectVisible" title="拒绝评价">
      <el-input v-model="rejectReason" type="textarea" placeholder="拒绝原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const status = ref('pending')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const selected = ref([])
const rejectVisible = ref(false)
const rejectReason = ref('')
const currentId = ref(null)

onMounted(load)

async function load() {
  const res = await request.get('/api/audit/review/pending', { params: { status: status.value, page: page.value, size: size.value } })
  list.value = res.records || []
  total.value = res.total || 0
}

async function pass(id) {
  await ElMessageBox.confirm('确认通过该评价？')
  await request.post('/api/audit/review/pass', { reviewId: id })
  ElMessage.success('已通过')
  load()
}

function openReject(row) {
  currentId.value = row.reviewId
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  await request.post('/api/audit/review/reject', { reviewId: currentId.value, reason: rejectReason.value })
  rejectVisible.value = false
  ElMessage.success('已拒绝')
  load()
}

async function batchPass() {
  await ElMessageBox.confirm(`确认批量通过 ${selected.value.length} 条？`)
  await request.post('/api/audit/review/batchPass', { reviewIds: selected.value })
  ElMessage.success('批量通过成功')
  load()
}
</script>
