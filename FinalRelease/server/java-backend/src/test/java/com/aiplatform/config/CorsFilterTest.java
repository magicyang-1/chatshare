//package com.aiplatform.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.FilterConfig;
//import jakarta.servlet.ServletException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.mock.web.MockFilterChain;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//
//import java.io.IOException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * CorsFilter 单元测试
// */
//public class CorsFilterTest {
//
//    private CorsFilter corsFilter;
//    private MockHttpServletRequest request;
//    private MockHttpServletResponse response;
//    private FilterChain filterChain;
//
//    @BeforeEach
//    public void setUp() {
//        corsFilter = new CorsFilter();
//        request = new MockHttpServletRequest();
//        response = new MockHttpServletResponse();
//        filterChain = new MockFilterChain();
//    }
//
//    @Test
//    public void testCorsFilterImplementsFilter() {
//        assertTrue(corsFilter instanceof jakarta.servlet.Filter);
//    }
//
//    @Test
//    public void testDoFilterForGetRequest() throws Exception {
//        // 设置GET请求
//        request.setMethod("GET");
//        request.setRequestURI("/api/test");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//
//        // 验证状态码是200（因为chain.doFilter会继续处理请求）
//        assertEquals(200, response.getStatus());
//    }
//
//    @Test
//    public void testDoFilterForOptionsRequest() throws Exception {
//        // 设置OPTIONS请求
//        request.setMethod("OPTIONS");
//        request.setRequestURI("/api/test");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//
//        // 验证OPTIONS请求返回200状态码
//        assertEquals(200, response.getStatus());
//    }
//
//    @Test
//    public void testDoFilterForPostRequest() throws Exception {
//        // 设置POST请求
//        request.setMethod("POST");
//        request.setRequestURI("/api/users");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//
//        // 验证状态码是200（因为chain.doFilter会继续处理请求）
//        assertEquals(200, response.getStatus());
//    }
//
//    @Test
//    public void testDoFilterForPutRequest() throws Exception {
//        // 设置PUT请求
//        request.setMethod("PUT");
//        request.setRequestURI("/api/users/123");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//    }
//
//    @Test
//    public void testDoFilterForDeleteRequest() throws Exception {
//        // 设置DELETE请求
//        request.setMethod("DELETE");
//        request.setRequestURI("/api/users/123");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//    }
//
//    @Test
//    public void testDoFilterForPatchRequest() throws Exception {
//        // 设置PATCH请求
//        request.setMethod("PATCH");
//        request.setRequestURI("/api/users/123");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//    }
//
//    @Test
//    public void testDoFilterForNonApiPath() throws Exception {
//        // 设置非API路径的请求
//        request.setMethod("GET");
//        request.setRequestURI("/static/css/style.css");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部仍然设置（因为过滤器对所有请求都生效）
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//    }
//
//    @Test
//    public void testDoFilterWithCustomHeaders() throws Exception {
//        // 设置请求头
//        request.setMethod("POST");
//        request.setRequestURI("/api/test");
//        request.addHeader("Content-Type", "application/json");
//        request.addHeader("Authorization", "Bearer token");
//
//        // 执行过滤器
//        corsFilter.doFilter(request, response, filterChain);
//
//        // 验证CORS头部
//        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));
//        assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", response.getHeader("Access-Control-Allow-Methods"));
//        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
//        assertEquals("Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
//                     response.getHeader("Access-Control-Allow-Headers"));
//        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
//    }
//
//    @Test
//    public void testInitMethod() {
//        // 测试init方法（应该不抛出异常）
//        FilterConfig filterConfig = null;
//        assertDoesNotThrow(() -> corsFilter.init(filterConfig));
//    }
//
//    @Test
//    public void testDestroyMethod() {
//        // 测试destroy方法（应该不抛出异常）
//        assertDoesNotThrow(() -> corsFilter.destroy());
//    }
//
//    @Test
//    public void testDoFilterWithNullRequest() {
//        // 测试空请求（会抛出NullPointerException）
//        assertThrows(NullPointerException.class, () -> {
//            corsFilter.doFilter(null, response, filterChain);
//        });
//    }
//
//    @Test
//    public void testDoFilterWithNullResponse() {
//        // 测试空响应（会抛出NullPointerException）
//        assertThrows(NullPointerException.class, () -> {
//            corsFilter.doFilter(request, null, filterChain);
//        });
//    }
//
//    @Test
//    public void testDoFilterWithNullFilterChain() {
//        // 测试空过滤器链（会抛出NullPointerException）
//        assertThrows(NullPointerException.class, () -> {
//            corsFilter.doFilter(request, response, null);
//        });
//    }
//
//    @Test
//    public void testCorsHeadersConsistency() throws Exception {
//        // 测试多次调用的一致性
//        request.setMethod("GET");
//        request.setRequestURI("/api/test");
//
//        // 第一次调用
//        MockHttpServletResponse response1 = new MockHttpServletResponse();
//        FilterChain filterChain1 = new MockFilterChain();
//        corsFilter.doFilter(request, response1, filterChain1);
//        String firstOrigin = response1.getHeader("Access-Control-Allow-Origin");
//        String firstMethods = response1.getHeader("Access-Control-Allow-Methods");
//
//        // 第二次调用（使用新的响应和过滤器链）
//        MockHttpServletResponse response2 = new MockHttpServletResponse();
//        FilterChain filterChain2 = new MockFilterChain();
//        corsFilter.doFilter(request, response2, filterChain2);
//        String secondOrigin = response2.getHeader("Access-Control-Allow-Origin");
//        String secondMethods = response2.getHeader("Access-Control-Allow-Methods");
//
//        // 验证一致性
//        assertEquals(firstOrigin, secondOrigin);
//        assertEquals(firstMethods, secondMethods);
//    }
//
//    @Test
//    public void testOptionsRequestWithDifferentPaths() throws Exception {
//        String[] paths = {"/api/users", "/api/chat", "/api/image", "/api/admin"};
//
//        for (String path : paths) {
//            MockHttpServletRequest testRequest = new MockHttpServletRequest();
//            MockHttpServletResponse testResponse = new MockHttpServletResponse();
//            FilterChain testFilterChain = new MockFilterChain();
//
//            testRequest.setMethod("OPTIONS");
//            testRequest.setRequestURI(path);
//
//            corsFilter.doFilter(testRequest, testResponse, testFilterChain);
//
//            // 验证所有路径都返回200状态码
//            assertEquals(200, testResponse.getStatus(), "Path: " + path);
//
//            // 验证CORS头部
//            assertEquals("*", testResponse.getHeader("Access-Control-Allow-Origin"));
//            assertEquals("GET, POST, PUT, DELETE, PATCH, OPTIONS", testResponse.getHeader("Access-Control-Allow-Methods"));
//        }
//    }
//}