package com.aiplatform.controller;

import com.aiplatform.exception.GlobalExceptionHandler;
import com.aiplatform.service.AiService;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        AIController.class, 
        GlobalExceptionHandler.class,
        AIControllerTest.TestConfig.class,
        AIControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class AIControllerTest {

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
    private AiService aiService;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        // 最小测试：验证所有依赖都被正确注入
        assertNotNull(mockMvc, "MockMvc should be injected");
        assertNotNull(objectMapper, "ObjectMapper should be injected");
        assertNotNull(aiService, "AiService should be mocked");
        assertNotNull(chatService, "ChatService should be mocked");
        
        System.out.println("✅ 基本配置测试通过 - 所有依赖都已正确注入");
    }

    // ========== 获取AI服务状态相关测试 ==========

    @Test
    public void testGetAIStatus_success() throws Exception {
        when(aiService.isAIServiceAvailable()).thenReturn(true);
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");

        mockMvc.perform(get("/ai/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.model").value("gpt-3.5-turbo"))
                .andExpect(jsonPath("$.service").value("OpenRouter API"))
                .andExpect(jsonPath("$.message").value("AI服务正常"));
        
        System.out.println("✅ 获取AI服务状态成功测试通过");
    }

    @Test
    public void testGetAIStatus_unavailable() throws Exception {
        when(aiService.isAIServiceAvailable()).thenReturn(false);
        when(aiService.getCurrentModel()).thenReturn("local");

        mockMvc.perform(get("/ai/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.model").value("local"))
                .andExpect(jsonPath("$.service").value("本地回复模式"))
                .andExpect(jsonPath("$.message").value("AI API未配置，使用本地回复"));
        
        System.out.println("✅ 获取AI服务状态不可用测试通过");
    }

    @Test
    public void testGetAIStatus_exception() throws Exception {
        when(aiService.isAIServiceAvailable()).thenThrow(new RuntimeException("AI服务异常"));

        mockMvc.perform(get("/ai/status"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("检查AI服务状态失败"));
        
        System.out.println("✅ 获取AI服务状态异常测试通过");
    }

    // ========== 测试AI回复相关测试 ==========

    @Test
    public void testTestAI_success() throws Exception {
        when(aiService.generateResponse(anyString(), any())).thenReturn("你好！我是AI助手。");
        when(aiService.isAIServiceAvailable()).thenReturn(true);
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");

        Map<String, Object> request = new HashMap<>();
        request.put("message", "你好");

        mockMvc.perform(post("/ai/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("你好！我是AI助手。"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.model").value("gpt-3.5-turbo"))
                .andExpect(jsonPath("$.mode").value("API"));
        
        System.out.println("✅ 测试AI回复成功测试通过");
    }

    @Test
    public void testTestAI_localMode() throws Exception {
        when(aiService.generateResponse(anyString(), any())).thenReturn("本地回复：你好！");
        when(aiService.isAIServiceAvailable()).thenReturn(false);
        when(aiService.getCurrentModel()).thenReturn("local");

        Map<String, Object> request = new HashMap<>();
        request.put("message", "你好");

        mockMvc.perform(post("/ai/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("本地回复：你好！"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.model").value("local"))
                .andExpect(jsonPath("$.mode").value("本地"));
        
        System.out.println("✅ 测试AI回复本地模式测试通过");
    }

    @Test
    public void testTestAI_exception() throws Exception {
        when(aiService.generateResponse(anyString(), any())).thenThrow(new RuntimeException("AI服务异常"));

        Map<String, Object> request = new HashMap<>();
        request.put("message", "你好");

        mockMvc.perform(post("/ai/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("AI测试失败"));
        
        System.out.println("✅ 测试AI回复异常测试通过");
    }

    @Test
    public void testTestAI_defaultMessage() throws Exception {
        when(aiService.generateResponse("你好", null)).thenReturn("默认回复");
        when(aiService.isAIServiceAvailable()).thenReturn(true);
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");

        mockMvc.perform(post("/ai/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("默认回复"));
        
        System.out.println("✅ 测试AI回复默认消息测试通过");
    }

    // ========== 获取AI配置信息相关测试 ==========

    @Test
    public void testGetAIConfig_success() throws Exception {
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");
        when(aiService.isAIServiceAvailable()).thenReturn(true);

        mockMvc.perform(get("/ai/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.model").value("gpt-3.5-turbo"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.provider").value("OpenRouter"))
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features.length()").value(3));
        
        System.out.println("✅ 获取AI配置信息成功测试通过");
    }

    @Test
    public void testGetAIConfig_unavailable() throws Exception {
        when(aiService.getCurrentModel()).thenReturn("local");
        when(aiService.isAIServiceAvailable()).thenReturn(false);

        mockMvc.perform(get("/ai/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.model").value("local"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.provider").value("OpenRouter"))
                .andExpect(jsonPath("$.notice").exists());
        
        System.out.println("✅ 获取AI配置信息不可用测试通过");
    }

    @Test
    public void testGetAIConfig_exception() throws Exception {
        when(aiService.getCurrentModel()).thenThrow(new RuntimeException("配置获取异常"));

        mockMvc.perform(get("/ai/config"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取AI配置失败"));
        
        System.out.println("✅ 获取AI配置信息异常测试通过");
    }

    // ========== 检查聊天服务状态相关测试 ==========

    @Test
    public void testGetChatAIStatus_success() throws Exception {
        when(chatService.isAIServiceAvailable()).thenReturn(true);
        when(chatService.getCurrentAIModel()).thenReturn("gpt-3.5-turbo");

        mockMvc.perform(get("/ai/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.aiEnabled").value(true))
                .andExpect(jsonPath("$.model").value("gpt-3.5-turbo"))
                .andExpect(jsonPath("$.chatAIStatus").value("启用"));
        
        System.out.println("✅ 检查聊天服务状态成功测试通过");
    }

    @Test
    public void testGetChatAIStatus_disabled() throws Exception {
        when(chatService.isAIServiceAvailable()).thenReturn(false);
        when(chatService.getCurrentAIModel()).thenReturn("local");

        mockMvc.perform(get("/ai/chat/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.aiEnabled").value(false))
                .andExpect(jsonPath("$.model").value("local"))
                .andExpect(jsonPath("$.chatAIStatus").value("本地模式"));
        
        System.out.println("✅ 检查聊天服务状态禁用测试通过");
    }

    @Test
    public void testGetChatAIStatus_exception() throws Exception {
        when(chatService.isAIServiceAvailable()).thenThrow(new RuntimeException("聊天服务异常"));

        mockMvc.perform(get("/ai/chat/status"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("检查聊天AI状态失败"));
        
        System.out.println("✅ 检查聊天服务状态异常测试通过");
    }

    // ========== 获取可用AI模型列表相关测试 ==========

    @Test
    public void testGetAvailableModels_success() throws Exception {
        // 模拟AI模型枚举
        var mockModels = Arrays.asList(
            com.aiplatform.entity.AIModel.GPT_4_1_NANO,
            com.aiplatform.entity.AIModel.GEMINI_2_5_FLASH
        );
        
        when(aiService.getAvailableModels()).thenReturn(mockModels);

        mockMvc.perform(get("/ai/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.models").isArray())
                .andExpect(jsonPath("$.models.length()").value(2))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.models[0].modelId").value("openai/gpt-4.1-nano"))
                .andExpect(jsonPath("$.models[0].displayName").value("GPT-4.1 Nano"))
                .andExpect(jsonPath("$.models[1].modelId").value("google/gemini-2.5-flash"))
                .andExpect(jsonPath("$.models[1].displayName").value("Gemini 2.5 Flash"));
        
        System.out.println("✅ 获取可用AI模型列表成功测试通过");
    }

    @Test
    public void testGetAvailableModels_empty() throws Exception {
        when(aiService.getAvailableModels()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/ai/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.models").isArray())
                .andExpect(jsonPath("$.models.length()").value(0))
                .andExpect(jsonPath("$.count").value(0));
        
        System.out.println("✅ 获取可用AI模型列表空结果测试通过");
    }

    @Test
    public void testGetAvailableModels_exception() throws Exception {
        when(aiService.getAvailableModels()).thenThrow(new RuntimeException("获取模型列表异常"));

        mockMvc.perform(get("/ai/models"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取模型列表失败"));
        
        System.out.println("✅ 获取可用AI模型列表异常测试通过");
    }

    // ========== 获取支持图片的AI模型相关测试 ==========

    @Test
    public void testGetImageSupportModels_success() throws Exception {
        var mockModels = Arrays.asList(
            com.aiplatform.entity.AIModel.GPT_4_1_NANO,
            com.aiplatform.entity.AIModel.GEMINI_2_5_FLASH
        );
        
        when(aiService.getImageSupportModels()).thenReturn(mockModels);

        mockMvc.perform(get("/ai/models/image-support"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.models").isArray())
                .andExpect(jsonPath("$.models.length()").value(2))
                .andExpect(jsonPath("$.count").value(2));
        
        System.out.println("✅ 获取支持图片的AI模型成功测试通过");
    }

    @Test
    public void testGetImageSupportModels_exception() throws Exception {
        when(aiService.getImageSupportModels()).thenThrow(new RuntimeException("获取图片支持模型异常"));

        mockMvc.perform(get("/ai/models/image-support"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取图片支持模型列表失败"));
        
        System.out.println("✅ 获取支持图片的AI模型异常测试通过");
    }

    // ========== 获取模型详细信息相关测试 ==========

    @Test
    public void testGetModelInfo_success() throws Exception {
        var mockModel = com.aiplatform.entity.AIModel.GPT_4_1_NANO;
        
        when(aiService.getModelInfo("test-model")).thenReturn(mockModel);

        mockMvc.perform(get("/ai/models/test-model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.model").exists());
        
        System.out.println("✅ 获取模型详细信息成功测试通过");
    }

    @Test
    public void testGetModelInfo_notFound() throws Exception {
        when(aiService.getModelInfo("invalid-model")).thenReturn(null);

        mockMvc.perform(get("/ai/models/invalid-model"))
                .andExpect(status().isNotFound());
        
        System.out.println("✅ 获取模型详细信息不存在测试通过");
    }

    @Test
    public void testGetModelInfo_exception() throws Exception {
        when(aiService.getModelInfo("test-model")).thenThrow(new RuntimeException("获取模型信息异常"));

        mockMvc.perform(get("/ai/models/test-model"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取模型信息失败"))
                .andExpect(jsonPath("$.error").value("获取模型信息异常"));
        
        System.out.println("✅ 获取模型详细信息异常测试通过");
    }

    // ========== 验证模型可用性相关测试 ==========

    @Test
    public void testCheckModelAvailability_available() throws Exception {
        when(aiService.isModelAvailable("test-model")).thenReturn(true);

        mockMvc.perform(get("/ai/models/test-model/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.modelId").value("test-model"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("模型可用"));
        
        System.out.println("✅ 验证模型可用性可用测试通过");
    }

    @Test
    public void testCheckModelAvailability_unavailable() throws Exception {
        when(aiService.isModelAvailable("invalid-model")).thenReturn(false);

        mockMvc.perform(get("/ai/models/invalid-model/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.modelId").value("invalid-model"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("模型不可用"));
        
        System.out.println("✅ 验证模型可用性不可用测试通过");
    }

    @Test
    public void testCheckModelAvailability_exception() throws Exception {
        when(aiService.isModelAvailable("test-model")).thenThrow(new RuntimeException("检查模型可用性异常"));

        mockMvc.perform(get("/ai/models/test-model/available"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("检查模型可用性失败"))
                .andExpect(jsonPath("$.error").value("检查模型可用性异常"));
        
        System.out.println("✅ 验证模型可用性异常测试通过");
    }

    // ========== 边界测试 ==========

    @Test
    public void testTestAI_emptyRequest() throws Exception {
        when(aiService.generateResponse("你好", null)).thenReturn("默认回复");
        when(aiService.isAIServiceAvailable()).thenReturn(true);
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");

        mockMvc.perform(post("/ai/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        System.out.println("✅ 测试AI回复空请求测试通过");
    }

    @Test
    public void testTestAI_nullMessage() throws Exception {
        when(aiService.generateResponse("你好", null)).thenReturn("默认回复");
        when(aiService.isAIServiceAvailable()).thenReturn(true);
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");

        Map<String, Object> request = new HashMap<>();
        request.put("message", null);

        mockMvc.perform(post("/ai/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        System.out.println("✅ 测试AI回复空消息测试通过");
    }


} 