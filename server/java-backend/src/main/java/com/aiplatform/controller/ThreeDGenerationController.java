package com.aiplatform.controller;

import com.aiplatform.dto.ThreeDRecordDTO;
import com.aiplatform.entity.Chat;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.entity.User;
import com.aiplatform.service.ChatService;
import com.aiplatform.service.DashScopeImageService;
import com.aiplatform.service.Meshy3DService;
import com.aiplatform.service.MessageService;
import com.aiplatform.service.UserService;
import com.aiplatform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/3d")
@RequiredArgsConstructor
@Slf4j
public class ThreeDGenerationController {

    private final Meshy3DService meshy3DService;
    private final MessageService messageService;
    private final ChatService chatService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    /**
     * 文生3d接口
     */
    @PostMapping("/text-to-3d")
    public ResponseEntity<Map<String, Object>> generateTextTo3D(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        
        try {
            // 验证token
            String token = authHeader.substring(7);
            String username = jwtTokenProvider.getEmailFromToken(token);
            
            // 检查管理员权限
            User user = userService.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (user.getRole() != User.UserRole.admin) {
                return ResponseEntity.status(403).body(
                    Map.of("error", "权限不足：文生3D功能仅对管理员开放")
                );
            }
            // 获取参数
            String prompt = (String) request.get("prompt");
            String mode = (String) request.get("mode");
            String art_style = (String) request.get("art_style");
            Boolean should_remesh = (Boolean) request.get("should_remesh");
            Integer seed = (Integer) request.get("seed");
            // Long chatId = Long.valueOf(request.get("chatId").toString());

            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "提示词不能为空")
                );
            }
            if (mode == null || mode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "模式不能为空")
                );
            }
            // 创建聊天
                Chat chat = chatService.createChat(user.getId(), "智能生3d", Chat.AiType.text_to_3d);
                log.info("为用户 {} 创建新的3D生成聊天会话: {}", username, chat.getId());

            // 创建消息
            Message message = new Message();
            message.setChatId(chat.getId());
            message.setContent("【3d生成】" + prompt);
            message.setRole(Message.MessageRole.user);
            message = messageService.saveMessage(message);
            
            String taskResponse = meshy3DService.createTextTo3D(prompt, mode, art_style, should_remesh, seed, message);
            
            // 解析Meshy API返回的JSON字符串
            // 格式: {"result": "018a210d-8ba4-705c-b111-1f1776f7f578"}
            String taskId = null;
            try {
                Map<String, Object> json = objectMapper.readValue(taskResponse, Map.class);
                taskId = (String) json.get("result");
            } catch (Exception e) {
                log.error("解析任务ID失败", e);
                return ResponseEntity.badRequest().body(
                    Map.of("error", "解析任务ID失败: " + e.getMessage())
                );
            }
            
            if (taskId == null) {
                log.error("无法解析任务ID，原始响应: {}", taskResponse);
                return ResponseEntity.badRequest().body(
                    Map.of("error", "创建任务失败：无法获取任务ID")
                );
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "3D生成成功");
            response.put("taskId", taskId); // 直接返回taskId，不使用嵌套结构
            log.info("3D生成成功: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("文生3D失败", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", "3D生成失败: " + e.getMessage())
            );
        }
    }

    @GetMapping("/get-text-to-3d-status/{taskId}")
    public ResponseEntity<Map<String, Object>> getTextTo3DStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String taskId) {
        try {
            String status = meshy3DService.getTextTo3DStatus(taskId);
            return ResponseEntity.ok(Map.of("status", status));
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "查询任务状态失败: " + e.getMessage()));
        }
    }
    @PostMapping("/text-to-3d-refine")
    public ResponseEntity<Map<String, Object>> refineTextTo3D(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        try {
            String taskId = (String) request.get("taskId");
            String prompt = (String) request.get("prompt");
            // 验证token
            String token = authHeader.substring(7);
            String username = jwtTokenProvider.getEmailFromToken(token);
            // 创建聊天
                Chat chat = chatService.createChat(userService.findByEmail(username).orElseThrow(() -> new RuntimeException("用户不存在")).getId(), "智能生3d", Chat.AiType.text_to_3d);
                log.info("为用户 {} 创建新的3D生成聊天会话: {}", username, chat.getId());

            // 创建消息
            Message message = new Message();
            message.setChatId(chat.getId());
            message.setContent("【3d精炼】" + prompt);
            message.setRole(Message.MessageRole.user);
            message = messageService.saveMessage(message);

            String status = meshy3DService.refineTextTo3D(taskId, prompt, message);
            log.info("3D精炼状态: {}", status);

            String taskId2 = null;
            try {
                Map<String, Object> json = objectMapper.readValue(status, Map.class);
                taskId2 = (String) json.get("result");
            } catch (Exception e) {
                log.error("解析任务ID失败", e);
                return ResponseEntity.badRequest().body(
                    Map.of("error", "解析任务ID失败: " + e.getMessage())
                );
            }
            
            if (taskId == null) {
                log.error("无法解析任务ID，原始响应: {}", status);
                return ResponseEntity.badRequest().body(
                    Map.of("error", "创建任务失败：无法获取任务ID")
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "3D精炼成功");
            response.put("taskId", taskId2); // 直接返回taskId，不使用嵌套结构
            log.info("3D精炼成功: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("细化任务失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "细化任务失败: " + e.getMessage()));
        }
    }
    //搜索历史
    @GetMapping("/search-history")
    public ResponseEntity<Map<String, Object>> searchHistory(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String username = jwtTokenProvider.getEmailFromToken(token);
            User user = userService.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Long id = user.getId();
            List<ThreeDRecordDTO> history = meshy3DService.searchHistory(id);
            return ResponseEntity.ok(Map.of("history", history));
        } catch (Exception e) {
            log.error("搜索历史失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", "搜索历史失败: " + e.getMessage()));
        }
    }
}