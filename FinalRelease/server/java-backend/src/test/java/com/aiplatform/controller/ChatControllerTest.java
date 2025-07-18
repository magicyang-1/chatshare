package com.aiplatform.controller;

import com.aiplatform.entity.Chat;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.User;
import com.aiplatform.repository.UserRepository;
import com.aiplatform.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        ChatController.class, 
        ChatControllerTest.TestConfig.class,
        ChatControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class ChatControllerTest {

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
                    .anyRequest().permitAll()
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        // 基本配置测试
        System.out.println("✅ ChatControllerTest 基本配置测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateChat_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setTitle("测试对话");
        testChat.setAiType(Chat.AiType.conversation);
        testChat.setAiModel("openai/gpt-4.1-nano");
        testChat.setCreatedAt(LocalDateTime.now());
        testChat.setMessageCount(0);

        Mockito.when(chatService.createChat(Mockito.eq(1L), Mockito.eq("测试对话"), Mockito.eq(Chat.AiType.conversation)))
                .thenReturn(testChat);
        
        // Mock updateChatModel 方法
        Mockito.when(chatService.updateChatModel(Mockito.eq(1L), Mockito.eq("openai/gpt-4.1-nano")))
                .thenReturn(testChat);

        mockMvc.perform(post("/chat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":"测试对话",
                      "aiType":"conversation",
                      "aiModel":"openai/gpt-4.1-nano"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chat.id").value(1))
                .andExpect(jsonPath("$.chat.title").value("测试对话"))
                .andExpect(jsonPath("$.message").value("聊天会话创建成功"));

        System.out.println("✅ 创建聊天会话测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateChat_missingTitle() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话（使用默认标题）
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setTitle("新对话");
        testChat.setAiType(Chat.AiType.conversation);
        testChat.setCreatedAt(LocalDateTime.now());

        Mockito.when(chatService.createChat(Mockito.eq(1L), Mockito.isNull(), Mockito.eq(Chat.AiType.conversation)))
                .thenReturn(testChat);
        
        // Mock updateChatModel 方法（虽然这个测试中没有 aiModel，但为了安全起见）
        Mockito.when(chatService.updateChatModel(Mockito.eq(1L), Mockito.anyString()))
                .thenReturn(testChat);

        mockMvc.perform(post("/chat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "aiType":"conversation"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chat.title").value("新对话"));

        System.out.println("✅ 创建聊天会话（默认标题）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateChat_invalidAiType() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话（使用默认AI类型）
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setTitle("测试对话");
        testChat.setAiType(Chat.AiType.text_to_text);
        testChat.setCreatedAt(LocalDateTime.now());

        Mockito.when(chatService.createChat(Mockito.eq(1L), Mockito.eq("测试对话"), Mockito.eq(Chat.AiType.text_to_text)))
                .thenReturn(testChat);
        
        // Mock updateChatModel 方法
        Mockito.when(chatService.updateChatModel(Mockito.eq(1L), Mockito.anyString()))
                .thenReturn(testChat);

        mockMvc.perform(post("/chat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":"测试对话",
                      "aiType":"invalid_type"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chat.aiType").value("text_to_text"));

        System.out.println("✅ 创建聊天会话（无效AI类型）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟用户消息
        Message userMessage = new Message();
        userMessage.setId(1L);
        userMessage.setChatId(1L);
        userMessage.setContent("你好");
        userMessage.setRole(Message.MessageRole.user);

        // 模拟AI回复
        Message aiMessage = new Message();
        aiMessage.setId(2L);
        aiMessage.setChatId(1L);
        aiMessage.setContent("你好！我是AI助手，有什么可以帮助你的吗？");
        aiMessage.setRole(Message.MessageRole.assistant);

        // 模拟AI响应
        String aiResponse = "你好！我是AI助手，有什么可以帮助你的吗？";

        Mockito.when(chatService.sendMessage(Mockito.eq(1L), Mockito.eq(1L), Mockito.eq("你好"), Mockito.eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(Mockito.eq(1L), Mockito.eq(1L), Mockito.eq(aiResponse), Mockito.eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(Mockito.eq("你好"), Mockito.eq("text_to_text"), Mockito.any(), Mockito.any(Map.class)))
                .thenReturn(aiResponse);

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"你好",
                      "aiType":"text_to_text"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(2))
                .andExpect(jsonPath("$.userMessageId").value(1))
                .andExpect(jsonPath("$.response").value("你好！我是AI助手，有什么可以帮助你的吗？"));

        System.out.println("✅ 发送消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_emptyContent() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":""
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("消息内容和附件不能同时为空"));

        System.out.println("✅ 发送空消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withAttachments() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟用户消息
        Message userMessage = new Message();
        userMessage.setId(1L);
        userMessage.setChatId(1L);
        userMessage.setContent("请分析这张图片");
        userMessage.setRole(Message.MessageRole.user);

        // 模拟AI回复
        Message aiMessage = new Message();
        aiMessage.setId(2L);
        aiMessage.setChatId(1L);
        aiMessage.setContent("这是一张图片，我可以看到...");
        aiMessage.setRole(Message.MessageRole.assistant);

        // 模拟AI响应
        String aiResponse = "这是一张图片，我可以看到...";

        Mockito.when(chatService.sendMessage(Mockito.eq(1L), Mockito.eq(1L), Mockito.eq("请分析这张图片"), Mockito.eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(Mockito.eq(1L), Mockito.eq(1L), Mockito.eq(aiResponse), Mockito.eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(Mockito.eq("请分析这张图片"), Mockito.eq("text_to_text"), Mockito.any(), Mockito.any(Map.class)))
                .thenReturn(aiResponse);

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"",
                      "attachments":[
                        {
                          "fileName":"test.jpg",
                          "fileUrl":"http://example.com/test.jpg"
                        }
                      ]
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(2));

        System.out.println("✅ 发送带附件的消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetChatMessages_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟消息列表
        List<Message> messages = Arrays.asList(
            createMessage(1L, 1L, "你好", Message.MessageRole.user),
            createMessage(2L, 1L, "你好！我是AI助手", Message.MessageRole.assistant)
        );

        Mockito.when(chatService.getChatMessages(Mockito.eq(1L), Mockito.eq(1L)))
                .thenReturn(messages);

        mockMvc.perform(get("/chat/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.messages[0].content").value("你好"))
                .andExpect(jsonPath("$.messages[1].content").value("你好！我是AI助手"));

        System.out.println("✅ 获取聊天历史测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetChatMessages_empty() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟空消息列表
        Mockito.when(chatService.getChatMessages(Mockito.eq(1L), Mockito.eq(1L)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/chat/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.count").value(0));

        System.out.println("✅ 获取空聊天历史测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteChat_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟删除操作
        Mockito.doNothing().when(chatService).deleteChat(Mockito.eq(1L), Mockito.eq(1L));

        mockMvc.perform(delete("/chat/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("聊天会话删除成功"));

        System.out.println("✅ 删除聊天会话测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testToggleFavorite_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setIsFavorite(true);

        Mockito.when(chatService.toggleFavorite(Mockito.eq(1L), Mockito.eq(1L)))
                .thenReturn(testChat);

        mockMvc.perform(patch("/chat/1/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isFavorite").value(true))
                .andExpect(jsonPath("$.message").value("已添加到收藏"));

        System.out.println("✅ 切换收藏状态测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testToggleProtection_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setIsProtected(true);

        Mockito.when(chatService.toggleProtection(Mockito.eq(1L), Mockito.eq(1L)))
                .thenReturn(testChat);

        mockMvc.perform(patch("/chat/1/protect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isProtected").value(true))
                .andExpect(jsonPath("$.message").value("已设为保护对话"));

        System.out.println("✅ 切换保护状态测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateTitle_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setTitle("新标题");

        Mockito.when(chatService.updateChatTitle(Mockito.eq(1L), Mockito.eq(1L), Mockito.eq("新标题")))
                .thenReturn(testChat);

        mockMvc.perform(patch("/chat/1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":"新标题"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.title").value("新标题"))
                .andExpect(jsonPath("$.message").value("标题更新成功"));

        System.out.println("✅ 更新标题测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateTitle_emptyTitle() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(patch("/chat/1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":""
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("标题不能为空"));

        System.out.println("✅ 更新空标题测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateModel_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天会话
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setAiModel("openai/gpt-4");

        Mockito.when(chatService.updateChatModel(Mockito.eq(1L), Mockito.eq(1L), Mockito.eq("openai/gpt-4")))
                .thenReturn(testChat);

        mockMvc.perform(patch("/chat/1/model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "aiModel":"openai/gpt-4"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.aiModel").value("openai/gpt-4"))
                .andExpect(jsonPath("$.message").value("模型更新成功"));

        System.out.println("✅ 更新模型测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateModel_emptyModel() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(patch("/chat/1/model")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "aiModel":""
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("模型参数不能为空"));

        System.out.println("✅ 更新空模型测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserChatList_success() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟聊天列表
        List<Chat> chats = Arrays.asList(
            createChat(1L, "对话1"),
            createChat(2L, "对话2")
        );
        Page<Chat> chatPage = new PageImpl<>(chats, PageRequest.of(0, 20), 2);

        Mockito.when(chatService.getUserChats(eq(1L), any(PageRequest.class)))
                .thenReturn(chatPage);

        mockMvc.perform(get("/chat/list")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chats").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").value(20));

        System.out.println("✅ 获取用户聊天列表测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserChatList_empty() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟空聊天列表
        Page<Chat> chatPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);

        Mockito.when(chatService.getUserChats(eq(1L), any(PageRequest.class)))
                .thenReturn(chatPage);

        mockMvc.perform(get("/chat/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chats").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        System.out.println("✅ 获取空聊天列表测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withImageUrl() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟用户消息
        Message userMessage = new Message();
        userMessage.setId(1L);
        userMessage.setChatId(1L);
        userMessage.setContent("分析这张图片");
        userMessage.setRole(Message.MessageRole.user);

        // 模拟AI回复
        Message aiMessage = new Message();
        aiMessage.setId(2L);
        aiMessage.setChatId(1L);
        aiMessage.setContent("这是一张图片分析结果...");
        aiMessage.setRole(Message.MessageRole.assistant);

        // 模拟AI响应
        String aiResponse = "这是一张图片分析结果...";

        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), eq("分析这张图片"), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), eq(aiResponse), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(eq("分析这张图片"), eq("text_to_text"), any(), any(Map.class)))
                .thenReturn(aiResponse);

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"分析这张图片",
                      "imageUrl":"http://example.com/image.jpg"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(2));

        System.out.println("✅ 发送带图片URL的消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withAdvancedOptions() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟用户消息
        Message userMessage = new Message();
        userMessage.setId(1L);
        userMessage.setChatId(1L);
        userMessage.setContent("生成一张图片");
        userMessage.setRole(Message.MessageRole.user);

        // 模拟AI回复
        Message aiMessage = new Message();
        aiMessage.setId(2L);
        aiMessage.setChatId(1L);
        aiMessage.setContent("图片生成结果...");
        aiMessage.setRole(Message.MessageRole.assistant);

        // 模拟AI响应
        String aiResponse = "图片生成结果...";

        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), eq("生成一张图片"), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), eq(aiResponse), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(eq("生成一张图片"), eq("text_to_image"), any(), any(Map.class)))
                .thenReturn(aiResponse);

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"生成一张图片",
                      "aiType":"text_to_image",
                      "size":"1024x1024",
                      "quality":"high",
                      "maxTokens":1000,
                      "temperature":0.7
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(2));

        System.out.println("✅ 发送带高级选项的消息测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateChat_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.createChat(any(), any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("创建聊天失败"));

        mockMvc.perform(post("/chat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":"测试对话"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("创建聊天失败"));

        System.out.println("✅ 创建聊天会话（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateChat_systemException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟系统异常
        Mockito.when(chatService.createChat(any(), any(), any()))
                .thenThrow(new RuntimeException("系统错误"));

        mockMvc.perform(post("/chat/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":"测试对话"
                    }
                    """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("创建聊天会话失败"));

        System.out.println("✅ 创建聊天会话（系统异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.sendMessage(any(), any(), any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("发送消息失败"));

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"测试消息"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("发送消息失败"));

        System.out.println("✅ 发送消息（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_systemException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟系统异常
        Mockito.when(chatService.sendMessage(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("系统错误"));

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"测试消息"
                    }
                    """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("发送消息失败"));

        System.out.println("✅ 发送消息（系统异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withAudioFile() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟消息
        Message userMessage = createMessage(1L, 1L, "请分析这个音频", Message.MessageRole.user);
        Message aiMessage = createMessage(2L, 1L, "音频分析结果", Message.MessageRole.assistant);
        
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(any(), any(), any(), any()))
                .thenReturn("音频分析结果");

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"请分析这个音频",
                      "audioFile":"audio.mp3",
                      "aiType":"audio_to_text"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("音频分析结果"));

        System.out.println("✅ 发送消息（音频文件）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withInputs() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟消息
        Message userMessage = createMessage(1L, 1L, "生成图片", Message.MessageRole.user);
        Message aiMessage = createMessage(2L, 1L, "图片生成结果", Message.MessageRole.assistant);
        
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(any(), any(), any(), any()))
                .thenReturn("图片生成结果");

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"生成图片",
                      "inputs":"一只可爱的小猫",
                      "aiType":"text_to_image"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("图片生成结果"));

        System.out.println("✅ 发送消息（输入参数）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetChatMessages_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.getChatMessages(any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("获取消息失败"));

        mockMvc.perform(get("/chat/1/messages"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("获取消息失败"));

        System.out.println("✅ 获取聊天消息（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteChat_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.doThrow(new com.aiplatform.exception.BusinessException("删除失败"))
                .when(chatService).deleteChat(any(), any());

        mockMvc.perform(delete("/chat/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("删除失败"));

        System.out.println("✅ 删除聊天会话（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testToggleFavorite_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.toggleFavorite(any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("操作失败"));

        mockMvc.perform(patch("/chat/1/favorite"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("操作失败"));

        System.out.println("✅ 切换收藏状态（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testToggleProtection_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.toggleProtection(any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("操作失败"));

        mockMvc.perform(patch("/chat/1/protect"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("操作失败"));

        System.out.println("✅ 切换保护状态（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateTitle_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.updateChatTitle(any(), any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("更新失败"));

        mockMvc.perform(patch("/chat/1/title")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title":"新标题"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("更新失败"));

        System.out.println("✅ 更新对话标题（业务异常）测试通过");
    }

//    @Test
//    @WithMockUser(username = "test@example.com")
//    public void testUpdateModel_businessException() throws Exception {
//        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        Mockito.when(userRepository.findByEmail("test@example.com"))
//                .thenReturn(Optional.of(testUser));
//
//        // 模拟业务异常
//        Mockito.when(chatService.updateChatModel(any(), any()))
//                .thenThrow(new com.aiplatform.exception.BusinessException("更新失败"));
//
//        mockMvc.perform(patch("/chat/1/model")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                    {
//                      "aiModel":"gpt-4"
//                    }
//                    """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.error").value("更新失败"));
//
//        System.out.println("✅ 更新对话模型（业务异常）测试通过");
//    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserChatList_businessException() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟业务异常
        Mockito.when(chatService.getUserChats(any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("获取失败"));

        mockMvc.perform(get("/chat/list"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("获取失败"));

        System.out.println("✅ 获取用户聊天列表（业务异常）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withMaxTokens() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟消息
        Message userMessage = createMessage(1L, 1L, "生成长文本", Message.MessageRole.user);
        Message aiMessage = createMessage(2L, 1L, "长文本生成结果", Message.MessageRole.assistant);
        
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(any(), any(), any(), any()))
                .thenReturn("长文本生成结果");

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"生成长文本",
                      "maxTokens":1000,
                      "temperature":0.7
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("长文本生成结果"));

        System.out.println("✅ 发送消息（最大令牌数）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withQuality() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟消息
        Message userMessage = createMessage(1L, 1L, "生成高质量图片", Message.MessageRole.user);
        Message aiMessage = createMessage(2L, 1L, "高质量图片生成结果", Message.MessageRole.assistant);
        
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(any(), any(), any(), any()))
                .thenReturn("高质量图片生成结果");

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"生成高质量图片",
                      "quality":"hd",
                      "size":"1024x1024"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("高质量图片生成结果"));

        System.out.println("✅ 发送消息（图片质量）测试通过");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testSendMessage_withMessageId() throws Exception {
        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟消息
        Message userMessage = createMessage(1L, 1L, "分析图片", Message.MessageRole.user);
        Message aiMessage = createMessage(2L, 1L, "图片分析结果", Message.MessageRole.assistant);
        
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.user)))
                .thenReturn(userMessage);
        Mockito.when(chatService.sendMessage(eq(1L), eq(1L), any(), eq(Message.MessageRole.assistant)))
                .thenReturn(aiMessage);
        Mockito.when(chatService.generateAIResponse(any(), any(), any(), any()))
                .thenReturn("图片分析结果");

        mockMvc.perform(post("/chat/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "content":"分析图片",
                      "attachments":[{"id":1,"fileName":"test.jpg"}]
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("图片分析结果"));

        System.out.println("✅ 发送消息（消息ID）测试通过");
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

    // 辅助方法：创建测试聊天会话
    private Chat createChat(Long id, String title) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUserId(1L);
        chat.setTitle(title);
        chat.setAiType(Chat.AiType.conversation);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setMessageCount(0);
        return chat;
    }
} 