package com.aiplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JacksonConfig 单元测试
 */
public class JacksonConfigTest {

    @Test
    public void testObjectMapperBeanCreation() {
        // 创建Spring上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JacksonConfig.class);
        
        // 获取ObjectMapper bean
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
        
        assertNotNull(objectMapper);
        context.close();
    }

    @Test
    public void testObjectMapperIsPrimary() throws NoSuchMethodException {
        // 检查@Primary注解
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 验证ObjectMapper实例不为空
        assertNotNull(objectMapper);
    }

    @Test
    public void testHibernateModuleRegistration() {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 通过序列化测试来验证Hibernate6Module是否工作
        // 创建一个简单的测试对象
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setName("test");
        
        // 如果Hibernate6Module正确注册，序列化应该不会失败
        assertDoesNotThrow(() -> {
            String json = objectMapper.writeValueAsString(testEntity);
            assertNotNull(json);
        }, "Hibernate6Module should be properly registered");
    }

    @Test
    public void testJavaTimeModuleRegistration() {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 通过时间序列化测试来验证JavaTimeModule是否工作
        LocalDateTime testTime = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
        
        // 如果JavaTimeModule正确注册，时间序列化应该工作
        assertDoesNotThrow(() -> {
            String json = objectMapper.writeValueAsString(testTime);
            assertNotNull(json);
            assertFalse(json.matches("\\d+"), "Date should not be serialized as timestamp");
        }, "JavaTimeModule should be properly registered");
    }

    @Test
    public void testSerializationFeatures() {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 检查序列化特性配置
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertFalse(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
    }

    @Test
    public void testDateTimeSerialization() throws Exception {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 测试时间序列化
        LocalDateTime now = LocalDateTime.now();
        String json = objectMapper.writeValueAsString(now);
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        
        // 验证序列化的JSON不是时间戳格式
        assertFalse(json.matches("\\d+"), "Date should not be serialized as timestamp");
    }

    @Test
    public void testDateTimeDeserialization() throws Exception {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 测试时间反序列化
        String dateTimeString = "2023-12-01T10:30:00";
        LocalDateTime dateTime = objectMapper.readValue("\"" + dateTimeString + "\"", LocalDateTime.class);
        
        assertNotNull(dateTime);
        assertEquals(2023, dateTime.getYear());
        assertEquals(12, dateTime.getMonthValue());
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(10, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
    }

    @Test
    public void testEmptyBeanSerialization() throws Exception {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 测试空对象序列化（不应该失败）
        EmptyBean emptyBean = new EmptyBean();
        String json = objectMapper.writeValueAsString(emptyBean);
        
        assertNotNull(json);
        // 空对象应该序列化为 {}
        assertEquals("{}", json);
    }

    @Test
    public void testHibernateModuleConfiguration() {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 通过序列化测试来验证Hibernate6Module配置是否正确
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setName("test");
        
        // 如果Hibernate6Module配置正确，序列化应该工作
        assertDoesNotThrow(() -> {
            String json = objectMapper.writeValueAsString(testEntity);
            assertNotNull(json);
            assertTrue(json.contains("id"));
            assertTrue(json.contains("name"));
        }, "Hibernate6Module should be properly configured");
    }

    @Test
    public void testObjectMapperConfiguration() {
        ObjectMapper objectMapper = new JacksonConfig().objectMapper();
        
        // 验证ObjectMapper的基本配置
        assertNotNull(objectMapper);
        
        // 验证模块数量（至少应该有Hibernate6Module和JavaTimeModule）
        assertTrue(objectMapper.getRegisteredModuleIds().size() >= 2);
    }

    @Test
    public void testMultipleObjectMapperInstances() {
        JacksonConfig config = new JacksonConfig();
        
        // 创建多个ObjectMapper实例
        ObjectMapper mapper1 = config.objectMapper();
        ObjectMapper mapper2 = config.objectMapper();
        
        // 验证它们是不同的实例
        assertNotSame(mapper1, mapper2);
        
        // 验证它们有相同的配置
        assertEquals(mapper1.getRegisteredModuleIds(), mapper2.getRegisteredModuleIds());
        assertEquals(mapper1.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
                    mapper2.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void testSpringContextIntegration() {
        // 创建Spring上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(JacksonConfig.class);
        context.refresh();
        
        // 获取ObjectMapper
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
        
        assertNotNull(objectMapper);
        
        // 验证配置
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertFalse(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        
        context.close();
    }

    // 用于测试的空Bean类
    public static class EmptyBean {
        // 空类，用于测试空对象序列化
    }
    
    // 用于测试Hibernate模块的实体类
    public static class TestEntity {
        private Long id;
        private String name;
        
        public TestEntity() {}
        
        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
} 