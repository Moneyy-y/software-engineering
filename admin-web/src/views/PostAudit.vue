<template>
  <el-card>
    <el-table :data="list">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="zone" label="分区" width="100" />
      <el-table-column prop="sensitiveHit" label="敏感词" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.sensitiveHit" type="danger">{{ row.sensitiveHit }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(row.postId)">通过</el-button>
          <el-button size="small" type="danger" @click="reject(row.postId)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])

onMounted(load)

async function load() {
  const res = await request.get('/api/admin/post/pending', { params: { page: 1, size: 50 } })
  list.value = res.records || []
}

async function approve(id) {
  await request.post('/api/admin/post/approve', null, { params: { postId: id } })
  ElMessage.success('已通过')
  load()
}

async function reject(id) {
  await request.post('/api/admin/post/reject', null, { params: { postId: id } })
  ElMessage.success('已拒绝')
  load()
}
</script>
