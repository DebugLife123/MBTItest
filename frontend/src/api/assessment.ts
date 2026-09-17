import request from '@/utils/request'
import type { 
  ApiResponse,
  TestAttempt,
  MbtiQuestion,
  SubmitAnswersRequest,
  TestResult,
  PageResponse
} from '@/types'

export const assessmentApi = {
  // 开始测评
  startAttempt() {
    return request.post<ApiResponse<TestAttempt>>('/attempts')
  },
  
  // 获取测评题目
  getQuestions(attemptId: number) {
    return request.get<ApiResponse<MbtiQuestion[]>>(`/attempts/${attemptId}/questions`)
  },
  
  // 提交答案
  submitAnswers(attemptId: number, data: SubmitAnswersRequest) {
    return request.post<ApiResponse<void>>(`/attempts/${attemptId}/answers`, data)
  },
  
  // 完成测评
  completeAttempt(attemptId: number) {
    return request.post<ApiResponse<TestResult>>(`/attempts/${attemptId}/complete`)
  },
  
  // 获取测评结果
  getResult(resultId: number) {
    return request.get<ApiResponse<TestResult>>(`/results/${resultId}`)
  },
  
  // 获取我的测评历史
  getMyResults(page = 0, size = 10) {
    return request.get<ApiResponse<PageResponse<TestResult>>>('/results/my', {
      params: { page, size }
    })
  }
}
