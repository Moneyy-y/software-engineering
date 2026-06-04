<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">+ 添加敏感词</el-button>
      <span class="hint">共 {{ list.length }} 条</span>
    </div>

    <el-table :data="list">
      <el-table-column prop="wordId" label="ID" width="80" />
      <el-table-column prop="content" label="敏感词" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-popconfirm title="确认删除该敏感词？" @confirm="remove(row.wordId)">
            <template #reference>
              <el-button size="small" type="danger" plain>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加敏感词弹窗 -->
    <el-dialog v-model="visible" title="添加敏感词" width="480px" @close="resetForm">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        @submit.prevent="submit"
      >
        <el-form-item label="敏感词" prop="content">
          <el-input
            v-model="form.content"
            placeholder="请输入敏感词"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select
            v-model="form.category"
            placeholder="请选择分类"
            style="width:100%"
            clearable
            filterable
            allow-create
          >
            <el-option
              v-for="cat in categoryOptions"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确认添加</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const categoryOptions = ref([])

const form = reactive({
  content: '',
  category: ''
})

const rules = {
  content: [
    { required: true, message: '请输入敏感词内容', trigger: 'blur' },
    { min: 1, max: 50, message: '敏感词长度在 1 到 50 个字符之间', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value && /^\s+$/.test(value)) {
          callback(new Error('敏感词不能为纯空格'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  category: [
    { required: true, message: '请选择或输入分类', trigger: 'change' }
  ]
}

onMounted(() => {
  load()
  loadCategories()
})

async function load() {
  list.value = await request.get('/api/admin/sensitive-word/list') || []
}

async function loadCategories() {
  try {
    categoryOptions.value = await request.get('/api/admin/sensitive-word/categories') || []
  } catch {
    categoryOptions.value = []
  }
}

function openAdd() {
  form.content = ''
  form.category = ''
  visible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await request.post('/api/admin/sensitive-word/add', null, {
      params: { content: form.content.trim(), category: form.category }
    })
    visible.value = false
    ElMessage.success('已添加')
    await load()
    await loadCategories()
  } finally {
    submitting.value = false
  }
}

async function remove(id) {
  await request.post('/api/admin/sensitive-word/delete', null, { params: { wordId: id } })
  ElMessage.success('已删除')
  load()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.hint {
  color: #909399;
  font-size: 13px;
}
</style>
