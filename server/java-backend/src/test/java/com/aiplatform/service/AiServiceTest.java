package com.aiplatform.service;

import com.aiplatform.config.AiConfig;
import com.aiplatform.dto.AIRequestDTO;
import com.aiplatform.dto.AIResponseDTO;
import com.aiplatform.entity.AIModel;
import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.repository.MessageAttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiConfig aiConfig;

    @Mock
    private MessageAttachmentRepository messageAttachmentRepository;

    @Mock
    private FileUploadService fileUploadService;

    private AiService aiService;
    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper();

        aiService = new AiService(aiConfig, messageAttachmentRepository, fileUploadService);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGenerateText_Success() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("Hello! How can I help you today?");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);
        choice.setIndex(0);
        choice.setFinishReason("stop");

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));
        response.setId("chatcmpl-123");
        response.setObject("chat.completion");
        response.setCreated(System.currentTimeMillis());
        response.setModel("gpt-3.5-turbo");

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.generateText("Hello", "gpt-3.5-turbo");

        // 验证结果
        assertEquals("Hello! How can I help you today?", result);

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/chat/completions", request.getPath());
        assertEquals("Bearer test-api-key", request.getHeader("Authorization"));
        assertEquals("application/json", request.getHeader("Content-Type"));
    }

    @Test
    void testGenerateText_EmptyResponse() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 模拟空响应
        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(new ArrayList<>());

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.generateText("Hello", "gpt-3.5-turbo");

        // 验证结果
        assertEquals("抱歉，AI服务暂时无法响应，请稍后再试。", result);
    }

    @Test
    void testGenerateText_ApiError() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 模拟API错误
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal Server Error\"}"));

        // 执行测试
        String result = aiService.generateText("Hello", "gpt-3.5-turbo");

        // 验证结果
        assertTrue(result.contains("抱歉，AI服务遇到错误"));
    }

    @Test
    void testGenerateImage_Success() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.ImageData imageData = new AIResponseDTO.ImageData();
        imageData.setUrl("https://example.com/generated-image.png");

        AIResponseDTO response = new AIResponseDTO();
        response.setData(Arrays.asList(imageData));
        response.setCreated(System.currentTimeMillis());

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.generateImage("A beautiful sunset", "dall-e-3");

        // 验证结果
        assertTrue(result.contains("图像生成成功"));
        assertTrue(result.contains("https://example.com/generated-image.png"));

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/images/generations", request.getPath());
    }

    @Test
    void testAnalyzeImage_WithRemoteUrl() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("This image shows a beautiful landscape with mountains and a lake.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.analyzeImage("https://example.com/image.jpg", "Describe this image", "gpt-4-turbo-preview");

        // 验证结果
        assertEquals("This image shows a beautiful landscape with mountains and a lake.", result);
    }

    @Test
    void testAnalyzeImage_WithLocalUrl() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备mock数据
        when(fileUploadService.imageToBase64("test-image.jpg")).thenReturn("base64-encoded-image");

        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("This is a test image analysis.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.analyzeImage("http://localhost:8080/api/files/test-image.jpg", "Analyze this image", "gpt-4-turbo-preview");

        // 验证结果
        assertEquals("This is a test image analysis.", result);
        verify(fileUploadService).imageToBase64("test-image.jpg");
    }

    @Test
    void testGenerateConversationResponse_WithImage() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("I can see the image and respond to your question.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.generateConversationResponse("What do you see?", "https://example.com/image.jpg", "gpt-4-turbo-preview");

        // 验证结果
        assertEquals("I can see the image and respond to your question.", result);
    }

    @Test
    void testGenerateConversationResponse_TextOnly() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("This is a text-only conversation response.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.generateConversationResponse("Hello", null, "gpt-3.5-turbo");

        // 验证结果
        assertEquals("This is a text-only conversation response.", result);
    }

    @Test
    void testGenerateResponse_WithAiType() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("Response based on AI type.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        Map<String, Object> options = new HashMap<>();
        options.put("imageUrl", "https://example.com/image.jpg");
        
        String result = aiService.generateResponse("Test prompt", "conversation", "gpt-3.5-turbo", options);

        // 验证结果
        assertEquals("Response based on AI type.", result);
    }

    @Test
    void testGenerateResponse_WithMessageId() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备mock数据
        MessageAttachment attachment = MessageAttachment.builder()
                .id(1L)
                .messageId(123L)
                .fileName("test-image.jpg")
                .filePath("/uploads/test-image.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .attachmentType(MessageAttachment.AttachmentType.IMAGE)
                .build();

        when(messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(123L))
                .thenReturn(Arrays.asList(attachment));

        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("Response with message attachment.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        Map<String, Object> options = new HashMap<>();
        options.put("messageId", 123L);
        
        String result = aiService.generateResponse("Test prompt", "image_to_text", "gpt-4-turbo-preview", options);

        // 验证结果
        assertEquals("Response with message attachment.", result);
        verify(messageAttachmentRepository).findByMessageIdOrderByCreatedAtAsc(123L);
    }

    @Test
    void testGenerateResponse_TextToImage() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.ImageData imageData = new AIResponseDTO.ImageData();
        imageData.setUrl("https://example.com/generated-image.png");

        AIResponseDTO response = new AIResponseDTO();
        response.setData(Arrays.asList(imageData));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        Map<String, Object> options = new HashMap<>();
        String result = aiService.generateResponse("Generate a sunset", "text_to_image", "dall-e-3", options);

        // 验证结果
        assertTrue(result.contains("图像生成成功"));
    }

    @Test
    void testGenerateResponse_ImageToTextWithoutImage() {
        // 执行测试
        Map<String, Object> options = new HashMap<>();
        String result = aiService.generateResponse("Analyze image", "image_to_text", "gpt-4-turbo-preview", options);

        // 验证结果
        assertEquals("请上传图片进行分析", result);
    }

    @Test
    void testGenerateResponse_UnsupportedType() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("Default text response.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        Map<String, Object> options = new HashMap<>();
        String result = aiService.generateResponse("Test", "unsupported_type", "gpt-3.5-turbo", options);

        // 验证结果
        assertEquals("Default text response.", result);
    }

    @Test
    void testGetAvailableModels() {
        // 执行测试
        List<AIModel> models = aiService.getAvailableModels();

        // 验证结果
        assertNotNull(models);
        assertTrue(models.size() > 0);
        assertEquals(AIModel.values().length, models.size());
    }

    @Test
    void testIsAIServiceAvailable() {
        // 测试可用情况
        when(aiConfig.getApiKey()).thenReturn("valid-api-key");
        assertTrue(aiService.isAIServiceAvailable());

        // 测试不可用情况
        when(aiConfig.getApiKey()).thenReturn(null);
        assertFalse(aiService.isAIServiceAvailable());

        when(aiConfig.getApiKey()).thenReturn("");
        assertFalse(aiService.isAIServiceAvailable());

        when(aiConfig.getApiKey()).thenReturn("   ");
        assertFalse(aiService.isAIServiceAvailable());
    }

    @Test
    void testGetCurrentModel() {
        // 执行测试
        when(aiConfig.getDefaultModel()).thenReturn("gpt-3.5-turbo");
        String model = aiService.getCurrentModel();

        // 验证结果
        assertEquals("gpt-3.5-turbo", model);
        verify(aiConfig).getDefaultModel();
    }

    @Test
    void testGetImageSupportModels() {
        // 执行测试
        List<AIModel> models = aiService.getImageSupportModels();

        // 验证结果
        assertNotNull(models);
        models.forEach(model -> assertTrue(model.isSupportsImage()));
    }

    @Test
    void testGetModelInfo() {
        // 执行测试
        AIModel model = aiService.getModelInfo("openai/gpt-4.1-nano");

        // 验证结果
        assertNotNull(model);
        assertEquals("openai/gpt-4.1-nano", model.getModelId());
    }

    @Test
    void testIsModelAvailable() {
        // 测试可用模型
        assertTrue(aiService.isModelAvailable("openai/gpt-4.1-nano"));
        
        // 测试不可用模型
        assertFalse(aiService.isModelAvailable("non-existent-model"));
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("Hello!");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        boolean result = aiService.healthCheck();

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testHealthCheck_Failure() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 模拟API错误
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\": \"Internal Server Error\"}"));

        // 执行测试
        boolean result = aiService.healthCheck();

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testGenerateResponse_LegacyMethod() throws Exception {
        when(aiConfig.getBaseUrl()).thenReturn(mockWebServer.url("/").toString());
        when(aiConfig.getApiKey()).thenReturn("test-api-key");
        
        // 准备响应数据
        AIResponseDTO.Message message = new AIResponseDTO.Message();
        message.setContent("Legacy method response.");
        message.setRole("assistant");

        AIResponseDTO.Choice choice = new AIResponseDTO.Choice();
        choice.setMessage(message);

        AIResponseDTO response = new AIResponseDTO();
        response.setChoices(Arrays.asList(choice));

        // 模拟API响应
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        // 执行测试
        String result = aiService.generateResponse("Test prompt", "gpt-3.5-turbo");

        // 验证结果
        assertEquals("Legacy method response.", result);
    }

    @Test
    void testAnalyzeImage_Base64ConversionError() throws Exception {
        // 模拟base64转换失败
        when(fileUploadService.imageToBase64("test-image.jpg"))
                .thenThrow(new RuntimeException("Base64 conversion failed"));

        // 执行测试
        String result = aiService.analyzeImage("http://localhost:8080/api/files/test-image.jpg", "Analyze", "gpt-4-turbo-preview");

        // 验证结果
        assertEquals("抱歉，图片处理失败，请稍后再试。", result);
    }

    @Test
    void testGenerateResponse_DevelopmentFeatures() {
        Map<String, Object> options = new HashMap<>();
        
        // 测试图像到图像转换
        String result1 = aiService.generateResponse("Test", "image_to_image", "gpt-3.5-turbo", options);
        assertEquals("图像到图像转换功能正在开发中...", result1);
        
        // 测试文本到3D
        String result2 = aiService.generateResponse("Test", "text_to_3d", "gpt-3.5-turbo", options);
        assertEquals("文本到3D功能正在开发中...", result2);
        
        // 测试文本到视频
        String result3 = aiService.generateResponse("Test", "text_to_video", "gpt-3.5-turbo", options);
        assertEquals("文本到视频功能正在开发中...", result3);
    }
} 