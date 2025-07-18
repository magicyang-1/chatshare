package com.aiplatform.controller;

import com.aiplatform.entity.PromptCategory;
import com.aiplatform.entity.PromptTemplate;
import com.aiplatform.entity.User;
import com.aiplatform.dto.PromptTemplateDTO;
import com.aiplatform.security.JwtTokenProvider;
import com.aiplatform.service.PromptTemplateService;
import com.aiplatform.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        PromptTemplateController.class, 
        PromptTemplateControllerTest.TestConfig.class,
        PromptTemplateControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class PromptTemplateControllerTest {

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
                )
                .addFilterBefore(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, 
                                                 HttpServletResponse response, 
                                                 FilterChain filterChain) throws ServletException, IOException {
                        // 跳过JWT验证，直接放行
                        filterChain.doFilter(request, response);
                    }
                }, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromptTemplateService promptTemplateService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        System.out.println("✅ PromptTemplateControllerTest 基础配置测试通过");
    }

    @Test
    public void testGetCategories_success() throws Exception {
        // 模拟分类数据
        List<PromptCategory> categories = Arrays.asList(
            createCategory(1L, "写作助手", "帮助用户进行各种写作"),
            createCategory(2L, "代码生成", "生成各种编程代码"),
            createCategory(3L, "翻译工具", "多语言翻译服务")
        );
        
        Mockito.when(promptTemplateService.getAllCategories())
                .thenReturn(categories);

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(3))
                .andExpect(jsonPath("$.categories[0].name").value("写作助手"))
                .andExpect(jsonPath("$.categories[1].name").value("代码生成"));

        System.out.println("✅ 获取分类测试通过");
    }

    @Test
    public void testGetTemplates_byCategory() throws Exception {
        // 模拟模板数据
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "写作助手模板", 1L),
            createTemplate(2L, "代码生成模板", 2L)
        );
        Page<PromptTemplate> page = new PageImpl<>(templates, PageRequest.of(0, 20), 2);
        
        Mockito.when(promptTemplateService.getTemplatesByCategory(1L, 0, 20))
                .thenReturn(page);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "写作助手模板"),
                    createTemplateDTO(2L, "代码生成模板")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.currentPage").value(0));

        System.out.println("✅ 按分类获取模板测试通过");
    }

    @Test
    public void testGetTemplates_byType() throws Exception {
        // 模拟官方模板
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "官方写作模板", 1L),
            createTemplate(2L, "官方代码模板", 2L)
        );
        Page<PromptTemplate> page = new PageImpl<>(templates, PageRequest.of(0, 20), 2);
        
        Mockito.when(promptTemplateService.getOfficialTemplates(0, 20))
                .thenReturn(page);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "官方写作模板"),
                    createTemplateDTO(2L, "官方代码模板")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("type", "official")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray());

        System.out.println("✅ 按类型获取模板测试通过");
    }

    @Test
    public void testGetTemplates_byKeyword() throws Exception {
        // 模拟搜索结果
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "写作助手", 1L),
            createTemplate(2L, "写作技巧", 1L)
        );
        Page<PromptTemplate> page = new PageImpl<>(templates, PageRequest.of(0, 20), 2);
        
        Mockito.when(promptTemplateService.searchTemplates("写作", null, 0, 20))
                .thenReturn(page);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "写作助手"),
                    createTemplateDTO(2L, "写作技巧")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("keyword", "写作")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray());

        System.out.println("✅ 关键词搜索模板测试通过");
    }

    @Test
    public void testGetFeaturedTemplates_success() throws Exception {
        // 模拟精选模板
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "精选写作模板", 1L),
            createTemplate(2L, "精选代码模板", 2L)
        );
        
        Mockito.when(promptTemplateService.getFeaturedTemplates())
                .thenReturn(templates);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "精选写作模板"),
                    createTemplateDTO(2L, "精选代码模板")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray())
                .andExpect(jsonPath("$.templates.length()").value(2));

        System.out.println("✅ 获取精选模板测试通过");
    }

    @Test
    public void testGetPopularTemplates_success() throws Exception {
        // 模拟热门模板
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "热门写作模板", 1L),
            createTemplate(2L, "热门代码模板", 2L)
        );
        
        Mockito.when(promptTemplateService.getPopularTemplates())
                .thenReturn(templates);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "热门写作模板"),
                    createTemplateDTO(2L, "热门代码模板")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray());

        System.out.println("✅ 获取热门模板测试通过");
    }

    @Test
    public void testGetLatestTemplates_success() throws Exception {
        // 模拟最新模板
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "最新写作模板", 1L),
            createTemplate(2L, "最新代码模板", 2L)
        );
        
        Mockito.when(promptTemplateService.getLatestTemplates())
                .thenReturn(templates);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "最新写作模板"),
                    createTemplateDTO(2L, "最新代码模板")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray());

        System.out.println("✅ 获取最新模板测试通过");
    }

    @Test
    public void testGetRecommendedTemplates_success() throws Exception {
        // 模拟推荐模板
        List<PromptTemplate> templates = Arrays.asList(
            createTemplate(1L, "推荐写作模板", 1L),
            createTemplate(2L, "推荐代码模板", 2L)
        );
        
        Mockito.when(promptTemplateService.getRecommendedTemplates("gpt-4"))
                .thenReturn(templates);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(
                    createTemplateDTO(1L, "推荐写作模板"),
                    createTemplateDTO(2L, "推荐代码模板")
                ));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/recommended")
                        .param("aiModel", "gpt-4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray());

        System.out.println("✅ 获取推荐模板测试通过");
    }

    @Test
    public void testGetTemplateById_success() throws Exception {
        // 模拟模板详情
        PromptTemplate template = createTemplate(1L, "测试模板", 1L);
        
        Mockito.when(promptTemplateService.getTemplateById(eq(1L)))
                .thenReturn(Optional.of(template));
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(createTemplateDTO(1L, "测试模板")));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.template.title").value("测试模板"));

        System.out.println("✅ 获取模板详情测试通过");
    }

    @Test
    public void testGetTemplateById_notFound() throws Exception {
        Mockito.when(promptTemplateService.getTemplateById(eq(999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/999"))
                .andExpect(status().isNotFound());

        System.out.println("✅ 获取模板详情（不存在）测试通过");
    }

    @Test
    public void testCreateTemplate_success() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟创建模板
        PromptTemplate template = createTemplate(1L, "新模板", 1L);
        template.setCreatorId(1L);
        template.setCreatorName("testuser");
        
        Mockito.when(promptTemplateService.createTemplate(any(PromptTemplate.class)))
                .thenReturn(template);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(createTemplateDTO(1L, "新模板")));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "新模板");
        request.put("description", "这是一个测试模板");
        request.put("content", "请帮我写一篇文章");
        request.put("categoryId", 1L);
        request.put("aiModel", "gpt-4");

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("模板创建成功"))
                .andExpect(jsonPath("$.template.title").value("新模板"));

        System.out.println("✅ 创建模板测试通过");
    }

    @Test
    public void testCreateTemplate_userNotFound() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户不存在
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        Map<String, Object> request = new HashMap<>();
        request.put("title", "新模板");
        request.put("content", "请帮我写一篇文章");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户不存在"));

        System.out.println("✅ 创建模板（用户不存在）测试通过");
    }

    @Test
    public void testUpdateTemplate_success() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板
        PromptTemplate existingTemplate = createTemplate(1L, "原模板", 1L);
        existingTemplate.setCreatorId(1L);
        Mockito.when(promptTemplateService.getTemplateById(eq(1L)))
                .thenReturn(Optional.of(existingTemplate));

        // 模拟更新模板
        PromptTemplate updatedTemplate = createTemplate(1L, "更新后的模板", 1L);
        Mockito.when(promptTemplateService.updateTemplate(eq(1L), any(PromptTemplate.class)))
                .thenReturn(updatedTemplate);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(createTemplateDTO(1L, "更新后的模板")));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "更新后的模板");
        request.put("description", "更新后的描述");
        request.put("content", "更新后的内容");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.put("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("模板更新成功"));

        System.out.println("✅ 更新模板测试通过");
    }

    @Test
    public void testUpdateTemplate_noPermission() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板（其他用户创建）
        PromptTemplate existingTemplate = createTemplate(1L, "原模板", 1L);
        existingTemplate.setCreatorId(999L); // 其他用户ID
        Mockito.when(promptTemplateService.getTemplateById(eq(1L)))
                .thenReturn(Optional.of(existingTemplate));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "更新后的模板");
        request.put("content", "更新后的内容");

        mockMvc.perform(MockMvcRequestBuilders.put("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("没有权限修改此模板"));

        System.out.println("✅ 更新模板（无权限）测试通过");
    }

    @Test
    public void testDeleteTemplate_success() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板
        PromptTemplate existingTemplate = createTemplate(1L, "要删除的模板", 1L);
        existingTemplate.setCreatorId(1L);
        Mockito.when(promptTemplateService.getTemplateById(eq(1L)))
                .thenReturn(Optional.of(existingTemplate));

        // 模拟删除模板
        Mockito.doNothing().when(promptTemplateService).deleteTemplate(eq(1L));

        mockMvc.perform(MockMvcRequestBuilders.delete("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("模板删除成功"));

        System.out.println("✅ 删除模板测试通过");
    }

    @Test
    public void testToggleLike_success() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟点赞操作
        Mockito.when(promptTemplateService.toggleLike(eq(1L), eq(1L)))
                .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates/1/like")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.message").value("点赞成功"));

        System.out.println("✅ 点赞模板测试通过");
    }

    @Test
    public void testUseTemplate_success() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟记录使用
        Mockito.doNothing().when(promptTemplateService).recordUsage(eq(1L), eq(1L), eq("gpt-4"));

        Map<String, String> request = new HashMap<>();
        request.put("aiModel", "gpt-4");

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates/1/use")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("使用记录已保存"));

        System.out.println("✅ 使用模板测试通过");
    }

    // 辅助方法：创建测试用户对象
    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setRole(User.UserRole.user);
        return user;
    }

    // 辅助方法：创建测试分类对象
    private PromptCategory createCategory(Long id, String name, String description) {
        PromptCategory category = new PromptCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setIcon("icon-" + id);
        category.setSortOrder(id.intValue());
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    // 辅助方法：创建测试模板对象
    private PromptTemplate createTemplate(Long id, String title, Long categoryId) {
        PromptTemplate template = new PromptTemplate();
        template.setId(id);
        template.setTitle(title);
        template.setDescription("这是一个测试模板");
        template.setContent("请帮我写一篇文章");
        template.setCategoryId(categoryId);
        template.setAiModel("gpt-4");
        template.setTemplateType(PromptTemplate.TemplateType.USER);
        template.setCreatorId(1L);
        template.setCreatorName("testuser");
        template.setTags("写作,文章");
        template.setLanguage("zh-CN");
        template.setDifficultyLevel(PromptTemplate.DifficultyLevel.BEGINNER);
        template.setIsPublic(true);
        template.setIsFeatured(false);
        template.setUsageCount(0);
        template.setLikeCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return template;
    }

    // 辅助方法：创建测试模板DTO对象
    private PromptTemplateDTO createTemplateDTO(Long id, String title) {
        PromptTemplateDTO dto = new PromptTemplateDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setDescription("这是一个测试模板");
        dto.setContent("请帮我写一篇文章");
        dto.setCategoryId(1L);
        dto.setCategoryName("写作助手");
        dto.setAiModel("gpt-4");
        dto.setTemplateType("USER");
        dto.setCreatorId(1L);
        dto.setCreatorName("testuser");
        dto.setTags("写作,文章");
        dto.setLanguage("zh-CN");
        dto.setDifficultyLevel("BEGINNER");
        dto.setIsPublic(true);
        dto.setIsFeatured(false);
        dto.setUsageCount(0);
        dto.setLikeCount(0);
        dto.setLiked(false);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    public void testGetCategories_empty() throws Exception {
        // 模拟空分类数据
        Mockito.when(promptTemplateService.getAllCategories())
                .thenReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories.length()").value(0));

        System.out.println("✅ 获取分类（空数据）测试通过");
    }

    @Test
    public void testGetCategories_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getAllCategories())
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/categories"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取分类失败: 服务异常"));

        System.out.println("✅ 获取分类（服务异常）测试通过");
    }

    @Test
    public void testGetTemplates_byCategory_empty() throws Exception {
        // 模拟空模板数据
        Page<PromptTemplate> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        
        Mockito.when(promptTemplateService.getTemplatesByCategory(1L, 0, 20))
                .thenReturn(emptyPage);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        System.out.println("✅ 按分类获取模板（空数据）测试通过");
    }

    @Test
    public void testGetTemplates_byCategory_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getTemplatesByCategory(any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("categoryId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取模板列表失败: 服务异常"));

        System.out.println("✅ 按分类获取模板（服务异常）测试通过");
    }

    @Test
    public void testGetTemplates_byType_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getOfficialTemplates(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("type", "official"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取模板列表失败: 服务异常"));

        System.out.println("✅ 按类型获取模板（服务异常）测试通过");
    }

    @Test
    public void testGetTemplates_byKeyword_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.searchTemplates(anyString(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("keyword", "测试"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取模板列表失败: 服务异常"));

        System.out.println("✅ 按关键词获取模板（服务异常）测试通过");
    }

    @Test
    public void testGetTemplates_byKeyword_emptyKeyword() throws Exception {
        // 模拟空关键词搜索 - 当关键词为空时，会调用getTemplatesByCategory
        Page<PromptTemplate> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        
        Mockito.when(promptTemplateService.getTemplatesByCategory(null, 0, 20))
                .thenReturn(emptyPage);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList());

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.templates").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        System.out.println("✅ 按关键词获取模板（空关键词）测试通过");
    }

    @Test
    public void testGetFeaturedTemplates_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getFeaturedTemplates())
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/featured"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取精选模板失败: 服务异常"));

        System.out.println("✅ 获取推荐模板（服务异常）测试通过");
    }

    @Test
    public void testGetPopularTemplates_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getPopularTemplates())
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/popular"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取热门模板失败: 服务异常"));

        System.out.println("✅ 获取热门模板（服务异常）测试通过");
    }

    @Test
    public void testGetLatestTemplates_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getLatestTemplates())
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/latest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取最新模板失败: 服务异常"));

        System.out.println("✅ 获取最新模板（服务异常）测试通过");
    }

    @Test
    public void testGetRecommendedTemplates_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟服务异常
        Mockito.when(promptTemplateService.getRecommendedTemplates(anyString()))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/recommended")
                        .header("Authorization", "Bearer valid-token")
                        .param("aiModel", "gpt-4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取推荐模板失败: 服务异常"));

        System.out.println("✅ 获取推荐模板（服务异常）测试通过");
    }

    @Test
    public void testGetTemplateById_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(promptTemplateService.getTemplateById(1L))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取模板详情失败: 服务异常"));

        System.out.println("✅ 获取模板详情（服务异常）测试通过");
    }

    @Test
    public void testCreateTemplate_invalidToken() throws Exception {
        // 模拟无效的 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "测试模板");
        request.put("content", "这是一个测试模板");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("创建模板失败: Invalid token"));

        System.out.println("✅ 创建模板（无效Token）测试通过");
    }

    @Test
    public void testCreateTemplate_emptyTitle() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟服务层验证异常
        Mockito.when(promptTemplateService.createTemplate(any(PromptTemplate.class)))
                .thenThrow(new IllegalArgumentException("模板标题不能为空"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "");
        request.put("content", "这是一个测试模板");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("创建模板失败: 模板标题不能为空"));

        System.out.println("✅ 创建模板（空标题）测试通过");
    }

    @Test
    public void testCreateTemplate_emptyContent() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟服务层验证异常
        Mockito.when(promptTemplateService.createTemplate(any(PromptTemplate.class)))
                .thenThrow(new IllegalArgumentException("模板内容不能为空"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "测试模板");
        request.put("content", "");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("创建模板失败: 模板内容不能为空"));

        System.out.println("✅ 创建模板（空内容）测试通过");
    }

    @Test
    public void testCreateTemplate_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟服务异常
        Mockito.when(promptTemplateService.createTemplate(any(PromptTemplate.class)))
                .thenThrow(new RuntimeException("服务异常"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "测试模板");
        request.put("content", "这是一个测试模板");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("创建模板失败: 服务异常"));

        System.out.println("✅ 创建模板（服务异常）测试通过");
    }

    @Test
    public void testUpdateTemplate_invalidToken() throws Exception {
        // 模拟无效的 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "更新后的模板");
        request.put("content", "这是更新后的内容");

        mockMvc.perform(MockMvcRequestBuilders.put("/prompt-templates/1")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("更新模板失败: Invalid token"));

        System.out.println("✅ 更新模板（无效Token）测试通过");
    }

    @Test
    public void testUpdateTemplate_emptyTitle() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板（用于权限检查）
        PromptTemplate existingTemplate = createTemplate(1L, "原模板", 1L);
        existingTemplate.setCreatorId(1L);
        Mockito.when(promptTemplateService.getTemplateById(1L))
                .thenReturn(Optional.of(existingTemplate));

        // 模拟服务层验证异常
        Mockito.when(promptTemplateService.updateTemplate(anyLong(), any(PromptTemplate.class)))
                .thenThrow(new IllegalArgumentException("模板标题不能为空"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "");
        request.put("content", "这是更新后的内容");

        mockMvc.perform(MockMvcRequestBuilders.put("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("更新模板失败: 模板标题不能为空"));

        System.out.println("✅ 更新模板（空标题）测试通过");
    }

    @Test
    public void testUpdateTemplate_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板（用于权限检查）
        PromptTemplate existingTemplate = createTemplate(1L, "原模板", 1L);
        existingTemplate.setCreatorId(1L);
        Mockito.when(promptTemplateService.getTemplateById(1L))
                .thenReturn(Optional.of(existingTemplate));

        // 模拟服务异常
        Mockito.when(promptTemplateService.updateTemplate(anyLong(), any(PromptTemplate.class)))
                .thenThrow(new RuntimeException("服务异常"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "更新后的模板");
        request.put("content", "这是更新后的内容");

        mockMvc.perform(MockMvcRequestBuilders.put("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("更新模板失败: 服务异常"));

        System.out.println("✅ 更新模板（服务异常）测试通过");
    }

    @Test
    public void testDeleteTemplate_invalidToken() throws Exception {
        // 模拟无效的 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/prompt-templates/1")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("删除模板失败: Invalid token"));

        System.out.println("✅ 删除模板（无效Token）测试通过");
    }

    @Test
    public void testDeleteTemplate_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板（用于权限检查）
        PromptTemplate existingTemplate = createTemplate(1L, "原模板", 1L);
        existingTemplate.setCreatorId(1L);
        Mockito.when(promptTemplateService.getTemplateById(1L))
                .thenReturn(Optional.of(existingTemplate));

        // 模拟服务异常
        Mockito.doThrow(new RuntimeException("服务异常"))
                .when(promptTemplateService).deleteTemplate(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("删除模板失败: 服务异常"));

        System.out.println("✅ 删除模板（服务异常）测试通过");
    }

    @Test
    public void testToggleLike_invalidToken() throws Exception {
        // 模拟无效的 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates/1/like")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("点赞操作失败: Invalid token"));

        System.out.println("✅ 切换点赞（无效Token）测试通过");
    }

    @Test
    public void testToggleLike_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟服务异常
        Mockito.when(promptTemplateService.toggleLike(any(), any()))
                .thenThrow(new RuntimeException("服务异常"));

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates/1/like")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("点赞操作失败: 服务异常"));

        System.out.println("✅ 切换点赞（服务异常）测试通过");
    }

    @Test
    public void testUseTemplate_invalidToken() throws Exception {
        // 模拟无效的 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        Map<String, String> request = new HashMap<>();
        request.put("aiModel", "gpt-4");

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates/1/use")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("记录使用失败: Invalid token"));

        System.out.println("✅ 使用模板（无效Token）测试通过");
    }

    @Test
    public void testUseTemplate_serviceException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟记录使用服务异常
        Mockito.doThrow(new RuntimeException("记录使用服务异常"))
                .when(promptTemplateService).recordUsage(anyLong(), anyLong(), anyString());

        Map<String, String> request = new HashMap<>();
        request.put("aiModel", "gpt-4");

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates/1/use")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("记录使用失败: 记录使用服务异常"));

        System.out.println("✅ 使用模板（服务异常）测试通过");
    }

    @Test
    public void testCreateTemplate_specialCharacters() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟模板创建成功
        PromptTemplate createdTemplate = createTemplate(1L, "特殊字符模板", 1L);
        Mockito.when(promptTemplateService.createTemplate(any(PromptTemplate.class)))
                .thenReturn(createdTemplate);
        Mockito.when(promptTemplateService.convertToDTO(anyList()))
                .thenReturn(Arrays.asList(createTemplateDTO(1L, "特殊字符模板：!@#$%^&*()")));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "特殊字符模板：!@#$%^&*()");
        request.put("content", "包含特殊字符的内容：<script>alert('test')</script>");
        request.put("categoryId", 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("模板创建成功"));

        System.out.println("✅ 创建模板（特殊字符）测试通过");
    }

    @Test
    public void testCreateTemplate_nullPointerException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟空指针异常
        Mockito.when(promptTemplateService.createTemplate(any(PromptTemplate.class)))
                .thenThrow(new NullPointerException("模板对象为空"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "测试模板");
        request.put("content", "这是一个测试模板");

        mockMvc.perform(MockMvcRequestBuilders.post("/prompt-templates")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("创建模板失败: 模板对象为空"));

        System.out.println("✅ 创建模板（空指针异常）测试通过");
    }

    @Test
    public void testUpdateTemplate_illegalArgumentException() throws Exception {
        // 模拟 JWT Token
        Mockito.when(jwtTokenProvider.getEmailFromToken("valid-token"))
                .thenReturn("test@example.com");

        // 模拟用户
        User testUser = createUser();
        Mockito.when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // 模拟现有模板
        PromptTemplate existingTemplate = createTemplate(1L, "原模板", 1L);
        existingTemplate.setCreatorId(1L);
        Mockito.when(promptTemplateService.getTemplateById(1L))
                .thenReturn(Optional.of(existingTemplate));

        // 模拟参数异常
        Mockito.when(promptTemplateService.updateTemplate(anyLong(), any(PromptTemplate.class)))
                .thenThrow(new IllegalArgumentException("模板ID无效"));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "更新后的模板");
        request.put("content", "更新后的内容");

        mockMvc.perform(MockMvcRequestBuilders.put("/prompt-templates/1")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("更新模板失败: 模板ID无效"));

        System.out.println("✅ 更新模板（参数异常）测试通过");
    }

    @Test
    public void testGetTemplates_databaseException() throws Exception {
        // 模拟数据库异常
        Mockito.when(promptTemplateService.getTemplatesByCategory(any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("数据库连接超时"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取模板列表失败: 数据库连接超时"));

        System.out.println("✅ 获取模板列表（数据库异常）测试通过");
    }

    @Test
    public void testGetCategories_networkException() throws Exception {
        // 模拟网络异常
        Mockito.when(promptTemplateService.getAllCategories())
                .thenThrow(new RuntimeException("网络连接失败"));

        mockMvc.perform(MockMvcRequestBuilders.get("/prompt-templates/categories"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取分类失败: 网络连接失败"));

        System.out.println("✅ 获取分类（网络异常）测试通过");
    }
} 