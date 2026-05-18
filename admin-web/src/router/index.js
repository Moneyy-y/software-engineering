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
      { path: 'redblack', component: () => import('../views/RedBlack.vue'), meta: { title: '红黑榜' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.path !== '/login' && !localStorage.getItem('token')) {
    next('/login')
  } else {
    next()
  }
})

export default router
