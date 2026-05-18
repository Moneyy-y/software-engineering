<template>
  <el-card>
    <el-form inline>
      <el-form-item label="状态">
        <el-select v-model="status" @change="load">
          <el-option label="全部" value="" />
          <el-option label="待受理" value="pending" />
          <el-option label="处理中" value="processing" />
          <el-option label="已办结" value="resolved" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-table :data="list">
      <el-table-column prop="feedbackId" label="ID" width="80" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" size="small" @click="accept(row.feedbackId)">受理</el-button>
          <el-button v-if="row.status === 'processing'" size="small" type="primary" @click="openReply(row)">回复</el-button>
          <el-button v-if="row.status === 'processing'" size="small" type="success" @click="close(row.feedbackId)">办结</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="replyVisible" title="回复反馈">
      <el-input v-model="replyContent" type="textarea" rows="4" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const status = ref('')
const replyVisible = ref(false)
const replyContent = ref('')
const currentId = ref(null)

onMounted(load)

async function load() {
  const res = await request.get('/api/feedback/list', { params: { status: status.value, page: 1, size: 50 } })
  list.value = res.records || []
}

function statusText(s) {
  return { pending: '待受理', processing: '处理中', resolved: '已办结' }[s] || s
}
function statusType(s) {
  return { pending: 'warning', processing: 'primary', resolved: 'success' }[s] || 'info'
}

async function accept(id) {
  await request.post('/api/feedback/accept', null, { params: { id } })
  ElMessage.success('已受理')
  load()
}

function openReply(row) {
  currentId.value = row.feedbackId
  replyContent.value = row.reply || ''
  replyVisible.value = true
}

async function submitReply() {
  await request.post('/api/feedback/reply', null, { params: { id: currentId.value, content: replyContent.value } })
  replyVisible.value = false
  ElMessage.success('回复成功')
  load()
}

async function close(id) {
  await request.post('/api/feedback/close', null, { params: { id } })
  ElMessage.success('已办结')
  load()
}
</script>
