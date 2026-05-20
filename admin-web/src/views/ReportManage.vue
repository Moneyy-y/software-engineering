<template>
  <el-card>
    <template #header>
      <span>举报管理</span>
      <el-select v-model="statusFilter" style="float:right;width:140px" @change="load">
        <el-option value="pending" label="待处理" />
        <el-option value="handled" label="已处理" />
        <el-option value="all" label="全部" />
      </el-select>
    </template>
    <el-table :data="reports">
      <el-table-column prop="reportId" label="ID" width="60" />
      <el-table-column prop="targetType" label="目标类型" width="80" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="reason" label="举报原因" width="120" />
      <el-table-column prop="description" label="详细描述" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleResult" label="处理说明" width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <template v-if="row.status === 'pending'">
            <el-popconfirm title="确认通过此举报？" @confirm="handle(row.reportId, 'approved', '已处理')">
              <template #reference><el-button size="small" type="success">通过</el-button></template>
            </el-popconfirm>
            <el-popconfirm title="确认驳回此举报？" @confirm="handle(row.reportId, 'rejected', '已驳回')">
              <template #reference><el-button size="small" type="danger">驳回</el-button></template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const reports = ref([])
const statusFilter = ref('pending')

onMounted(load)

function statusLabel(s) {
  return { pending: '待处理', approved: '已通过', rejected: '已驳回' }[s] || s
}

function statusTag(s) {
  return { pending: 'warning', approved: 'success', rejected: 'info' }[s] || 'info'
}

async function load() {
  reports.value = await request.get('/api/report/list', { params: { status: statusFilter.value } }) || []
}

async function handle(id, status, handleResult) {
  await request.put(`/api/report/handle/${id}`, { status, handleResult })
  ElMessage.success('处理完成')
  load()
}
</script>