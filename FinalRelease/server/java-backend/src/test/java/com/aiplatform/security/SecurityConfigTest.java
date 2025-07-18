package com.aiplatform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * SecurityConfig测试类
 * 演示如何测试Spring Security配置
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    void testPasswordEncoder() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 验证结果
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);

        // 测试密码编码和匹配
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    void testAuthenticationManager() throws Exception {
        // 设置mock行为
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // 执行测试
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        // 验证结果
        assertNotNull(result);
        assertEquals(authenticationManager, result);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    void testFilterChain() throws Exception {
        // 创建HttpSecurity的mock，模拟链式调用
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        
        // 模拟链式调用 - 每个方法都返回HttpSecurity本身
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        
        // 使用doReturn避免类型推断问题
        SecurityFilterChain mockChain = mock(SecurityFilterChain.class);
        doReturn(mockChain).when(httpSecurity).build();

        // 执行测试
        SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);

        // 验证结果
        assertNotNull(filterChain);
        assertEquals(mockChain, filterChain);
        
        // 验证方法调用
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).cors(any());
        verify(httpSecurity).sessionManagement(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).addFilterBefore(any(), any());
        verify(httpSecurity).build();
    }

    @Test
    void testPasswordEncoderMultipleEncodings() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 测试多次编码产生不同的结果（因为使用了随机盐）
        String rawPassword = "testPassword123";
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2);
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }

    @Test
    void testPasswordEncoderWithSpecialCharacters() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 测试包含特殊字符的密码
        String specialPassword = "Test@123!密码";
        String encodedPassword = passwordEncoder.encode(specialPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(specialPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(specialPassword, encodedPassword));
    }

    @Test
    void testPasswordEncoderWithUnicodeCharacters() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 测试包含Unicode字符的密码
        String unicodePassword = "测试密码123";
        String encodedPassword = passwordEncoder.encode(unicodePassword);

        assertNotNull(encodedPassword);
        assertNotEquals(unicodePassword, encodedPassword);
        assertTrue(passwordEncoder.matches(unicodePassword, encodedPassword));
    }

    @Test
    void testPasswordEncoderWithEmptyPassword() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 测试空密码
        String emptyPassword = "";
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(emptyPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(emptyPassword, encodedPassword));
    }

    @Test
    void testSecurityConfigConstructor() {
        // 测试构造函数
        SecurityConfig config = new SecurityConfig(jwtAuthenticationFilter);
        assertNotNull(config);
    }

    @Test
    void testAuthenticationManagerException() throws Exception {
        // 设置mock行为 - 模拟异常
        when(authenticationConfiguration.getAuthenticationManager())
                .thenThrow(new Exception("Authentication configuration error"));

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            securityConfig.authenticationManager(authenticationConfiguration);
        });

        assertEquals("Authentication configuration error", exception.getMessage());
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    void testPasswordEncoderPerformance() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 测试编码性能（不应该太慢）
        long startTime = System.currentTimeMillis();
        String encodedPassword = passwordEncoder.encode("testPassword123");
        long endTime = System.currentTimeMillis();

        assertNotNull(encodedPassword);
        assertTrue((endTime - startTime) < 1000); // 编码应该在1秒内完成
    }

    @Test
    void testPasswordEncoderStrength() {
        // 执行测试
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // 测试密码强度 - BCrypt应该产生足够长的哈希
        String encodedPassword = passwordEncoder.encode("testPassword123");
        assertTrue(encodedPassword.length() > 50); // BCrypt哈希通常很长
    }
} 