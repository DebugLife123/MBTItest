import { beforeEach, describe, expect, it, vi } from 'vitest'
import request from '@/utils/request'
import { aiApi } from '@/api/ai'

vi.mock('@/utils/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), patch: vi.fn() }
}))

describe('AI API contracts', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses the expected user chat, session and status endpoints', async () => {
    vi.mocked(request.get).mockResolvedValue({ code: 0, data: [] })
    vi.mocked(request.post).mockResolvedValue({ code: 0, data: {} })
    vi.mocked(request.delete).mockResolvedValue({ code: 0, data: null })

    await aiApi.status()
    await aiApi.chat(3, '继续分析')
    await aiApi.sessions()
    await aiApi.session(8)
    await aiApi.deleteSession(8)

    expect(request.get).toHaveBeenCalledWith('/ai/status')
    expect(request.post).toHaveBeenCalledWith('/ai/chat', { sessionId: 3, message: '继续分析' })
    expect(request.get).toHaveBeenCalledWith('/ai/sessions')
    expect(request.get).toHaveBeenCalledWith('/ai/sessions/8')
    expect(request.delete).toHaveBeenCalledWith('/ai/sessions/8')
  })

  it('uses the expected administrator team-analysis endpoints', async () => {
    vi.mocked(request.post).mockResolvedValue({ code: 0, data: {} })
    vi.mocked(request.get).mockResolvedValue({ code: 0, data: [] })
    const payload = { typeCodes: ['INTJ', 'ENFP'], focus: '跨职能协作' }

    await aiApi.analyzeTeam(payload)
    await aiApi.teamHistory()
    await aiApi.latestTeamAnalysis()
    await aiApi.availableTypes()

    expect(request.post).toHaveBeenCalledWith('/admin/ai/team-analysis', payload)
    expect(request.get).toHaveBeenCalledWith('/admin/ai/team-analysis/history')
    expect(request.get).toHaveBeenCalledWith('/admin/ai/team-analysis/latest')
    expect(request.get).toHaveBeenCalledWith('/admin/ai/team-analysis/available-types')
  })
})
