package com.aiplatform.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException 单元测试
 */
public class BusinessExceptionTest {

    @Test
    public void testConstructorWithMessage() {
        String message = "业务异常消息";
        BusinessException exception = new BusinessException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCode());
        assertNull(exception.getData());
    }

    @Test
    public void testConstructorWithCodeAndMessage() {
        String code = "BUSINESS_ERROR";
        String message = "业务异常消息";
        BusinessException exception = new BusinessException(code, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
        assertNull(exception.getData());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        String message = "业务异常消息";
        Throwable cause = new RuntimeException("原始异常");
        BusinessException exception = new BusinessException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getCode());
        assertNull(exception.getData());
    }

    @Test
    public void testConstructorWithCodeMessageAndData() {
        String code = "VALIDATION_ERROR";
        String message = "验证失败";
        Object data = new Object();
        BusinessException exception = new BusinessException(code, message, data);
        
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
        assertEquals(data, exception.getData());
    }

    @Test
    public void testGetCode() {
        String code = "TEST_CODE";
        BusinessException exception = new BusinessException(code, "测试消息");
        assertEquals(code, exception.getCode());
    }

    @Test
    public void testGetData() {
        Object data = "测试数据";
        BusinessException exception = new BusinessException("CODE", "消息", data);
        assertEquals(data, exception.getData());
    }

    @Test
    public void testExceptionInheritance() {
        BusinessException exception = new BusinessException("测试消息");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testNullValues() {
        BusinessException exception = new BusinessException(null, null, null);
        assertNull(exception.getMessage());
        assertNull(exception.getCode());
        assertNull(exception.getData());
    }

    @Test
    public void testEmptyValues() {
        BusinessException exception = new BusinessException("", "", "");
        assertEquals("", exception.getMessage());
        assertEquals("", exception.getCode());
        assertEquals("", exception.getData());
    }
} 