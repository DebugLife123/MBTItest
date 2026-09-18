import type { ApiResponse } from '@/types'
import request from '@/utils/request'

export interface AiStatus {
  enabled: boolean
  configuredProvider: string
  activeProvider: string
  model: string
  mockMode: boolean
  disclaimer: string
}

export interface AiChatMessage {
  id?: number
  role: 'USER' | 'ASSISTANT'
  content: string
  createdAt?: string
  streaming?: boolean
}

export interface AiChatSession {
  id: number
  title: string
  personalityType: string | null
  provider: string
  model: string
  status: 'ACTIVE' | 'ARCHIVED'
  createdAt: string
  updatedAt: string
  messages: AiChatMessage[]
}

export interface AiChatResponse {
  sessionId: number
  title: string
  reply: string
  provider: string
  model: string
  personalityType: string | null
  createdAt: string
}

export interface TeamAnalysisRecord {
  id: number
  totalMembers: number
  typeCounts: Record<string, number>
  focus: string | null
  report: string
  provider: string
  model: string
  createdAt: string
}

export interface TeamAnalysisPayload {
  typeCodes: string[]
  focus?: string
}

export const aiApi = {
  status() {
    return request.get<ApiResponse<AiStatus>>('/ai/status')
  },
  chat(sessionId: number | null, message: string) {
    return request.post<ApiResponse<AiChatResponse>>('/ai/chat', { sessionId, message })
  },
  sessions() {
    return request.get<ApiResponse<AiChatSession[]>>('/ai/sessions')
  },
  session(id: number) {
    return request.get<ApiResponse<AiChatSession>>(`/ai/sessions/${id}`)
  },
  deleteSession(id: number) {
    return request.delete<ApiResponse<void>>(`/ai/sessions/${id}`)
  },
  analyzeTeam(payload: TeamAnalysisPayload) {
    return request.post<ApiResponse<TeamAnalysisRecord>>('/admin/ai/team-analysis', payload)
  },
  teamHistory() {
    return request.get<ApiResponse<TeamAnalysisRecord[]>>('/admin/ai/team-analysis/history')
  },
  latestTeamAnalysis() {
    return request.get<ApiResponse<TeamAnalysisRecord>>('/admin/ai/team-analysis/latest')
  },
  availableTypes() {
    return request.get<ApiResponse<string[]>>('/admin/ai/team-analysis/available-types')
  }
}
