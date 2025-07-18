package com.aiplatform.security;

import com.aiplatform.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtAuthenticationFilter测试类
 * 演示如何测试JWT认证过滤器
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private UserDetails userDetails;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        // 创建测试用户数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password_123");
        testUser.setRole(User.UserRole.user);
        testUser.setStatus(User.UserStatus.active);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // 创建UserDetails
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .authorities(Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_user")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        validToken = "valid.jwt.token";
        invalidToken = "invalid.jwt.token";

        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilterInternal_ValidToken_Success() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmailFromToken(validToken);
        verify(userDetailsService).loadUserByUsername(testUser.getEmail());
        verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer  " + invalidToken;
        
        // 设置mock行为 - 注意：getTokenFromRequest会提取"Bearer "后面的所有内容
        // "Bearer  " + invalidToken -> substring(7) -> " " + invalidToken
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(" " + invalidToken)).thenReturn(false);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider).validateToken(" " + invalidToken);
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(null);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_EmptyAuthorizationHeader() throws ServletException, IOException {
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn("");

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoBearerPrefix() throws ServletException, IOException {
        // 准备测试数据
        String tokenWithoutBearer = validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(tokenWithoutBearer);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_BearerPrefixOnly() throws ServletException, IOException {
        // 准备测试数据
        String bearerOnly = "Bearer ";
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerOnly);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_UserNotFound() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn("nonexistent@example.com");
        when(userDetailsService.loadUserByUsername("nonexistent@example.com"))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // 执行测试并验证异常
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        // 验证异常信息
        assertEquals("User not found", exception.getMessage());

        // 验证mock方法被调用
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmailFromToken(validToken);
        verify(userDetailsService).loadUserByUsername("nonexistent@example.com");
        verify(securityContext, never()).setAuthentication(any());
        // 注意：由于异常被抛出，filterChain.doFilter可能不会被调用
    }

    @Test
    void testDoFilterInternal_ValidTokenWithWhitespace() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer  " + validToken + "  ";
        
        // 设置mock行为 - 注意：getTokenFromRequest会提取"Bearer "后面的所有内容
        // "Bearer  " + validToken + "  " -> substring(7) -> " " + validToken + "  "
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(" " + validToken + "  ")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(" " + validToken + "  ")).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(jwtTokenProvider).validateToken(" " + validToken + "  ");
        verify(jwtTokenProvider).getEmailFromToken(" " + validToken + "  ");
        verify(userDetailsService).loadUserByUsername(testUser.getEmail());
        verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_AuthenticationDetailsSet() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(securityContext).setAuthentication(argThat(authentication -> {
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                return token.getDetails() != null;
            }
            return false;
        }));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_AuthenticationAuthoritiesSet() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(securityContext).setAuthentication(argThat(authentication -> {
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                return token.getAuthorities().size() == 1 &&
                       token.getAuthorities().iterator().next().getAuthority().equals("ROLE_user");
            }
            return false;
        }));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_AdminUser() throws ServletException, IOException {
        // 准备测试数据
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("admin");
        adminUser.setPassword("encoded_admin_123");
        adminUser.setRole(User.UserRole.admin);
        adminUser.setStatus(User.UserStatus.active);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());

        UserDetails adminUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(adminUser.getEmail())
                .password(adminUser.getPassword())
                .authorities(Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(adminUser.getEmail());
        when(userDetailsService.loadUserByUsername(adminUser.getEmail())).thenReturn(adminUserDetails);

        // 执行测试
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证结果
        verify(securityContext).setAuthentication(argThat(authentication -> {
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                return token.getAuthorities().size() == 1 &&
                       token.getAuthorities().iterator().next().getAuthority().equals("ROLE_admin");
            }
            return false;
        }));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_UserDetailsServiceException() throws ServletException, IOException {
        // 准备测试数据
        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为 - 模拟用户详情服务抛出异常
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail()))
                .thenThrow(new RuntimeException("User service error"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        // 验证异常信息
        assertEquals("User service error", exception.getMessage());

        // 验证mock方法被调用
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getEmailFromToken(validToken);
        verify(userDetailsService).loadUserByUsername(testUser.getEmail());
        verify(securityContext, never()).setAuthentication(any());
        // 注意：由于异常被抛出，filterChain.doFilter可能不会被调用
    }

    @Test
    void testGetTokenFromRequest_ValidBearerToken() {
        // 准备测试数据
        String bearerToken = "Bearer " + validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // 执行测试 - 通过反射调用私有方法
        try {
            java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getTokenFromRequest", HttpServletRequest.class);
            method.setAccessible(true);
            String result = (String) method.invoke(jwtAuthenticationFilter, request);

            // 验证结果
            assertEquals(validToken, result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void testGetTokenFromRequest_NoBearerPrefix() {
        // 准备测试数据
        String tokenWithoutBearer = validToken;
        
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(tokenWithoutBearer);

        // 执行测试 - 通过反射调用私有方法
        try {
            java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getTokenFromRequest", HttpServletRequest.class);
            method.setAccessible(true);
            String result = (String) method.invoke(jwtAuthenticationFilter, request);

            // 验证结果
            assertNull(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void testGetTokenFromRequest_NullHeader() {
        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn(null);

        // 执行测试 - 通过反射调用私有方法
        try {
            java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getTokenFromRequest", HttpServletRequest.class);
            method.setAccessible(true);
            String result = (String) method.invoke(jwtAuthenticationFilter, request);

            // 验证结果
            assertNull(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }
} 