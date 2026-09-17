import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { User } from '@/types'
import { authApi } from '@/api/auth'
import { configureTokenRefresher } from '@/utils/request'

const USER_STORAGE_KEY = 'user'

function readStoredUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY)
    return raw ? JSON.parse(raw) as User : null
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(readStoredUser())
  const token = ref<string | null>(localStorage.getItem('token'))

  const setUser = (newUser: User | null) => {
    user.value = newUser
    if (newUser) localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(newUser))
    else localStorage.removeItem(USER_STORAGE_KEY)
  }

  const setToken = (newToken: string | null) => {
    token.value = newToken
    if (newToken) localStorage.setItem('token', newToken)
    else localStorage.removeItem('token')
  }

  const persistAuth = (payload: { token: string; refreshToken: string; user: User }) => {
    setToken(payload.token)
    setUser(payload.user)
    if (payload.refreshToken) localStorage.setItem('refreshToken', payload.refreshToken)
  }

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    if (res.code === 0 && res.data) persistAuth(res.data)
    return res
  }

  const register = async (username: string, password: string, nickname?: string, email?: string) => {
    const res = await authApi.register({ username, password, nickname, email })
    if (res.code === 0 && res.data) persistAuth(res.data)
    return res
  }

  const refresh = async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) throw new Error('缺少刷新令牌')
    const res = await authApi.refreshToken(refreshToken)
    if (res.code === 0 && res.data) persistAuth(res.data)
    return res
  }

  // HTTP layer triggers this after a 401; the active store remains the single owner of persisted auth data.
  const registerTokenRefresher = () => configureTokenRefresher(async (refreshToken) => {
    const res = await authApi.refreshToken(refreshToken)
    if (res.code !== 0 || !res.data) throw new Error(res.message || '刷新登录状态失败')
    persistAuth(res.data)
    return res.data
  })
  registerTokenRefresher()

  const logout = async () => {
    try {
      if (token.value) await authApi.logout()
    } catch {
      // Local logout must always succeed even when the server token is already invalid.
    } finally {
      setToken(null)
      setUser(null)
      localStorage.removeItem('refreshToken')
    }
  }

  const fetchCurrentUser = async () => {
    if (!token.value) return null
    try {
      const res = await authApi.getCurrentUser()
      if (res.code === 0 && res.data) {
        setUser(res.data)
        return res.data
      }
      return null
    } catch {
      try {
        return (await refresh()).data.user
      } catch {
        await logout()
        return null
      }
    }
  }

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  return { user, token, login, register, refresh, logout, fetchCurrentUser, isAuthenticated, isAdmin, registerTokenRefresher }
})
