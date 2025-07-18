package com.aiplatform.controller;

import com.aiplatform.entity.Chat;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.User;
import com.aiplatform.entity.UserSettings;
import com.aiplatform.repository.ChatRepository;
import com.aiplatform.repository.MessageRepository;
import com.aiplatform.repository.UserRepository;
import com.aiplatform.repository.UserSettingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.Mockito;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        DataController.class, 
        DataControllerTest.TestConfig.class,
        DataControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class DataControllerTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    })
    static class TestConfig {
    }

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRepository chatRepository;

    @MockBean
    private MessageRepository messageRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        // 基本配置测试
        System.out.println("✅ DataControllerTest 基本配置测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetSettings_withExistingSettings() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟用户设置
        UserSettings userSettings = new UserSettings();
        userSettings.setId(1L);
        userSettings.setUserId(1L);
        userSettings.setAutoCleanupEnabled(true);
        userSettings.setRetentionDays(60);
        userSettings.setMaxChats(200);
        userSettings.setProtectedLimit(20);

        Mockito.when(userSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(userSettings));

        mockMvc.perform(MockMvcRequestBuilders.get("/data/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoDelete").value(true))
                .andExpect(jsonPath("$.retentionDays").value(60))
                .andExpect(jsonPath("$.maxChatCount").value(200))
                .andExpect(jsonPath("$.protectedChats").value(20));

        System.out.println("✅ 获取用户设置（已存在）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetSettings_withoutExistingSettings() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟没有用户设置
        Mockito.when(userSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        // 模拟保存默认设置
        UserSettings defaultSettings = new UserSettings();
        defaultSettings.setId(1L);
        defaultSettings.setUserId(1L);
        defaultSettings.setAutoCleanupEnabled(false);
        defaultSettings.setRetentionDays(30);
        defaultSettings.setMaxChats(100);
        defaultSettings.setProtectedLimit(10);

        Mockito.when(userSettingsRepository.save(any(UserSettings.class)))
                .thenReturn(defaultSettings);

        mockMvc.perform(MockMvcRequestBuilders.get("/data/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoDelete").value(false))
                .andExpect(jsonPath("$.retentionDays").value(30))
                .andExpect(jsonPath("$.maxChatCount").value(100))
                .andExpect(jsonPath("$.protectedChats").value(10));

        System.out.println("✅ 获取用户设置（默认设置）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testUpdateSettings_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有用户设置
        UserSettings existingSettings = new UserSettings();
        existingSettings.setId(1L);
        existingSettings.setUserId(1L);
        existingSettings.setAutoCleanupEnabled(false);
        existingSettings.setRetentionDays(30);
        existingSettings.setMaxChats(100);
        existingSettings.setProtectedLimit(10);

        Mockito.when(userSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingSettings));

        // 模拟保存更新后的设置
        UserSettings updatedSettings = new UserSettings();
        updatedSettings.setId(1L);
        updatedSettings.setUserId(1L);
        updatedSettings.setAutoCleanupEnabled(true);
        updatedSettings.setRetentionDays(60);
        updatedSettings.setMaxChats(200);
        updatedSettings.setProtectedLimit(20);

        Mockito.when(userSettingsRepository.save(any(UserSettings.class)))
                .thenReturn(updatedSettings);

        mockMvc.perform(MockMvcRequestBuilders.put("/data/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "autoDelete": true,
                      "retentionDays": 60,
                      "maxChatCount": 200,
                      "protectedChats": 20
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("设置更新成功"))
                .andExpect(jsonPath("$.autoDelete").value(true))
                .andExpect(jsonPath("$.retentionDays").value(60))
                .andExpect(jsonPath("$.maxChatCount").value(200))
                .andExpect(jsonPath("$.protectedChats").value(20));

        System.out.println("✅ 更新用户设置测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testUpdateSettings_partialUpdate() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有用户设置
        UserSettings existingSettings = new UserSettings();
        existingSettings.setId(1L);
        existingSettings.setUserId(1L);
        existingSettings.setAutoCleanupEnabled(false);
        existingSettings.setRetentionDays(30);
        existingSettings.setMaxChats(100);
        existingSettings.setProtectedLimit(10);

        Mockito.when(userSettingsRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingSettings));

        // 模拟保存更新后的设置
        UserSettings updatedSettings = new UserSettings();
        updatedSettings.setId(1L);
        updatedSettings.setUserId(1L);
        updatedSettings.setAutoCleanupEnabled(true);
        updatedSettings.setRetentionDays(30); // 保持不变
        updatedSettings.setMaxChats(100); // 保持不变
        updatedSettings.setProtectedLimit(10); // 保持不变

        Mockito.when(userSettingsRepository.save(any(UserSettings.class)))
                .thenReturn(updatedSettings);

        mockMvc.perform(MockMvcRequestBuilders.put("/data/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "autoDelete": true
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.autoDelete").value(true));

        System.out.println("✅ 部分更新用户设置测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetStatistics_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟统计数据
        Mockito.when(chatRepository.countByUserId(1L)).thenReturn(10);
        Mockito.when(messageRepository.countByUserId(1L)).thenReturn(50);
        Mockito.when(chatRepository.countByUserIdAndIsProtectedTrue(1L)).thenReturn(3);

        // 模拟过期聊天
        List<Chat> oldChats = Arrays.asList(
            createChat(1L, "旧对话1"),
            createChat(2L, "旧对话2")
        );
        Mockito.when(chatRepository.findChatsToCleanup(eq(1L), any(LocalDateTime.class)))
                .thenReturn(oldChats);

        mockMvc.perform(MockMvcRequestBuilders.get("/data/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalChats").value(10))
                .andExpect(jsonPath("$.totalMessages").value(50))
                .andExpect(jsonPath("$.protectedChats").value(3))
                .andExpect(jsonPath("$.totalSize").value("0.0 MB"))
                .andExpect(jsonPath("$.oldChats").value(2));

        System.out.println("✅ 获取数据统计测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testCleanupData_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟需要清理的聊天
        List<Chat> chatsToCleanup = Arrays.asList(
            createChat(1L, "过期对话1"),
            createChat(2L, "过期对话2"),
            createChat(3L, "过期对话3")
        );
        Mockito.when(chatRepository.findChatsToCleanup(eq(1L), any(LocalDateTime.class)))
                .thenReturn(chatsToCleanup);

        // 模拟删除操作
        Mockito.doNothing().when(messageRepository).deleteByChatId(anyLong());
        Mockito.doNothing().when(chatRepository).delete(any(Chat.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/data/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("数据清理完成"))
                .andExpect(jsonPath("$.deletedChats").value(3))
                .andExpect(jsonPath("$.freedSpace").value("1.5 MB"));

        System.out.println("✅ 清理过期数据测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testCleanupData_noDataToCleanup() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟没有需要清理的聊天
        Mockito.when(chatRepository.findChatsToCleanup(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.post("/data/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.deletedChats").value(0))
                .andExpect(jsonPath("$.freedSpace").value("0.0 MB"));

        System.out.println("✅ 清理过期数据（无数据）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testDeleteAllData_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟所有聊天
        List<Chat> allChats = Arrays.asList(
            createChat(1L, "对话1", false),
            createChat(2L, "对话2", false),
            createChat(3L, "保护对话", true)
        );
        Mockito.when(chatRepository.findByUserIdOrderByLastActivityDesc(1L))
                .thenReturn(allChats);

        // 模拟删除操作
        Mockito.doNothing().when(messageRepository).deleteByChatId(anyLong());
        Mockito.doNothing().when(chatRepository).delete(any(Chat.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/data/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "confirmText": "CONFIRM_DELETE"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("所有非保护数据已删除"))
                .andExpect(jsonPath("$.deletedChats").value(2));

        System.out.println("✅ 删除所有数据测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testDeleteAllData_invalidConfirmText() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.delete("/data/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "confirmText": "WRONG_TEXT"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("确认文本不正确"));

        System.out.println("✅ 删除所有数据（错误确认文本）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testExportData_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天数据
        List<Chat> chats = Arrays.asList(
            createChat(1L, "对话1"),
            createChat(2L, "对话2")
        );
        Mockito.when(chatRepository.findByUserIdOrderByLastActivityDesc(1L))
                .thenReturn(chats);

        // 模拟消息数据
        List<Message> messages1 = Arrays.asList(
            createMessage(1L, 1L, "你好", Message.MessageRole.user),
            createMessage(2L, 1L, "你好！我是AI助手", Message.MessageRole.assistant)
        );
        List<Message> messages2 = Arrays.asList(
            createMessage(3L, 2L, "分析图片", Message.MessageRole.user),
            createMessage(4L, 2L, "这是一张图片分析结果", Message.MessageRole.assistant)
        );

        Mockito.when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L))
                .thenReturn(messages1);
        Mockito.when(messageRepository.findByChatIdOrderByCreatedAtAsc(2L))
                .thenReturn(messages2);

        mockMvc.perform(MockMvcRequestBuilders.get("/data/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.totalChats").value(2))
                .andExpect(jsonPath("$.chats").isArray())
                .andExpect(jsonPath("$.chats[0].title").value("对话1"))
                .andExpect(jsonPath("$.chats[1].title").value("对话2"));

        System.out.println("✅ 导出数据测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testExportData_emptyData() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟空聊天数据
        Mockito.when(chatRepository.findByUserIdOrderByLastActivityDesc(1L))
                .thenReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get("/data/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.totalChats").value(0))
                .andExpect(jsonPath("$.chats").isArray());

        System.out.println("✅ 导出数据（空数据）测试通过");
    }

    // 辅助方法：创建测试聊天
    private Chat createChat(Long id, String title) {
        return createChat(id, title, false);
    }

    private Chat createChat(Long id, String title, boolean isProtected) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUserId(1L);
        chat.setTitle(title);
        chat.setAiType(Chat.AiType.conversation);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setLastActivity(LocalDateTime.now());
        chat.setIsProtected(isProtected);
        chat.setIsFavorite(false);
        chat.setMessageCount(0);
        return chat;
    }

    // 辅助方法：创建测试消息
    private Message createMessage(Long id, Long chatId, String content, Message.MessageRole role) {
        Message message = new Message();
        message.setId(id);
        message.setChatId(chatId);
        message.setContent(content);
        message.setRole(role);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
} 