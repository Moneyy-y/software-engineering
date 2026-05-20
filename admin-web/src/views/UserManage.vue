<template>
  <el-card>
    <template #header>
      <span>用户管理</span>
      <div style="float:right">
        <el-input v-model="search.username" placeholder="用户名" clearable style="width:140px;margin-right:8px" @clear="load" @keyup.enter="load" />
        <el-select v-model="search.role" placeholder="角色" clearable style="width:120px;margin-right:8px" @change="load">
          <el-option value="admin" label="管理员" />
          <el-option value="auditor" label="审核员" />
          <el-option value="student" label="学生" />
        </el-select>
        <el-select v-model="search.status" placeholder="状态" clearable style="width:100px" @change="load">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="停用" />
        </el-select>
      </div>
    </template>
    <el-table :data="users">
      <el-table-column prop="userId" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="mobile" label="手机号" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : row.role === 'auditor' ? 'warning' : 'info'">
            {{ roleMap[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            :disabled="row.role === 'admin' && row.userId === 1"
            @change="toggleStatus(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" @click="openRoleDialog(row)">改角色</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top:16px;justify-content:flex-end"
      @current-change="load"
    />
  </el-card>
  <el-dialog v-model="roleVisible" title="修改角色" width="360px">
    <el-form label-width="80px">
      <el-form-item label="当前角色">
        <el-tag>{{ roleMap[roleForm.role] || roleForm.role }}</el-tag>
      </el-form-item>
      <el-form-item label="新角色">
        <el-select v-model="roleForm.newRole" style="width:100%">
          <el-option value="admin" label="管理员" />
          <el-option value="auditor" label="审核员" />
          <el-option value="student" label="学生" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roleVisible = false">取消</el-button>
      <el-button type="primary" :disabled="!roleForm.newRole" @click="saveRole">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const users = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const search = reactive({ username: '', role: '', status: '' })
const roleVisible = ref(false)
const roleForm = reactive({ userId: null, role: '', newRole: '' })

const roleMap = { admin: '管理员', auditor: '审核员', student: '学生' }

onMounted(load)

async function load() {
  const params = { page: page.value, size: size.value }
  if (search.username) params.username = search.username
  if (search.role) params.role = search.role
  if (search.status !== '') params.status = search.status
  const data = await request.get('/api/admin/user/list', { params })
  users.value = data.records || []
  total.value = data.total || 0
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.post('/api/admin/user/save', { userId: row.userId, status: newStatus })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已停用')
  } catch {
    // error handled by interceptor
  }
}

function openRoleDialog(row) {
  roleForm.userId = row.userId
  roleForm.role = row.role
  roleForm.newRole = ''
  roleVisible.value = true
}

async function saveRole() {
  if (roleForm.newRole === roleForm.role) {
    ElMessage.info('角色未变更')
    roleVisible.value = false
    return
  }
  try {
    await request.post('/api/admin/user/changeRole', null, {
      params: { userId: roleForm.userId, role: roleForm.newRole }
    })
    ElMessage.success('角色已修改')
    roleVisible.value = false
    load()
  } catch {
    // error handled by interceptor
  }
}
</script>