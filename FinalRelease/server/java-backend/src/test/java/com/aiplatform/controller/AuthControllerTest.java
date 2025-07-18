package com.aiplatform.controller;

import com.aiplatform.dto.UserDTO;
import com.aiplatform.entity.User;
import com.aiplatform.exception.BusinessException;
import com.aiplatform.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        AuthController.class, 
        AuthControllerTest.TestConfig.class,
        AuthControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class AuthControllerTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    })
    static class TestConfig {
        // 空配置类，仅用于排除JPA自动配置
    }

    @Configuration
    static class TestSecurityConfig {
        
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)  // 禁用CSRF保护
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()  // 允许所有请求
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        // 最小测试：验证所有依赖都被正确注入
        assertNotNull(mockMvc, "MockMvc should be injected");
        assertNotNull(objectMapper, "ObjectMapper should be injected");
        assertNotNull(userService, "UserService should be mocked");
        
        System.out.println("✅ 基本配置测试通过 - 所有依赖都已正确注入");
    }

    // ========== 用户注册相关测试 ==========

    @Test
    public void testRegister_success() throws Exception {
        // 模拟用户数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);
        user.setCreatedAt(LocalDateTime.now());

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);
        UserDTO.AuthResponse authResponse = new UserDTO.AuthResponse("jwt-token", "refresh-token", userResponse);

        Mockito.when(userService.register(any(UserDTO.UserRegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
        
        System.out.println("✅ 用户注册成功测试通过");
    }

    @Test
    public void testRegister_missingUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少用户名注册测试通过");
    }

    @Test
    public void testRegister_missingEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少邮箱注册测试通过");
    }

    @Test
    public void testRegister_missingPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少密码注册测试通过");
    }

    @Test
    public void testRegister_missingConfirmPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "password":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少确认密码注册测试通过");
    }

    @Test
    public void testRegister_invalidEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"invalid-email",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 无效邮箱注册测试通过");
    }

    @Test
    public void testRegister_shortUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"ab",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 用户名过短注册测试通过");
    }

    @Test
    public void testRegister_shortPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "password":"123",
                      "confirmPassword":"123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 密码过短注册测试通过");
    }

    @Test
    public void testRegister_businessException() throws Exception {
        Mockito.when(userService.register(any(UserDTO.UserRegisterRequest.class)))
                .thenThrow(new BusinessException("邮箱已存在"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("邮箱已存在"));
        
        System.out.println("✅ 注册业务异常测试通过");
    }

    @Test
    public void testRegister_systemException() throws Exception {
        Mockito.when(userService.register(any(UserDTO.UserRegisterRequest.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("注册失败，请稍后重试"));
        
        System.out.println("✅ 注册系统异常测试通过");
    }

    // ========== 用户登录相关测试 ==========

    @Test
    public void testLogin_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);
        UserDTO.AuthResponse authResponse = new UserDTO.AuthResponse("jwt-token", "refresh-token", userResponse);

        Mockito.when(userService.login(any(UserDTO.UserLoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com",
                      "password":"password123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
        
        System.out.println("✅ 用户登录成功测试通过");
    }

    @Test
    public void testLogin_withRememberMe() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);
        UserDTO.AuthResponse authResponse = new UserDTO.AuthResponse("jwt-token", "refresh-token", userResponse);

        Mockito.when(userService.login(any(UserDTO.UserLoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com",
                      "password":"password123",
                      "rememberMe":true
                    }
                    """))
                .andExpect(status().isOk());
        
        System.out.println("✅ 记住我登录测试通过");
    }

    @Test
    public void testLogin_missingEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "password":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少邮箱登录测试通过");
    }

    @Test
    public void testLogin_missingPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少密码登录测试通过");
    }

    @Test
    public void testLogin_businessException() throws Exception {
        Mockito.when(userService.login(any(UserDTO.UserLoginRequest.class)))
                .thenThrow(new BusinessException("邮箱或密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com",
                      "password":"wrongpassword"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("邮箱或密码错误"));
        
        System.out.println("✅ 登录业务异常测试通过");
    }

    @Test
    public void testLogin_systemException() throws Exception {
        Mockito.when(userService.login(any(UserDTO.UserLoginRequest.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com",
                      "password":"password123"
                    }
                    """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("登录失败，请稍后重试"));
        
        System.out.println("✅ 登录系统异常测试通过");
    }

    // ========== 获取当前用户信息相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetCurrentUser_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);
        user.setCreatedAt(LocalDateTime.now());

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);

        Mockito.when(userService.getCurrentUser())
                .thenReturn(userResponse);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("user"));
        
        System.out.println("✅ 获取当前用户信息成功测试通过");
    }

