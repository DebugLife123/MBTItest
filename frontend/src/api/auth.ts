import request from '@/utils/request'
import type { 
  LoginRequest, 
  RegisterRequest, 
  AuthResponse, 
  ApiResponse,
  User 
} from '@/types'

export const authApi = {
  login(data: LoginRequest) {
    return request.post<ApiResponse<AuthResponse>>('/auth/login', data)
  },
  
  register(data: RegisterRequest) {
    return request.post<ApiResponse<AuthResponse>>('/auth/register', data)
  },
  
  logout() {
    return request.post<ApiResponse<void>>('/auth/logout')
  },
  
  getCurrentUser() {
    return request.get<ApiResponse<User>>('/auth/me')
  },
  
  refreshToken(refreshToken: string) {
    return request.post<ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken })
  }
}
