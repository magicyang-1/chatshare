package com.aiplatform.controller;

import com.aiplatform.entity.Chat;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.User;
import com.aiplatform.repository.ChatRepository;
import com.aiplatform.repository.UserRepository;
import com.aiplatform.service.ChatService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        HistoryController.class, 
        HistoryControllerTest.TestConfig.class,
        HistoryControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class HistoryControllerTest {

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
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatRepository chatRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        System.out.println("✅ HistoryControllerTest 基础配置测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_success() throws Exception {
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
        Page<Chat> chatPage = new PageImpl<>(chats, PageRequest.of(0, 20), 2);
        
        Mockito.when(chatRepository.findChatsWithFilters(
            eq(1L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(chatPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chats").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.currentPage").value(0));

        System.out.println("✅ 获取用户对话列表测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_withFilters() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟筛选后的聊天数据
        List<Chat> filteredChats = Arrays.asList(
            createChat(1L, "收藏对话", true, false)
        );
        Page<Chat> chatPage = new PageImpl<>(filteredChats, PageRequest.of(0, 20), 1);
        
        Mockito.when(chatRepository.findChatsWithFilters(
            eq(1L), any(), any(), eq(true), any(), any(Pageable.class)))
                .thenReturn(chatPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats")
                        .param("isFavorite", "true")
                        .param("keyword", "对话"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        System.out.println("✅ 获取用户对话列表（带筛选）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetChatDetail_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天
        Chat chat = createChat(1L, "测试对话");
        Mockito.when(chatRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(chat));

        // 模拟消息列表
        List<Message> messages = Arrays.asList(
            createMessage(1L, 1L, "你好", Message.MessageRole.user),
            createMessage(2L, 1L, "你好！我是AI助手", Message.MessageRole.assistant)
        );
        Mockito.when(chatService.getChatMessages(1L, 1L))
                .thenReturn(messages);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chat.id").value(1))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.messages.length()").value(2));

        System.out.println("✅ 获取对话详情测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetChatDetail_notFound() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天不存在
        Mockito.when(chatRepository.findByIdAndUserId(999L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("对话不存在或无权限访问"));

        System.out.println("✅ 获取对话详情（不存在）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserStats_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟统计数据
        Mockito.when(chatRepository.countByUserId(1L)).thenReturn(10);
        Mockito.when(chatRepository.countByUserIdAndIsFavoriteTrue(1L)).thenReturn(3);
        Mockito.when(chatRepository.countByUserIdAndIsProtectedTrue(1L)).thenReturn(2);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.totalChats").value(10))
                .andExpect(jsonPath("$.stats.favoriteChats").value(3))
                .andExpect(jsonPath("$.stats.protectedChats").value(2));

        System.out.println("✅ 获取用户统计信息测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testDeleteChat_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/history/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("聊天记录删除成功"));

        System.out.println("✅ 删除聊天记录测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetSearchSuggestions_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟搜索建议
        List<Chat> suggestionChats = Arrays.asList(
            createChat(1L, "对话1"),
            createChat(2L, "对话2")
        );
        Mockito.when(chatRepository.findTop5ByUserIdAndTitleContainingIgnoreCase(1L, "对话"))
                .thenReturn(suggestionChats);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/search-suggestions")
                        .param("query", "对话"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.suggestions.length()").value(2));

        System.out.println("✅ 获取搜索建议测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_favorite() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天
        Chat chat = createChat(1L, "测试对话", false, false);
        Mockito.when(chatRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(chat));
        Mockito.when(chatRepository.save(any(Chat.class)))
                .thenReturn(chat);

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "favorite");
        request.put("chatIds", Arrays.asList(1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.totalCount").value(1));

        System.out.println("✅ 批量操作（收藏）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_protect() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天
        Chat chat = createChat(1L, "测试对话", false, false);
        Mockito.when(chatRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(chat));
        Mockito.when(chatRepository.save(any(Chat.class)))
                .thenReturn(chat);

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "protect");
        request.put("chatIds", Arrays.asList(1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(1));

        System.out.println("✅ 批量操作（保护）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_delete() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天
        Chat chat = createChat(1L, "测试对话");
        Mockito.when(chatRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(chat));

        // 模拟删除操作
        Mockito.doNothing().when(chatService).deleteChat(1L, 1L);

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "delete");
        request.put("chatIds", Arrays.asList(1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(1));

        System.out.println("✅ 批量操作（删除）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_invalidOperation() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天存在，这样代码会执行到 switch 语句的 default 分支
        Chat chat = createChat(1L, "测试对话");
        Mockito.when(chatRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(chat));

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "invalid");
        request.put("chatIds", Arrays.asList(1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.message").value("成功处理 1/1 个对话"));

        System.out.println("✅ 批量操作（无效操作）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_incompleteParams() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "favorite");
        // 缺少 chatIds

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        System.out.println("✅ 批量操作（参数不完整）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testExportChats_success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/history/export")
                        .param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.downloadUrl").value("/api/history/download/temp.json"))
                .andExpect(jsonPath("$.message").value("导出任务已创建"));

        System.out.println("✅ 导出聊天记录测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatRepository.findChatsWithFilters(any(), any(), any(), any(), any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("获取对话失败"));

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("获取对话失败"));

        System.out.println("✅ 获取用户对话列表（业务异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_withDateRange() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟筛选后的聊天数据
        List<Chat> filteredChats = Arrays.asList(
            createChat(1L, "最近对话")
        );
        Page<Chat> chatPage = new PageImpl<>(filteredChats, PageRequest.of(0, 20), 1);
        
        Mockito.when(chatRepository.findChatsWithFilters(
            eq(1L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(chatPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        System.out.println("✅ 获取用户对话列表（日期范围）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_withAiType() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟筛选后的聊天数据
        List<Chat> filteredChats = Arrays.asList(
            createChat(1L, "图片生成对话")
        );
        Page<Chat> chatPage = new PageImpl<>(filteredChats, PageRequest.of(0, 20), 1);
        
        Mockito.when(chatRepository.findChatsWithFilters(
            eq(1L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(chatPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats")
                        .param("aiType", "text_to_image"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        System.out.println("✅ 获取用户对话列表（AI类型）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetChatDetail_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatRepository.findByIdAndUserId(any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("获取对话详情失败"));

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("获取对话详情失败"));

        System.out.println("✅ 获取对话详情（业务异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetChatDetail_withManyMessages() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天
        Chat chat = createChat(1L, "长对话");
        Mockito.when(chatRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(chat));

        // 模拟大量消息
        List<Message> messages = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            messages.add(createMessage((long) i, 1L, "消息" + i, 
                i % 2 == 0 ? Message.MessageRole.assistant : Message.MessageRole.user));
        }
        Mockito.when(chatService.getChatMessages(1L, 1L))
                .thenReturn(messages);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chat.id").value(1))
                .andExpect(jsonPath("$.messages.length()").value(50));

        System.out.println("✅ 获取对话详情（大量消息）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserStats_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatRepository.countByUserId(any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("获取统计失败"));

        mockMvc.perform(MockMvcRequestBuilders.get("/history/stats"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("获取统计失败"));

        System.out.println("✅ 获取用户统计（业务异常）测试通过");
    }

//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
//    public void testDeleteChat_businessException() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟业务异常
//        Mockito.doThrow(new com.aiplatform.exception.BusinessException("删除失败"))
//                .when(chatService).deleteChat(any(), any());
//
//        mockMvc.perform(MockMvcRequestBuilders.delete("/history/chats/1"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.error").value("删除失败"));
//
//        System.out.println("✅ 删除对话（业务异常）测试通过");
//    }
//
//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
//    public void testGetSearchSuggestions_businessException() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟业务异常
//        Mockito.when(chatRepository.findTop5ByUserIdAndTitleContainingIgnoreCase(any(), any()))
//                .thenThrow(new com.aiplatform.exception.BusinessException("获取建议失败"));
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/history/suggestions")
//                        .param("keyword", "测试"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.error").value("获取建议失败"));
//
//        System.out.println("✅ 获取搜索建议（业务异常）测试通过");
//    }
//
//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
//    public void testGetSearchSuggestions_emptyKeyword() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟空关键词搜索
//        Mockito.when(chatRepository.findTop5ByUserIdAndTitleContainingIgnoreCase(any(), eq("")))
//                .thenReturn(Arrays.asList());
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/history/suggestions")
//                        .param("keyword", ""))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.suggestions").isArray())
//                .andExpect(jsonPath("$.suggestions.length()").value(0));
//
//        System.out.println("✅ 获取搜索建议（空关键词）测试通过");
//    }
//
//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
//    public void testBatchOperation_businessException() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟业务异常
//        Mockito.doThrow(new com.aiplatform.exception.BusinessException("批量操作失败"))
//                .when(chatService).toggleFavorite(any(), any());
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                    {
//                      "operation":"favorite",
//                      "chatIds":[1,2,3]
//                    }
//                    """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.error").value("批量操作失败"));
//
//        System.out.println("✅ 批量操作（业务异常）测试通过");
//    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_emptyChatIds() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "operation":"favorite",
                      "chatIds":[]
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("参数不完整"));

        System.out.println("✅ 批量操作（空聊天ID列表）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testBatchOperation_missingOperation() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/history/batch-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "chatIds":[1,2,3]
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("参数不完整"));

        System.out.println("✅ 批量操作（缺少操作类型）测试通过");
    }

//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
//    public void testExportChats_businessException() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟业务异常
//        Mockito.when(chatService.getChatById(any()))
//                .thenThrow(new com.aiplatform.exception.BusinessException("导出失败"));
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/history/export")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                    {
//                      "chatIds":[1,2,3],
//                      "format":"json"
//                    }
//                    """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.error").value("导出失败"));
//
//        System.out.println("✅ 导出对话（业务异常）测试通过");
//    }
//
//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
//    public void testExportChats_differentFormats() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟导出成功
//        Mockito.when(chatService.getChatById(any()))
//                .thenReturn(createChat(1L, "测试对话"));
//
//        String[] formats = {"json", "txt", "csv"};
//        for (String format : formats) {
//            mockMvc.perform(MockMvcRequestBuilders.post("/history/export")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(String.format("""
//                        {
//                          "chatIds":[1,2,3],
//                          "format":"%s"
//                        }
//                        """, format)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true));
//        }
//
//        System.out.println("✅ 导出对话（不同格式）测试通过");
//    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_pagination() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟分页数据
        List<Chat> chats = Arrays.asList(
            createChat(1L, "对话1"),
            createChat(2L, "对话2")
        );
        Page<Chat> chatPage = new PageImpl<>(chats, PageRequest.of(1, 5), 12); // 第2页，每页5条，总共12条
        
        Mockito.when(chatRepository.findChatsWithFilters(
            eq(1L), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(chatPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.chats.length()").value(2));

        System.out.println("✅ 获取用户对话列表（分页）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    public void testGetUserChats_allFilters() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟筛选后的聊天数据
        List<Chat> filteredChats = Arrays.asList(
            createChat(1L, "收藏的图片对话", true, false)
        );
        Page<Chat> chatPage = new PageImpl<>(filteredChats, PageRequest.of(0, 20), 1);
        
        Mockito.when(chatRepository.findChatsWithFilters(
            eq(1L), any(), any(), eq(true), any(), any(Pageable.class)))
                .thenReturn(chatPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/history/chats")
                        .param("keyword", "图片")
                        .param("aiType", "text_to_image")
                        .param("isFavorite", "true")
                        .param("isProtected", "false")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));

        System.out.println("✅ 获取用户对话列表（所有筛选条件）测试通过");
    }

    // 辅助方法：创建测试聊天对象
    private Chat createChat(Long id, String title) {
        return createChat(id, title, false, false);
    }

    private Chat createChat(Long id, String title, boolean isFavorite, boolean isProtected) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setTitle(title);
        chat.setUserId(1L);
        chat.setIsFavorite(isFavorite);
        chat.setIsProtected(isProtected);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setLastActivity(LocalDateTime.now());
        return chat;
    }

    // 辅助方法：创建测试消息对象
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