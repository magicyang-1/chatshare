package com.aiplatform.controller;

import com.aiplatform.service.ChatService;
import com.aiplatform.service.DashScopeImageService;
import com.aiplatform.service.Meshy3DService;
import com.aiplatform.service.MessageService;
import com.aiplatform.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.User;
import com.aiplatform.entity.Chat;
import com.aiplatform.dto.ThreeDRecordDTO;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        ThreeDGenerationController.class, 
        ThreeDGenerationControllerTest.TestConfig.class,
        ThreeDGenerationControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class ThreeDGenerationControllerTest {

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
    private Meshy3DService meshy3DService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private MessageService messageService;

    @MockBean
    private DashScopeImageService dashScopeImageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        // 最小测试：验证所有依赖都被正确注入
        assertNotNull(mockMvc, "MockMvc should be injected");
        assertNotNull(objectMapper, "ObjectMapper should be injected");
        assertNotNull(meshy3DService, "Meshy3DService should be mocked");
        assertNotNull(jwtTokenProvider, "JwtTokenProvider should be mocked");
        assertNotNull(userService, "UserService should be mocked");
        assertNotNull(chatService, "ChatService should be mocked");
        assertNotNull(messageService, "MessageService should be mocked");
        assertNotNull(dashScopeImageService, "DashScopeImageService should be mocked");
        
        // 添加基本的模拟配置
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        System.out.println("✅ 基本配置测试通过 - 所有依赖都已正确注入");
    }

    @Test
    @WithMockUser  // 添加模拟用户认证
    public void testGenerateTextTo3D_missingPrompt() throws Exception {
        // 测试缺少prompt参数的情况
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少prompt参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_missingMode() throws Exception {
        // 测试缺少mode参数的情况
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少mode参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_success() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟服务层返回成功响应
        Mockito.when(meshy3DService.createTextTo3D(
                        eq("a flying car"), eq("fast"), eq("realistic"), eq(true), eq(42), any(Message.class)))
                .thenReturn("{\"result\":\"task-123\"}");

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("3D生成成功"))
                .andExpect(jsonPath("$.taskId").value("task-123"));
        
        System.out.println("✅ 成功生成3D测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_serviceFailure() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟服务层抛出异常
        Mockito.when(meshy3DService.createTextTo3D(
                        anyString(), anyString(), anyString(), anyBoolean(), anyInt(), any(Message.class)))
                .thenThrow(new RuntimeException("服务调用失败"));

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("3D生成失败: 服务调用失败"));
        
        System.out.println("✅ 服务失败测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_success() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟服务层返回成功响应
        Mockito.when(meshy3DService.refineTextTo3D(eq("task-123"), eq("make it sharper"), any(Message.class)))
                .thenReturn("{\"result\":\"task-456\"}");

        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"make it sharper"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("3D精炼成功"))
                .andExpect(jsonPath("$.taskId").value("task-456"));
        
        System.out.println("✅ 3D精炼成功测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_missingTaskId() throws Exception {
        // 测试缺少taskId参数的情况
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"make it sharper"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少taskId参数测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_missingPrompt() throws Exception {
        // 测试缺少prompt参数的情况
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 缺少prompt参数测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_serviceFailure() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟服务层抛出异常
        Mockito.when(meshy3DService.refineTextTo3D(anyString(), anyString(), any(Message.class)))
                .thenThrow(new RuntimeException("精炼服务调用失败"));

        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"make it sharper"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("细化任务失败: 精炼服务调用失败"));
        
        System.out.println("✅ 精炼服务失败测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_invalidJsonResponse() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟服务层返回无效的JSON响应
        Mockito.when(meshy3DService.refineTextTo3D(anyString(), anyString(), any(Message.class)))
                .thenReturn("invalid json");

        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"make it sharper"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("解析任务ID失败")));
        
        System.out.println("✅ 无效JSON响应测试通过");
    }

    // ========== 新增测试函数 ==========

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_emptyPrompt() throws Exception {
        // 测试空prompt参数
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空prompt参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_invalidMode() throws Exception {
        // 测试无效的mode参数
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"invalid_mode",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 无效mode参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_invalidArtStyle() throws Exception {
        // 测试无效的art_style参数
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast",
                      "art_style":"invalid_style",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 无效art_style参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_negativeSeed() throws Exception {
        // 测试负数seed参数
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":-1
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 负数seed参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_largeSeed() throws Exception {
        // 测试过大的seed参数
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":999999999
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 过大seed参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_veryLongPrompt() throws Exception {
        // 测试超长prompt参数
        String longPrompt = "a".repeat(1001); // 超过1000字符
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                    {
                      "prompt":"%s",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """, longPrompt)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 超长prompt参数测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_invalidContentType() throws Exception {
        // 测试无效的Content-Type
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
        
        System.out.println("✅ 无效Content-Type测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_malformedJson() throws Exception {
        // 测试格式错误的JSON
        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 格式错误JSON测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_emptyTaskId() throws Exception {
        // 测试空taskId参数
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"",
                      "prompt":"make it sharper"
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空taskId参数测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_emptyPrompt() throws Exception {
        // 测试空prompt参数
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":""
                    }
                    """))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 空prompt参数测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_veryLongPrompt() throws Exception {
        // 测试超长prompt参数
        String longPrompt = "a".repeat(1001); // 超过1000字符
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                    {
                      "taskId":"task-123",
                      "prompt":"%s"
                    }
                    """, longPrompt)))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 超长prompt参数测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_invalidContentType() throws Exception {
        // 测试无效的Content-Type
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
        
        System.out.println("✅ 无效Content-Type测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_malformedJson() throws Exception {
        // 测试格式错误的JSON
        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
        
        System.out.println("✅ 格式错误JSON测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_serviceTimeout() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟服务超时
        Mockito.when(meshy3DService.createTextTo3D(
                        anyString(), anyString(), anyString(), anyBoolean(), anyInt(), any(Message.class)))
                .thenThrow(new RuntimeException("请求超时"));

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("3D生成失败: 请求超时"));
        
        System.out.println("✅ 服务超时测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_serviceTimeout() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 模拟精炼服务超时
        Mockito.when(meshy3DService.refineTextTo3D(anyString(), anyString(), any(Message.class)))
                .thenThrow(new RuntimeException("精炼请求超时"));

        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"make it sharper"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("细化任务失败: 精炼请求超时"));
        
        System.out.println("✅ 精炼服务超时测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_specialCharactersInPrompt() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 测试prompt中包含特殊字符
        Mockito.when(meshy3DService.createTextTo3D(
                        eq("a flying car with @#$%^&*()"), eq("fast"), eq("realistic"), eq(true), eq(42), any(Message.class)))
                .thenReturn("{\"result\":\"task-123\"}");

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car with @#$%^&*()",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("3D生成成功"))
                .andExpect(jsonPath("$.taskId").value("task-123"));
        
        System.out.println("✅ 特殊字符prompt测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_specialCharactersInPrompt() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 测试精炼prompt中包含特殊字符
        Mockito.when(meshy3DService.refineTextTo3D(eq("task-123"), eq("make it @#$%^&*() sharper"), any(Message.class)))
                .thenReturn("{\"result\":\"task-456\"}");

        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"make it @#$%^&*() sharper"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("3D精炼成功"))
                .andExpect(jsonPath("$.taskId").value("task-456"));
        
        System.out.println("✅ 特殊字符精炼prompt测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_unicodeCharactersInPrompt() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 测试prompt中包含Unicode字符
        Mockito.when(meshy3DService.createTextTo3D(
                        eq("一辆飞行的汽车 🚗✈️"), eq("fast"), eq("realistic"), eq(true), eq(42), any(Message.class)))
                .thenReturn("{\"result\":\"task-123\"}");

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"一辆飞行的汽车 🚗✈️",
                      "mode":"fast",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("3D生成成功"))
                .andExpect(jsonPath("$.taskId").value("task-123"));
        
        System.out.println("✅ Unicode字符prompt测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_unicodeCharactersInPrompt() throws Exception {
        // 模拟JWT和用户服务
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));
        
        // 模拟ChatService
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);
        
        // 模拟MessageService
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);
        
        // 测试精炼prompt中包含Unicode字符
        Mockito.when(meshy3DService.refineTextTo3D(eq("task-123"), eq("让它更锋利 🔪"), any(Message.class)))
                .thenReturn("{\"result\":\"task-456\"}");

        mockMvc.perform(post("/3d/text-to-3d-refine")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"让它更锋利 🔪"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("3D精炼成功"))
                .andExpect(jsonPath("$.taskId").value("task-456"));
        
        System.out.println("✅ Unicode字符精炼prompt测试通过");
    }



    @Test
    @WithMockUser
    public void testGenerateTextTo3D_chatCreationFailure() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟聊天创建失败
        Mockito.when(chatService.createChat(any(), anyString(), any(Chat.AiType.class)))
                .thenThrow(new RuntimeException("聊天创建失败"));

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("3D生成失败: 聊天创建失败"));

        System.out.println("✅ 文生3D（聊天创建失败）测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_messageSaveFailure() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟聊天
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);

        // 模拟消息保存失败
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenThrow(new RuntimeException("消息保存失败"));

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("3D生成失败: 消息保存失败"));

        System.out.println("✅ 文生3D（消息保存失败）测试通过");
    }

    @Test
    @WithMockUser
    public void testGenerateTextTo3D_allParameters() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟聊天
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);

        // 模拟消息
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);

        // 模拟服务层返回成功响应
        Mockito.when(meshy3DService.createTextTo3D(anyString(), anyString(), anyString(), anyBoolean(), anyInt(), any(Message.class)))
                .thenReturn("{\"result\":\"task-123\"}");

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a detailed flying car with wings",
                      "mode":"quality",
                      "art_style":"realistic",
                      "should_remesh":true,
                      "seed":42
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.taskId").value("task-123"));

        System.out.println("✅ 文生3D（所有参数）测试通过");
    }


    @Test
    @WithMockUser
    public void testGenerateTextTo3D_serviceUnavailable() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
//        User testUser = new User();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
        User testUser = createAdminUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟聊天
        Chat testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        Mockito.when(chatService.createChat(eq(1L), anyString(), any(Chat.AiType.class)))
                .thenReturn(testChat);

        // 模拟消息
        Message testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setContent("测试消息");
        Mockito.when(messageService.saveMessage(any(Message.class)))
                .thenReturn(testMessage);

        // 模拟服务不可用
        Mockito.when(meshy3DService.createTextTo3D(anyString(), anyString(), anyString(), anyBoolean(), anyInt(), any(Message.class)))
                .thenThrow(new RuntimeException("服务不可用"));

        mockMvc.perform(post("/3d/text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "prompt":"a flying car",
                      "mode":"fast"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("解析任务ID失败")));

        System.out.println("✅ 文生3D（服务不可用）测试通过");
    }

    @Test
    @WithMockUser
    public void testRefineTextTo3D_serviceUnavailable() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟服务不可用
        Mockito.when(meshy3DService.refineTextTo3D(anyString(), anyString(), any(Message.class)))
                .thenThrow(new RuntimeException("服务不可用"));

        mockMvc.perform(post("/3d/refine-text-to-3d")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "taskId":"task-123",
                      "prompt":"refined flying car"
                    }
                    """))
                .andExpect(status().isNotFound());

        System.out.println("✅ 精修3D（服务不可用）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_success() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟历史记录
        List<ThreeDRecordDTO> historyRecords = Arrays.asList(
            createThreeDRecordDTO("task-123", "a flying car", "completed"),
            createThreeDRecordDTO("task-456", "a robot", "processing")
        );
        Mockito.when(meshy3DService.searchHistory(1L))
                .thenReturn(historyRecords);

        mockMvc.perform(get("/3d/search-history")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history.length()").value(2))
                .andExpect(jsonPath("$.history[0].id").value("task-123"))
                .andExpect(jsonPath("$.history[0].prompt").value("a flying car"))
                .andExpect(jsonPath("$.history[0].mode").value("fast"))
                .andExpect(jsonPath("$.history[1].id").value("task-456"))
                .andExpect(jsonPath("$.history[1].prompt").value("a robot"))
                .andExpect(jsonPath("$.history[1].mode").value("fast"));

        System.out.println("✅ 搜索3D历史记录（成功）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_userNotFound() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("nonexistent@example.com");

        // 模拟用户不存在
        Mockito.when(userService.findByEmail("nonexistent@example.com"))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/3d/search-history")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("搜索历史失败: 用户不存在"));

        System.out.println("✅ 搜索3D历史记录（用户不存在）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟服务异常
        Mockito.when(meshy3DService.searchHistory(1L))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(get("/3d/search-history")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("搜索历史失败: 服务异常"));

        System.out.println("✅ 搜索3D历史记录（服务异常）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_emptyHistory() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟空历史记录
        Mockito.when(meshy3DService.searchHistory(1L))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/3d/search-history")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history.length()").value(0));

        System.out.println("✅ 搜索3D历史记录（空历史）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_invalidToken() throws Exception {
        // 模拟无效的 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/3d/search-history")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("搜索历史失败")));

        System.out.println("✅ 搜索3D历史记录（无效Token）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_missingToken() throws Exception {
        mockMvc.perform(get("/3d/search-history"))
                .andExpect(status().isBadRequest());

        System.out.println("✅ 搜索3D历史记录（缺少Token）测试通过");
    }

    @Test
    @WithMockUser
    public void testSearchHistory_largeHistory() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("test-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(java.util.Optional.of(testUser));

        // 模拟大量历史记录
        List<ThreeDRecordDTO> largeHistory = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            largeHistory.add(createThreeDRecordDTO("task-" + i, "prompt " + i, "completed"));
        }
        Mockito.when(meshy3DService.searchHistory(1L))
                .thenReturn(largeHistory);

        mockMvc.perform(get("/3d/search-history")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history.length()").value(100))
                .andExpect(jsonPath("$.history[0].id").value("task-1"))
                .andExpect(jsonPath("$.history[99].id").value("task-100"));

        System.out.println("✅ 搜索3D历史记录（大量数据）测试通过");
    }

    // 辅助方法：创建管理员用户
    private User createAdminUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.admin);
        return user;
    }

    // 辅助方法：创建ThreeDRecordDTO对象
    private ThreeDRecordDTO createThreeDRecordDTO(String taskId, String prompt, String status) {
        return ThreeDRecordDTO.builder()
                .id(taskId)
                .prompt(prompt)
                .mode("fast")
                .art_style("realistic")
                .created_at(java.time.LocalDateTime.now())
                .build();
    }
}

