package com.aiplatform.service;

import com.aiplatform.dto.PromptTemplateDTO;
import com.aiplatform.entity.PromptTemplate;
import com.aiplatform.entity.PromptCategory;
import com.aiplatform.entity.PromptLike;
import com.aiplatform.entity.PromptUsageStats;
import com.aiplatform.entity.User;
import com.aiplatform.exception.BusinessException;
import com.aiplatform.repository.PromptCategoryRepository;
import com.aiplatform.repository.PromptTemplateRepository;
import com.aiplatform.repository.PromptLikeRepository;
import com.aiplatform.repository.PromptUsageStatsRepository;
import com.aiplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptTemplateServiceTest {

    @Mock
    private PromptTemplateRepository promptTemplateRepository;

    @Mock
    private PromptCategoryRepository promptCategoryRepository;

    @Mock
    private PromptLikeRepository promptLikeRepository;

    @Mock
    private PromptUsageStatsRepository promptUsageStatsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PromptTemplateService promptTemplateService;

    private User testUser;
    private PromptTemplate testTemplate;
    private PromptCategory testCategory;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        // 创建测试分类
        testCategory = new PromptCategory();
        testCategory.setId(1L);
        testCategory.setName("测试分类");
        testCategory.setDescription("测试分类描述");

        // 创建测试模板
        testTemplate = new PromptTemplate();
        testTemplate.setId(1L);
        testTemplate.setTitle("测试模板");
        testTemplate.setContent("测试内容");
        testTemplate.setDescription("测试描述");
        testTemplate.setCategoryId(1L);
        testTemplate.setCreatorId(1L);
        testTemplate.setIsPublic(true);
        testTemplate.setIsFeatured(false);
        testTemplate.setLikeCount(0);
        testTemplate.setUsageCount(0);
        testTemplate.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllCategories() {
        // 准备mock数据
        when(promptCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(Arrays.asList(testCategory));

        // 执行测试
        List<PromptCategory> result = promptTemplateService.getAllCategories();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory.getId(), result.get(0).getId());

        // 验证调用
        verify(promptCategoryRepository).findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Test
    void testCreateTemplate_Success() {
        // 准备mock数据
        when(promptTemplateRepository.save(any(PromptTemplate.class))).thenReturn(testTemplate);

        // 执行测试
        PromptTemplate result = promptTemplateService.createTemplate(testTemplate);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTemplate.getId(), result.getId());

        // 验证调用
        verify(promptTemplateRepository).save(any(PromptTemplate.class));
    }

    @Test
    void testGetTemplateById_Success() {
        // 准备mock数据
        when(promptTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // 执行测试
        Optional<PromptTemplate> result = promptTemplateService.getTemplateById(1L);

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(testTemplate.getId(), result.get().getId());

        // 验证调用
        verify(promptTemplateRepository).findById(1L);
    }

    @Test
    void testGetFeaturedTemplates() {
        // 准备mock数据
        when(promptTemplateRepository.findByIsFeaturedTrueAndIsPublicTrueOrderByUsageCountDescCreatedAtDesc())
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        List<PromptTemplate> result = promptTemplateService.getFeaturedTemplates();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTemplate.getId(), result.get(0).getId());

        // 验证调用
        verify(promptTemplateRepository).findByIsFeaturedTrueAndIsPublicTrueOrderByUsageCountDescCreatedAtDesc();
    }

    @Test
    void testToggleLike_Success_AddLike() {
        // 准备mock数据 - 用户未点赞
        when(promptLikeRepository.findByTemplateIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(promptLikeRepository.save(any(PromptLike.class))).thenReturn(new PromptLike());
        when(promptTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(promptTemplateRepository.save(any(PromptTemplate.class))).thenReturn(testTemplate);
        when(promptLikeRepository.countByTemplateId(1L)).thenReturn(1);

        // 执行测试
        boolean result = promptTemplateService.toggleLike(1L, 1L);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(promptLikeRepository).findByTemplateIdAndUserId(1L, 1L);
        verify(promptLikeRepository).save(any(PromptLike.class));
        verify(promptTemplateRepository).findById(1L);
        verify(promptTemplateRepository).save(any(PromptTemplate.class));
        verify(promptLikeRepository).countByTemplateId(1L);
    }

    @Test
    void testToggleLike_Success_RemoveLike() {
        // 准备mock数据 - 用户已点赞
        PromptLike existingLike = new PromptLike();
        existingLike.setTemplateId(1L);
        existingLike.setUserId(1L);
        
        when(promptLikeRepository.findByTemplateIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingLike));
        when(promptTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(promptTemplateRepository.save(any(PromptTemplate.class))).thenReturn(testTemplate);
        when(promptLikeRepository.countByTemplateId(1L)).thenReturn(0);

        // 执行测试
        boolean result = promptTemplateService.toggleLike(1L, 1L);

        // 验证结果
        assertFalse(result);

        // 验证调用
        verify(promptLikeRepository).findByTemplateIdAndUserId(1L, 1L);
        verify(promptLikeRepository).delete(existingLike);
        verify(promptTemplateRepository).findById(1L);
        verify(promptTemplateRepository).save(any(PromptTemplate.class));
        verify(promptLikeRepository).countByTemplateId(1L);
    }

    @Test
    void testDeleteTemplate_Success() {
        // 执行测试
        promptTemplateService.deleteTemplate(1L);

        // 验证调用 - deleteTemplate方法只调用deleteById，不调用findById
        verify(promptTemplateRepository).deleteById(1L);
    }

    @Test
    void testUpdateTemplate_Success() {
        // 准备mock数据
        when(promptTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(promptTemplateRepository.save(any(PromptTemplate.class))).thenReturn(testTemplate);

        // 执行测试
        PromptTemplate result = promptTemplateService.updateTemplate(1L, testTemplate);

        // 验证结果
        assertNotNull(result);

        // 验证调用
        verify(promptTemplateRepository).findById(1L);
        verify(promptTemplateRepository).save(any(PromptTemplate.class));
    }

    @Test
    void testUpdateTemplate_NotFound() {
        // 准备mock数据
        when(promptTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            promptTemplateService.updateTemplate(999L, testTemplate);
        });

        // 验证调用
        verify(promptTemplateRepository).findById(999L);
        verify(promptTemplateRepository, never()).save(any(PromptTemplate.class));
    }

    @Test
    void testGetTemplateById_NotFound() {
        // 准备mock数据
        when(promptTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试
        Optional<PromptTemplate> result = promptTemplateService.getTemplateById(999L);

        // 验证结果
        assertFalse(result.isPresent());

        // 验证调用
        verify(promptTemplateRepository).findById(999L);
    }

    @Test
    void testIsLikedByUser() {
        // 准备mock数据
        when(promptLikeRepository.existsByTemplateIdAndUserId(1L, 1L)).thenReturn(true);

        // 执行测试
        boolean result = promptTemplateService.isLikedByUser(1L, 1L);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(promptLikeRepository).existsByTemplateIdAndUserId(1L, 1L);
    }

    @Test
    void testRecordUsage() {
        // 准备mock数据
        when(promptTemplateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(promptTemplateRepository.save(any(PromptTemplate.class))).thenReturn(testTemplate);
        when(promptUsageStatsRepository.countByTemplateId(1L)).thenReturn(5);

        // 执行测试
        promptTemplateService.recordUsage(1L, 1L, "gpt-3.5-turbo");

        // 验证调用
        verify(promptUsageStatsRepository).save(any(PromptUsageStats.class));
        verify(promptTemplateRepository).findById(1L);
        verify(promptTemplateRepository).save(any(PromptTemplate.class));
        verify(promptUsageStatsRepository).countByTemplateId(1L);
    }

    @Test
    void testGetUserLikedTemplates() {
        // 准备mock数据
        List<Long> likedTemplateIds = Arrays.asList(1L, 2L, 3L);
        when(promptLikeRepository.findTemplateIdsByUserId(1L)).thenReturn(likedTemplateIds);

        // 执行测试
        List<Long> result = promptTemplateService.getUserLikedTemplates(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(likedTemplateIds, result);

        // 验证调用
        verify(promptLikeRepository).findTemplateIdsByUserId(1L);
    }

    @Test
    void testGetPopularTemplates() {
        // 准备mock数据
        when(promptTemplateRepository.findTop10ByIsPublicTrueOrderByUsageCountDesc())
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        List<PromptTemplate> result = promptTemplateService.getPopularTemplates();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTemplate.getId(), result.get(0).getId());

        // 验证调用
        verify(promptTemplateRepository).findTop10ByIsPublicTrueOrderByUsageCountDesc();
    }

    @Test
    void testGetLatestTemplates() {
        // 准备mock数据
        when(promptTemplateRepository.findTop10ByIsPublicTrueOrderByCreatedAtDesc())
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        List<PromptTemplate> result = promptTemplateService.getLatestTemplates();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTemplate.getId(), result.get(0).getId());

        // 验证调用
        verify(promptTemplateRepository).findTop10ByIsPublicTrueOrderByCreatedAtDesc();
    }

    @Test
    void testGetRecommendedTemplates() {
        // 准备mock数据
        when(promptTemplateRepository.findByAiModelAndIsPublicTrueOrderByUsageCountDescCreatedAtDesc("gpt-3.5-turbo"))
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        List<PromptTemplate> result = promptTemplateService.getRecommendedTemplates("gpt-3.5-turbo");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTemplate.getId(), result.get(0).getId());

        // 验证调用
        verify(promptTemplateRepository).findByAiModelAndIsPublicTrueOrderByUsageCountDescCreatedAtDesc("gpt-3.5-turbo");
    }

    @Test
    void testConvertToDTO() {
        // 准备mock数据
        List<PromptTemplate> templates = Arrays.asList(testTemplate);
        List<PromptCategory> categories = Arrays.asList(testCategory);
        List<Long> likedTemplateIds = Arrays.asList(1L);
        
        when(promptCategoryRepository.findAll()).thenReturn(categories);
        when(promptLikeRepository.findTemplateIdsByUserId(1L)).thenReturn(likedTemplateIds);

        // 执行测试
        List<PromptTemplateDTO> result = promptTemplateService.convertToDTO(templates, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证调用
        verify(promptCategoryRepository).findAll();
        verify(promptLikeRepository).findTemplateIdsByUserId(1L);
    }

    @Test
    void testConvertToDTO_WithoutUserId() {
        // 准备mock数据
        List<PromptTemplate> templates = Arrays.asList(testTemplate);
        List<PromptCategory> categories = Arrays.asList(testCategory);
        
        when(promptCategoryRepository.findAll()).thenReturn(categories);

        // 执行测试
        List<PromptTemplateDTO> result = promptTemplateService.convertToDTO(templates);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证调用
        verify(promptCategoryRepository).findAll();
        verify(promptLikeRepository, never()).findTemplateIdsByUserId(any());
    }
} 