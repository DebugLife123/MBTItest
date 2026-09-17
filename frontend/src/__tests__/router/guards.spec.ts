import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { createAppRouter } from '@/router'

vi.mock('@/api/auth', () => ({
  authApi: {
    login: vi.fn(), register: vi.fn(), logout: vi.fn(),
    getCurrentUser: vi.fn(), refreshToken: vi.fn()
  }
}))

const setAuth = (role: 'USER' | 'ADMIN') => {
  localStorage.setItem('token', 'access-token')
  localStorage.setItem('user', JSON.stringify({ id: 1, username: role === 'ADMIN' ? 'admin' : 'user', role, createdAt: '2026-01-01' }))
}

describe('Router guards', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('allows public routes without authentication', async () => {
    const router = createAppRouter(createMemoryHistory())
    await router.push('/login')
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('redirects unauthenticated users to login', async () => {
    const router = createAppRouter(createMemoryHistory())
    await router.push('/home')
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('allows authenticated users to access protected routes', async () => {
    setAuth('USER')
    const router = createAppRouter(createMemoryHistory())
    await router.push('/home')
    expect(router.currentRoute.value.path).toBe('/home')
  })

  it('blocks non-admin users from admin routes', async () => {
    setAuth('USER')
    const router = createAppRouter(createMemoryHistory())
    await router.push('/admin')
    expect(router.currentRoute.value.path).toBe('/home')
  })

  it('allows admin users to access admin routes', async () => {
    setAuth('ADMIN')
    const router = createAppRouter(createMemoryHistory())
    await router.push('/admin')
    expect(router.currentRoute.value.path).toBe('/admin')
  })
})
