import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { AxiosAdapter } from 'axios'
import request, { configureTokenRefresher, httpClient } from '@/utils/request'
import type { AuthResponse } from '@/types'

const refreshedAuth: AuthResponse = {
  token: 'new-access',
  refreshToken: 'new-refresh',
  user: { id: 1, username: 'user', role: 'USER', createdAt: '2026-01-01' }
}

describe('HTTP request authentication', () => {
  let originalAdapter: AxiosAdapter

  beforeEach(() => {
    localStorage.clear()
    originalAdapter = httpClient.defaults.adapter as AxiosAdapter
  })

  afterEach(() => {
    configureTokenRefresher(null)
    httpClient.defaults.adapter = originalAdapter
  })

  it('refreshes once, persists the new token, and retries the original request', async () => {
    localStorage.setItem('token', 'expired-access')
    localStorage.setItem('refreshToken', 'old-refresh')

    let calls = 0
    httpClient.defaults.adapter = async (config) => {
      calls += 1
      if (calls === 2) {
        return {
          data: { code: 0, message: 'OK', data: { value: 'ok' }, timestamp: '', path: null, traceId: null },
          status: 200,
          statusText: 'OK',
          headers: {},
          config
        }
      }
      return Promise.reject({ response: { status: 401, data: { code: 'UNAUTHORIZED', detail: 'expired' } }, config })
    }

    configureTokenRefresher(async (refreshToken) => {
      expect(refreshToken).toBe('old-refresh')
      localStorage.setItem('token', refreshedAuth.token)
      localStorage.setItem('refreshToken', refreshedAuth.refreshToken)
      return refreshedAuth
    })

    const result = await request.get<any>('/protected')
    expect(result.data.value).toBe('ok')
    expect(calls).toBe(2)
    expect(localStorage.getItem('token')).toBe('new-access')
  })

  it('shares one refresh across concurrent 401 responses', async () => {
    localStorage.setItem('token', 'expired-access')
    localStorage.setItem('refreshToken', 'old-refresh')
    let refreshed = false

    let releaseRefresh!: () => void
    const refreshBlocked = new Promise<void>((resolve) => { releaseRefresh = resolve })
    let refreshCalls = 0
    configureTokenRefresher(async () => {
      refreshCalls += 1
      await refreshBlocked
      return refreshedAuth
    })

    let protectedCalls = 0
    httpClient.defaults.adapter = async (config) => {
      protectedCalls += 1
      if (config.url === '/protected' && !refreshed) {
        refreshed = true
        return Promise.reject({ response: { status: 401, data: { detail: 'expired' } }, config })
      }
      return {
        data: { code: 0, message: 'OK', data: [], timestamp: '', path: null, traceId: null },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      }
    }

    const first = request.get('/protected')
    const second = request.get('/protected')
    await Promise.resolve()
    releaseRefresh()

    await expect(first).resolves.toBeTruthy()
    await expect(second).resolves.toBeTruthy()
    expect(refreshCalls).toBe(1)
  })

  it('does not refresh when a public login request returns 401', async () => {
    localStorage.setItem('token', 'stale-access')
    localStorage.setItem('refreshToken', 'stale-refresh')

    let refreshCalls = 0
    let loginCalls = 0
    configureTokenRefresher(async () => {
      refreshCalls += 1
      return refreshedAuth
    })
    httpClient.defaults.adapter = async (config) => {
      loginCalls += 1
      return Promise.reject({
        response: { status: 401, data: { code: 'UNAUTHORIZED', detail: '用户名或密码错误' } },
        config
      })
    }

    await expect(request.post('/auth/login', { username: 'user', password: 'bad' }))
      .rejects.toMatchObject({ status: 401 })
    expect(loginCalls).toBe(1)
    expect(refreshCalls).toBe(0)
    expect(localStorage.getItem('token')).toBe('stale-access')
  })
  it('clears auth state when refresh fails', async () => {
    localStorage.setItem('token', 'expired-access')
    localStorage.setItem('refreshToken', 'bad-refresh')
    localStorage.setItem('user', '{"id":1}')

    httpClient.defaults.adapter = async (config) =>
      Promise.reject({ response: { status: 401, data: { code: 'UNAUTHORIZED' } }, config })
    configureTokenRefresher(async () => { throw new Error('refresh failed') })

    await expect(request.get('/protected')).rejects.toMatchObject({ status: 401 })
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
  })
})
