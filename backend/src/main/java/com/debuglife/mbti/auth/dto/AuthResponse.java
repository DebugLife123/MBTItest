package com.debuglife.mbti.auth.dto;

import com.debuglife.mbti.auth.entity.User;

public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDTO user;

    public AuthResponse(String token, String refreshToken, UserDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public static class UserDTO {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private String role;
        private String createdAt;

        public UserDTO(Long id, String username, String nickname, String email, String avatar, String role, String createdAt) {
            this.id = id;
            this.username = username;
            this.nickname = nickname;
            this.email = email;
            this.avatar = avatar;
            this.role = role;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public static UserDTO fromEntity(User user) {
            return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole().name(),
                user.getCreatedAt().toString()
            );
        }
    }
}
