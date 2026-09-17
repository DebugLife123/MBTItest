package com.debuglife.mbti.common.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
            "MyVerySecureSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong123456");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", 604800000L);
    }

    @Test
    void testGenerateToken() {
        String token = tokenProvider.generateToken("testuser", 1L);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = tokenProvider.generateRefreshToken("testuser");

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = tokenProvider.generateToken("testuser", 1L);
        String username = tokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void testValidateTokenSuccess() {
        String token = tokenProvider.generateToken("testuser", 1L);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void testValidateTokenInvalid() {
        String invalidToken = "invalid.token.here";

        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateTokenExpired() {
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 1L);
        String token = tokenProvider.generateToken("testuser", 1L);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(tokenProvider.validateToken(token));
    }
}
