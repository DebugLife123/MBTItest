import { createRouter, createWebHistory, type Router, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

export const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/Register.vue') },
  { path: '/home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { requiresAuth: true } },
  { path: '/assessment', name: 'Assessment', component: () => import('@/views/assessment/Test.vue'), meta: { requiresAuth: true } },
  { path: '/result/:id', name: 'Result', component: () => import('@/views/result/Detail.vue'), meta: { requiresAuth: true } },
  { path: '/user-center', name: 'UserCenter', component: () => import('@/views/user/Center.vue'), meta: { requiresAuth: true } },
  { path: '/history', name: 'History', component: () => import('@/views/result/History.vue'), meta: { requiresAuth: true } },
  { path: '/ai-chat', name: 'AiChat', component: () => import('@/views/ai/Chat.vue'), meta: { requiresAuth: true } },
  { path: '/types', name: 'TypeIndex', component: () => import('@/views/types/Index.vue'), meta: { requiresAuth: true } },
  { path: '/types/:code', name: 'TypeDetail', component: () => import('@/views/types/Detail.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'Admin', component: () => import('@/views/admin/Dashboard.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/analytics', name: 'AdminAnalytics', component: () => import('@/views/admin/Analytics.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/questions', name: 'AdminQuestions', component: () => import('@/views/admin/Questions.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/ai-analysis', name: 'AdminAiAnalysis', component: () => import('@/views/admin/AiAnalysis.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/audit-logs', name: 'AdminAuditLogs', component: () => import('@/views/admin/AuditLogs.vue'), meta: { requiresAuth: true, requiresAdmin: true } }
]

export function createAppRouter(history = createWebHistory()): Router {
  const router = createRouter({ history, routes })
  router.beforeEach((to) => {
    const userStore = useUserStore()
    if (to.meta.requiresAuth && !userStore.isAuthenticated) return '/login'
    if (to.meta.requiresAdmin && !userStore.isAdmin) return '/home'
    return true
  })
  return router
}

export default createAppRouter()
