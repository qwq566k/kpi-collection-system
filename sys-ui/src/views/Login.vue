<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">科研KPI成果收集系统</h1>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="employeeId">
          <label class="form-label">工号</label>
          <el-input
            v-model="loginForm.employeeId"
            placeholder="请输入您的工号"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <label class="form-label">密码</label>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'
import { parseToken } from '../utils/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  employeeId: '',
  password: ''
})

const loginRules = {
  employeeId: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const response = await login(loginForm)
    
    // 从后端响应中获取用户角色信息
    // 如果后端在响应中直接提供role字段
    let userRole = response.role
    
    // 如果后端没有提供role字段，尝试从token中解析
    if (!userRole && response.token) {
      const tokenInfo = parseToken(response.token)
      if (tokenInfo && tokenInfo.role) {
        userRole = tokenInfo.role
      }
    }
    
    // 如果没有获取到角色信息，报错
    if (!userRole) {
      throw new Error('无法获取用户角色信息')
    }
    
    // 使用store设置用户信息
    userStore.setUserInfo({
      token: response.token,
      id: response.id,
      role: userRole,
      name: response.name || ''
    })
    
    ElMessage.success('登录成功')
    
    // 根据用户角色跳转到对应页面
    if (userRole === 'admin') {
      router.push('/admin')
    } else {
      router.push('/user')
    }
    
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查工号和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  color: #333;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
}

.login-form {
  width: 100%;
}

.form-label {
  display: block;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 4px;
}

:deep(.el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
