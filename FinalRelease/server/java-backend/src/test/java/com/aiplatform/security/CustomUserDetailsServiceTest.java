package com.aiplatform.security;

import com.aiplatform.entity.User;
import com.aiplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CustomUserDetailsService测试类
 * 演示如何测试Spring Security的UserDetailsService实现
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private User adminUser;
    private User bannedUser;
    private User inactiveUser;

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

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("admin");
        adminUser.setPassword("encoded_admin_123");
        adminUser.setRole(User.UserRole.admin);
        adminUser.setStatus(User.UserStatus.active);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());

        bannedUser = new User();
        bannedUser.setId(3L);
        bannedUser.setEmail("banned@example.com");
        bannedUser.setUsername("banneduser");
        bannedUser.setPassword("encoded_banned_123");
        bannedUser.setRole(User.UserRole.user);
        bannedUser.setStatus(User.UserStatus.banned);
        bannedUser.setCreatedAt(LocalDateTime.now());
        bannedUser.setUpdatedAt(LocalDateTime.now());

        inactiveUser = new User();
        inactiveUser.setId(4L);
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setUsername("inactiveuser");
        inactiveUser.setPassword("encoded_inactive_123");
        inactiveUser.setRole(User.UserRole.user);
        inactiveUser.setStatus(User.UserStatus.inactive);
        inactiveUser.setCreatedAt(LocalDateTime.now());
        inactiveUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testLoadUserByUsername_Success() {
        // 准备测试数据
        String email = "test@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(testUser.getEmail(), userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_user")));
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_AdminUser() {
        // 准备测试数据
        String email = "admin@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(adminUser.getEmail(), userDetails.getUsername());
        assertEquals(adminUser.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_admin")));
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_SupportUser() {
        // 准备测试数据
        User supportUser = new User();
        supportUser.setId(5L);
        supportUser.setEmail("support@example.com");
        supportUser.setUsername("support");
        supportUser.setPassword("encoded_support_123");
        supportUser.setRole(User.UserRole.support);
        supportUser.setStatus(User.UserStatus.active);
        supportUser.setCreatedAt(LocalDateTime.now());
        supportUser.setUpdatedAt(LocalDateTime.now());

        String email = "support@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(supportUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(supportUser.getEmail(), userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_support")));
        assertTrue(userDetails.isEnabled());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_BannedUser() {
        // 准备测试数据
        String email = "banned@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(bannedUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(bannedUser.getEmail(), userDetails.getUsername());
        assertEquals(bannedUser.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_user")));
        assertTrue(userDetails.isAccountNonExpired());
        assertFalse(userDetails.isAccountNonLocked()); // 被禁用的用户账户被锁定
        assertTrue(userDetails.isCredentialsNonExpired());
        assertFalse(userDetails.isEnabled()); // 被禁用的用户不可用

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_InactiveUser() {
        // 准备测试数据
        String email = "inactive@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(inactiveUser.getEmail(), userDetails.getUsername());
        assertEquals(inactiveUser.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_user")));
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertFalse(userDetails.isEnabled()); // 非活跃用户不可用

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // 准备测试数据
        String email = "nonexistent@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });

        // 验证异常信息
        assertEquals("用户不存在: " + email, exception.getMessage());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_NullEmail() {
        // 设置mock行为 - null参数会传递给repository
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(null);
        });

        // 验证异常信息
        assertEquals("用户不存在: null", exception.getMessage());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    void testLoadUserByUsername_EmptyEmail() {
        // 设置mock行为 - 空字符串参数会传递给repository
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("");
        });

        // 验证异常信息
        assertEquals("用户不存在: ", exception.getMessage());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail("");
    }

    @Test
    void testLoadUserByUsername_WhitespaceEmail() {
        // 设置mock行为 - 空白字符串参数会传递给repository
        when(userRepository.findByEmail("  ")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("  ");
        });

        // 验证异常信息
        assertEquals("用户不存在:   ", exception.getMessage());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail("  ");
    }

    @Test
    void testLoadUserByUsername_SpecialCharacters() {
        // 准备测试数据
        User specialUser = new User();
        specialUser.setId(6L);
        specialUser.setEmail("test+user@example.com");
        specialUser.setUsername("testuser");
        specialUser.setPassword("encoded_password_123");
        specialUser.setRole(User.UserRole.user);
        specialUser.setStatus(User.UserStatus.active);
        specialUser.setCreatedAt(LocalDateTime.now());
        specialUser.setUpdatedAt(LocalDateTime.now());

        String email = "test+user@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(specialUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(specialUser.getEmail(), userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_user")));
        assertTrue(userDetails.isEnabled());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testLoadUserByUsername_UnicodeCharacters() {
        // 准备测试数据
        User unicodeUser = new User();
        unicodeUser.setId(7L);
        unicodeUser.setEmail("测试用户@example.com");
        unicodeUser.setUsername("测试用户");
        unicodeUser.setPassword("encoded_password_123");
        unicodeUser.setRole(User.UserRole.user);
        unicodeUser.setStatus(User.UserStatus.active);
        unicodeUser.setCreatedAt(LocalDateTime.now());
        unicodeUser.setUpdatedAt(LocalDateTime.now());

        String email = "测试用户@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(unicodeUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(unicodeUser.getEmail(), userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_user")));
        assertTrue(userDetails.isEnabled());

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testAuthorityFormat() {
        // 准备测试数据
        String email = "test@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证权限格式
        assertTrue(userDetails.getAuthorities().stream()
                .allMatch(authority -> authority.getAuthority().startsWith("ROLE_")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + testUser.getRole().name())));

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testMultipleAuthorities() {
        // 准备测试数据 - 模拟有多个权限的用户
        User multiRoleUser = new User();
        multiRoleUser.setId(8L);
        multiRoleUser.setEmail("multiuser@example.com");
        multiRoleUser.setUsername("multiuser");
        multiRoleUser.setPassword("encoded_password_123");
        multiRoleUser.setRole(User.UserRole.admin); // 管理员角色
        multiRoleUser.setStatus(User.UserStatus.active);
        multiRoleUser.setCreatedAt(LocalDateTime.now());
        multiRoleUser.setUpdatedAt(LocalDateTime.now());

        String email = "multiuser@example.com";
        
        // 设置mock行为
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(multiRoleUser));

        // 执行测试
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size()); // 当前实现只有一个角色
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_admin")));

        // 验证mock方法被调用
        verify(userRepository, times(1)).findByEmail(email);
    }
} 