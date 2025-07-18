package com.aiplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtTokenProvider测试类
 * 演示如何测试JWT令牌生成和验证功能
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_SECRET = "test-secret-key-for-testing-only-must-be-long-enough";
    private static final long TEST_EXPIRATION = 3600000;// 1小时
    private static final long TEST_REFRESH_EXPIRATION =86400024;

    @BeforeEach
    void setUp() {
        // 设置私有字段值
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration",TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpiration", TEST_REFRESH_EXPIRATION);
    }

    @Test
    void testGenerateToken_Success() {
        // 执行测试
        String token = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 验证结果
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT格式：header.payload.signature

        // 验证令牌内容
        String emailFromToken = jwtTokenProvider.getEmailFromToken(token);
        assertEquals(TEST_EMAIL, emailFromToken);

        // 验证令牌有效性
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testGenerateRefreshToken_Success() {
        // 执行测试
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_EMAIL);

        // 验证结果
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(refreshToken.split("\\.").length == 3);

        // 验证令牌内容
        String emailFromToken = jwtTokenProvider.getEmailFromToken(refreshToken);
        assertEquals(TEST_EMAIL, emailFromToken);

        // 验证令牌有效性
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
    }

    @Test
    void testGetEmailFromToken_Success() {
        // 准备测试数据
        String token = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 执行测试
        String email = jwtTokenProvider.getEmailFromToken(token);

        // 验证结果
        assertEquals(TEST_EMAIL, email);
    }

    @Test
    void testGetEmailFromToken_InvalidToken() {
        // 准备测试数据
        String invalidToken = "invalid.token.here";

        // 执行测试并验证异常
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.getEmailFromToken(invalidToken);
        });
    }

    @Test
    void testGetEmailFromToken_NullToken() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.getEmailFromToken(null);
        });
    }

    @Test
    void testGetEmailFromToken_EmptyToken() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.getEmailFromToken("");
        });
    }

    @Test
    void testValidateToken_ValidToken() {
        // 准备测试数据
        String token = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 执行测试
        boolean isValid = jwtTokenProvider.validateToken(token);

        // 验证结果
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // 准备测试数据
        String invalidToken = "invalid.token.here";

        // 执行测试
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // 执行测试
        boolean isValid = jwtTokenProvider.validateToken(null);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // 执行测试
        boolean isValid = jwtTokenProvider.validateToken("");

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_MalformedToken() {
        // 准备测试数据
        String malformedToken = "header.payload"; // 缺少签名部分

        // 执行测试
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpired_ValidToken() {
        // 准备测试数据
        String token = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 执行测试
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // 验证结果
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        // 准备测试数据 - 创建一个已过期的令牌
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -10);// 负值表示已过期
        String expiredToken = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 执行测试
        boolean isExpired = jwtTokenProvider.isTokenExpired(expiredToken);

        // 验证结果
        assertTrue(isExpired);
    }

    @Test
    void testIsTokenExpired_InvalidToken() {
        // 准备测试数据
        String invalidToken = "invalid.token.here";

        // 执行测试
        boolean isExpired = jwtTokenProvider.isTokenExpired(invalidToken);

        // 验证结果
        assertTrue(isExpired);
    }

    @Test
    void testGetExpirationFromToken_Success() {
        // 准备测试数据
        String token = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 执行测试
        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        // 验证结果
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // 过期时间应该在当前时间之后
    }

    @Test
    void testGetExpirationFromToken_InvalidToken() {
        // 准备测试数据
        String invalidToken = "invalid.token.here";

        // 执行测试并验证异常
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.getExpirationFromToken(invalidToken);
        });
    }

    @Test
    void testTokenExpirationTime() {
        // 准备测试数据
        long expectedExpiration = System.currentTimeMillis() + TEST_EXPIRATION;
        String token = jwtTokenProvider.generateToken(TEST_EMAIL);

        // 执行测试
        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        // 验证结果 - 允许1000毫秒的误差
        long actualExpiration = expiration.getTime();
        assertTrue(Math.abs(actualExpiration - expectedExpiration) <1000);
    }

    @Test
    void testRefreshTokenExpirationTime() {
        // 准备测试数据
        long expectedExpiration = System.currentTimeMillis() + TEST_REFRESH_EXPIRATION;
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_EMAIL);

        // 执行测试
        Date expiration = jwtTokenProvider.getExpirationFromToken(refreshToken);

        // 验证结果 - 允许1000毫秒的误差
        long actualExpiration = expiration.getTime();
        assertTrue(Math.abs(actualExpiration - expectedExpiration) <1000);
    }

    @Test
    void testDifferentEmailsGenerateDifferentTokens() {
        // 准备测试数据
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // 执行测试
        String token1 = jwtTokenProvider.generateToken(email1);
        String token2 = jwtTokenProvider.generateToken(email2);

        // 验证结果
        assertNotEquals(token1, token2);
        assertEquals(email1, jwtTokenProvider.getEmailFromToken(token1));
        assertEquals(email2, jwtTokenProvider.getEmailFromToken(token2));
    }

    @Test
    void testSameEmailGeneratesValidTokens() {
        // 准备测试数据
        String email = "user@example.com";

        // 执行测试
        String token1 = jwtTokenProvider.generateToken(email);
        String token2 = jwtTokenProvider.generateToken(email);

        // 验证结果 - 两个令牌都应该有效且包含相同的邮箱
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token1));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token2));
        
        // 注意：由于JWT生成基于时间戳，如果在同一毫秒内生成，令牌可能相同
        // 这是正常行为，我们主要验证令牌的有效性和功能
    }

    @Test
    void testTokenWithSpecialCharacters() {
        // 准备测试数据
        String emailWithSpecialChars = "test+user@example.com";

        // 执行测试
        String token = jwtTokenProvider.generateToken(emailWithSpecialChars);

        // 验证结果
        assertNotNull(token);
        assertEquals(emailWithSpecialChars, jwtTokenProvider.getEmailFromToken(token));
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testTokenWithUnicodeCharacters() {
        // 准备测试数据
        String emailWithUnicode = "测试用户@example.com";

        // 执行测试
        String token = jwtTokenProvider.generateToken(emailWithUnicode);

        // 验证结果
        assertNotNull(token);
        assertEquals(emailWithUnicode, jwtTokenProvider.getEmailFromToken(token));
        assertTrue(jwtTokenProvider.validateToken(token));
    }
} 