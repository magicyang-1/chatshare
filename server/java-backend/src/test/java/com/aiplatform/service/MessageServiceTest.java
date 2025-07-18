package com.aiplatform.service;

import com.aiplatform.entity.Message;
import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.exception.BusinessException;
import com.aiplatform.repository.MessageAttachmentRepository;
import com.aiplatform.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageAttachmentRepository messageAttachmentRepository;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private MessageAttachment testAttachment;

    @BeforeEach
    void setUp() {
        // 创建测试消息
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setChatId(1L);
        testMessage.setRole(Message.MessageRole.user);
        testMessage.setContent("测试消息");
        testMessage.setCreatedAt(LocalDateTime.now());

        // 创建测试附件
        testAttachment = MessageAttachment.builder()
                .id(1L)
                .messageId(1L)
                .fileName("test.jpg")
                .originalName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();
    }

    // ==================== getMessageById 方法测试 ====================
    @Test
    void testGetMessageById_Success() {
        // 准备mock数据
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // 执行测试
        Message result = messageService.getMessageById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        assertEquals(testMessage.getContent(), result.getContent());

        // 验证调用
        verify(messageRepository).findById(1L);
    }

    @Test
    void testGetMessageById_NotFound() {
        // 准备mock数据
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试
        Message result = messageService.getMessageById(999L);

        // 验证结果
        assertNull(result);
        verify(messageRepository).findById(999L);
    }

    @Test
    void testGetMessageById_NullId() {
        // 执行测试
        Message result = messageService.getMessageById(null);

        // 验证结果
        assertNull(result);
        verify(messageRepository).findById(null);
    }

    @Test
    void testGetMessageById_RepositoryException() {
        // 准备mock数据 - 模拟异常
        when(messageRepository.findById(1L))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.getMessageById(1L);
        });

        // 验证调用
        verify(messageRepository).findById(1L);
    }

    // ==================== getMessageAttachments 方法测试 ====================
    @Test
    void testGetMessageAttachments_Success() {
        // 准备mock数据
        when(messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(1L))
                .thenReturn(Arrays.asList(testAttachment));

        // 执行测试
        List<MessageAttachment> result = messageService.getMessageAttachments(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAttachment.getId(), result.get(0).getId());

        // 验证调用
        verify(messageAttachmentRepository).findByMessageIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetMessageAttachments_EmptyList() {
        // 准备mock数据
        when(messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(1L))
                .thenReturn(Arrays.asList());

        // 执行测试
        List<MessageAttachment> result = messageService.getMessageAttachments(1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证调用
        verify(messageAttachmentRepository).findByMessageIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetMessageAttachments_NullId() {
        // 准备mock数据
        when(messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(null))
                .thenReturn(Arrays.asList());

        // 执行测试
        List<MessageAttachment> result = messageService.getMessageAttachments(null);

        // 验证结果
        assertNotNull(result);
        verify(messageAttachmentRepository).findByMessageIdOrderByCreatedAtAsc(null);
    }

    @Test
    void testGetMessageAttachments_RepositoryException() {
        // 准备mock数据 - 模拟异常
        when(messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(1L))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.getMessageAttachments(1L);
        });

        // 验证调用
        verify(messageAttachmentRepository).findByMessageIdOrderByCreatedAtAsc(1L);
    }

    // ==================== deleteMessage 方法测试 ====================
    @Test
    void testDeleteMessage_Success() {
        // 执行测试
        messageService.deleteMessage(1L);

        // 验证调用
        verify(messageAttachmentRepository).deleteByMessageId(1L);
        verify(messageRepository).deleteById(1L);
    }

    @Test
    void testDeleteMessage_NotFound() {
        // 执行测试 - deleteMessage方法不会抛出异常，即使消息不存在也会执行删除操作
        messageService.deleteMessage(999L);

        // 验证调用
        verify(messageAttachmentRepository).deleteByMessageId(999L);
        verify(messageRepository).deleteById(999L);
    }

    @Test
    void testDeleteMessage_NullId() {
        // 执行测试
        messageService.deleteMessage(null);

        // 验证调用
        verify(messageAttachmentRepository).deleteByMessageId(null);
        verify(messageRepository).deleteById(null);
    }

    @Test
    void testDeleteMessage_RepositoryException() {
        // 准备mock数据 - 模拟异常
        doThrow(new RuntimeException("Database error"))
                .when(messageAttachmentRepository).deleteByMessageId(1L);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.deleteMessage(1L);
        });

        // 验证调用
        verify(messageAttachmentRepository).deleteByMessageId(1L);
        verify(messageRepository, never()).deleteById(any());
    }

    // ==================== saveMessage 方法测试 ====================
    @Test
    void testSaveMessage_Success() {
        // 准备mock数据
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // 执行测试
        Message result = messageService.saveMessage(testMessage);

        // 验证结果
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());

        // 验证调用
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testSaveMessage_WithNullCreatedAt() {
        // 准备测试数据
        Message messageWithoutCreatedAt = new Message();
        messageWithoutCreatedAt.setId(2L);
        messageWithoutCreatedAt.setChatId(1L);
        messageWithoutCreatedAt.setRole(Message.MessageRole.assistant);
        messageWithoutCreatedAt.setContent("新消息");
        messageWithoutCreatedAt.setCreatedAt(null); // 设置为null

        // 准备mock数据
        when(messageRepository.save(any(Message.class))).thenReturn(messageWithoutCreatedAt);

        // 执行测试
        Message result = messageService.saveMessage(messageWithoutCreatedAt);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getCreatedAt()); // 应该被设置为当前时间

        // 验证调用
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testSaveMessage_WithNullContent() {
        // 准备测试数据
        Message messageWithNullContent = new Message();
        messageWithNullContent.setId(3L);
        messageWithNullContent.setChatId(1L);
        messageWithNullContent.setRole(Message.MessageRole.user);
        messageWithNullContent.setContent(null);

        // 准备mock数据
        when(messageRepository.save(any(Message.class))).thenReturn(messageWithNullContent);

        // 执行测试
        Message result = messageService.saveMessage(messageWithNullContent);

        // 验证结果
        assertNotNull(result);
        assertNull(result.getContent());

        // 验证调用
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testSaveMessage_WithEmptyContent() {
        // 准备测试数据
        Message messageWithEmptyContent = new Message();
        messageWithEmptyContent.setId(4L);
        messageWithEmptyContent.setChatId(1L);
        messageWithEmptyContent.setRole(Message.MessageRole.user);
        messageWithEmptyContent.setContent("");

        // 准备mock数据
        when(messageRepository.save(any(Message.class))).thenReturn(messageWithEmptyContent);

        // 执行测试
        Message result = messageService.saveMessage(messageWithEmptyContent);

        // 验证结果
        assertNotNull(result);
        assertEquals("", result.getContent());

        // 验证调用
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testSaveMessage_RepositoryException() {
        // 准备mock数据 - 模拟异常
        when(messageRepository.save(any(Message.class)))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.saveMessage(testMessage);
        });

        // 验证调用
        verify(messageRepository).save(any(Message.class));
    }

    // ==================== updateMessage 方法测试 ====================
    @Test
    void testUpdateMessage_Success() {
        // 准备mock数据
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // 执行测试
        Message result = messageService.updateMessage(1L, "更新的消息");

        // 验证结果
        assertNotNull(result);

        // 验证调用
        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testUpdateMessage_NotFound() {
        // 准备mock数据
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试
        Message result = messageService.updateMessage(999L, "更新的消息");

        // 验证结果
        assertNull(result);

        // 验证调用
        verify(messageRepository).findById(999L);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testUpdateMessage_NullId() {
        // 准备mock数据
        when(messageRepository.findById(null)).thenReturn(Optional.empty());

        // 执行测试
        Message result = messageService.updateMessage(null, "更新的消息");

        // 验证结果
        assertNull(result);

        // 验证调用
        verify(messageRepository).findById(null);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testUpdateMessage_NullContent() {
        // 准备mock数据
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // 执行测试
        Message result = messageService.updateMessage(1L, null);

        // 验证结果
        assertNotNull(result);

        // 验证调用
        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testUpdateMessage_EmptyContent() {
        // 准备mock数据
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // 执行测试
        Message result = messageService.updateMessage(1L, "");

        // 验证结果
        assertNotNull(result);

        // 验证调用
        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void testUpdateMessage_RepositoryException() {
        // 准备mock数据
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class)))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.updateMessage(1L, "更新的消息");
        });

        // 验证调用
        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
    }

    // ==================== addAttachmentToMessage 方法测试 ====================
    @Test
    void testAddAttachmentToMessage_Success() {
        // 准备测试数据
        MessageAttachment newAttachment = MessageAttachment.builder()
                .id(2L)
                .fileName("new.jpg")
                .originalName("new.jpg")
                .mimeType("image/jpeg")
                .fileSize(2048L)
                .build();

        // 准备mock数据
        when(messageAttachmentRepository.save(any(MessageAttachment.class))).thenReturn(newAttachment);

        // 执行测试
        messageService.addAttachmentToMessage(1L, newAttachment);

        // 验证结果
        assertEquals(1L, newAttachment.getMessageId());
        assertNotNull(newAttachment.getCreatedAt());

        // 验证调用
        verify(messageAttachmentRepository).save(newAttachment);
    }

    @Test
    void testAddAttachmentToMessage_WithNullCreatedAt() {
        // 准备测试数据
        MessageAttachment attachmentWithoutCreatedAt = MessageAttachment.builder()
                .id(3L)
                .fileName("test.pdf")
                .originalName("test.pdf")
                .mimeType("application/pdf")
                .fileSize(512L)
                .createdAt(null) // 设置为null
                .build();

        // 准备mock数据
        when(messageAttachmentRepository.save(any(MessageAttachment.class))).thenReturn(attachmentWithoutCreatedAt);

        // 执行测试
        messageService.addAttachmentToMessage(1L, attachmentWithoutCreatedAt);

        // 验证结果
        assertEquals(1L, attachmentWithoutCreatedAt.getMessageId());
        assertNotNull(attachmentWithoutCreatedAt.getCreatedAt()); // 应该被设置为当前时间

        // 验证调用
        verify(messageAttachmentRepository).save(attachmentWithoutCreatedAt);
    }

    @Test
    void testAddAttachmentToMessage_NullMessageId() {
        // 准备测试数据
        MessageAttachment attachment = MessageAttachment.builder()
                .id(4L)
                .fileName("test.txt")
                .originalName("test.txt")
                .mimeType("text/plain")
                .fileSize(100L)
                .build();

        // 准备mock数据
        when(messageAttachmentRepository.save(any(MessageAttachment.class))).thenReturn(attachment);

        // 执行测试
        messageService.addAttachmentToMessage(null, attachment);

        // 验证结果
        assertNull(attachment.getMessageId());

        // 验证调用
        verify(messageAttachmentRepository).save(attachment);
    }

    @Test
    void testAddAttachmentToMessage_RepositoryException() {
        // 准备测试数据
        MessageAttachment attachment = MessageAttachment.builder()
                .id(5L)
                .fileName("test.jpg")
                .originalName("test.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024L)
                .build();

        // 准备mock数据 - 模拟异常
        when(messageAttachmentRepository.save(any(MessageAttachment.class)))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.addAttachmentToMessage(1L, attachment);
        });

        // 验证调用
        verify(messageAttachmentRepository).save(attachment);
    }

    // ==================== getChatMessages 方法测试 ====================
    @Test
    void testGetChatMessages_Success() {
        // 准备测试数据
        Message message1 = new Message();
        message1.setId(1L);
        message1.setChatId(1L);
        message1.setContent("消息1");

        Message message2 = new Message();
        message2.setId(2L);
        message2.setChatId(1L);
        message2.setContent("消息2");

        List<Message> messages = Arrays.asList(message1, message2);

        // 准备mock数据
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(messages);

        // 执行测试
        List<Message> result = messageService.getChatMessages(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(message1.getId(), result.get(0).getId());
        assertEquals(message2.getId(), result.get(1).getId());

        // 验证调用
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetChatMessages_EmptyList() {
        // 准备mock数据
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList());

        // 执行测试
        List<Message> result = messageService.getChatMessages(1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证调用
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetChatMessages_NullChatId() {
        // 准备mock数据
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(null)).thenReturn(Arrays.asList());

        // 执行测试
        List<Message> result = messageService.getChatMessages(null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证调用
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(null);
    }

    @Test
    void testGetChatMessages_RepositoryException() {
        // 准备mock数据 - 模拟异常
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L))
                .thenThrow(new RuntimeException("Database error"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            messageService.getChatMessages(1L);
        });

        // 验证调用
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(1L);
    }
} 