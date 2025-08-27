import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, clearUserInfo, parseToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const userRole = ref(localStorage.getItem('userRole') || '')
  const userName = ref('')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userRole.value === 'admin')
  const isUser = computed(() => userRole.value === 'teacher')

  // 初始化用户信息
  const initUserInfo = () => {
    const userInfo = getUserInfo()
    if (userInfo) {
      token.value = userInfo.token
      userId.value = userInfo.id
      userRole.value = userInfo.role
      userName.value = userInfo.name
    }
  }

  // 设置用户信息
  const setUserInfo = (userData) => {
    const { token: newToken, id, role, name } = userData
    
    // 保存到localStorage
    localStorage.setItem('token', newToken)
    localStorage.setItem('userId', id)
    localStorage.setItem('userRole', role)
    
    // 更新store状态
    token.value = newToken
    userId.value = id
    userRole.value = role
    userName.value = name || ''
  }

  // 清除用户信息
  const logout = () => {
    clearUserInfo()
    token.value = ''
    userId.value = ''
    userRole.value = ''
    userName.value = ''
  }

  // 更新用户角色（从token重新解析）
  const updateUserRole = () => {
    if (token.value) {
      const tokenInfo = parseToken(token.value)
      if (tokenInfo && tokenInfo.role) {
        userRole.value = tokenInfo.role
        localStorage.setItem('userRole', tokenInfo.role)
      }
    }
  }

  // 初始化
  initUserInfo()

  return {
    // 状态
    token,
    userId,
    userRole,
    userName,
    
    // 计算属性
    isLoggedIn,
    isAdmin,
    isUser,
    
    // 方法
    setUserInfo,
    logout,
    updateUserRole,
    initUserInfo
  }
})


