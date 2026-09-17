import request from '@/utils/request'
import type { ApiResponse, MbtiPersonality, MbtiQuestion, PageResponse } from '@/types'

export interface UserListItem {
  id: number
  username: string
  email: string | null
  role: string
  enabled: boolean
  attemptCount: number
  lastLoginAt: string | null
  createdAt: string
}

export interface UserStatistics {
  totalUsers: number
  activeUsers: number
  totalAttempts: number
  completedAttempts: number
  completionRate: number
  lastUpdated: string
}

export interface PersonalityDistribution {
  personalityType: string
  count: number
  percentage: number
}

export interface CompletionRatePoint {
  date: string
  totalAttempts: number
  completedAttempts: number
  completionRate: number
}

export interface QuestionPayload {
  dimensionId: number
  content: string
  optionA: string
  optionB: string
  answerType: string
  sortOrder?: number
}

export const adminApi = {
  getUsers(params: { keyword?: string; page: number; size: number; sortBy?: string; direction?: string }) {
    return request.get<ApiResponse<PageResponse<UserListItem>>>('/admin/users', { params })
  },
  updateUserStatus(id: number, enabled: boolean) {
    return request.put<ApiResponse<void>>(`/admin/users/${id}/status`, null, { params: { enabled } })
  },
  deleteUser(id: number) {
    return request.delete<ApiResponse<void>>(`/admin/users/${id}`)
  },
  getStatistics() {
    return request.get<ApiResponse<UserStatistics>>('/admin/statistics')
  },
  getPersonalityDistribution() {
    return request.get<ApiResponse<PersonalityDistribution[]>>('/admin/analytics/personality-distribution')
  },
  getCompletionRateTrend(days = 14) {
    return request.get<ApiResponse<CompletionRatePoint[]>>('/admin/analytics/completion-rate', { params: { days } })
  },
  getQuestions() {
    return request.get<ApiResponse<MbtiQuestion[]>>('/admin/questions')
  },
  createQuestion(payload: QuestionPayload) {
    return request.post<ApiResponse<MbtiQuestion>>('/admin/questions', payload)
  },
  updateQuestion(id: number, payload: QuestionPayload) {
    return request.put<ApiResponse<MbtiQuestion>>(`/admin/questions/${id}`, payload)
  },
  deleteQuestion(id: number) {
    return request.delete<ApiResponse<void>>(`/admin/questions/${id}`)
  },
  getPersonalities() {
    return request.get<ApiResponse<MbtiPersonality[]>>('/admin/personalities')
  },
  updatePersonality(typeCode: string, payload: Partial<MbtiPersonality>) {
    return request.put<ApiResponse<MbtiPersonality>>(`/admin/personalities/${typeCode}`, payload)
  }
}