//    @Test
//    @WithMockUser(username = "test@example.com")
//    public void testGetCurrentUser_exception() throws Exception {
//        Mockito.when(userService.getCurrentUser())
//                .thenThrow(new RuntimeException("获取用户信息失败"));
//
//        mockMvc.perform(get("/auth/me")
//                        .header("Authorization", "Bearer test-token"))
//                .andExpect(status().isInternalServerError());
//
//        System.out.println("✅ 获取当前用户信息异常测试通过");
//    }

    // ========== 更新用户资料相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateProfile_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("newusername");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);

        Mockito.when(userService.updateProfile(any(UserDTO.UserProfileUpdateRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(put("/auth/profile")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"newusername"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"));
        
        System.out.println("✅ 更新用户资料成功测试通过");
    }

//    @Test
//    @WithMockUser(username = "test@example.com")
//    public void testUpdateProfile_exception() throws Exception {
//        Mockito.when(userService.updateProfile(any(UserDTO.UserProfileUpdateRequest.class)))
//                .thenThrow(new RuntimeException("更新用户资料失败"));
//
//        mockMvc.perform(put("/auth/profile")
//                        .header("Authorization", "Bearer test-token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                    {
//                      "username":"newusername"
//                    }
//                    """))
//                .andExpect(status().isInternalServerError());
//
//        System.out.println("✅ 更新用户资料异常测试通过");
//    }

    // ========== 修改密码相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_success() throws Exception {
        Mockito.doNothing().when(userService).changePassword(any(UserDTO.PasswordChangeRequest.class));

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"oldpassword",
                      "newPassword":"newpassword123",
                      "confirmPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().string("密码修改成功"));
        
        System.out.println("✅ 修改密码成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_missingCurrentPassword() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "newPassword":"newpassword123",
                      "confirmPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少当前密码测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_missingNewPassword() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"oldpassword",
                      "confirmPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少新密码测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_missingConfirmPassword() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"oldpassword",
                      "newPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少确认密码测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_shortNewPassword() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"oldpassword",
                      "newPassword":"123",
                      "confirmPassword":"123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 新密码过短测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_businessException() throws Exception {
        Mockito.doThrow(new BusinessException("当前密码错误"))
                .when(userService).changePassword(any(UserDTO.PasswordChangeRequest.class));

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"wrongpassword",
                      "newPassword":"newpassword123",
                      "confirmPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("当前密码错误"));
        
        System.out.println("✅ 修改密码业务异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_systemException() throws Exception {
        Mockito.doThrow(new RuntimeException("数据库连接失败"))
                .when(userService).changePassword(any(UserDTO.PasswordChangeRequest.class));

        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"oldpassword",
                      "newPassword":"newpassword123",
                      "confirmPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("密码修改失败，请稍后重试"));
        
        System.out.println("✅ 修改密码系统异常测试通过");
    }

    // ========== 用户登出相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testLogout_success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("登出成功"));
        
        System.out.println("✅ 用户登出成功测试通过");
    }

    // ========== 边界值测试 ==========

    @Test
    public void testRegister_longUsername() throws Exception {
        String longUsername = "a".repeat(51); // 超过50字符
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                    {
                      "username":"%s",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """, longUsername)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 用户名过长注册测试通过");
    }

    @Test
    public void testRegister_emptyUsername() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空用户名注册测试通过");
    }

    @Test
    public void testRegister_emptyEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空邮箱注册测试通过");
    }

    @Test
    public void testRegister_emptyPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"testuser",
                      "email":"test@example.com",
                      "password":"",
                      "confirmPassword":""
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空密码注册测试通过");
    }

    @Test
    public void testLogin_emptyEmail() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"",
                      "password":"password123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空邮箱登录测试通过");
    }

    @Test
    public void testLogin_emptyPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"test@example.com",
                      "password":""
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空密码登录测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_emptyCurrentPassword() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"",
                      "newPassword":"newpassword123",
                      "confirmPassword":"newpassword123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空当前密码测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_emptyNewPassword() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "currentPassword":"oldpassword",
                      "newPassword":"",
                      "confirmPassword":""
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空新密码测试通过");
    }

    // ========== 特殊字符测试 ==========

    @Test
    public void testRegister_specialCharactersInUsername() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("test@#$%^&*()");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);
        UserDTO.AuthResponse authResponse = new UserDTO.AuthResponse("jwt-token", "refresh-token", userResponse);

        Mockito.when(userService.register(any(UserDTO.UserRegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"test@#$%^&*()",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isOk());
        
        System.out.println("✅ 特殊字符用户名注册测试通过");
    }

    @Test
    public void testRegister_unicodeCharactersInUsername() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("测试用户🚀");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);

        UserDTO.UserResponse userResponse = UserDTO.UserResponse.fromEntity(user);
        UserDTO.AuthResponse authResponse = new UserDTO.AuthResponse("jwt-token", "refresh-token", userResponse);

        Mockito.when(userService.register(any(UserDTO.UserRegisterRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username":"测试用户🚀",
                      "email":"test@example.com",
                      "password":"password123",
                      "confirmPassword":"password123"
                    }
                    """))
                .andExpect(status().isOk());
        
        System.out.println("✅ Unicode字符用户名注册测试通过");
    }

    // ========== 无效JSON测试 ==========

    @Test
    public void testRegister_malformedJson() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 格式错误JSON注册测试通过");
    }

    @Test
    public void testLogin_malformedJson() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 格式错误JSON登录测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_malformedJson() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 格式错误JSON修改密码测试通过");
    }

    // ========== 无效Content-Type测试 ==========

    @Test
    public void testRegister_invalidContentType() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
        
        System.out.println("✅ 无效Content-Type注册测试通过");
    }

    @Test
    public void testLogin_invalidContentType() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
        
        System.out.println("✅ 无效Content-Type登录测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testChangePassword_invalidContentType() throws Exception {
        mockMvc.perform(post("/auth/change-password")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
        
        System.out.println("✅ 无效Content-Type修改密码测试通过");
    }
}