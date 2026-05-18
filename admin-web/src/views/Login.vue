<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>高校餐饮管理端</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })

async function handleLogin() {
  loading.value = true
  try {
    const data = await request.post('/api/admin/login', form.value)
    userStore.setLogin(data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误信息已由 request 拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.login-card { width: 400px; padding: 20px; }
h2 { text-align: center; margin-bottom: 24px; color: #303133; }
</style>
