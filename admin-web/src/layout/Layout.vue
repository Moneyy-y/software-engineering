<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">餐饮管理</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item
          v-for="menu in userStore.menus"
          :key="menu.menuId"
          :index="menu.path"
        >
          <el-icon v-if="getIconComponent(menu.icon)">
            <component :is="getIconComponent(menu.icon)" />
          </el-icon>
          <span>{{ menu.name }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ $route.meta.title }}</span>
        <div>
          <span class="user">{{ userStore.nickname }}（{{ roleLabel }}）</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { getIconComponent } from '../utils/iconMap'

const userStore = useUserStore()
const router = useRouter()

const roleLabel = computed(() => {
  const map = { admin: '管理员', auditor: '审核员' }
  return map[userStore.role] || userStore.role
})

onMounted(() => {
  if (userStore.token && userStore.menus.length === 0) {
    userStore.fetchMenus()
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #304156; }
.logo { color: #fff; font-size: 18px; font-weight: bold; padding: 20px; text-align: center; border-bottom: 1px solid #3d4f66; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.user { margin-right: 12px; color: #606266; }
</style>
