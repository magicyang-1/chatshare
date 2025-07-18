package com.aiplatform.controller;

import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.service.FileUploadService;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        FileController.class, 
        FileControllerTest.TestConfig.class,
        FileControllerTest.TestSecurityConfig.class
    }
)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=false"
})
public class FileControllerTest {

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
                    .requestMatchers("/files/cleanup").hasRole("admin")
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileUploadService fileUploadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBasicConfiguration() {
        System.out.println("✅ FileControllerTest 基础配置测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_success() throws Exception {
        // 模拟文件上传服务
        MessageAttachment attachment = createMessageAttachment();
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenReturn(attachment);

        // 创建模拟文件
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test-image.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file)
                        .param("messageId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传成功"))
                .andExpect(jsonPath("$.fileId").value(1))
                .andExpect(jsonPath("$.fileUrl").value("/api/files/test-image.jpg"))
                .andExpect(jsonPath("$.data.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.data.originalName").value("original-image.jpg"))
                .andExpect(jsonPath("$.data.fileSize").value(1024))
                .andExpect(jsonPath("$.data.mimeType").value("image/jpeg"))
                .andExpect(jsonPath("$.data.isImage").value(true));

        System.out.println("✅ 文件上传测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_emptyFile() throws Exception {
        // 创建空文件
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "empty.txt", 
            "text/plain", 
            new byte[0]
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件不能为空"));

        System.out.println("✅ 文件上传（空文件）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_serviceException() throws Exception {
        // 模拟服务异常
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenThrow(new RuntimeException("文件上传失败"));

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        System.out.println("✅ 文件上传（服务异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFile_success() throws Exception {
        // 模拟文件信息
        MessageAttachment attachment = createMessageAttachment();
        Mockito.when(fileUploadService.getFileInfo("test-image.jpg"))
                .thenReturn(attachment);
        Mockito.when(fileUploadService.getFileContent("test-image.jpg"))
                .thenReturn("test image content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.get("/files/test-image.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test-image.jpg\""));

        System.out.println("✅ 获取文件测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFile_notFound() throws Exception {
        // 模拟文件不存在 - 使用 BusinessException 来获得 404 状态码
        Mockito.when(fileUploadService.getFileInfo("nonexistent.jpg"))
                .thenThrow(new com.aiplatform.exception.BusinessException("文件不存在"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files/nonexistent.jpg"))
                .andExpect(status().isNotFound());

        System.out.println("✅ 获取文件（不存在）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testDeleteFile_success() throws Exception {
        // 模拟删除成功
        Mockito.doNothing().when(fileUploadService).deleteFile("test-image.jpg");

        mockMvc.perform(MockMvcRequestBuilders.delete("/files/test-image.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件删除成功"));

        System.out.println("✅ 删除文件测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testDeleteFile_notFound() throws Exception {
        // 模拟删除失败
        Mockito.doThrow(new com.aiplatform.exception.BusinessException("文件不存在"))
                .when(fileUploadService).deleteFile("nonexistent.jpg");

        mockMvc.perform(MockMvcRequestBuilders.delete("/files/nonexistent.jpg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        System.out.println("✅ 删除文件（不存在）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFileBase64_success() throws Exception {
        // 模拟Base64转换
        Mockito.when(fileUploadService.imageToBase64("test-image.jpg"))
                .thenReturn("data:image/jpeg;base64,dGVzdCBpbWFnZQ==");

        mockMvc.perform(MockMvcRequestBuilders.get("/files/test-image.jpg/base64"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("test-image.jpg"))
                .andExpect(jsonPath("$.data.base64").value("data:image/jpeg;base64,dGVzdCBpbWFnZQ=="));

        System.out.println("✅ 获取文件Base64测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFileBase64_notFound() throws Exception {
        // 模拟文件不存在
        Mockito.when(fileUploadService.imageToBase64("nonexistent.jpg"))
                .thenThrow(new com.aiplatform.exception.BusinessException("文件不存在"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files/nonexistent.jpg/base64"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        System.out.println("✅ 获取文件Base64（不存在）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "admin@example.com", roles = {"admin"})
    public void testCleanupOrphanedFiles_success() throws Exception {
        // 模拟清理成功
        Mockito.doNothing().when(fileUploadService).cleanOrphanedFiles();

        mockMvc.perform(MockMvcRequestBuilders.post("/files/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("孤立文件清理完成"));

        System.out.println("✅ 清理孤立文件测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "admin@example.com", roles = {"admin"})
    public void testCleanupOrphanedFiles_exception() throws Exception {
        // 模拟清理失败
        Mockito.doThrow(new RuntimeException("清理失败"))
                .when(fileUploadService).cleanOrphanedFiles();

        mockMvc.perform(MockMvcRequestBuilders.post("/files/cleanup"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        System.out.println("✅ 清理孤立文件（异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testCleanupOrphanedFiles_unauthorized() throws Exception {
        // 普通用户无权访问管理员功能
        mockMvc.perform(MockMvcRequestBuilders.post("/files/cleanup"))
                .andExpect(status().isForbidden());

        System.out.println("✅ 清理孤立文件（无权限）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_businessException() throws Exception {
        // 模拟业务异常
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenThrow(new com.aiplatform.exception.BusinessException("不支持的文件格式"));

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不支持的文件格式"));

        System.out.println("✅ 文件上传（业务异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_ioException() throws Exception {
        // 模拟IO异常
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenThrow(new java.io.IOException("磁盘空间不足"));

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件上传失败：磁盘空间不足"));

        System.out.println("✅ 文件上传（IO异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_largeFile() throws Exception {
        // 模拟大文件上传
        MessageAttachment attachment = createMessageAttachment();
        attachment.setFileSize(10 * 1024 * 1024L); // 10MB
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenReturn(attachment);

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "large-file.jpg", 
            "image/jpeg", 
            "large file content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileSize").value(10 * 1024 * 1024));

        System.out.println("✅ 文件上传（大文件）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_withoutMessageId() throws Exception {
        // 模拟文件上传（无消息ID）
        MessageAttachment attachment = createMessageAttachment();
        Mockito.when(fileUploadService.uploadFile(any(), isNull()))
                .thenReturn(attachment);

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文件上传成功"));

        System.out.println("✅ 文件上传（无消息ID）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFile_ioException() throws Exception {
        // 模拟文件信息获取成功但内容获取失败
        MessageAttachment attachment = createMessageAttachment();
        Mockito.when(fileUploadService.getFileInfo("test.jpg"))
                .thenReturn(attachment);
        Mockito.when(fileUploadService.getFileContent("test.jpg"))
                .thenThrow(new java.io.IOException("文件读取失败"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files/test.jpg"))
                .andExpect(status().isInternalServerError());

        System.out.println("✅ 获取文件（IO异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFile_nonImageFile() throws Exception {
        // 模拟非图片文件
        MessageAttachment attachment = createMessageAttachment();
        attachment.setMimeType("application/pdf");
        attachment.setAttachmentType(MessageAttachment.AttachmentType.DOCUMENT);
        Mockito.when(fileUploadService.getFileInfo("test.pdf"))
                .thenReturn(attachment);
        Mockito.when(fileUploadService.getFileContent("test.pdf"))
                .thenReturn("PDF content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.get("/files/test.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""));

        System.out.println("✅ 获取文件（非图片文件）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFile_unknownMimeType() throws Exception {
        // 模拟未知MIME类型
        MessageAttachment attachment = createMessageAttachment();
        attachment.setMimeType(null);
        Mockito.when(fileUploadService.getFileInfo("test.xyz"))
                .thenReturn(attachment);
        Mockito.when(fileUploadService.getFileContent("test.xyz"))
                .thenReturn("unknown content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.get("/files/test.xyz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"));

        System.out.println("✅ 获取文件（未知MIME类型）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testDeleteFile_ioException() throws Exception {
        // 模拟删除IO异常
        Mockito.doThrow(new java.io.IOException("文件删除失败"))
                .when(fileUploadService).deleteFile("test.jpg");

        mockMvc.perform(MockMvcRequestBuilders.delete("/files/test.jpg"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件删除失败：文件删除失败"));

        System.out.println("✅ 删除文件（IO异常）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFileBase64_ioException() throws Exception {
        // 模拟Base64转换IO异常
        Mockito.when(fileUploadService.imageToBase64("test.jpg"))
                .thenThrow(new java.io.IOException("文件读取失败"));

        mockMvc.perform(MockMvcRequestBuilders.get("/files/test.jpg/base64"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取文件失败：文件读取失败"));

        System.out.println("✅ 获取文件Base64（IO异常）测试通过");
    }

//    @Test
//    @org.springframework.security.test.context.support.WithMockUser(username = "admin@example.com", roles = {"admin"})
//    public void testCleanupOrphanedFiles_ioException() throws Exception {
//        // 模拟清理IO异常
//        Mockito.doThrow(new java.io.IOException("清理失败"))
//                .when(fileUploadService).cleanOrphanedFiles();
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/files/cleanup"))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").value("文件清理失败：清理失败"));
//
//        System.out.println("✅ 清理孤立文件（IO异常）测试通过");
//    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_specialCharacters() throws Exception {
        // 模拟包含特殊字符的文件名
        MessageAttachment attachment = createMessageAttachment();
        attachment.setOriginalName("测试文件@#$%.jpg");
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenReturn(attachment);

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "测试文件@#$%.jpg", 
            "image/jpeg", 
            "test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.originalName").value("测试文件@#$%.jpg"));

        System.out.println("✅ 文件上传（特殊字符文件名）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_differentImageFormats() throws Exception {
        // 测试不同图片格式
        String[] formats = {"image/png", "image/gif", "image/webp", "image/bmp"};
        
        for (String format : formats) {
            MessageAttachment attachment = createMessageAttachment();
            attachment.setMimeType(format);
            Mockito.when(fileUploadService.uploadFile(any(), any()))
                    .thenReturn(attachment);

            MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test." + format.split("/")[1], 
                format, 
                "test content".getBytes()
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.mimeType").value(format));
        }

        System.out.println("✅ 文件上传（不同图片格式）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testGetFileBase64_largeImage() throws Exception {
        // 模拟大图片Base64转换
        String largeBase64 = "data:image/jpeg;base64," + "A".repeat(10000); // 模拟大Base64字符串
        Mockito.when(fileUploadService.imageToBase64("large-image.jpg"))
                .thenReturn(largeBase64);

        mockMvc.perform(MockMvcRequestBuilders.get("/files/large-image.jpg/base64"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.base64").value(largeBase64));

        System.out.println("✅ 获取文件Base64（大图片）测试通过");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com", roles = {"user"})
    public void testUploadFile_withDimensions() throws Exception {
        // 模拟带尺寸信息的图片
        MessageAttachment attachment = createMessageAttachment();
        attachment.setWidth(1920);
        attachment.setHeight(1080);
        Mockito.when(fileUploadService.uploadFile(any(), any()))
                .thenReturn(attachment);

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "hd-image.jpg", 
            "image/jpeg", 
            "hd content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.width").value(1920))
                .andExpect(jsonPath("$.data.height").value(1080))
                .andExpect(jsonPath("$.data.dimensions").value("1920×1080"));

        System.out.println("✅ 文件上传（带尺寸信息）测试通过");
    }

    // 辅助方法：创建测试附件对象
    private MessageAttachment createMessageAttachment() {
        return MessageAttachment.builder()
                .id(1L)
                .messageId(1L)
                .fileName("test-image.jpg")
                .originalName("original-image.jpg")
                .filePath("/uploads/test-image.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .attachmentType(MessageAttachment.AttachmentType.IMAGE)
                .width(800)
                .height(600)
                .createdAt(LocalDateTime.now())
                .build();
    }
} 