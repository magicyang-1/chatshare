package com.aiplatform.controller;

import com.aiplatform.entity.PromptCategory;
import com.aiplatform.entity.PromptTemplate;
import com.aiplatform.entity.User;
import com.aiplatform.dto.PromptTemplateDTO;
import com.aiplatform.security.JwtTokenProvider;
import com.aiplatform.service.PromptTemplateService;
import com.aiplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/prompt-templates")
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            List<PromptCategory> categories = promptTemplateService.getAllCategories();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取分类失败: " + e.getMessage())
            );
        }
    }

    /**
     * 获取模板列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTemplates(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        try {
            // 获取用户ID（如果提供了认证头）
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    log.info("解析token: {}", token.substring(0, Math.min(20, token.length())) + "...");
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    log.info("从token获取到邮箱: {}", email);
                    Optional<User> userOpt = userService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getId();
                        log.info("找到用户ID: {}", userId);
                    } else {
                        log.warn("未找到用户: {}", email);
                    }
                } catch (Exception e) {
                    log.warn("解析用户token失败，将以游客身份获取模板: {}", e.getMessage());
                }
            } else {
                log.info("未提供认证头，将以游客身份获取模板");
            }
            
            Page<PromptTemplate> templates;
            if (categoryId != null && categoryId > 0) {
                templates = promptTemplateService.getTemplatesByCategory(categoryId, page, size);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                templates = promptTemplateService.searchTemplates(keyword, categoryId, page, size);
            } else if ("OFFICIAL".equals(type)) {
                templates = promptTemplateService.getOfficialTemplates(page, size);
            } else if ("USER".equals(type)) {
                templates = promptTemplateService.getUserTemplates(page, size);
            } else {
                templates = promptTemplateService.getTemplatesByCategory(null, page, size);
            }
            
            // 使用包含用户ID的convertToDTO方法
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(templates.getContent(), userId);
            
            // 添加调试日志
            log.info("返回模板数量: {}, 用户ID: {}", templateDTOs.size(), userId);
            templateDTOs.forEach(dto -> {
                log.info("模板 {} ({}): liked={}, likeCount={}", dto.getId(), dto.getTitle(), dto.getLiked(), dto.getLikeCount());
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templates", templateDTOs);
            response.put("totalPages", templates.getTotalPages());
            response.put("totalElements", templates.getTotalElements());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取模板列表失败: " + e.getMessage())
            );
        }
    }

    /**
     * 获取精选模板
     */
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedTemplates(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 获取用户ID（如果提供了认证头）
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    log.info("精选模板 - 解析token: {}", token.substring(0, Math.min(20, token.length())) + "...");
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    log.info("精选模板 - 从token获取到邮箱: {}", email);
                    Optional<User> userOpt = userService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getId();
                        log.info("精选模板 - 找到用户ID: {}", userId);
                    } else {
                        log.warn("精选模板 - 未找到用户: {}", email);
                    }
                } catch (Exception e) {
                    log.warn("精选模板 - 解析用户token失败，将以游客身份获取精选模板: {}", e.getMessage());
                }
            } else {
                log.info("精选模板 - 未提供认证头，将以游客身份获取精选模板");
            }
            
            List<PromptTemplate> templates = promptTemplateService.getFeaturedTemplates();
            // 使用包含用户ID的convertToDTO方法
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(templates, userId);
            
            // 添加调试日志
            log.info("精选模板 - 返回模板数量: {}, 用户ID: {}", templateDTOs.size(), userId);
            templateDTOs.forEach(dto -> {
                log.info("精选模板 {} ({}): liked={}, likeCount={}", dto.getId(), dto.getTitle(), dto.getLiked(), dto.getLikeCount());
            });
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templates", templateDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取精选模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取精选模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 获取热门模板
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularTemplates(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 获取用户ID（如果提供了认证头）
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    Optional<User> userOpt = userService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getId();
                    }
                } catch (Exception e) {
                    log.warn("解析用户token失败，将以游客身份获取热门模板: {}", e.getMessage());
                }
            }
            
            List<PromptTemplate> templates = promptTemplateService.getPopularTemplates();
            // 使用包含用户ID的convertToDTO方法
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(templates, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templates", templateDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取热门模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取热门模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 获取最新模板
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestTemplates(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 获取用户ID（如果提供了认证头）
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    Optional<User> userOpt = userService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getId();
                    }
                } catch (Exception e) {
                    log.warn("解析用户token失败，将以游客身份获取最新模板: {}", e.getMessage());
                }
            }
            
            List<PromptTemplate> templates = promptTemplateService.getLatestTemplates();
            // 使用包含用户ID的convertToDTO方法
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(templates, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templates", templateDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最新模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取最新模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 根据AI模型推荐模板
     */
    @GetMapping("/recommended")
    public ResponseEntity<Map<String, Object>> getRecommendedTemplates(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("aiModel") String aiModel) {
        try {
            // 获取用户ID（如果提供了认证头）
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    Optional<User> userOpt = userService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getId();
                    }
                } catch (Exception e) {
                    log.warn("解析用户token失败，将以游客身份获取推荐模板: {}", e.getMessage());
                }
            }
            
            List<PromptTemplate> templates = promptTemplateService.getRecommendedTemplates(aiModel);
            // 使用包含用户ID的convertToDTO方法
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(templates, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templates", templateDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取推荐模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取推荐模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        try {
            // 获取用户ID（如果提供了认证头）
            Long userId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    Optional<User> userOpt = userService.findByEmail(email);
                    if (userOpt.isPresent()) {
                        userId = userOpt.get().getId();
                    }
                } catch (Exception e) {
                    log.warn("解析用户token失败，将以游客身份获取模板详情: {}", e.getMessage());
                }
            }
            
            Optional<PromptTemplate> template = promptTemplateService.getTemplateById(id);
            if (template.isPresent()) {
                // 使用包含用户ID的convertToDTO方法
                List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(List.of(template.get()), userId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("template", templateDTOs.get(0));
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取模板详情失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "获取模板详情失败: " + e.getMessage())
            );
        }
    }

    /**
     * 创建模板
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PromptTemplate template) {
        
        try {
            // 验证token并获取用户信息
            String token = authHeader.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "用户不存在")
                );
            }
            
            User user = userOpt.get();
            
            // 设置创建者信息
            template.setCreatorId(user.getId());
            template.setCreatorName(user.getUsername());
            template.setTemplateType(PromptTemplate.TemplateType.USER);
            
            PromptTemplate savedTemplate = promptTemplateService.createTemplate(template);
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(List.of(savedTemplate));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "模板创建成功");
            response.put("template", templateDTOs.get(0));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "创建模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody PromptTemplate template) {
        
        try {
            // 验证token并获取用户信息
            String token = authHeader.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "用户不存在")
                );
            }
            
            User user = userOpt.get();
            
            // 检查权限
            Optional<PromptTemplate> existingTemplate = promptTemplateService.getTemplateById(id);
            if (existingTemplate.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            PromptTemplate existing = existingTemplate.get();
            if (!existing.getCreatorId().equals(user.getId()) && !"admin".equals(user.getRole())) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "没有权限修改此模板")
                );
            }
            
            PromptTemplate updatedTemplate = promptTemplateService.updateTemplate(id, template);
            List<PromptTemplateDTO> templateDTOs = promptTemplateService.convertToDTO(List.of(updatedTemplate));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "模板更新成功");
            response.put("template", templateDTOs.get(0));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "更新模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        
        try {
            // 验证token并获取用户信息
            String token = authHeader.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "用户不存在")
                );
            }
            
            User user = userOpt.get();
            
            // 检查权限
            Optional<PromptTemplate> existingTemplate = promptTemplateService.getTemplateById(id);
            if (existingTemplate.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            PromptTemplate existing = existingTemplate.get();
            if (!existing.getCreatorId().equals(user.getId()) && !"admin".equals(user.getRole())) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "没有权限删除此模板")
                );
            }
            
            promptTemplateService.deleteTemplate(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "模板删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除模板失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "删除模板失败: " + e.getMessage())
            );
        }
    }

    /**
     * 点赞/取消点赞模板
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        
        try {
            // 验证token并获取用户信息
            String token = authHeader.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "用户不存在")
                );
            }
            
            User user = userOpt.get();
            
            boolean isLiked = promptTemplateService.toggleLike(id, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("liked", isLiked);
            response.put("message", isLiked ? "点赞成功" : "取消点赞成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "点赞操作失败: " + e.getMessage())
            );
        }
    }

    /**
     * 使用模板
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<Map<String, Object>> useTemplate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        try {
            // 验证token并获取用户信息
            String token = authHeader.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(token);
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "用户不存在")
                );
            }
            
            User user = userOpt.get();
            
            String aiModel = request.get("aiModel");
            promptTemplateService.recordUsage(id, user.getId(), aiModel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "使用记录已保存");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("记录使用失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "记录使用失败: " + e.getMessage())
            );
        }
    }
} 