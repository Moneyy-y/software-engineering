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
        <el-form-item label="验证码">
          <div class="captcha-container">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 180px" />
            <img 
              :src="captchaImage" 
              alt="验证码" 
              class="captcha-image"
              @click="refreshCaptcha"
              title="点击刷新"
            />
          </div>
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const captchaImage = ref('')
const form = ref({ 
  username: 'admin', 
  password: 'admin123',
  captcha: '',
  captchaKey: ''
})

async function refreshCaptcha() {
  try {
    const data = await request.get('/api/admin/captcha')
    captchaImage.value = data.captchaImage
    form.value.captchaKey = data.captchaKey
    form.value.captcha = ''
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

async function handleLogin() {
  if (!form.value.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  loading.value = true
  try {
    const data = await request.post('/api/admin/login', form.value)
    userStore.setLogin(data)
    await userStore.fetchMenus()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误信息已由 request 拦截器提示
    // 登录失败时刷新验证码
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.login-card { width: 420px; padding: 30px; }
h2 { text-align: center; margin-bottom: 24px; color: #303133; }
.captcha-container { display: flex; align-items: center; gap: 12px; width: 100%; }
.captcha-container .el-input { flex: 1; min-width: 0; }
.captcha-image { width: 120px; height: 40px; cursor: pointer; border-radius: 4px; border: 1px solid #d9d9d9; object-fit: cover; }
</style>