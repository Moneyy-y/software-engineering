<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">餐饮管理</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>数据看板</el-menu-item>
        <el-menu-item index="/shop"><el-icon><OfficeBuilding /></el-icon>食堂档口</el-menu-item>
        <el-menu-item index="/dish"><el-icon><Goods /></el-icon>菜品管理</el-menu-item>
        <el-menu-item index="/audit"><el-icon><DocumentChecked /></el-icon>评价审核</el-menu-item>
        <el-menu-item index="/post-audit"><el-icon><ChatLineSquare /></el-icon>帖子审核</el-menu-item>
        <el-menu-item index="/sensitive"><el-icon><Warning /></el-icon>敏感词库</el-menu-item>
        <el-menu-item index="/feedback"><el-icon><ChatDotRound /></el-icon>反馈处理</el-menu-item>
        <el-menu-item index="/redblack"><el-icon><Trophy /></el-icon>红黑榜</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ $route.meta.title }}</span>
        <div>
          <span class="user">{{ userStore.nickname }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

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
