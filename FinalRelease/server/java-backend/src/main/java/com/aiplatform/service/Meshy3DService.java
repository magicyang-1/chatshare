package com.aiplatform.service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.aiplatform.dto.AIResponseDTO;
import com.aiplatform.dto.ThreeDRecordDTO;
import com.aiplatform.entity.Chat;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.entity.User;
import com.aiplatform.repository.MessageAttachmentRepository;
import com.aiplatform.repository.ChatRepository;
import com.aiplatform.repository.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class Meshy3DService {
    private static final String API_KEY = "msy_jnQVeA6HLxoDIDLs8w8iunn887KQZTzVy7uo";
    private static final String BASE_URL = "https://api.meshy.ai/openapi/v2";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private MessageAttachmentRepository messageAttachmentRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 从JSON响应中提取result字段
     */
    private String extractResultFromResponse(String jsonResponse) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            if (jsonNode.has("result")) {
                return jsonNode.get("result").asText();
            } else {
                log.warn("响应中没有找到result字段: {}", jsonResponse);
                return jsonResponse; // 如果没有result字段，返回原始响应
            }
        } catch (Exception e) {
            log.error("解析JSON响应失败: {}", jsonResponse, e);
            throw new RuntimeException("解析响应失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建一个 Text to 3D 任务
     */
    public String createTextTo3D(String prompt, String mode, String art_style, Boolean should_remesh, Integer seed, Message message) throws Exception {
        // 清理输入参数，移除特殊字符
        String cleanPrompt = prompt != null ? prompt.replaceAll("[\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F]", " ").trim() : "";
        String cleanMode = mode != null ? mode.replaceAll("[\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F]", " ").trim() : "";
        String cleanArtStyle = art_style != null ? art_style.replaceAll("[\\u00A0\\u2000-\\u200F\\u2028-\\u202F\\u205F-\\u206F]", " ").trim() : "";

        // 使用StringBuilder构建JSON，避免格式化问题
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"prompt\":\"").append(cleanPrompt).append("\",");
        jsonBuilder.append("\"mode\":\"").append(cleanMode).append("\",");
        jsonBuilder.append("\"art_style\":\"").append(cleanArtStyle).append("\",");
        jsonBuilder.append("\"should_remesh\":").append(should_remesh != null ? should_remesh : false).append(",");
        jsonBuilder.append("\"seed\":").append(seed != null ? seed : 0);
        jsonBuilder.append("}");

        String json = jsonBuilder.toString();

        // 添加调试日志
        log.info("Generated JSON: {}", json);
        System.out.println(json);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/text-to-3d"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200 || resp.statusCode() == 202) {
            // 提取result字段
            String taskId = extractResultFromResponse(resp.body());

            // 创建并保存附件记录
            MessageAttachment attachment = new MessageAttachment();
            attachment.setMessageId(message.getId());
            attachment.setFileName("preview_" + art_style+ '_' + UUID.randomUUID().toString() + "task");
            attachment.setFilePath(taskId); // 使用taskId作为文件路径
            attachment.setMimeType("3d/preview");
            attachment.setAttachmentType(MessageAttachment.AttachmentType.OTHER);
            attachment.setFileSize(0L);
            attachment.setCreatedAt(LocalDateTime.now());

            MessageAttachment savedAttachment = messageAttachmentRepository.save(attachment);
            log.info("文生3d成功生成并保存: {}", taskId);
            return resp.body();
        }
             else {
                throw new RuntimeException("创建任务失败：" + resp.statusCode() + " / " + resp.body());
            }
        }

    public String refineTextTo3D(String taskId,String prompt,Message message) throws Exception {
        String json = """
            {
                "mode": "refine",
                "preview_task_id": "%s",
                "prompt": "%s"
            }
            """.formatted(taskId,prompt);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/text-to-3d"))
            .header("Authorization", "Bearer " + API_KEY)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200 || resp.statusCode() == 202) {
            // 提取result字段
            String taskId2 = extractResultFromResponse(resp.body());

            // 创建并保存附件记录
            MessageAttachment attachment = new MessageAttachment();
            attachment.setMessageId(message.getId());
            attachment.setFileName("refine_" + "refine_" + UUID.randomUUID().toString() + "task");
            attachment.setFilePath(taskId2); // 使用taskId作为文件路径
            attachment.setMimeType("3d/refine");
            attachment.setAttachmentType(MessageAttachment.AttachmentType.OTHER);
            attachment.setFileSize(0L);
            attachment.setCreatedAt(LocalDateTime.now());

            MessageAttachment savedAttachment = messageAttachmentRepository.save(attachment);
            log.info("文生3d成功精炼并保存: {}", taskId2);
            return resp.body();
        } else {
            throw new RuntimeException("细化任务失败：" + resp.statusCode() + " / " + resp.body());
        }
    }
    /**
     * 查询TextTo3D任务
     */
    public String getTextTo3DStatus(String taskId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/text-to-3d/" + taskId))
            .header("Authorization", "Bearer " + API_KEY)
            .GET()
            .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200 || resp.statusCode() == 202) {
            return resp.body();
        } else {
            throw new RuntimeException("查询任务失败：" + resp.statusCode());
        }
    }

    //搜索历史
    public List<ThreeDRecordDTO> searchHistory(Long id) throws Exception {
        List<Chat> chats = chatRepository.findByUserIdOrderByLastActivityDesc(id);
        if (chats.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ThreeDRecordDTO> history = new ArrayList<>();
        
        for (Chat chat : chats) {
            List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chat.getId());
            
            for (Message message : messages) {
                List<MessageAttachment> attachments = messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(message.getId());
                
                for(MessageAttachment attachment : attachments) {
                    if(!attachment.getMimeType().equals("3d/preview") && !attachment.getMimeType().equals("3d/refine")) {
                        continue;
                    }
                    
                    ThreeDRecordDTO threeDRecordDTO = new ThreeDRecordDTO();
                    //唯一标识
                    threeDRecordDTO.setId(attachment.getFilePath());
                    //提示词
                    threeDRecordDTO.setPrompt(message.getContent());
                    //preview or refine
                    String mode = attachment.getMimeType().split("/")[1];
                    String art_style = attachment.getFileName().split("_")[1];
                    LocalDateTime created_at = attachment.getCreatedAt();
                    if(mode.equals("preview")) {
                        mode = "preview";
                    } else if(mode.equals("refine")) {
                        mode = "refine";
                    }
                    threeDRecordDTO.setMode(mode);
                    threeDRecordDTO.setArt_style(art_style);
                    threeDRecordDTO.setCreated_at(created_at);
                    history.add(threeDRecordDTO);
                }
            }
        }
        
        return history;
    }

}
