<template>
  <div class="admin-layout">
    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>科研KPI成果收集系统</h2>
      </div>
      <nav class="sidebar-nav">
        <router-link 
          to="/admin" 
          class="nav-item"
          :class="{ active: $route.path === '/admin' }"
        >
          <el-icon><Document /></el-icon>
          成果审核
        </router-link>
        <router-link 
          to="/admin/stats" 
          class="nav-item"
          :class="{ active: $route.path === '/admin/stats' }"
        >
          <el-icon><TrendCharts /></el-icon>
          成果统计
        </router-link>
        <router-link 
          to="/admin/export" 
          class="nav-item"
          :class="{ active: $route.path === '/admin/export' }"
        >
          <el-icon><Download /></el-icon>
          成果导出
        </router-link>
        <router-link 
          to="/admin/users" 
          class="nav-item"
          :class="{ active: $route.path === '/admin/users' }"
        >
          <el-icon><UserFilled /></el-icon>
          用户管理
        </router-link>
      </nav>
    </div>

    <!-- 右侧主内容区域 -->
    <div class="main-content">
      <!-- 顶部用户信息栏 -->
      <div class="top-bar">
        <div class="top-bar-right">
          <el-button type="text" @click="showChangePassword = true">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
          <el-button type="text" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
          <div class="user-info">
            <el-avatar size="small" class="user-avatar">{{ userName?.[0] || '用' }}</el-avatar>
            <span class="user-name">{{ userName || '用户' }}</span>
          </div>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="page-content">
        <slot />
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  TrendCharts, 
  Download, 
  UserFilled, 
  Lock, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { updatePassword, logout } from '../api/user'
import { getUserInfo } from '../utils/auth'

const router = useRouter()

// 修改密码
const showChangePassword = ref(false)
const userName = ref('')
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 修改密码
const handleChangePassword = async () => {
  try {
    const userId = localStorage.getItem('userId')
    await updatePassword({
      id: userId,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    showChangePassword.value = false
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 清除本地存储的用户信息
    localStorage.clear()
    // 跳转到登录页面
    router.push('/login')
  } catch (error) {
    console.error('修改密码失败:', error)
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await logout()
    localStorage.clear()
    router.push('/login')
    ElMessage.success('退出登录成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

// 初始化用户显示名
onMounted(() => {
  const info = getUserInfo()
  if (info && info.name) {
    userName.value = info.name
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background-color: #000000;
  color: white;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-nav {
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  color: white;
  text-decoration: none;
  transition: background-color 0.3s;
}

.nav-item:hover {
  background-color: #3b82f6;
}

.nav-item.active {
  background-color: #3b82f6;
}

.nav-item .el-icon {
  font-size: 18px;
}

.main-content {
  flex: 1;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.top-bar {
  background-color: #f8fafc;
  padding: 16px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.top-bar-right .el-button {
  color: #64748b;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  background-color: #3b82f6;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.page-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
