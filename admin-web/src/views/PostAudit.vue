<template>
  <el-card>
    <el-form inline class="toolbar">
      <el-form-item label="状态">
        <el-select
          v-model="filterStatus"
          placeholder="请选择状态"
          style="width: 140px"
          @change="onFilterChange"
        >
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
          <el-option label="全部" value="all" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="filterStatus === 'pending'">
        <el-button type="success" :disabled="!selected.length" @click="batchApprove">批量通过</el-button>
        <el-button type="danger" :disabled="!selected.length" @click="openBatchReject">批量拒绝</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="danger" plain :disabled="!selected.length" @click="batchDelete">批量删除</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" @selection-change="onSelect">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="title" label="标题" min-width="120" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="zone" label="分区" width="90" />
      <el-table-column prop="auditStatus" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.auditStatus)">{{ statusLabel(row.auditStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sensitiveHit" label="敏感词" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.sensitiveHit" type="danger">{{ row.sensitiveHit }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="拒绝原因" width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 'pending'">
            <el-button size="small" type="success" @click="approve(row.postId)">通过</el-button>
            <el-button size="small" type="danger" @click="openReject(row)">拒绝</el-button>
          </template>
          <el-popconfirm title="确认删除该帖子？删除后不可恢复" @confirm="remove(row.postId)">
            <template #reference>
              <el-button size="small" type="danger" plain>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:16px"
    />

    <el-dialog v-model="rejectVisible" :title="rejectBatch ? '批量拒绝帖子' : '拒绝帖子'" width="480px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请填写拒绝原因（必填）"
        maxlength="500"
        show-word-limit
      />
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
const filterStatus = ref('all')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const selected = ref([])
const loading = ref(false)
const rejectVisible = ref(false)
const rejectReason = ref('')
const rejectBatch = ref(false)
const rejectIds = ref([])

onMounted(load)

function statusLabel(s) {
  return { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[s] || s
}

function statusTag(s) {
  return { pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info'
}

function onFilterChange() {
  page.value = 1
  selected.value = []
  load()
}

function onSelect(rows) {
  selected.value = rows.map(r => r.postId)
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/api/admin/post/pending', {
      params: { status: filterStatus.value, page: page.value, size: size.value }
    })
    list.value = res?.records || []
    total.value = res?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  await request.post('/api/admin/post/approve', null, { params: { postId: id } })
  ElMessage.success('已通过')
  load()
}

async function batchApprove() {
  await ElMessageBox.confirm(`确认批量通过 ${selected.value.length} 条帖子？`)
  await request.post('/api/admin/post/approve/batch', selected.value)
  ElMessage.success('批量通过成功')
  selected.value = []
  load()
}

function openReject(row) {
  rejectBatch.value = false
  rejectIds.value = [row.postId]
  rejectReason.value = ''
  rejectVisible.value = true
}

function openBatchReject() {
  rejectBatch.value = true
  rejectIds.value = [...selected.value]
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  if (rejectBatch.value) {
    await request.post('/api/admin/post/reject/batch', {
      postIds: rejectIds.value,
      reason: rejectReason.value.trim()
    })
    ElMessage.success('批量拒绝成功')
    selected.value = []
  } else {
    await request.post('/api/admin/post/reject', null, {
      params: { postId: rejectIds.value[0], reason: rejectReason.value.trim() }
    })
    ElMessage.success('已拒绝')
  }
  rejectVisible.value = false
  load()
}

async function remove(id) {
  await request.post('/api/admin/post/delete', null, { params: { postId: id } })
  ElMessage.success('已删除')
  load()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selected.value.length} 条帖子？`, '警告', { type: 'warning' })
  await request.post('/api/admin/post/delete/batch', selected.value)
  ElMessage.success('批量删除成功')
  selected.value = []
  load()
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 8px;
}
</style>
