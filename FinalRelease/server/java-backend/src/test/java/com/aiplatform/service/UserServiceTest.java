package com.aiplatform.service;

import com.aiplatform.dto.UserDTO;
import com.aiplatform.entity.User;
import com.aiplatform.entity.Chat;
import com.aiplatform.exception.BusinessException;
import com.aiplatform.repository.UserRepository;
import com.aiplatform.repository.ChatRepository;
import com.aiplatform.repository.MessageRepository;
import com.aiplatform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO.UserRegisterRequest registerRequest;
    private UserDTO.UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.UserRole.user);
        testUser.setStatus(User.UserStatus.active);
        testUser.setCreatedAt(LocalDateTime.now());

        // 创建注册请求
        registerRequest = new UserDTO.UserRegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        // 创建登录请求
        loginRequest = new UserDTO.UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {
        // 准备mock数据
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any(String.class))).thenReturn("jwt-token");
        when(jwtTokenProvider.generateRefreshToken(any(String.class))).thenReturn("refresh-token");

        // 执行测试
        UserDTO.AuthResponse result = userService.register(registerRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertNotNull(result.getUser());

        // 验证调用
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken(any(String.class));
        verify(jwtTokenProvider).generateRefreshToken(any(String.class));
    }

    @Test
    void testRegister_PasswordMismatch() {
        // 准备测试数据
        registerRequest.setConfirmPassword("differentPassword");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("两次输入的密码不一致", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // 准备mock数据
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // 准备mock数据
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("用户名已被使用", exception.getMessage());
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken("test@example.com")).thenReturn("jwt-token");
        when(jwtTokenProvider.generateRefreshToken("test@example.com")).thenReturn("refresh-token");

        // 执行测试
        UserDTO.AuthResponse result = userService.login(loginRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertNotNull(result.getUser());

        // 验证调用
        verify(userRepository).findByEmail("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken("test@example.com");
        verify(jwtTokenProvider).generateRefreshToken("test@example.com");
    }

    @Test
    void testLogin_UserNotFound() {
        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLogin_UserBanned() {
        // 准备mock数据
        testUser.setStatus(User.UserStatus.banned);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("账户已被禁用", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLogin_BadCredentials() {
        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testTriggerAutoCleanupForUser() {
        // 准备mock数据
        Chat oldChat = new Chat();
        oldChat.setId(1L);
        oldChat.setUserId(1L);
        oldChat.setTitle("旧聊天");
        oldChat.setIsProtected(false);

        when(chatRepository.findChatsToCleanup(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(oldChat));

        // 执行测试
        userService.triggerAutoCleanupForUser(1L);

        // 验证调用
        verify(chatRepository).findChatsToCleanup(eq(1L), any(LocalDateTime.class));
        verify(messageRepository).deleteByChatId(1L);
        verify(chatRepository).delete(oldChat);
    }

    @Test
    void testGetCurrentUser_Success() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // 执行测试
        UserDTO.UserResponse result = userService.getCurrentUser();

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());

        // 验证调用
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getCurrentUser();
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testUpdateProfile_Success() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备测试数据
        UserDTO.UserProfileUpdateRequest request = new UserDTO.UserProfileUpdateRequest();
        request.setUsername("newusername");
        request.setPermissions("CHAT,UPLOAD");

        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        UserDTO.UserResponse result = userService.updateProfile(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        // 验证调用
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).existsByUsername("newusername");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateProfile_UsernameAlreadyExists() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备测试数据
        UserDTO.UserProfileUpdateRequest request = new UserDTO.UserProfileUpdateRequest();
        request.setUsername("existingusername");

        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existingusername")).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateProfile(request);
        });

        assertEquals("用户名已被使用", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).existsByUsername("existingusername");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_Success() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备测试数据
        UserDTO.PasswordChangeRequest request = new UserDTO.PasswordChangeRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        userService.changePassword(request);

        // 验证调用
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).matches("newPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_PasswordMismatch() {
        // 准备测试数据
        UserDTO.PasswordChangeRequest request = new UserDTO.PasswordChangeRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("differentPassword");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(request);
        });

        assertEquals("两次输入的新密码不一致", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_WrongCurrentPassword() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备测试数据
        UserDTO.PasswordChangeRequest request = new UserDTO.PasswordChangeRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(request);
        });

        assertEquals("当前密码错误", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePassword_SamePassword() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        // 准备测试数据
        UserDTO.PasswordChangeRequest request = new UserDTO.PasswordChangeRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("samePassword");
        request.setConfirmPassword("samePassword");

        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("samePassword", "encodedPassword")).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(request);
        });

        assertEquals("新密码不能与当前密码相同", exception.getMessage());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).matches("samePassword", "encodedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testSearchUsers_WithKeyword() {
        // 准备测试数据
        UserDTO.UserSearchRequest request = new UserDTO.UserSearchRequest();
        request.setKeyword("test");
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("createdAt");
        request.setSortDirection("desc");

        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.searchUsers(eq("test"), any(Pageable.class))).thenReturn(userPage);

        // 执行测试
        Page<UserDTO.UserResponse> result = userService.searchUsers(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // 验证调用
        verify(userRepository).searchUsers(eq("test"), any(Pageable.class));
    }

    @Test
    void testSearchUsers_WithoutKeyword() {
        // 准备测试数据
        UserDTO.UserSearchRequest request = new UserDTO.UserSearchRequest();
        request.setKeyword("");
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("createdAt");
        request.setSortDirection("asc");

        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // 执行测试
        Page<UserDTO.UserResponse> result = userService.searchUsers(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        // 验证调用
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void testUpdateUserStatus_Success() {
        // 准备测试数据
        UserDTO.UserStatusUpdateRequest request = new UserDTO.UserStatusUpdateRequest();
        request.setStatus("banned");
        request.setReason("违规行为");

        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        userService.updateUserStatus(1L, request);

        // 验证调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserStatus_UserNotFound() {
        // 准备测试数据
        UserDTO.UserStatusUpdateRequest request = new UserDTO.UserStatusUpdateRequest();
        request.setStatus("banned");

        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserStatus(1L, request);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserStatus_InvalidStatus() {
        // 准备测试数据
        UserDTO.UserStatusUpdateRequest request = new UserDTO.UserStatusUpdateRequest();
        request.setStatus("invalid_status");

        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserStatus(1L, request);
        });

        assertEquals("无效的状态值: invalid_status", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserStatistics() {
        // 准备mock数据
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(User.UserStatus.active)).thenReturn(80L);
        when(userRepository.countByRole(User.UserRole.admin)).thenReturn(5L);
        when(userRepository.countByRole(User.UserRole.support)).thenReturn(10L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(20L);
        when(userRepository.countByStatus(User.UserStatus.banned)).thenReturn(5L);

        // 执行测试
        UserDTO.UserStatistics result = userService.getUserStatistics();

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(80L, result.getActiveUsers());
        assertEquals(5L, result.getAdminUsers());
        assertEquals(10L, result.getCustomerServiceUsers());
        assertEquals(20L, result.getNewUsersThisMonth());
        assertEquals(5L, result.getLockedUsers());

        // 验证调用
        verify(userRepository).count();
        verify(userRepository).countByStatus(User.UserStatus.active);
        verify(userRepository).countByRole(User.UserRole.admin);
        verify(userRepository).countByRole(User.UserRole.support);
        verify(userRepository).countByCreatedAtAfter(any(LocalDateTime.class));
        verify(userRepository).countByStatus(User.UserStatus.banned);
    }

    @Test
    void testFindByEmail() {
        // 准备mock数据
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // 执行测试
        Optional<User> result = userService.findByEmail("test@example.com");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());

        // 验证调用
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testUpdateUserRole_Success() {
        // 准备测试数据
        UserDTO.UserRoleUpdateRequest request = new UserDTO.UserRoleUpdateRequest();
        request.setRole("admin");
        request.setReason("提升为管理员");

        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        UserDTO.UserResponse result = userService.updateUserRole(1L, request);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());

        // 验证调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRole_UserNotFound() {
        // 准备测试数据
        UserDTO.UserRoleUpdateRequest request = new UserDTO.UserRoleUpdateRequest();
        request.setRole("admin");

        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserRole(1L, request);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserRole_InvalidRole() {
        // 准备测试数据
        UserDTO.UserRoleUpdateRequest request = new UserDTO.UserRoleUpdateRequest();
        request.setRole("invalid_role");

        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUserRole(1L, request);
        });

        assertEquals("无效的角色类型: invalid_role", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetCurrentUserEmail_NotAuthenticated() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getCurrentUser();
        });

        assertEquals("用户未登录", exception.getMessage());
    }

    @Test
    void testGetCurrentUserEmail_NullAuthentication() {
        // 设置SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getCurrentUser();
        });

        assertEquals("用户未登录", exception.getMessage());
    }
} 