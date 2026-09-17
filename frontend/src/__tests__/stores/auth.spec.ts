import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

vi.mock('@/api/auth', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
    refreshToken: vi.fn()
  }
}))

const response = (data: any) => ({ code: 0, message: 'OK', data, timestamp: '', path: null, traceId: null })

describe('Auth Store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('restores user and role from localStorage on startup', () => {
    localStorage.setItem('token', 'persisted-token')
    localStorage.setItem('user', JSON.stringify({ id: 1, username: 'admin', role: 'ADMIN', createdAt: '2026-01-01' }))
    const store = useUserStore()
    expect(store.token).toBe('persisted-token')
    expect(store.user?.role).toBe('ADMIN')
    expect(store.isAuthenticated).toBe(true)
    expect(store.isAdmin).toBe(true)
  })

  it('persists the server user after login', async () => {
    vi.mocked(authApi.login).mockResolvedValue(response({
      token: 'access', refreshToken: 'refresh',
      user: { id: 2, username: 'user', role: 'USER', createdAt: '2026-01-01' }
    }))
    await useUserStore().login('user', 'password123')
    expect(localStorage.getItem('token')).toBe('access')
    expect(localStorage.getItem('refreshToken')).toBe('refresh')
    expect(JSON.parse(localStorage.getItem('user')!).role).toBe('USER')
  })

  it('refreshes the access token and updates persisted user', async () => {
    localStorage.setItem('refreshToken', 'old-refresh')
    vi.mocked(authApi.refreshToken).mockResolvedValue(response({
      token: 'new-access', refreshToken: 'new-refresh',
      user: { id: 3, username: 'admin', role: 'ADMIN', createdAt: '2026-01-01' }
    }))
    await useUserStore().refresh()
    expect(localStorage.getItem('token')).toBe('new-access')
    expect(localStorage.getItem('refreshToken')).toBe('new-refresh')
  })

  it('clears all auth state on logout', async () => {
    localStorage.setItem('token', 'access')
    localStorage.setItem('refreshToken', 'refresh')
    localStorage.setItem('user', JSON.stringify({ id: 1, username: 'user', role: 'USER', createdAt: '2026-01-01' }))
    vi.mocked(authApi.logout).mockResolvedValue(response(undefined))
    const store = useUserStore()
    await store.logout()
    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
  })
})
