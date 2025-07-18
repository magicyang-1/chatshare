package com.aiplatform.service;

import com.aiplatform.entity.Chat;
import com.aiplatform.entity.Message;
import com.aiplatform.entity.MessageAttachment;
import com.aiplatform.entity.User;
import com.aiplatform.exception.BusinessException;
import com.aiplatform.repository.ChatRepository;
import com.aiplatform.repository.MessageAttachmentRepository;
import com.aiplatform.repository.MessageRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageAttachmentRepository messageAttachmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private Chat testChat;
    private Message testMessage;
    private MessageAttachment testAttachment;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // 创建测试聊天会话
        testChat = new Chat();
        testChat.setId(1L);
        testChat.setUserId(1L);
        testChat.setTitle("测试聊天");
        testChat.setAiType(Chat.AiType.conversation);
        testChat.setIsFavorite(false);
        testChat.setIsProtected(false);
        testChat.setMessageCount(0);
        testChat.setLastActivity(LocalDateTime.now());

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
                .filePath("/uploads/test.jpg")
                .attachmentType(MessageAttachment.AttachmentType.IMAGE)
                .build();
    }

    @Test
    void testCreateChat_Success() {
        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // 执行测试
        Chat result = chatService.createChat(1L, "新聊天", Chat.AiType.conversation);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试聊天", result.getTitle());
        assertEquals(Chat.AiType.conversation, result.getAiType());
        assertFalse(result.getIsFavorite());
        assertFalse(result.getIsProtected());
        assertEquals(0, result.getMessageCount());

        // 验证调用
        verify(userRepository).findById(1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testCreateChat_UserNotFound() {
        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatService.createChat(1L, "新聊天", Chat.AiType.conversation);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void testCreateChat_WithNullTitle() {
        // 准备mock数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Chat expectedChat = new Chat();
        expectedChat.setId(1L);
        expectedChat.setUserId(1L);
        expectedChat.setTitle("新对话");
        expectedChat.setAiType(Chat.AiType.conversation);
        
        when(chatRepository.save(any(Chat.class))).thenReturn(expectedChat);

        // 执行测试
        Chat result = chatService.createChat(1L, null, Chat.AiType.conversation);

        // 验证结果
        assertNotNull(result);
        assertEquals("新对话", result.getTitle());
    }

    @Test
    void testSendMessage_Success() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(chatRepository.save(any(Chat.class))).thenReturn(testChat);

        // 执行测试
        Message result = chatService.sendMessage(1L, 1L, "测试消息", Message.MessageRole.user);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getChatId());
        assertEquals(Message.MessageRole.user, result.getRole());
        assertEquals("测试消息", result.getContent());

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(messageRepository).save(any(Message.class));
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testSendMessage_ChatNotFound() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatService.sendMessage(1L, 1L, "测试消息", Message.MessageRole.user);
        });

        assertEquals("聊天会话不存在或无权限访问", exception.getMessage());
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testGetChatMessages_Success() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));
        when(messageRepository.findByChatIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(testMessage));
        when(messageAttachmentRepository.findByMessageIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(testAttachment));

        // 执行测试
        List<Message> result = chatService.getChatMessages(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
        assertEquals(1, result.get(0).getAttachments().size());
        assertEquals(testAttachment.getId(), result.get(0).getAttachments().get(0).getId());

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc(1L);
        verify(messageAttachmentRepository).findByMessageIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetChatMessages_ChatNotFound() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatService.getChatMessages(1L, 1L);
        });

        assertEquals("聊天会话不存在或无权限访问", exception.getMessage());
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(messageRepository, never()).findByChatIdOrderByCreatedAtAsc(anyLong());
    }

    @Test
    void testGetUserChats_Success() {
        // 准备mock数据
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> expectedPage = new PageImpl<>(Arrays.asList(testChat));
        when(chatRepository.findByUserIdOrderByLastActivityDesc(1L, pageable)).thenReturn(expectedPage);

        // 执行测试
        Page<Chat> result = chatService.getUserChats(1L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testChat.getId(), result.getContent().get(0).getId());

        // 验证调用
        verify(chatRepository).findByUserIdOrderByLastActivityDesc(1L, pageable);
    }

    @Test
    void testGetChatById_Success() {
        // 准备mock数据
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));

        // 执行测试
        Chat result = chatService.getChatById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testChat.getId(), result.getId());

        // 验证调用
        verify(chatRepository).findById(1L);
    }

    @Test
    void testGetChatById_NotFound() {
        // 准备mock数据
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatService.getChatById(1L);
        });

        assertEquals("聊天会话不存在", exception.getMessage());
        verify(chatRepository).findById(1L);
    }

    @Test
    void testDeleteChat_Success() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));

        // 执行测试
        chatService.deleteChat(1L, 1L);

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(messageRepository).deleteByChatId(1L);
        verify(chatRepository).delete(testChat);
    }

    @Test
    void testDeleteChat_NotFound() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            chatService.deleteChat(1L, 1L);
        });

        assertEquals("聊天会话不存在或无权限访问", exception.getMessage());
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(messageRepository, never()).deleteByChatId(anyLong());
        verify(chatRepository, never()).delete(any(Chat.class));
    }

    @Test
    void testUpdateChatTitle_Success() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));
        
        Chat updatedChat = new Chat();
        updatedChat.setId(1L);
        updatedChat.setTitle("更新后的标题");
        when(chatRepository.save(any(Chat.class))).thenReturn(updatedChat);

        // 执行测试
        Chat result = chatService.updateChatTitle(1L, 1L, "更新后的标题");

        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的标题", result.getTitle());

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testToggleFavorite_Success() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));
        
        Chat updatedChat = new Chat();
        updatedChat.setId(1L);
        updatedChat.setIsFavorite(true);
        when(chatRepository.save(any(Chat.class))).thenReturn(updatedChat);

        // 执行测试
        Chat result = chatService.toggleFavorite(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getIsFavorite());

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testToggleProtection_Success() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));
        
        Chat updatedChat = new Chat();
        updatedChat.setId(1L);
        updatedChat.setIsProtected(true);
        when(chatRepository.save(any(Chat.class))).thenReturn(updatedChat);

        // 执行测试
        Chat result = chatService.toggleProtection(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getIsProtected());

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testGenerateAIResponse_Success() {
        // 准备mock数据
        Map<String, Object> options = new HashMap<>();
        when(aiService.generateResponse("用户消息", "conversation", "gpt-3.5-turbo", options))
                .thenReturn("AI回复");

        // 执行测试
        String result = chatService.generateAIResponse("用户消息", "conversation", "gpt-3.5-turbo", options);

        // 验证结果
        assertEquals("AI回复", result);

        // 验证调用
        verify(aiService).generateResponse("用户消息", "conversation", "gpt-3.5-turbo", options);
    }

    @Test
    void testGenerateAIResponse_EmptyMessage() {
        // 执行测试
        String result = chatService.generateAIResponse("", "conversation", "gpt-3.5-turbo", new HashMap<>());

        // 验证结果
        assertEquals("请输入您的问题。", result);

        // 验证调用
        verify(aiService, never()).generateResponse(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testGenerateAIResponse_NullMessage() {
        // 执行测试
        String result = chatService.generateAIResponse(null, "conversation", "gpt-3.5-turbo", new HashMap<>());

        // 验证结果
        assertEquals("请输入您的问题。", result);

        // 验证调用
        verify(aiService, never()).generateResponse(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testGenerateAIResponse_Exception() {
        // 准备mock数据
        Map<String, Object> options = new HashMap<>();
        when(aiService.generateResponse("用户消息", "conversation", "gpt-3.5-turbo", options))
                .thenThrow(new RuntimeException("AI服务异常"));

        // 执行测试
        String result = chatService.generateAIResponse("用户消息", "conversation", "gpt-3.5-turbo", options);

        // 验证结果
        assertTrue(result.contains("抱歉，AI服务暂时遇到问题"));

        // 验证调用
        verify(aiService).generateResponse("用户消息", "conversation", "gpt-3.5-turbo", options);
    }

    @Test
    void testUpdateChatModel_WithoutUserId() {
        // 准备mock数据
        when(chatRepository.findById(1L)).thenReturn(Optional.of(testChat));
        
        Chat updatedChat = new Chat();
        updatedChat.setId(1L);
        updatedChat.setAiModel("gpt-4");
        when(chatRepository.save(any(Chat.class))).thenReturn(updatedChat);

        // 执行测试
        Chat result = chatService.updateChatModel(1L, "gpt-4");

        // 验证结果
        assertNotNull(result);
        assertEquals("gpt-4", result.getAiModel());

        // 验证调用
        verify(chatRepository).findById(1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testUpdateChatModel_WithUserId() {
        // 准备mock数据
        when(chatRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testChat));
        
        Chat updatedChat = new Chat();
        updatedChat.setId(1L);
        updatedChat.setAiModel("gpt-4");
        when(chatRepository.save(any(Chat.class))).thenReturn(updatedChat);

        // 执行测试
        Chat result = chatService.updateChatModel(1L, 1L, "gpt-4");

        // 验证结果
        assertNotNull(result);
        assertEquals("gpt-4", result.getAiModel());

        // 验证调用
        verify(chatRepository).findByIdAndUserId(1L, 1L);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testIsAIServiceAvailable() {
        // 准备mock数据
        when(aiService.isAIServiceAvailable()).thenReturn(true);

        // 执行测试
        boolean result = chatService.isAIServiceAvailable();

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(aiService).isAIServiceAvailable();
    }

    @Test
    void testGetCurrentAIModel() {
        // 准备mock数据
        when(aiService.getCurrentModel()).thenReturn("gpt-3.5-turbo");

        // 执行测试
        String result = chatService.getCurrentAIModel();

        // 验证结果
        assertEquals("gpt-3.5-turbo", result);

        // 验证调用
        verify(aiService).getCurrentModel();
    }

    @Test
    void testAttachFilesToMessage_Success() {
        // 准备mock数据
        List<Map<String, Object>> attachments = new ArrayList<>();
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("fileId", "test.jpg");
        attachment.put("fileName", "test.jpg");
        attachment.put("originalName", "test.jpg");
        attachment.put("fileType", "image/jpeg");
        attachments.add(attachment);

        when(messageAttachmentRepository.findByFileName("test.jpg")).thenReturn(testAttachment);
        when(messageAttachmentRepository.save(any(MessageAttachment.class))).thenReturn(testAttachment);

        // 执行测试
        chatService.attachFilesToMessage(1L, attachments);

        // 验证调用
        verify(messageAttachmentRepository).findByFileName("test.jpg");
        verify(messageAttachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testAttachFilesToMessage_NotFound() {
        // 准备mock数据
        List<Map<String, Object>> attachments = new ArrayList<>();
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("fileId", "nonexistent.jpg");
        attachment.put("fileName", "nonexistent.jpg");
        attachment.put("originalName", "nonexistent.jpg");
        attachment.put("fileType", "image/jpeg");
        attachments.add(attachment);

        when(messageAttachmentRepository.findByFileName("nonexistent.jpg")).thenReturn(null);
        when(messageAttachmentRepository.findByMessageIdIsNull()).thenReturn(new ArrayList<>());

        // 执行测试
        chatService.attachFilesToMessage(1L, attachments);

        // 验证调用
        verify(messageAttachmentRepository, times(2)).findByFileName("nonexistent.jpg");
        verify(messageAttachmentRepository, times(2)).findByMessageIdIsNull();
        verify(messageAttachmentRepository, never()).save(any(MessageAttachment.class));
    }

    @Test
    void testAttachFilesToMessage_FindByOriginalName() {
        // 准备mock数据
        List<Map<String, Object>> attachments = new ArrayList<>();
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("fileId", "file123");
        attachment.put("fileName", "renamed.jpg");
        attachment.put("originalName", "original.jpg");
        attachment.put("fileType", "image/jpeg");
        attachments.add(attachment);

        MessageAttachment unassignedAttachment = MessageAttachment.builder()
                .id(2L)
                .fileName("renamed.jpg")
                .originalName("original.jpg")
                .mimeType("image/jpeg")
                .attachmentType(MessageAttachment.AttachmentType.IMAGE)
                .build();

        when(messageAttachmentRepository.findByFileName("file123")).thenReturn(null);
        when(messageAttachmentRepository.findByFileName("renamed.jpg")).thenReturn(null);
        when(messageAttachmentRepository.findByMessageIdIsNull()).thenReturn(Arrays.asList(unassignedAttachment));
        when(messageAttachmentRepository.save(any(MessageAttachment.class))).thenReturn(unassignedAttachment);

        // 执行测试
        chatService.attachFilesToMessage(1L, attachments);

        // 验证调用
        verify(messageAttachmentRepository).findByFileName("file123");
        verify(messageAttachmentRepository).findByFileName("renamed.jpg");
        verify(messageAttachmentRepository).findByMessageIdIsNull();
        verify(messageAttachmentRepository).save(any(MessageAttachment.class));
    }

    @Test
    void testAttachFilesByNames_Success() {
        // 准备mock数据
        List<String> fileNames = Arrays.asList("test1.jpg", "test2.jpg");
        
        MessageAttachment attachment1 = MessageAttachment.builder()
                .id(1L)
                .fileName("test1.jpg")
                .messageId(null)
                .build();
        
        MessageAttachment attachment2 = MessageAttachment.builder()
                .id(2L)
                .fileName("test2.jpg")
                .messageId(null)
                .build();

        when(messageAttachmentRepository.findByFileNameIn(fileNames))
                .thenReturn(Arrays.asList(attachment1, attachment2));
        when(messageAttachmentRepository.save(any(MessageAttachment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        chatService.attachFilesByNames(1L, fileNames);

        // 验证调用
        verify(messageAttachmentRepository).findByFileNameIn(fileNames);
        verify(messageAttachmentRepository, times(2)).save(any(MessageAttachment.class));
    }

    @Test
    void testAttachFilesByNames_AlreadyAssigned() {
        // 准备mock数据
        List<String> fileNames = Arrays.asList("test.jpg");
        
        MessageAttachment attachment = MessageAttachment.builder()
                .id(1L)
                .fileName("test.jpg")
                .messageId(999L) // 已经关联到其他消息
                .build();

        when(messageAttachmentRepository.findByFileNameIn(fileNames))
                .thenReturn(Arrays.asList(attachment));

        // 执行测试
        chatService.attachFilesByNames(1L, fileNames);

        // 验证调用
        verify(messageAttachmentRepository).findByFileNameIn(fileNames);
        verify(messageAttachmentRepository, never()).save(any(MessageAttachment.class));
    }
} 