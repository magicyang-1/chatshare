package com.aiplatform.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AiConfig 单元测试
 */
public class AiConfigTest {

    private AiConfig aiConfig;

    @BeforeEach
    public void setUp() {
        aiConfig = new AiConfig();
    }

    @Test
    public void testOpenRouterConfig() {
        AiConfig.OpenRouter openRouter = new AiConfig.OpenRouter();
        
        // 测试默认值
        assertEquals("https://openrouter.ai/api/v1", openRouter.getBaseUrl());
        assertEquals("", openRouter.getApiKey());
        assertEquals("openai/gpt-4.1-nano", openRouter.getDefaultModel());
        assertEquals(30000, openRouter.getTimeout());
        assertEquals(2000, openRouter.getMaxTokens());
        assertEquals(0.7, openRouter.getTemperature());

        // 测试设置值
        openRouter.setBaseUrl("https://test.openrouter.ai/api/v1");
        openRouter.setApiKey("test-api-key");
        openRouter.setDefaultModel("test-model");
        openRouter.setTimeout(60000);
        openRouter.setMaxTokens(4000);
        openRouter.setTemperature(0.5);

        assertEquals("https://test.openrouter.ai/api/v1", openRouter.getBaseUrl());
        assertEquals("test-api-key", openRouter.getApiKey());
        assertEquals("test-model", openRouter.getDefaultModel());
        assertEquals(60000, openRouter.getTimeout());
        assertEquals(4000, openRouter.getMaxTokens());
        assertEquals(0.5, openRouter.getTemperature());
    }

    @Test
    public void testLocalConfig() {
        AiConfig.Local local = new AiConfig.Local();
        
        // 测试默认值
        assertTrue(local.isEnabled());
        assertEquals("http://202.120.38.3:55322", local.getBaseUrl());
        assertEquals("", local.getApiKey());
        assertEquals(60000, local.getTimeout());
        assertEquals(4000, local.getMaxTokens());
        assertEquals(0.7, local.getTemperature());

        // 测试设置值
        local.setEnabled(false);
        local.setBaseUrl("http://localhost:8080");
        local.setApiKey("local-api-key");
        local.setTimeout(30000);
        local.setMaxTokens(2000);
        local.setTemperature(0.3);

        assertFalse(local.isEnabled());
        assertEquals("http://localhost:8080", local.getBaseUrl());
        assertEquals("local-api-key", local.getApiKey());
        assertEquals(30000, local.getTimeout());
        assertEquals(2000, local.getMaxTokens());
        assertEquals(0.3, local.getTemperature());
    }

    @Test
    public void testDashScopeConfig() {
        AiConfig.DashScope dashScope = new AiConfig.DashScope();
        
        // 测试默认值
        assertTrue(dashScope.isEnabled());
        assertEquals("", dashScope.getApiKey());
        assertEquals(60000, dashScope.getTimeout());
        assertEquals("wanx-v1", dashScope.getDefaultModel());
        assertEquals("1024*1024", dashScope.getDefaultSize());
        assertEquals("<auto>", dashScope.getDefaultStyle());

        // 测试设置值
        dashScope.setEnabled(false);
        dashScope.setApiKey("dashscope-api-key");
        dashScope.setTimeout(30000);
        dashScope.setDefaultModel("test-model");
        dashScope.setDefaultSize("512*512");
        dashScope.setDefaultStyle("<realistic>");

        assertFalse(dashScope.isEnabled());
        assertEquals("dashscope-api-key", dashScope.getApiKey());
        assertEquals(30000, dashScope.getTimeout());
        assertEquals("test-model", dashScope.getDefaultModel());
        assertEquals("512*512", dashScope.getDefaultSize());
        assertEquals("<realistic>", dashScope.getDefaultStyle());
    }

    @Test
    public void testAiConfigCompatibilityMethods() {
        // 设置OpenRouter配置
        AiConfig.OpenRouter openRouter = new AiConfig.OpenRouter();
        openRouter.setBaseUrl("https://test.openrouter.ai/api/v1");
        openRouter.setApiKey("test-api-key");
        openRouter.setDefaultModel("test-model");
        openRouter.setTimeout(45000);
        aiConfig.setOpenrouter(openRouter);

        // 测试兼容性方法
        assertEquals("https://test.openrouter.ai/api/v1", aiConfig.getBaseUrl());
        assertEquals("test-api-key", aiConfig.getApiKey());
        assertEquals("test-model", aiConfig.getDefaultModel());
        assertEquals(45000, aiConfig.getTimeout());
    }

    @Test
    public void testAiConfigSetters() {
        // 测试设置各个配置
        AiConfig.OpenRouter openRouter = new AiConfig.OpenRouter();
        AiConfig.Local local = new AiConfig.Local();
        AiConfig.DashScope dashScope = new AiConfig.DashScope();

        aiConfig.setOpenrouter(openRouter);
        aiConfig.setLocal(local);
        aiConfig.setDashscope(dashScope);

        assertNotNull(aiConfig.getOpenrouter());
        assertNotNull(aiConfig.getLocal());
        assertNotNull(aiConfig.getDashscope());
    }

    @Test
    public void testNullValues() {
        AiConfig.OpenRouter openRouter = new AiConfig.OpenRouter();
        openRouter.setBaseUrl(null);
        openRouter.setApiKey(null);
        openRouter.setDefaultModel(null);

        assertNull(openRouter.getBaseUrl());
        assertNull(openRouter.getApiKey());
        assertNull(openRouter.getDefaultModel());
    }

    @Test
    public void testEmptyValues() {
        AiConfig.OpenRouter openRouter = new AiConfig.OpenRouter();
        openRouter.setBaseUrl("");
        openRouter.setApiKey("");
        openRouter.setDefaultModel("");

        assertEquals("", openRouter.getBaseUrl());
        assertEquals("", openRouter.getApiKey());
        assertEquals("", openRouter.getDefaultModel());
    }

    @Test
    public void testDashScopeConfigWithNullValues() {
        AiConfig.DashScope dashScope = new AiConfig.DashScope();
        dashScope.setApiKey(null);
        dashScope.setDefaultModel(null);
        dashScope.setDefaultSize(null);
        dashScope.setDefaultStyle(null);

        assertNull(dashScope.getApiKey());
        assertNull(dashScope.getDefaultModel());
        assertNull(dashScope.getDefaultSize());
        assertNull(dashScope.getDefaultStyle());
    }

    @Test
    public void testLocalConfigWithNullValues() {
        AiConfig.Local local = new AiConfig.Local();
        local.setBaseUrl(null);
        local.setApiKey(null);

        assertNull(local.getBaseUrl());
        assertNull(local.getApiKey());
    }

    @Test
    public void testToString() {
        AiConfig.OpenRouter openRouter = new AiConfig.OpenRouter();
        openRouter.setBaseUrl("https://test.com");
        openRouter.setApiKey("test-key");
        
        String toString = openRouter.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("https://test.com"));
        assertTrue(toString.contains("test-key"));
    }

    @Test
    public void testEqualsAndHashCode() {
        AiConfig.OpenRouter config1 = new AiConfig.OpenRouter();
        AiConfig.OpenRouter config2 = new AiConfig.OpenRouter();
        
        config1.setBaseUrl("https://test1.com");
        config2.setBaseUrl("https://test1.com");
        
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        
        config2.setBaseUrl("https://test2.com");
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
    }
} 