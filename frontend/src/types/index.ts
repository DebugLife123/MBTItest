// 统一响应体 - 匹配后端 ApiResponse record 结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
  path: string | null
  traceId: string | null
}

// 用户相关
export interface User {
  id: number
  username: string
  nickname?: string
  avatar?: string
  email?: string
  role: 'USER' | 'ADMIN'
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  user: User
}

// 测评相关
export interface MbtiDimension {
  id: number
  code: string
  name: string
  description: string
}

export interface MbtiQuestion {
  id: number
  dimensionId: number
  content: string
  optionA: string
  optionB: string
  answerType: 'E' | 'I' | 'S' | 'N' | 'T' | 'F' | 'J' | 'P'
  sortOrder: number
}

export interface MbtiPersonality {
  id: number
  typeCode: string
  typeName: string
  description: string
  strengths?: string
  weaknesses?: string
  careerSuggestions?: string
}

// 测评会话
export interface TestAttempt {
  id: number
  userId: number
  status: 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'
  startedAt: string
  completedAt?: string
  resultType?: string
}

export interface Answer {
  questionId: number
  answer: 'A' | 'B'
}

export interface SubmitAnswersRequest {
  answers: Answer[]
}

export interface TestResult {
  id: number
  attemptId: number
  userId: number
  personalityId: number
  typeCode: string
  eScore: number
  iScore: number
  sScore: number
  nScore: number
  tScore: number
  fScore: number
  jScore: number
  pScore: number
  personality: MbtiPersonality
  createdAt: string
}

// 分页
export interface PageRequest {
  page: number
  size: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}
