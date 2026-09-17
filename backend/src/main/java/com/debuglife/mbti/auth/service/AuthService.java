package com.debuglife.mbti.auth.service;

import com.debuglife.mbti.auth.dto.*;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(User.UserRole.USER);
        
        user = userRepository.save(user);
        
        String token = tokenProvider.generateToken(user.getUsername(), user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
        
        return new AuthResponse(token, refreshToken, AuthResponse.UserDTO.fromEntity(user));
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String token = tokenProvider.generateToken(user.getUsername(), user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
        
        return new AuthResponse(token, refreshToken, AuthResponse.UserDTO.fromEntity(user));
    }
    
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
