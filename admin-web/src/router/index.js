import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'dish', component: () => import('../views/DishManage.vue'), meta: { title: '菜品管理' } },
      { path: 'shop', component: () => import('../views/ShopManage.vue'), meta: { title: '食堂档口' } },
      { path: 'audit', component: () => import('../views/Audit.vue'), meta: { title: '评价审核' } },
      { path: 'post-audit', component: () => import('../views/PostAudit.vue'), meta: { title: '帖子审核' } },
      { path: 'sensitive', component: () => import('../views/SensitiveWord.vue'), meta: { title: '敏感词库' } },
      { path: 'feedback', component: () => import('../views/Feedback.vue'), meta: { title: '反馈处理' } },
      { path: 'redblack', component: () => import('../views/RedBlack.vue'), meta: { title: '红黑榜' } },
      { path: 'report', component: () => import('../views/ReportManage.vue'), meta: { title: '举报管理' } },
      { path: 'permission', component: () => import('../views/PermissionManage.vue'), meta: { title: '权限管理' } },
      { path: 'user-manage', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.path !== '/login' && !localStorage.getItem('token')) {
    next('/login')
    return
  }
  if (to.path === '/login' && localStorage.getItem('token')) {
    next('/dashboard')
    return
  }
  const menus = JSON.parse(localStorage.getItem('menus') || '[]')
  const allowedPaths = menus.map(m => m.path)
  if (to.path !== '/' && to.path !== '/dashboard' && to.path !== '/login' && allowedPaths.length > 0) {
    if (!allowedPaths.includes(to.path)) {
      if (from.path && from.path !== '/login') {
        next(false)
      } else {
        next('/dashboard')
      }
      return
    }
  }
  next()
})

export default router
