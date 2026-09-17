import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export interface GrowthPoint {
  resultId: number
  typeCode: string
  typeName: string | null
  eScore: number
  iScore: number
  sScore: number
  nScore: number
  tScore: number
  fScore: number
  jScore: number
  pScore: number
  createdAt: string
  changedDimensions: string[]
}

export interface CompatibilityResponse {
  myType: string
  otherType: string
  otherUsername: string
  score: number
  level: string
  summary: string
  sharedTraits: string[]
  complementaryTraits: string[]
  watchOuts: string[]
}

export interface CareerAdviceResponse {
  typeCode: string
  typeName: string
  summary: string
  careerSuggestions: string
  recommendedRoles: string[]
  skillSuggestions: string[]
}

export const userApi = {
  getGrowthTrack(limit = 20) {
    return request.get<ApiResponse<GrowthPoint[]>>('/user/growth', { params: { limit } })
  },
  getCompatibility(otherUsername: string) {
    return request.post<ApiResponse<CompatibilityResponse>>('/user/compatibility', { otherUsername })
  },
  getCareerAdvice() {
    return request.get<ApiResponse<CareerAdviceResponse>>('/user/career-advice')
  },
  exportHistory() {
    return request.download('/user/export')
  }
}
