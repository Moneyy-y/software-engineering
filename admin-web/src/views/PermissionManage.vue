<template>
  <el-card>
    <template #header><span>权限管理</span></template>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="菜单列表" name="menus">
        <el-table :data="allMenus">
          <el-table-column prop="menuId" label="ID" width="60" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="path" label="路由" />
          <el-table-column prop="icon" label="图标" />
          <el-table-column prop="sortOrder" label="排序" width="60" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="角色菜单分配" name="roles">
        <el-select v-model="selectedRole" placeholder="选择角色" style="width:200px;margin-bottom:16px" @change="loadRoleMenus">
          <el-option value="admin" label="管理员" />
          <el-option value="auditor" label="审核员" />
          <el-option value="student" label="学生" />
        </el-select>
        <el-checkbox-group v-if="selectedRole" v-model="checkedMenus">
          <div v-for="m in allMenus" :key="m.menuId" style="margin-bottom:8px">
            <el-checkbox :label="m.menuId">
              {{ m.name }}（{{ m.path }}）
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-button v-if="selectedRole" type="primary" @click="saveRoleMenus" style="margin-top:16px">保存分配</el-button>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('menus')
const allMenus = ref([])
const selectedRole = ref('')
const checkedMenus = ref([])

onMounted(async () => {
  allMenus.value = await request.get('/api/permission/menus/all') || []
})

async function loadRoleMenus() {
  const ids = await request.get(`/api/permission/role/menus/${selectedRole.value}`)
  checkedMenus.value = ids || []
}

async function saveRoleMenus() {
  await request.post(`/api/permission/role/menus/${selectedRole.value}`, checkedMenus.value)
  ElMessage.success('角色菜单分配已保存')
}
</script>