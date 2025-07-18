package com.aiplatform.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebConfig 单元测试
 *
 * 说明：
 * CorsRegistry 没有 getCorsConfigurationSource() 方法，无法直接获取 CORS 配置。
 * 复杂断言建议在集成测试中用 MockMvc 进行端到端测试。
 * 此处仅测试接口实现和 addCorsMappings 方法调用。
 */
public class WebConfigTest {

    private WebConfig webConfig;
    private CorsRegistry corsRegistry;

    @BeforeEach
    public void setUp() {
        webConfig = new WebConfig();
        corsRegistry = new CorsRegistry();
    }

    @Test
    public void testWebConfigImplementsWebMvcConfigurer() {
        assertTrue(webConfig instanceof WebMvcConfigurer);
    }

    @Test
    public void testAddCorsMappingsDoesNotThrow() {
        // 只验证方法调用不会抛出异常
        assertDoesNotThrow(() -> webConfig.addCorsMappings(corsRegistry));
    }
} 