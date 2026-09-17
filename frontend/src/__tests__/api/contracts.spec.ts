import { describe, it, expect, vi, beforeEach } from 'vitest'
import request from '@/utils/request'
import { authApi } from '@/api/auth'
import { assessmentApi } from '@/api/assessment'

vi.mock('@/utils/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), patch: vi.fn() }
}))

const apiResponse = (data: any) => ({ code: 0, message: 'OK', data, timestamp: '', path: null, traceId: null })

describe('API contracts', () => {
  beforeEach(() => vi.clearAllMocks())

  it('calls the real auth login endpoint with request body', async () => {
    const payload = { username: 'testuser', password: 'password123' }
    vi.mocked(request.post).mockResolvedValue(apiResponse({
      token: 'access', refreshToken: 'refresh',
      user: { id: 1, username: 'testuser', role: 'USER', createdAt: '2026-01-01' }
    }))
    await authApi.login(payload)
    expect(request.post).toHaveBeenCalledWith('/auth/login', payload)
  })

  it('calls refresh and me endpoints', async () => {
    vi.mocked(request.post).mockResolvedValue(apiResponse({
      token: 'a', refreshToken: 'r',
      user: { id: 1, username: 'u', role: 'USER', createdAt: '2026-01-01' }
    }))
    vi.mocked(request.get).mockResolvedValue(apiResponse({ id: 1, username: 'u', role: 'USER', createdAt: '2026-01-01' }))
    await authApi.refreshToken('refresh-token')
    await authApi.getCurrentUser()
    expect(request.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'refresh-token' })
    expect(request.get).toHaveBeenCalledWith('/auth/me')
  })

  it('calls the real attempt endpoints', async () => {
    vi.mocked(request.post).mockResolvedValue(apiResponse({ id: 7 }))
    vi.mocked(request.get).mockResolvedValue(apiResponse([]))
    const answers = { answers: [{ questionId: 1, answer: 'A' as const }] }
    await assessmentApi.startAttempt()
    await assessmentApi.getQuestions(7)
    await assessmentApi.submitAnswers(7, answers)
    await assessmentApi.completeAttempt(7)
    await assessmentApi.getResult(9)
    await assessmentApi.getMyResults(0, 10)
    expect(request.post).toHaveBeenNthCalledWith(1, '/attempts')
    expect(request.get).toHaveBeenNthCalledWith(1, '/attempts/7/questions')
    expect(request.post).toHaveBeenNthCalledWith(2, '/attempts/7/answers', answers)
    expect(request.post).toHaveBeenNthCalledWith(3, '/attempts/7/complete')
    expect(request.get).toHaveBeenNthCalledWith(2, '/results/9')
    expect(request.get).toHaveBeenNthCalledWith(3, '/results/my', { params: { page: 0, size: 10 } })
  })
})
