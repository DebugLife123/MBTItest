import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const setUser = (newUser: User | null) => {
    user.value = newUser
  }

  const setToken = (newToken: string | null) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    if (res.code === 0 && res.data) {
      setToken(res.data.token)
      setUser(res.data.user)
      localStorage.setItem('refreshToken', res.data.refreshToken)
    }
    return res
  }

  const register = async (username: string, password: string, nickname?: string, email?: string) => {
    const res = await authApi.register({ username, password, nickname, email })
    if (res.code === 0 && res.data) {
      setToken(res.data.token)
      setUser(res.data.user)
      localStorage.setItem('refreshToken', res.data.refreshToken)
    }
    return res
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  const fetchCurrentUser = async () => {
    if (!token.value) return
    try {
      const res = await authApi.getCurrentUser()
      if (res.code === 0 && res.data) {
        setUser(res.data)
      }
    } catch (error) {
      logout()
    }
  }

  return {
    user,
    token,
    login,
    register,
    logout,
    fetchCurrentUser,
    isAuthenticated: () => !!token.value
  }
})
