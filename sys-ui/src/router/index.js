import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import UserHome from '../views/UserHome.vue'
import AdminAudit from '../views/admin/Audit.vue'
import AdminStats from '../views/admin/Stats.vue'
import AdminExport from '../views/admin/Export.vue'
import AdminUsers from '../views/admin/Users.vue'

const routes = [
  {
    path: '/',
    redirect: '/login' // 默认跳转到登录页
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {title: '大数据与计算机学院-科研成果收集'}
  },
  {
    path: '/user',
    name: 'UserHome',
    component: UserHome,
    meta: { requiresAuth: true, role: 'teacher', title: '大数据与计算机学院-科研成果收集' }
  },
  {
    path: '/admin',
    name: 'AdminAudit',
    component: AdminAudit,
    meta: { requiresAuth: true, role: 'admin', title: '大数据与计算机学院-科研成果收集' }
  },
  {
    path: '/admin/stats',
    name: 'AdminStats',
    component: AdminStats,
    meta: { requiresAuth: true, role: 'admin', title: '大数据与计算机学院-科研成果收集' }
  },
  {
    path: '/admin/export',
    name: 'AdminExport',
    component: AdminExport,
    meta: { requiresAuth: true, role: 'admin', title: '大数据与计算机学院-科研成果收集' }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers,
    meta: { requiresAuth: true, role: 'admin', title: '大数据与计算机学院-科研成果收集' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  } else {
    document.title = '科研KPI成果收集系统' // 默认标题
  }
  
  // 直接从localStorage获取用户信息，避免在路由守卫中使用store
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  // 导入token验证工具
  import('../utils/auth.js').then(({ isTokenExpired, getUserInfo }) => {
    if (to.meta.requiresAuth) {
      if (!token || isTokenExpired(token)) {
        // 清除过期的token
        if (token) {
          localStorage.removeItem('token')
          localStorage.removeItem('userRole')
        }
        next('/login')
      } else {
        // 从token中重新解析用户信息，确保角色信息正确
        const userInfo = getUserInfo()
        const currentRole = userInfo?.role || userRole
        
        if (to.meta.role && to.meta.role !== currentRole) {
          // 角色不匹配，重定向到对应的首页
          if (currentRole === 'admin') {
            next('/admin')
          } else {
            next('/user')
          }
        } else {
          next()
        }
      }
    } else {
      // 如果已登录用户访问登录页，重定向到对应的首页
      if (to.path === '/login' && token && !isTokenExpired(token)) {
        const userInfo = getUserInfo()
        const currentRole = userInfo?.role || userRole
        
        if (currentRole === 'admin') {
          next('/admin')
        } else {
          next('/user')
        }
      } else {
        next()
      }
    }
  }).catch(() => {
    // 如果导入失败，使用简单逻辑
    if (to.meta.requiresAuth && !token) {
      next('/login')
    } else if (to.path === '/login' && token) {
      if (userRole === 'admin') {
        next('/admin')
      } else {
        next('/user')
      }
    } else {
      next()
    }
  })
})

export default router
