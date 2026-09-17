import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/Register.vue')
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/assessment',
      name: 'Assessment',
      component: () => import('@/views/assessment/Test.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/result/:id',
      name: 'Result',
      component: () => import('@/views/result/Detail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/history',
      name: 'History',
      component: () => import('@/views/result/History.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.isAuthenticated()) {
    next('/login')
  } else if (to.meta.requiresAdmin && userStore.user?.role !== 'ADMIN') {
    next('/home')
  } else {
    next()
  }
})

export default router
