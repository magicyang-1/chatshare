//package com.aiplatform.performance;
//
//import com.aiplatform.entity.User;
//import com.aiplatform.entity.Chat;
//import com.aiplatform.entity.Message;
//import com.aiplatform.repository.UserRepository;
//import com.aiplatform.repository.ChatRepository;
//import com.aiplatform.repository.MessageRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 数据库性能测试类
// *
// * 测试内容：
// * - 单字段访问性能测试
// * - 批量查询性能测试
// * - 并发访问性能测试
// * - 不同实体的性能对比
// */
//@SpringBootTest
//@ActiveProfiles("test")
//@DisplayName("数据库性能测试")
//class DatabasePerformanceTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ChatRepository chatRepository;
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    private static final int ITERATIONS = 1000;
//    private static final int CONCURRENT_THREADS = 10;
//
//    @BeforeEach
//    void setUp() {
//        // 确保测试数据存在
//        assertTrue(userRepository.count() > 0, "用户表中应该有数据");
//        assertTrue(chatRepository.count() > 0, "聊天表中应该有数据");
//        assertTrue(messageRepository.count() > 0, "消息表中应该有数据");
//    }
//
//    // ==================== 单字段访问性能测试 ====================
//
//    @Test
//    @DisplayName("用户邮箱字段访问性能测试 - 1000次")
//    @Transactional(readOnly = true)
//    void testUserEmailFieldAccess() {
//        System.out.println("开始用户邮箱字段访问性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 获取所有用户ID
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        assertFalse(userIds.isEmpty(), "用户ID列表不能为空");
//
//        // 访问邮箱字段1000次
//        for (int i = 0; i < ITERATIONS; i++) {
//            Long userId = userIds.get(i % userIds.size());
//            userRepository.findById(userId).ifPresent(user -> {
//                String email = user.getEmail();
//                assertNotNull(email, "邮箱不能为空");
//            });
//        }
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("用户邮箱字段访问测试完成:\n");
//        System.out.printf("- 总访问次数: %d\n", ITERATIONS);
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每次访问: %.2f ms\n", (double) duration / ITERATIONS);
//        System.out.printf("- 每秒访问次数: %.2f\n", (double) ITERATIONS / duration * 1000);
//
//        // 性能断言：1000次访问应该在5秒内完成
//        assertTrue(duration < 5000, "1000次邮箱字段访问应该在5秒内完成，实际耗时: " + duration + "ms");
//    }
//
//    @Test
//    @DisplayName("聊天标题字段访问性能测试 - 1000次")
//    @Transactional(readOnly = true)
//    void testChatTitleFieldAccess() {
//        System.out.println("开始聊天标题字段访问性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 获取所有聊天ID
//        List<Long> chatIds = chatRepository.findAll().stream()
//                .map(Chat::getId)
//                .collect(Collectors.toList());
//
//        assertFalse(chatIds.isEmpty(), "聊天ID列表不能为空");
//
//        // 访问标题字段1000次
//        for (int i = 0; i < ITERATIONS; i++) {
//            Long chatId = chatIds.get(i % chatIds.size());
//            chatRepository.findById(chatId).ifPresent(chat -> {
//                String title = chat.getTitle();
//                assertNotNull(title, "标题不能为空");
//            });
//        }
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("聊天标题字段访问测试完成:\n");
//        System.out.printf("- 总访问次数: %d\n", ITERATIONS);
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每次访问: %.2f ms\n", (double) duration / ITERATIONS);
//        System.out.printf("- 每秒访问次数: %.2f\n", (double) ITERATIONS / duration * 1000);
//
//        // 性能断言：1000次访问应该在5秒内完成
//        assertTrue(duration < 5000, "1000次标题字段访问应该在5秒内完成，实际耗时: " + duration + "ms");
//    }
//
//    @Test
//    @DisplayName("消息内容字段访问性能测试 - 1000次")
//    @Transactional(readOnly = true)
//    void testMessageContentFieldAccess() {
//        System.out.println("开始消息内容字段访问性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 获取所有消息ID
//        List<Long> messageIds = messageRepository.findAll().stream()
//                .map(Message::getId)
//                .collect(Collectors.toList());
//
//        assertFalse(messageIds.isEmpty(), "消息ID列表不能为空");
//
//        // 访问内容字段1000次
//        for (int i = 0; i < ITERATIONS; i++) {
//            Long messageId = messageIds.get(i % messageIds.size());
//            messageRepository.findById(messageId).ifPresent(message -> {
//                String content = message.getContent();
//                assertNotNull(content, "内容不能为空");
//            });
//        }
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("消息内容字段访问测试完成:\n");
//        System.out.printf("- 总访问次数: %d\n", ITERATIONS);
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每次访问: %.2f ms\n", (double) duration / ITERATIONS);
//        System.out.printf("- 每秒访问次数: %.2f\n", (double) ITERATIONS / duration * 1000);
//
//        // 性能断言：1000次访问应该在5秒内完成
//        assertTrue(duration < 5000, "1000次内容字段访问应该在5秒内完成，实际耗时: " + duration + "ms");
//    }
//
//    // ==================== 批量查询性能测试 ====================
//
//    @Test
//    @DisplayName("批量查询用户邮箱性能测试")
//    @Transactional(readOnly = true)
//    void testBatchUserEmailQuery() {
//        System.out.println("开始批量查询用户邮箱性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 批量查询所有用户的邮箱
//        List<String> emails = userRepository.findAll().stream()
//                .map(User::getEmail)
//                .collect(Collectors.toList());
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("批量查询用户邮箱测试完成:\n");
//        System.out.printf("- 查询用户数量: %d\n", emails.size());
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每个用户: %.2f ms\n", (double) duration / emails.size());
//
//        assertFalse(emails.isEmpty(), "邮箱列表不能为空");
//        assertTrue(duration < 1000, "批量查询应该在1秒内完成，实际耗时: " + duration + "ms");
//    }
//
//    @Test
//    @DisplayName("批量查询聊天标题性能测试")
//    @Transactional(readOnly = true)
//    void testBatchChatTitleQuery() {
//        System.out.println("开始批量查询聊天标题性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 批量查询所有聊天的标题
//        List<String> titles = chatRepository.findAll().stream()
//                .map(Chat::getTitle)
//                .collect(Collectors.toList());
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("批量查询聊天标题测试完成:\n");
//        System.out.printf("- 查询聊天数量: %d\n", titles.size());
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每个聊天: %.2f ms\n", (double) duration / titles.size());
//
//        assertFalse(titles.isEmpty(), "标题列表不能为空");
//        assertTrue(duration < 1000, "批量查询应该在1秒内完成，实际耗时: " + duration + "ms");
//    }
//
//    // ==================== 并发访问性能测试 ====================
//
//    @Test
//    @DisplayName("并发访问用户邮箱字段性能测试")
//    @Transactional(readOnly = true)
//    void testConcurrentUserEmailAccess() throws InterruptedException {
//        System.out.println("开始并发访问用户邮箱字段性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 获取所有用户ID
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        assertFalse(userIds.isEmpty(), "用户ID列表不能为空");
//
//        // 创建线程池
//        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
//
//        // 创建并发任务
//        List<CompletableFuture<Void>> futures = IntStream.range(0, ITERATIONS)
//                .mapToObj(i -> CompletableFuture.runAsync(() -> {
//                    Long userId = userIds.get(i % userIds.size());
//                    userRepository.findById(userId).ifPresent(user -> {
//                        String email = user.getEmail();
//                        assertNotNull(email, "邮箱不能为空");
//                    });
//                }, executor))
//                .collect(Collectors.toList());
//
//        // 等待所有任务完成
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        // 关闭线程池
//        executor.shutdown();
//        executor.awaitTermination(10, TimeUnit.SECONDS);
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("并发访问用户邮箱字段测试完成:\n");
//        System.out.printf("- 并发线程数: %d\n", CONCURRENT_THREADS);
//        System.out.printf("- 总访问次数: %d\n", ITERATIONS);
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每次访问: %.2f ms\n", (double) duration / ITERATIONS);
//        System.out.printf("- 每秒访问次数: %.2f\n", (double) ITERATIONS / duration * 1000);
//
//        // 性能断言：并发1000次访问应该在10秒内完成
//        assertTrue(duration < 10000, "并发1000次邮箱字段访问应该在10秒内完成，实际耗时: " + duration + "ms");
//    }
//
//    // ==================== 性能对比测试 ====================
//
//    @Test
//    @DisplayName("不同实体字段访问性能对比测试")
//    @Transactional(readOnly = true)
//    void testEntityFieldAccessComparison() {
//        System.out.println("开始不同实体字段访问性能对比测试...");
//
//        // 获取测试数据
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .limit(100)
//                .collect(Collectors.toList());
//
//        List<Long> chatIds = chatRepository.findAll().stream()
//                .map(Chat::getId)
//                .limit(100)
//                .collect(Collectors.toList());
//
//        List<Long> messageIds = messageRepository.findAll().stream()
//                .map(Message::getId)
//                .limit(100)
//                .collect(Collectors.toList());
//
//        // 测试用户邮箱访问
//        long userStartTime = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            Long userId = userIds.get(i % userIds.size());
//            userRepository.findById(userId).ifPresent(user -> user.getEmail());
//        }
//        long userDuration = System.currentTimeMillis() - userStartTime;
//
//        // 测试聊天标题访问
//        long chatStartTime = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            Long chatId = chatIds.get(i % chatIds.size());
//            chatRepository.findById(chatId).ifPresent(chat -> chat.getTitle());
//        }
//        long chatDuration = System.currentTimeMillis() - chatStartTime;
//
//        // 测试消息内容访问
//        long messageStartTime = System.currentTimeMillis();
//        for (int i = 0; i < 100; i++) {
//            Long messageId = messageIds.get(i % messageIds.size());
//            messageRepository.findById(messageId).ifPresent(message -> message.getContent());
//        }
//        long messageDuration = System.currentTimeMillis() - messageStartTime;
//
//        System.out.printf("不同实体字段访问性能对比:\n");
//        System.out.printf("- 用户邮箱访问 (100次): %d ms, 平均: %.2f ms\n", userDuration, (double) userDuration / 100);
//        System.out.printf("- 聊天标题访问 (100次): %d ms, 平均: %.2f ms\n", chatDuration, (double) chatDuration / 100);
//        System.out.printf("- 消息内容访问 (100次): %d ms, 平均: %.2f ms\n", messageDuration, (double) messageDuration / 100);
//
//        // 性能断言：所有测试都应该在合理时间内完成
//        assertTrue(userDuration < 1000, "用户邮箱访问应该在1秒内完成");
//        assertTrue(chatDuration < 1000, "聊天标题访问应该在1秒内完成");
//        assertTrue(messageDuration < 1000, "消息内容访问应该在1秒内完成");
//    }
//
//    // ==================== 综合性能测试 ====================
//
//    @Test
//    @DisplayName("综合数据库性能测试 - 1000次混合字段访问")
//    @Transactional(readOnly = true)
//    void testComprehensiveDatabasePerformance() {
//        System.out.println("开始综合数据库性能测试...");
//
//        long startTime = System.currentTimeMillis();
//
//        // 获取测试数据
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        List<Long> chatIds = chatRepository.findAll().stream()
//                .map(Chat::getId)
//                .collect(Collectors.toList());
//
//        List<Long> messageIds = messageRepository.findAll().stream()
//                .map(Message::getId)
//                .collect(Collectors.toList());
//
//        int userCount = 0, chatCount = 0, messageCount = 0;
//
//        // 混合访问不同实体的字段
//        for (int i = 0; i < ITERATIONS; i++) {
//            switch (i % 3) {
//                case 0:
//                    // 访问用户邮箱
//                    if (!userIds.isEmpty()) {
//                        Long userId = userIds.get(i % userIds.size());
//                        userRepository.findById(userId).ifPresent(user -> {
//                            String email = user.getEmail();
//                            assertNotNull(email, "邮箱不能为空");
//                        });
//                        userCount++;
//                    }
//                    break;
//                case 1:
//                    // 访问聊天标题
//                    if (!chatIds.isEmpty()) {
//                        Long chatId = chatIds.get(i % chatIds.size());
//                        chatRepository.findById(chatId).ifPresent(chat -> {
//                            String title = chat.getTitle();
//                            assertNotNull(title, "标题不能为空");
//                        });
//                        chatCount++;
//                    }
//                    break;
//                case 2:
//                    // 访问消息内容
//                    if (!messageIds.isEmpty()) {
//                        Long messageId = messageIds.get(i % messageIds.size());
//                        messageRepository.findById(messageId).ifPresent(message -> {
//                            String content = message.getContent();
//                            assertNotNull(content, "内容不能为空");
//                        });
//                        messageCount++;
//                    }
//                    break;
//            }
//        }
//
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        System.out.printf("综合数据库性能测试完成:\n");
//        System.out.printf("- 总访问次数: %d\n", ITERATIONS);
//        System.out.printf("- 用户邮箱访问: %d 次\n", userCount);
//        System.out.printf("- 聊天标题访问: %d 次\n", chatCount);
//        System.out.printf("- 消息内容访问: %d 次\n", messageCount);
//        System.out.printf("- 总耗时: %d ms\n", duration);
//        System.out.printf("- 平均每次访问: %.2f ms\n", (double) duration / ITERATIONS);
//        System.out.printf("- 每秒访问次数: %.2f\n", (double) ITERATIONS / duration * 1000);
//
//        // 性能断言：综合测试应该在10秒内完成
//        assertTrue(duration < 10000, "综合1000次字段访问应该在10秒内完成，实际耗时: " + duration + "ms");
//    }
//}