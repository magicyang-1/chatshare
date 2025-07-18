package com.aiplatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MathService 单元测试类
 * 
 * 测试覆盖：
 * - 所有数学运算方法
 * - 正常输入值
 * - 边界值（0、负数、大数）
 * - 异常情况
 */
@DisplayName("MathService 测试")
class MathServiceTest {

    private MathService mathService;

    @BeforeEach
    void setUp() {
        mathService = new MathService();
    }

    // ==================== plus1 方法测试 ====================

    @Test
    @DisplayName("plus1 - 正常加法运算")
    void testPlus1NormalOperation() {
        // 测试正常加法运算
        assertEquals(5, mathService.plus1(2, 3), "2 + 3 应该等于 5");
        assertEquals(10, mathService.plus1(7, 3), "7 + 3 应该等于 10");
        assertEquals(0, mathService.plus1(-5, 5), "-5 + 5 应该等于 0");
    }

    @Test
    @DisplayName("plus1 - 零值测试")
    void testPlus1WithZero() {
        assertEquals(5, mathService.plus1(0, 5), "0 + 5 应该等于 5");
        assertEquals(3, mathService.plus1(3, 0), "3 + 0 应该等于 3");
        assertEquals(0, mathService.plus1(0, 0), "0 + 0 应该等于 0");
    }

    @Test
    @DisplayName("plus1 - 负数测试")
    void testPlus1WithNegativeNumbers() {
        assertEquals(-2, mathService.plus1(-5, 3), "-5 + 3 应该等于 -2");
        assertEquals(-8, mathService.plus1(-3, -5), "-3 + (-5) 应该等于 -8");
        assertEquals(2, mathService.plus1(5, -3), "5 + (-3) 应该等于 2");
    }

    @Test
    @DisplayName("plus1 - 大数测试")
    void testPlus1WithLargeNumbers() {
        assertEquals(2000000, mathService.plus1(1000000, 1000000), "大数加法测试");
        assertEquals(Integer.MAX_VALUE - 1, mathService.plus1(Integer.MAX_VALUE - 2, 1), "接近Integer.MAX_VALUE的加法");
    }

    @ParameterizedTest
    @CsvSource({
        "1, 2, 3",
        "5, 3, 8",
        "0, 0, 0",
        "-1, 1, 0",
        "10, -5, 5"
    })
    @DisplayName("plus1 - 参数化测试")
    void testPlus1Parameterized(int a, int b, int expected) {
        assertEquals(expected, mathService.plus1(a, b), 
            String.format("%d + %d 应该等于 %d", a, b, expected));
    }

    // ==================== minus1 方法测试 ====================

    @Test
    @DisplayName("minus1 - 正常减法运算")
    void testMinus1NormalOperation() {
        assertEquals(-1, mathService.minus1(2, 3), "2 - 3 应该等于 -1");
        assertEquals(4, mathService.minus1(7, 3), "7 - 3 应该等于 4");
        assertEquals(-10, mathService.minus1(-5, 5), "-5 - 5 应该等于 -10");
    }

    @Test
    @DisplayName("minus1 - 零值测试")
    void testMinus1WithZero() {
        assertEquals(-5, mathService.minus1(0, 5), "0 - 5 应该等于 -5");
        assertEquals(3, mathService.minus1(3, 0), "3 - 0 应该等于 3");
        assertEquals(0, mathService.minus1(0, 0), "0 - 0 应该等于 0");
    }

    @Test
    @DisplayName("minus1 - 负数测试")
    void testMinus1WithNegativeNumbers() {
        assertEquals(-8, mathService.minus1(-5, 3), "-5 - 3 应该等于 -8");
        assertEquals(2, mathService.minus1(-3, -5), "-3 - (-5) 应该等于 2");
        assertEquals(8, mathService.minus1(5, -3), "5 - (-3) 应该等于 8");
    }

