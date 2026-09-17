import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse, AuthResponse } from '@/types'

interface ProblemDetail {
  code?: string
  title?: string
  detail?: string
  message?: string
  traceId?: string
}

interface RetriableConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

type TokenRefresher = (refreshToken: string) => Promise<AuthResponse>

export const httpClient: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let refreshPromise: Promise<AuthResponse> | null = null
let tokenRefresher: TokenRefresher | null = null

const clearAuthState = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
}

const redirectToLogin = () => {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

const refreshAccessToken = async (): Promise<AuthResponse> => {
  if (!tokenRefresher) throw new Error('Token refresher is not configured')
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) throw new Error('缺少刷新令牌')

  if (!refreshPromise) {
    refreshPromise = tokenRefresher(refreshToken).finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

/** Allows the Pinia store to own user persistence while the HTTP layer only manages token retry. */
export function configureTokenRefresher(refresher: TokenRefresher | null) {
  tokenRefresher = refresher
}

httpClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

httpClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => response.data as any,
  async (error: AxiosError<ProblemDetail>) => {
    const originalRequest = error.config as RetriableConfig | undefined
    const status = error.response?.status
    const requestUrl = originalRequest?.url || ''
    const isRefreshRequest = requestUrl.includes('/auth/refresh')
    const isPublicAuthRequest =
      requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register')

    if (
      status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !isRefreshRequest &&
      !isPublicAuthRequest
    ) {
      originalRequest._retry = true
      try {
        await refreshAccessToken()
        return httpClient(originalRequest)
      } catch {
        clearAuthState()
        redirectToLogin()
      }
    } else if (status === 401 && isRefreshRequest) {
      clearAuthState()
      redirectToLogin()
    }

    const body = error.response?.data
    const message = body?.detail || body?.message || body?.title || error.message || '请求失败'
    if (status && status >= 500) {
      ElMessage.error(message)
    }
    return Promise.reject({
      ...body,
      message,
      status,
      traceId: body?.traceId
    })
  }
)

export async function downloadFile(url: string): Promise<Blob> {
  const response = await httpClient.get<Blob>(url, { responseType: 'blob' })
  return response as unknown as Blob
}

interface RequestInstance {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>
  download(url: string, config?: AxiosRequestConfig): Promise<Blob>
}

const request = httpClient as unknown as RequestInstance
request.download = downloadFile
export default request