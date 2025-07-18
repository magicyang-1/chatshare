package com.aiplatform.service;

import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.exception.BusinessException;
import com.aiplatform.repository.MessageAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock
    private MessageAttachmentRepository attachmentRepository;

    @InjectMocks
    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    private MessageAttachment testAttachment;
    private MockMultipartFile testImageFile;
    private MockMultipartFile testTextFile;
    private MockMultipartFile testLargeFile;

    @BeforeEach
    void setUp() throws IOException {
        // 设置临时上传目录
        ReflectionTestUtils.setField(fileUploadService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileUploadService, "maxFileSize", "50MB");

        // 创建测试附件
        testAttachment = MessageAttachment.builder()
                .id(1L)
                .messageId(1L)
                .fileName("20240101_120000_abcd1234.jpg")
                .originalName("test.jpg")
                .filePath(tempDir.resolve("test.jpg").toString())
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .attachmentType(MessageAttachment.AttachmentType.IMAGE)
                .width(800)
                .height(600)
                .build();

        // 创建测试文件
        testImageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        testTextFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "fake text content".getBytes()
        );

        // 创建超大文件（超过50MB）
        byte[] largeContent = new byte[51 * 1024 * 1024]; // 51MB
        testLargeFile = new MockMultipartFile(
                "file",
                "large.txt",
                "text/plain",
                largeContent
        );
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // 准备mock数据
        when(attachmentRepository.save(any(MessageAttachment.class))).thenReturn(testAttachment);

        // 执行测试
        MessageAttachment result = fileUploadService.uploadFile(testImageFile, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testAttachment.getId(), result.getId());
        assertEquals(testAttachment.getFileName(), result.getFileName());

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testUploadFile_EmptyFile() {
        // 创建空文件
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(emptyFile, 1L);
        });

        assertEquals("文件不能为空", exception.getMessage());
        verify(attachmentRepository, never()).save(any(MessageAttachment.class));
    }

    @Test
    void testUploadFile_FileTooLarge() {
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(testLargeFile, 1L);
        });

        assertEquals("文件大小不能超过50MB", exception.getMessage());
        verify(attachmentRepository, never()).save(any(MessageAttachment.class));
    }

    @Test
    void testUploadFile_NullContentType() {
        // 创建没有Content-Type的文件
        MockMultipartFile fileWithoutType = new MockMultipartFile(
                "file",
                "test.txt",
                null,
                "content".getBytes()
        );

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(fileWithoutType, 1L);
        });

        assertEquals("无法确定文件类型", exception.getMessage());
        verify(attachmentRepository, never()).save(any(MessageAttachment.class));
    }

    @Test
    void testUploadFile_UnsupportedImageType() {
        // 创建不支持的图片格式
        MockMultipartFile unsupportedImage = new MockMultipartFile(
                "file",
                "test.tiff",
                "image/tiff",
                "fake tiff content".getBytes()
        );

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(unsupportedImage, 1L);
        });

        assertTrue(exception.getMessage().contains("不支持的图片格式"));
        verify(attachmentRepository, never()).save(any(MessageAttachment.class));
    }

    @Test
    void testUploadFile_TextDocument() throws IOException {
        // 准备mock数据
        MessageAttachment textAttachment = MessageAttachment.builder()
                .id(2L)
                .messageId(1L)
                .fileName("20240101_120000_abcd1234.txt")
                .originalName("test.txt")
                .filePath(tempDir.resolve("test.txt").toString())
                .fileSize(1024L)
                .mimeType("text/plain")
                .attachmentType(MessageAttachment.AttachmentType.DOCUMENT)
                .build();

        when(attachmentRepository.save(any(MessageAttachment.class))).thenReturn(textAttachment);

        // 执行测试
        MessageAttachment result = fileUploadService.uploadFile(testTextFile, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(MessageAttachment.AttachmentType.DOCUMENT, result.getAttachmentType());

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testGetFileInfo_Success() {
        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试
        MessageAttachment result = fileUploadService.getFileInfo("test.jpg");

        // 验证结果
        assertNotNull(result);
        assertEquals(testAttachment.getId(), result.getId());
        assertEquals(testAttachment.getFileName(), result.getFileName());

        // 验证调用
        verify(attachmentRepository).findByFileName("test.jpg");
    }

    @Test
    void testGetFileInfo_NotFound() {
        // 准备mock数据
        when(attachmentRepository.findByFileName("nonexistent.jpg")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.getFileInfo("nonexistent.jpg");
        });

        assertEquals("文件不存在", exception.getMessage());
        verify(attachmentRepository).findByFileName("nonexistent.jpg");
    }

    @Test
    void testImageToBase64_Success() throws IOException {
        // 创建测试图片文件
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "fake image content".getBytes());
        
        // 更新测试附件的文件路径
        testAttachment.setFilePath(testFile.toString());

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试
        String result = fileUploadService.imageToBase64("test.jpg");

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // 验证调用
        verify(attachmentRepository).findByFileName("test.jpg");
    }

    @Test
    void testImageToBase64_FileNotFound() {
        // 准备mock数据
        when(attachmentRepository.findByFileName("nonexistent.jpg")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.imageToBase64("nonexistent.jpg");
        });

        assertEquals("图片文件不存在", exception.getMessage());
        verify(attachmentRepository).findByFileName("nonexistent.jpg");
    }

    @Test
    void testImageToBase64_NotImage() {
        // 创建非图片附件
        MessageAttachment textAttachment = MessageAttachment.builder()
                .id(2L)
                .fileName("test.txt")
                .attachmentType(MessageAttachment.AttachmentType.DOCUMENT)
                .build();

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.txt")).thenReturn(textAttachment);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.imageToBase64("test.txt");
        });

        assertEquals("图片文件不存在", exception.getMessage());
        verify(attachmentRepository).findByFileName("test.txt");
    }

    @Test
    void testImageToBase64_PhysicalFileNotExists() {
        // 设置不存在的文件路径
        testAttachment.setFilePath("/nonexistent/path/test.jpg");

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.imageToBase64("test.jpg");
        });

        assertEquals("物理文件不存在", exception.getMessage());
        verify(attachmentRepository).findByFileName("test.jpg");
    }

    @Test
    void testGetFileContent_Success() throws IOException {
        // 创建测试文件
        Path testFile = tempDir.resolve("test.jpg");
        byte[] testContent = "fake image content".getBytes();
        Files.write(testFile, testContent);
        
        // 更新测试附件的文件路径
        testAttachment.setFilePath(testFile.toString());

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试
        byte[] result = fileUploadService.getFileContent("test.jpg");

        // 验证结果
        assertNotNull(result);
        assertArrayEquals(testContent, result);

        // 验证调用
        verify(attachmentRepository).findByFileName("test.jpg");
    }

    @Test
    void testGetFileContent_FileNotFound() {
        // 准备mock数据
        when(attachmentRepository.findByFileName("nonexistent.jpg")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.getFileContent("nonexistent.jpg");
        });

        assertEquals("文件不存在", exception.getMessage());
        verify(attachmentRepository).findByFileName("nonexistent.jpg");
    }

    @Test
    void testGetFileContent_PhysicalFileNotExists() {
        // 设置不存在的文件路径
        testAttachment.setFilePath("/nonexistent/path/test.jpg");

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.getFileContent("test.jpg");
        });

        assertEquals("物理文件不存在", exception.getMessage());
        verify(attachmentRepository).findByFileName("test.jpg");
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        // 创建测试文件
        Path testFile = tempDir.resolve("test.jpg");
        Files.write(testFile, "fake image content".getBytes());
        
        // 更新测试附件的文件路径
        testAttachment.setFilePath(testFile.toString());

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试
        fileUploadService.deleteFile("test.jpg");

        // 验证物理文件被删除
        assertFalse(Files.exists(testFile));

        // 验证调用
        verify(attachmentRepository).findByFileName("test.jpg");
        verify(attachmentRepository).delete(testAttachment);
    }

    @Test
    void testDeleteFile_FileNotFound() throws IOException {
        // 准备mock数据
        when(attachmentRepository.findByFileName("nonexistent.jpg")).thenReturn(null);

        // 执行测试（不应该抛出异常）
        assertDoesNotThrow(() -> {
            fileUploadService.deleteFile("nonexistent.jpg");
        });

        // 验证调用
        verify(attachmentRepository).findByFileName("nonexistent.jpg");
        verify(attachmentRepository, never()).delete(any(MessageAttachment.class));
    }

    @Test
    void testDeleteFile_PhysicalFileNotExists() throws IOException {
        // 设置不存在的文件路径
        testAttachment.setFilePath("/nonexistent/path/test.jpg");

        // 准备mock数据
        when(attachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);

        // 执行测试（不应该抛出异常）
        assertDoesNotThrow(() -> {
            fileUploadService.deleteFile("test.jpg");
        });

        // 验证调用
        verify(attachmentRepository).findByFileName("test.jpg");
        verify(attachmentRepository).delete(testAttachment);
    }

    @Test
    void testCleanOrphanedFiles_Success() throws IOException {
        // 创建孤立文件
        MessageAttachment orphaned1 = MessageAttachment.builder()
                .id(1L)
                .fileName("orphaned1.jpg")
                .filePath(tempDir.resolve("orphaned1.jpg").toString())
                .build();

        MessageAttachment orphaned2 = MessageAttachment.builder()
                .id(2L)
                .fileName("orphaned2.jpg")
                .filePath(tempDir.resolve("orphaned2.jpg").toString())
                .build();

        // 创建物理文件
        Path file1 = tempDir.resolve("orphaned1.jpg");
        Path file2 = tempDir.resolve("orphaned2.jpg");
        Files.write(file1, "content1".getBytes());
        Files.write(file2, "content2".getBytes());

        // 准备mock数据
        when(attachmentRepository.findOrphanedAttachments())
                .thenReturn(Arrays.asList(orphaned1, orphaned2));
        when(attachmentRepository.findByFileName("orphaned1.jpg")).thenReturn(orphaned1);
        when(attachmentRepository.findByFileName("orphaned2.jpg")).thenReturn(orphaned2);

        // 执行测试
        fileUploadService.cleanOrphanedFiles();

        // 验证物理文件被删除
        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));

        // 验证调用
        verify(attachmentRepository).findOrphanedAttachments();
        verify(attachmentRepository).findByFileName("orphaned1.jpg");
        verify(attachmentRepository).findByFileName("orphaned2.jpg");
        verify(attachmentRepository).delete(orphaned1);
        verify(attachmentRepository).delete(orphaned2);
    }

    @Test
    void testCleanOrphanedFiles_NoOrphanedFiles() {
        // 准备mock数据
        when(attachmentRepository.findOrphanedAttachments()).thenReturn(Arrays.asList());

        // 执行测试
        fileUploadService.cleanOrphanedFiles();

        // 验证调用
        verify(attachmentRepository).findOrphanedAttachments();
        verify(attachmentRepository, never()).delete(any(MessageAttachment.class));
    }

    @Test
    void testCleanOrphanedFiles_WithError() throws IOException {
        // 创建孤立文件
        MessageAttachment orphaned = MessageAttachment.builder()
                .id(1L)
                .fileName("orphaned.jpg")
                .filePath("/invalid/path/orphaned.jpg") // 无效路径
                .build();

        // 准备mock数据
        when(attachmentRepository.findOrphanedAttachments()).thenReturn(Arrays.asList(orphaned));
        when(attachmentRepository.findByFileName("orphaned.jpg")).thenReturn(orphaned);

        // 执行测试（不应该抛出异常）
        assertDoesNotThrow(() -> {
            fileUploadService.cleanOrphanedFiles();
        });

        // 验证调用
        verify(attachmentRepository).findOrphanedAttachments();
        verify(attachmentRepository).findByFileName("orphaned.jpg");
        verify(attachmentRepository).delete(orphaned);
    }

    @Test
    void testDetermineAttachmentType_Image() throws IOException {
        // 创建图片文件
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "fake png content".getBytes()
        );

        when(attachmentRepository.save(any(MessageAttachment.class))).thenAnswer(invocation -> {
            MessageAttachment attachment = invocation.getArgument(0);
            assertEquals(MessageAttachment.AttachmentType.IMAGE, attachment.getAttachmentType());
            return attachment;
        });

        // 执行测试
        fileUploadService.uploadFile(imageFile, 1L);

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testDetermineAttachmentType_Audio() throws IOException {
        // 创建音频文件
        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "fake mp3 content".getBytes()
        );

        when(attachmentRepository.save(any(MessageAttachment.class))).thenAnswer(invocation -> {
            MessageAttachment attachment = invocation.getArgument(0);
            assertEquals(MessageAttachment.AttachmentType.AUDIO, attachment.getAttachmentType());
            return attachment;
        });

        // 执行测试
        fileUploadService.uploadFile(audioFile, 1L);

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testDetermineAttachmentType_Video() throws IOException {
        // 创建视频文件
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test.mp4",
                "video/mp4",
                "fake mp4 content".getBytes()
        );

        when(attachmentRepository.save(any(MessageAttachment.class))).thenAnswer(invocation -> {
            MessageAttachment attachment = invocation.getArgument(0);
            assertEquals(MessageAttachment.AttachmentType.VIDEO, attachment.getAttachmentType());
            return attachment;
        });

        // 执行测试
        fileUploadService.uploadFile(videoFile, 1L);

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testDetermineAttachmentType_PDF() throws IOException {
        // 创建PDF文件
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "fake pdf content".getBytes()
        );

        when(attachmentRepository.save(any(MessageAttachment.class))).thenAnswer(invocation -> {
            MessageAttachment attachment = invocation.getArgument(0);
            assertEquals(MessageAttachment.AttachmentType.DOCUMENT, attachment.getAttachmentType());
            return attachment;
        });

        // 执行测试
        fileUploadService.uploadFile(pdfFile, 1L);

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testDetermineAttachmentType_Other() throws IOException {
        // 创建其他类型文件
        MockMultipartFile otherFile = new MockMultipartFile(
                "file",
                "test.zip",
                "application/zip",
                "fake zip content".getBytes()
        );

        when(attachmentRepository.save(any(MessageAttachment.class))).thenAnswer(invocation -> {
            MessageAttachment attachment = invocation.getArgument(0);
            assertEquals(MessageAttachment.AttachmentType.OTHER, attachment.getAttachmentType());
            return attachment;
        });

        // 执行测试
        fileUploadService.uploadFile(otherFile, 1L);

        // 验证调用
        verify(attachmentRepository).save(any(MessageAttachment.class));
    }
} 