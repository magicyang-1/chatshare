package com.aiplatform.controller;

import com.aiplatform.entity.AdminMessage;
import com.aiplatform.entity.SupportChat;
import com.aiplatform.entity.User;
import com.aiplatform.repository.AdminMessageRepository;
import com.aiplatform.repository.ChatRepository;
import com.aiplatform.repository.MessageRepository;
import com.aiplatform.repository.SupportChatRepository;
import com.aiplatform.repository.UserRepository;
import com.aiplatform.security.JwtTokenProvider;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        UserController.class, 
        UserControllerTest.TestConfig.class,
        UserControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class UserControllerTest {

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
    private UserRepository userRepository;

    @MockBean
    private ChatRepository chatRepository;

    @MockBean
    private MessageRepository messageRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AdminMessageRepository adminMessageRepository;

    @MockBean
    private SupportChatRepository supportChatRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        // 最小测试：验证所有依赖都被正确注入
        assertNotNull(mockMvc, "MockMvc should be injected");
        assertNotNull(objectMapper, "ObjectMapper should be injected");
        assertNotNull(userRepository, "UserRepository should be mocked");
        assertNotNull(chatRepository, "ChatRepository should be mocked");
        assertNotNull(messageRepository, "MessageRepository should be mocked");
        assertNotNull(passwordEncoder, "PasswordEncoder should be mocked");
        assertNotNull(jwtTokenProvider, "JwtTokenProvider should be mocked");
        assertNotNull(adminMessageRepository, "AdminMessageRepository should be mocked");
        assertNotNull(supportChatRepository, "SupportChatRepository should be mocked");
        
        System.out.println("✅ 基本配置测试通过 - 所有依赖都已正确注入");
    }

    // ========== 用户资料相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserProfile_success() throws Exception {
        // 模拟用户数据
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setStatus(User.UserStatus.active);
        user.setPermissions("basic");
        user.setCreatedAt(LocalDateTime.now());

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("user"))
                .andExpect(jsonPath("$.status").value("active"));
        
        System.out.println("✅ 获取用户资料成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserProfile_userNotFound() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 用户不存在测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateUserProfile_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(user);

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"newemail@example.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().string("资料更新成功"));
        
        System.out.println("✅ 更新用户资料成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateUserProfile_userNotFound() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"newemail@example.com"
                    }
                    """))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 更新用户资料失败测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateUserProfile_emptyEmail() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":""
                    }
                    """))
                .andExpect(status().isOk()); // 空邮箱应该被忽略
        
        System.out.println("✅ 空邮箱更新测试通过");
    }

    // ========== 使用统计相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUsageStats_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now().minusDays(5));

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(chatRepository.countByUserId(1L))
                .thenReturn(10);
        Mockito.when(messageRepository.countByUserId(1L))
                .thenReturn(50);
        Mockito.when(chatRepository.countByUserIdAndIsFavoriteTrue(1L))
                .thenReturn(3);

        mockMvc.perform(get("/user/usage-stats")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalChats").value(10))
                .andExpect(jsonPath("$.totalMessages").value(50))
                .andExpect(jsonPath("$.favoriteChats").value(3))
                .andExpect(jsonPath("$.usageDays").exists());
        
        System.out.println("✅ 获取使用统计成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUsageStats_userNotFound() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/user/usage-stats")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound());
        
        System.out.println("✅ 用户不存在统计测试通过");
    }

    // ========== 权限相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetPermissions_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.user);
        user.setPermissions("basic");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/user/permissions")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("user"))
                .andExpect(jsonPath("$.permissions").value("basic"))
                .andExpect(jsonPath("$.isAdmin").value(false))
                .andExpect(jsonPath("$.isSupport").value(false));
        
        System.out.println("✅ 获取权限信息成功测试通过");
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    public void testGetPermissions_adminUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setRole(User.UserRole.admin);
        user.setPermissions("admin");

        Mockito.when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(get("/user/permissions")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("admin"))
                .andExpect(jsonPath("$.isAdmin").value(true));
        
        System.out.println("✅ 管理员权限测试通过");
    }

    // ========== 消息相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserMessages_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        AdminMessage message = new AdminMessage();
        message.setId(1L);
        message.setSubject("测试消息");
        message.setContent("消息内容");
        message.setMessageType(AdminMessage.MessageType.PRIVATE);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setToUserId(1L);

        List<AdminMessage> messages = Arrays.asList(message);
        Page<AdminMessage> page = new PageImpl<>(messages, PageRequest.of(0, 20), 1);

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(adminMessageRepository.findByToUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/user/messages")
                        .header("Authorization", "Bearer test-token")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].subject").value("测试消息"));
        
        System.out.println("✅ 获取用户消息成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserMessages_userNotFound() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/user/messages")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 用户不存在消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testMarkMessageAsRead_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        AdminMessage message = new AdminMessage();
        message.setId(1L);
        message.setToUserId(1L);
        message.setIsRead(false);

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(adminMessageRepository.findById(1L))
                .thenReturn(Optional.of(message));
        Mockito.when(adminMessageRepository.save(any(AdminMessage.class)))
                .thenReturn(message);

        mockMvc.perform(patch("/user/messages/1/read")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("消息已标记为已读"));
        
        System.out.println("✅ 标记消息已读成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testMarkMessageAsRead_messageNotFound() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(adminMessageRepository.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/user/messages/1/read")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 消息不存在测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testMarkMessageAsRead_unauthorized() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        AdminMessage message = new AdminMessage();
        message.setId(1L);
        message.setToUserId(2L); // 不属于当前用户

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(adminMessageRepository.findById(1L))
                .thenReturn(Optional.of(message));

        mockMvc.perform(patch("/user/messages/1/read")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden());
        
        System.out.println("✅ 无权限标记消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteMessage_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        AdminMessage message = new AdminMessage();
        message.setId(1L);
        message.setToUserId(1L);

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(adminMessageRepository.findById(1L))
                .thenReturn(Optional.of(message));

        mockMvc.perform(delete("/user/messages/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("消息已删除"));
        
        System.out.println("✅ 删除消息成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteMessage_unauthorized() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        AdminMessage message = new AdminMessage();
        message.setId(1L);
        message.setToUserId(2L); // 不属于当前用户

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(adminMessageRepository.findById(1L))
                .thenReturn(Optional.of(message));

        mockMvc.perform(delete("/user/messages/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isForbidden());
        
        System.out.println("✅ 无权限删除消息测试通过");
    }

    // ========== 客服相关测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetSupportChat_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        SupportChat chat = new SupportChat();
        chat.setId(1L);
        chat.setContent("客服消息");
        chat.setSenderType(SupportChat.SenderType.SUPPORT);
        chat.setIsRead(false);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUserId(1L);
        chat.setSupportId(2L);

        List<SupportChat> chats = Arrays.asList(chat);

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(supportChatRepository.findByUserIdOrderByCreatedAtAsc(1L))
                .thenReturn(chats);

        mockMvc.perform(get("/user/support/chat")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").value("客服消息"));
        
        System.out.println("✅ 获取客服对话成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendToSupport_success() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(supportChatRepository.save(any(SupportChat.class)))
                .thenReturn(new SupportChat());

        mockMvc.perform(post("/user/support/message")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"我需要帮助"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().string("消息已发送给客服，请等待回复"));
        
        System.out.println("✅ 发送客服消息成功测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendToSupport_emptyContent() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/user/support/message")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":""
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("消息内容不能为空"));
        
        System.out.println("✅ 空消息内容测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendToSupport_missingContent() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/user/support/message")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "supportId":1
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("消息内容不能为空"));
        
        System.out.println("✅ 缺少消息内容测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetSupportStaff_success() throws Exception {
        User support1 = new User();
        support1.setId(1L);
        support1.setUsername("support1");
        support1.setEmail("support1@example.com");
        support1.setRole(User.UserRole.support);
        support1.setStatus(User.UserStatus.active);

        User support2 = new User();
        support2.setId(2L);
        support2.setUsername("support2");
        support2.setEmail("support2@example.com");
        support2.setRole(User.UserRole.support);
        support2.setStatus(User.UserStatus.active);

        List<User> supportStaff = Arrays.asList(support1, support2);

        Mockito.when(userRepository.findByRoleInAndStatus(
                        eq(Arrays.asList(User.UserRole.support)), 
                        eq(User.UserStatus.active)))
                .thenReturn(supportStaff);

        mockMvc.perform(get("/user/support/staff")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("support1"))
                .andExpect(jsonPath("$[1].username").value("support2"));
        
        System.out.println("✅ 获取客服人员列表成功测试通过");
    }

    // ========== 边界值测试 ==========



    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendToSupport_withSupportId() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(supportChatRepository.save(any(SupportChat.class)))
                .thenReturn(new SupportChat());

        mockMvc.perform(post("/user/support/message")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"我需要帮助",
                      "supportId":2
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().string("消息已发送给客服，请等待回复"));
        
        System.out.println("✅ 指定客服ID测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendToSupport_invalidSupportId() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(supportChatRepository.save(any(SupportChat.class)))
                .thenReturn(new SupportChat());

        mockMvc.perform(post("/user/support/message")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"我需要帮助",
                      "supportId":"invalid"
                    }
                    """))
                .andExpect(status().isOk()) // 无效的supportId应该被忽略
                .andExpect(content().string("消息已发送给客服，请等待回复"));
        
        System.out.println("✅ 无效客服ID测试通过");
    }

    // ========== 异常处理测试 ==========

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserProfile_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 获取用户资料异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateUserProfile_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(put("/user/profile")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email":"newemail@example.com"
                    }
                    """))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 更新用户资料异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUsageStats_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/user/usage-stats")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 获取使用统计异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetPermissions_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/user/permissions")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 获取权限信息异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserMessages_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/user/messages")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 获取用户消息异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetSupportChat_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/user/support/chat")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 获取客服对话异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendToSupport_exception() throws Exception {
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(post("/user/support/message")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"我需要帮助"
                    }
                    """))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 发送客服消息异常测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetSupportStaff_exception() throws Exception {
        Mockito.when(userRepository.findByRoleInAndStatus(any(), any()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/user/support/staff")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isInternalServerError());
        
        System.out.println("✅ 获取客服人员列表异常测试通过");
    }
} 