    @ParameterizedTest
    @CsvSource({
        "5, 2, 3",
        "3, 5, -2",
        "0, 0, 0",
        "1, 1, 0",
        "-5, -3, -2"
    })
    @DisplayName("minus1 - 参数化测试")
    void testMinus1Parameterized(int a, int b, int expected) {
        assertEquals(expected, mathService.minus1(a, b), 
            String.format("%d - %d 应该等于 %d", a, b, expected));
    }

    // ==================== plus3 方法测试 ====================

    @Test
    @DisplayName("plus3 - 正常加法运算")
    void testPlus3NormalOperation() {
        assertEquals(5, mathService.plus3(2, 3), "2 + 3 应该等于 5");
        assertEquals(10, mathService.plus3(7, 3), "7 + 3 应该等于 10");
        assertEquals(0, mathService.plus3(-5, 5), "-5 + 5 应该等于 0");
    }

    @Test
    @DisplayName("plus3 - 边界值测试")
    void testPlus3BoundaryValues() {
        assertEquals(Integer.MAX_VALUE, mathService.plus3(Integer.MAX_VALUE, 0), "MAX_VALUE + 0");
        assertEquals(Integer.MIN_VALUE, mathService.plus3(Integer.MIN_VALUE, 0), "MIN_VALUE + 0");
    }

    @ParameterizedTest
    @CsvSource({
        "10, 20, 30",
        "-10, 20, 10",
        "0, 100, 100",
        "999, 1, 1000"
    })
    @DisplayName("plus3 - 参数化测试")
    void testPlus3Parameterized(int a, int b, int expected) {
        assertEquals(expected, mathService.plus3(a, b), 
            String.format("%d + %d 应该等于 %d", a, b, expected));
    }

    // ==================== minus3 方法测试 ====================

    @Test
    @DisplayName("minus3 - 正常减法运算")
    void testMinus3NormalOperation() {
        assertEquals(-1, mathService.minus3(2, 3), "2 - 3 应该等于 -1");
        assertEquals(4, mathService.minus3(7, 3), "7 - 3 应该等于 4");
        assertEquals(-10, mathService.minus3(-5, 5), "-5 - 5 应该等于 -10");
    }

    @Test
    @DisplayName("minus3 - 边界值测试")
    void testMinus3BoundaryValues() {
        assertEquals(Integer.MAX_VALUE, mathService.minus3(Integer.MAX_VALUE, 0), "MAX_VALUE - 0");
        assertEquals(Integer.MIN_VALUE, mathService.minus3(Integer.MIN_VALUE, 0), "MIN_VALUE - 0");
    }

    @ParameterizedTest
    @CsvSource({
        "30, 20, 10",
        "10, 20, -10",
        "100, 0, 100",
        "1000, 999, 1"
    })
    @DisplayName("minus3 - 参数化测试")
    void testMinus3Parameterized(int a, int b, int expected) {
        assertEquals(expected, mathService.minus3(a, b), 
            String.format("%d - %d 应该等于 %d", a, b, expected));
    }

    // ==================== plus4 方法测试 ====================

    @Test
    @DisplayName("plus4 - 正常加法运算")
    void testPlus4NormalOperation() {
        assertEquals(5, mathService.plus4(2, 3), "2 + 3 应该等于 5");
        assertEquals(10, mathService.plus4(7, 3), "7 + 3 应该等于 10");
        assertEquals(0, mathService.plus4(-5, 5), "-5 + 5 应该等于 0");
    }

    @Test
    @DisplayName("plus4 - 溢出测试")
    void testPlus4Overflow() {
        // 测试接近溢出的情况
        assertEquals(Integer.MAX_VALUE - 1, mathService.plus4(Integer.MAX_VALUE - 2, 1), "接近MAX_VALUE的加法");
    }

