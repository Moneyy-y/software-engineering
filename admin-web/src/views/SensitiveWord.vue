<template>
  <el-card>
    <el-form inline>
      <el-form-item>
        <el-input v-model="newWord" placeholder="新敏感词" />
      </el-form-item>
      <el-form-item>
        <el-input v-model="newCategory" placeholder="分类" style="width:120px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="add">添加</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list">
      <el-table-column prop="wordId" label="ID" width="80" />
      <el-table-column prop="content" label="敏感词" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row.wordId)">删除</el-button>
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
const newWord = ref('')
const newCategory = ref('default')

onMounted(load)

async function load() {
  list.value = await request.get('/api/admin/sensitive-word/list') || []
}

async function add() {
  if (!newWord.value.trim()) return
  await request.post('/api/admin/sensitive-word/add', null, {
    params: { content: newWord.value, category: newCategory.value }
  })
  newWord.value = ''
  ElMessage.success('已添加')
  load()
}

async function remove(id) {
  await request.post('/api/admin/sensitive-word/delete', null, { params: { wordId: id } })
  ElMessage.success('已删除')
  load()
}
</script>
