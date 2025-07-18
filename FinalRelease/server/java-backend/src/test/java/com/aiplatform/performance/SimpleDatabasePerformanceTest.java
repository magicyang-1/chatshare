//package com.aiplatform.performance;
//
//import com.aiplatform.entity.User;
//import com.aiplatform.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 简化数据库性能测试类
// *
// * 专门用于快速测试数据库中某字段访问1000次的性能
// * 测试目标：用户邮箱字段
// */
//@SpringBootTest
//@DisplayName("简化数据库性能测试")
//class SimpleDatabasePerformanceTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private static final int ITERATIONS = 1000;
//
//    @BeforeEach
//    void setUp() {
//        System.out.println("=== 数据库连接诊断 ===");
//
//        try {
//            // 检查现有数据
//            long userCount = userRepository.count();
//            System.out.println("当前用户表中有 " + userCount + " 条记录");
//
//            // 尝试获取所有用户并显示前几个
//            List<User> allUsers = userRepository.findAll();
//            System.out.println("查询到的用户总数: " + allUsers.size());
//
//            if (!allUsers.isEmpty()) {
//                System.out.println("前3个用户信息:");
//                for (int i = 0; i < Math.min(3, allUsers.size()); i++) {
//                    User user = allUsers.get(i);
//                    System.out.println("  用户" + (i+1) + ": ID=" + user.getId() +
//                                     ", 邮箱=" + user.getEmail() +
//                                     ", 用户名=" + user.getUsername());
//                }
//            }
//
//            // 如果没有数据，创建一些测试数据
//            if (userCount == 0) {
//                System.out.println("用户表为空，创建测试数据...");
//                for (int i = 1; i <= 10; i++) {
//                    User user = new User();
//                    user.setEmail("test" + i + "@example.com");
//                    user.setUsername("testuser" + i);
//                    user.setPassword("password" + i);
//                    user.setRole(User.UserRole.user);
//                    user.setStatus(User.UserStatus.active);
//                    userRepository.save(user);
//                }
//                userCount = userRepository.count();
//                System.out.println("已创建 " + userCount + " 条测试数据");
//            }
//
//            // 确保测试数据存在
//            assertTrue(userCount > 0, "用户表中应该有数据，当前数量: " + userCount);
//            System.out.println("测试数据准备完成，用户表中共有 " + userCount + " 条记录");
//
//        } catch (Exception e) {
//            System.err.println("数据库连接或查询出错: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }
//
//    @Test
//    @DisplayName("用户邮箱字段访问性能测试 - 1000次")
//    @Transactional(readOnly = true)
//    void testUserEmailFieldAccess() {
//        System.out.println("=== 开始用户邮箱字段访问性能测试 ===");
//        System.out.println("测试目标：访问用户邮箱字段 " + ITERATIONS + " 次");
//
//        // 记录开始时间
//        long startTime = System.currentTimeMillis();
//
//        // 获取所有用户ID
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        assertFalse(userIds.isEmpty(), "用户ID列表不能为空");
//        System.out.println("获取到 " + userIds.size() + " 个用户ID");
//
//        // 访问邮箱字段1000次
//        final int[] successCount = {0};
//        final int[] nullCount = {0};
//
//        for (int i = 0; i < ITERATIONS; i++) {
//            Long userId = userIds.get(i % userIds.size());
//            userRepository.findById(userId).ifPresent(user -> {
//                String email = user.getEmail();
//                if (email != null) {
//                    // 验证邮箱格式（简单验证）
//                    assertTrue(email.contains("@"), "邮箱格式不正确: " + email);
//                    successCount[0]++;
//                } else {
//                    nullCount[0]++;
//                }
//            });
//
//            // 每100次输出一次进度
//            if ((i + 1) % 100 == 0) {
//                System.out.println("已完成 " + (i + 1) + " 次访问...");
//            }
//        }
//
//        // 记录结束时间
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        // 输出详细结果
//        System.out.println("\n=== 测试结果 ===");
//        System.out.printf("总访问次数: %d\n", ITERATIONS);
//        System.out.printf("成功访问次数: %d\n", successCount[0]);
//        System.out.printf("空值次数: %d\n", nullCount[0]);
//        System.out.printf("总耗时: %d ms\n", duration);
//        System.out.printf("平均每次访问: %.2f ms\n", (double) duration / ITERATIONS);
//        System.out.printf("每秒访问次数: %.2f\n", (double) ITERATIONS / duration * 1000);
//
//        // 性能分析
//        System.out.println("\n=== 性能分析 ===");
//        if (duration < 1000) {
//            System.out.println("性能评级: 优秀 (耗时 < 1秒)");
//        } else if (duration < 3000) {
//            System.out.println("性能评级: 良好 (耗时 1-3秒)");
//        } else if (duration < 5000) {
//            System.out.println("性能评级: 一般 (耗时 3-5秒)");
//        } else {
//            System.out.println("性能评级: 需要优化 (耗时 > 5秒)");
//        }
//
//        // 性能断言
//        assertTrue(duration < 5000, "1000次邮箱字段访问应该在5秒内完成，实际耗时: " + duration + "ms");
//        assertTrue(successCount[0] > 0, "应该有成功的访问记录");
//
//        System.out.println("\n=== 测试完成 ===");
//    }
//
//    @Test
//    @DisplayName("用户邮箱字段批量查询性能测试")
//    @Transactional(readOnly = true)
//    void testBatchUserEmailQuery() {
//        System.out.println("=== 开始用户邮箱字段批量查询性能测试 ===");
//
//        // 记录开始时间
//        long startTime = System.currentTimeMillis();
//
//        // 批量查询所有用户的邮箱
//        List<String> emails = userRepository.findAll().stream()
//                .map(User::getEmail)
//                .collect(Collectors.toList());
//
//        // 记录结束时间
//        long endTime = System.currentTimeMillis();
//        long duration = endTime - startTime;
//
//        // 输出结果
//        System.out.println("\n=== 批量查询结果 ===");
//        System.out.printf("查询用户数量: %d\n", emails.size());
//        System.out.printf("总耗时: %d ms\n", duration);
//        System.out.printf("平均每个用户: %.2f ms\n", (double) duration / emails.size());
//
//        // 统计有效邮箱数量
//        long validEmailCount = emails.stream()
//                .filter(email -> email != null && email.contains("@"))
//                .count();
//
//        System.out.printf("有效邮箱数量: %d\n", validEmailCount);
//        System.out.printf("无效邮箱数量: %d\n", emails.size() - validEmailCount);
//
//        // 性能断言
//        assertFalse(emails.isEmpty(), "邮箱列表不能为空");
//        assertTrue(duration < 1000, "批量查询应该在1秒内完成，实际耗时: " + duration + "ms");
//
//        System.out.println("=== 批量查询测试完成 ===");
//    }
//
//    @Test
//    @DisplayName("用户邮箱字段访问性能对比测试")
//    @Transactional(readOnly = true)
//    void testUserEmailAccessComparison() {
//        System.out.println("=== 开始用户邮箱字段访问性能对比测试 ===");
//
//        // 获取用户ID列表
//        List<Long> userIds = userRepository.findAll().stream()
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        assertFalse(userIds.isEmpty(), "用户ID列表不能为空");
//
//        // 测试1: 单次查询访问
//        System.out.println("\n--- 测试1: 单次查询访问 ---");
//        long singleStartTime = System.currentTimeMillis();
//
//        for (int i = 0; i < 100; i++) {
//            Long userId = userIds.get(i % userIds.size());
//            userRepository.findById(userId).ifPresent(user -> {
//                String email = user.getEmail();
//                assertNotNull(email, "邮箱不能为空");
//            });
//        }
//
//        long singleDuration = System.currentTimeMillis() - singleStartTime;
//        System.out.printf("单次查询访问 (100次): %d ms, 平均: %.2f ms\n",
//                singleDuration, (double) singleDuration / 100);
//
//        // 测试2: 批量查询访问
//        System.out.println("\n--- 测试2: 批量查询访问 ---");
//        long batchStartTime = System.currentTimeMillis();
//
//        List<String> emails = userRepository.findAll().stream()
//                .limit(100)
//                .map(User::getEmail)
//                .collect(Collectors.toList());
//
//        long batchDuration = System.currentTimeMillis() - batchStartTime;
//        System.out.printf("批量查询访问 (100个): %d ms, 平均: %.2f ms\n",
//                batchDuration, (double) batchDuration / emails.size());
//
//        // 性能对比分析
//        System.out.println("\n--- 性能对比分析 ---");
//        double singleAvg = (double) singleDuration / 100;
//        double batchAvg = (double) batchDuration / emails.size();
//
//        System.out.printf("单次查询平均耗时: %.2f ms\n", singleAvg);
//        System.out.printf("批量查询平均耗时: %.2f ms\n", batchAvg);
//
//        if (batchAvg < singleAvg) {
//            double improvement = ((singleAvg - batchAvg) / singleAvg) * 100;
//            System.out.printf("批量查询性能提升: %.1f%%\n", improvement);
//        } else {
//            double degradation = ((batchAvg - singleAvg) / singleAvg) * 100;
//            System.out.printf("批量查询性能下降: %.1f%%\n", degradation);
//        }
//
//        // 性能断言
//        assertTrue(singleDuration < 1000, "单次查询应该在1秒内完成");
//        assertTrue(batchDuration < 1000, "批量查询应该在1秒内完成");
//
//        System.out.println("=== 性能对比测试完成 ===");
//    }
//}