    @ParameterizedTest
    @CsvSource({
        "15, 25, 40",
        "-15, 25, 10",
        "0, 200, 200",
        "1999, 1, 2000"
    })
    @DisplayName("plus4 - 参数化测试")
    void testPlus4Parameterized(int a, int b, int expected) {
        assertEquals(expected, mathService.plus4(a, b), 
            String.format("%d + %d 应该等于 %d", a, b, expected));
    }

    // ==================== minus4 方法测试 ====================

    @Test
    @DisplayName("minus4 - 正常减法运算")
    void testMinus4NormalOperation() {
        assertEquals(-1, mathService.minus4(2, 3), "2 - 3 应该等于 -1");
        assertEquals(4, mathService.minus4(7, 3), "7 - 3 应该等于 4");
        assertEquals(-10, mathService.minus4(-5, 5), "-5 - 5 应该等于 -10");
    }

    @Test
    @DisplayName("minus4 - 下溢测试")
    void testMinus4Underflow() {
        // 测试接近下溢的情况
        assertEquals(Integer.MIN_VALUE + 1, mathService.minus4(Integer.MIN_VALUE + 2, 1), "接近MIN_VALUE的减法");
    }

    @ParameterizedTest
    @CsvSource({
        "40, 25, 15",
        "10, 25, -15",
        "200, 0, 200",
        "2000, 1999, 1"
    })
    @DisplayName("minus4 - 参数化测试")
    void testMinus4Parameterized(int a, int b, int expected) {
        assertEquals(expected, mathService.minus4(a, b), 
            String.format("%d - %d 应该等于 %d", a, b, expected));
    }

    // ==================== 综合测试 ====================

    @Test
    @DisplayName("所有方法一致性测试")
    void testAllMethodsConsistency() {
        int a = 10;
        int b = 5;
        
        // 测试所有plus方法返回相同结果
        assertEquals(mathService.plus1(a, b), mathService.plus3(a, b), "plus1 和 plus3 应该返回相同结果");
        assertEquals(mathService.plus1(a, b), mathService.plus4(a, b), "plus1 和 plus4 应该返回相同结果");
        
        // 测试所有minus方法返回相同结果
        assertEquals(mathService.minus1(a, b), mathService.minus3(a, b), "minus1 和 minus3 应该返回相同结果");
        assertEquals(mathService.minus1(a, b), mathService.minus4(a, b), "minus1 和 minus4 应该返回相同结果");
    }

    @Test
    @DisplayName("数学运算基本性质测试")
    void testMathematicalProperties() {
        int a = 7;
        int b = 3;
        
        // 测试加法交换律
        assertEquals(mathService.plus1(a, b), mathService.plus1(b, a), "加法应该满足交换律");
        
        // 测试减法的非交换性
        assertNotEquals(mathService.minus1(a, b), mathService.minus1(b, a), "减法不满足交换律");
        
        // 测试加法和减法的关系
        assertEquals(a, mathService.plus1(mathService.minus1(a, b), b), "a = (a-b) + b");
    }

    @Test
    @DisplayName("特殊值测试")
    void testSpecialValues() {
        // 测试最大和最小值
        assertEquals(Integer.MAX_VALUE, mathService.plus1(Integer.MAX_VALUE, 0), "MAX_VALUE + 0");
        assertEquals(Integer.MIN_VALUE, mathService.minus1(Integer.MIN_VALUE, 0), "MIN_VALUE - 0");
        
        // 测试相同数相减
        assertEquals(0, mathService.minus1(100, 100), "相同数相减应该等于0");
        
        // 测试相反数相加
        assertEquals(0, mathService.plus1(100, -100), "相反数相加应该等于0");
    }

    @Test
    @DisplayName("性能测试 - 多次调用")
    void testPerformance() {
        int iterations = 1000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            mathService.plus1(i, i + 1);
            mathService.minus1(i + 1, i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证性能在合理范围内（1000次调用应该在100ms内完成）
        assertTrue(duration < 100, "1000次调用应该在100ms内完成，实际耗时: " + duration + "ms");
    }
} 