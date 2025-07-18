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
       assertEquals(-2, mathService.plus5(-5, 3), "-5 + 3 应该等于 -2");
       assertEquals(-8, mathService.plus6(-3, -5), "-3 + (-5) 应该等于 -8");
       assertEquals(2, mathService.plus4(5, -3), "5 + (-3) 应该等于 2");
       assertEquals(2, mathService.plus7(5, -3), "5 + (-3) 应该等于 2");
       assertEquals(2, mathService.plus8(5, -3), "5 + (-3) 应该等于 2");
       assertEquals(2, mathService.plus9(5, -3), "5 + (-3) 应该等于 2");
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

    // ==================== 基础数学函数测试 ====================

    @Test
    @DisplayName("max - 最大值函数测试")
    void testMax() {
        assertEquals(5, mathService.max(3, 5), "max(3, 5) 应该等于 5");
        assertEquals(5, mathService.max(5, 3), "max(5, 3) 应该等于 5");
        assertEquals(0, mathService.max(0, 0), "max(0, 0) 应该等于 0");
        assertEquals(-3, mathService.max(-5, -3), "max(-5, -3) 应该等于 -3");
        assertEquals(Integer.MAX_VALUE, mathService.max(Integer.MAX_VALUE, 0), "max(MAX_VALUE, 0) 应该等于 MAX_VALUE");
    }

    @Test
    @DisplayName("min - 最小值函数测试")
    void testMin() {
        assertEquals(3, mathService.min(3, 5), "min(3, 5) 应该等于 3");
        assertEquals(3, mathService.min(5, 3), "min(5, 3) 应该等于 3");
        assertEquals(0, mathService.min(0, 0), "min(0, 0) 应该等于 0");
        assertEquals(-5, mathService.min(-5, -3), "min(-5, -3) 应该等于 -5");
        assertEquals(Integer.MIN_VALUE, mathService.min(Integer.MIN_VALUE, 0), "min(MIN_VALUE, 0) 应该等于 MIN_VALUE");
    }

    @Test
    @DisplayName("abs - 绝对值函数测试")
    void testAbs() {
        assertEquals(5, mathService.abs(5), "abs(5) 应该等于 5");
        assertEquals(5, mathService.abs(-5), "abs(-5) 应该等于 5");
        assertEquals(0, mathService.abs(0), "abs(0) 应该等于 0");
        assertEquals(Integer.MAX_VALUE, mathService.abs(Integer.MAX_VALUE), "abs(MAX_VALUE) 应该等于 MAX_VALUE");
    }

    @Test
    @DisplayName("power - 幂运算测试")
    void testPower() {
        assertEquals(8.0, mathService.power(2, 3), 0.001, "2^3 应该等于 8");
        assertEquals(1.0, mathService.power(5, 0), 0.001, "5^0 应该等于 1");
        assertEquals(0.25, mathService.power(2, -2), 0.001, "2^(-2) 应该等于 0.25");
        assertEquals(4.0, mathService.power(16, 0.5), 0.001, "16^0.5 应该等于 4");
    }

    @Test
    @DisplayName("sqrt - 平方根测试")
    void testSqrt() {
        assertEquals(2.0, mathService.sqrt(4), 0.001, "sqrt(4) 应该等于 2");
        assertEquals(0.0, mathService.sqrt(0), 0.001, "sqrt(0) 应该等于 0");
        assertEquals(1.414, mathService.sqrt(2), 0.001, "sqrt(2) 应该约等于 1.414");
        assertTrue(Double.isNaN(mathService.sqrt(-1)), "sqrt(-1) 应该返回 NaN");
    }

    @Test
    @DisplayName("cbrt - 立方根测试")
    void testCbrt() {
        assertEquals(2.0, mathService.cbrt(8), 0.001, "cbrt(8) 应该等于 2");
        assertEquals(0.0, mathService.cbrt(0), 0.001, "cbrt(0) 应该等于 0");
        assertEquals(-2.0, mathService.cbrt(-8), 0.001, "cbrt(-8) 应该等于 -2");
    }

    @Test
    @DisplayName("log - 自然对数测试")
    void testLog() {
        assertEquals(0.0, mathService.log(1), 0.001, "log(1) 应该等于 0");
        assertEquals(1.0, mathService.log(Math.E), 0.001, "log(e) 应该等于 1");
        assertTrue(Double.isInfinite(mathService.log(0)), "log(0) 应该返回负无穷");
        assertTrue(Double.isNaN(mathService.log(-1)), "log(-1) 应该返回 NaN");
    }

    @Test
    @DisplayName("log10 - 常用对数测试")
    void testLog10() {
        assertEquals(0.0, mathService.log10(1), 0.001, "log10(1) 应该等于 0");
        assertEquals(1.0, mathService.log10(10), 0.001, "log10(10) 应该等于 1");
        assertEquals(2.0, mathService.log10(100), 0.001, "log10(100) 应该等于 2");
    }

    @Test
    @DisplayName("三角函数测试")
    void testTrigonometricFunctions() {
        // 正弦函数
        assertEquals(0.0, mathService.sin(0), 0.001, "sin(0) 应该等于 0");
        assertEquals(1.0, mathService.sin(Math.PI/2), 0.001, "sin(π/2) 应该等于 1");
        assertEquals(0.0, mathService.sin(Math.PI), 0.001, "sin(π) 应该等于 0");
        
        // 余弦函数
        assertEquals(1.0, mathService.cos(0), 0.001, "cos(0) 应该等于 1");
        assertEquals(0.0, mathService.cos(Math.PI/2), 0.001, "cos(π/2) 应该等于 0");
        assertEquals(-1.0, mathService.cos(Math.PI), 0.001, "cos(π) 应该等于 -1");
        
        // 正切函数
        assertEquals(0.0, mathService.tan(0), 0.001, "tan(0) 应该等于 0");
        assertEquals(1.0, mathService.tan(Math.PI/4), 0.001, "tan(π/4) 应该等于 1");
    }

    @Test
    @DisplayName("反三角函数测试")
    void testInverseTrigonometricFunctions() {
        assertEquals(0.0, mathService.asin(0), 0.001, "asin(0) 应该等于 0");
        assertEquals(Math.PI/2, mathService.asin(1), 0.001, "asin(1) 应该等于 π/2");
        
        assertEquals(Math.PI/2, mathService.acos(0), 0.001, "acos(0) 应该等于 π/2");
        assertEquals(0.0, mathService.acos(1), 0.001, "acos(1) 应该等于 0");
        
        assertEquals(0.0, mathService.atan(0), 0.001, "atan(0) 应该等于 0");
        assertEquals(Math.PI/4, mathService.atan(1), 0.001, "atan(1) 应该等于 π/4");
    }

    @Test
    @DisplayName("双曲函数测试")
    void testHyperbolicFunctions() {
        assertEquals(0.0, mathService.sinh(0), 0.001, "sinh(0) 应该等于 0");
        assertEquals(1.0, mathService.cosh(0), 0.001, "cosh(0) 应该等于 1");
        assertEquals(0.0, mathService.tanh(0), 0.001, "tanh(0) 应该等于 0");
    }

    // ==================== 高级数学函数测试 ====================

    @Test
    @DisplayName("factorial - 阶乘测试")
    void testFactorial() {
        assertEquals(1, mathService.factorial(0), "0! 应该等于 1");
        assertEquals(1, mathService.factorial(1), "1! 应该等于 1");
        assertEquals(2, mathService.factorial(2), "2! 应该等于 2");
        assertEquals(6, mathService.factorial(3), "3! 应该等于 6");
        assertEquals(24, mathService.factorial(4), "4! 应该等于 24");
        assertEquals(120, mathService.factorial(5), "5! 应该等于 120");
        assertEquals(2432902008176640000L, mathService.factorial(20), "20! 应该等于正确值");
    }

    @Test
    @DisplayName("factorial - 异常测试")
    void testFactorialExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.factorial(-1), "负数阶乘应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.factorial(21), "过大数值阶乘应该抛出异常");
    }

    @Test
    @DisplayName("fibonacci - 斐波那契数列测试")
    void testFibonacci() {
        assertEquals(0, mathService.fibonacci(0), "fibonacci(0) 应该等于 0");
        assertEquals(1, mathService.fibonacci(1), "fibonacci(1) 应该等于 1");
        assertEquals(1, mathService.fibonacci(2), "fibonacci(2) 应该等于 1");
        assertEquals(2, mathService.fibonacci(3), "fibonacci(3) 应该等于 2");
        assertEquals(3, mathService.fibonacci(4), "fibonacci(4) 应该等于 3");
        assertEquals(5, mathService.fibonacci(5), "fibonacci(5) 应该等于 5");
        assertEquals(8, mathService.fibonacci(6), "fibonacci(6) 应该等于 8");
        assertEquals(13, mathService.fibonacci(7), "fibonacci(7) 应该等于 13");
    }

    @Test
    @DisplayName("fibonacci - 异常测试")
    void testFibonacciExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.fibonacci(-1), "负数斐波那契应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.fibonacci(93), "过大数值斐波那契应该抛出异常");
    }

    @Test
    @DisplayName("gcd - 最大公约数测试")
    void testGcd() {
        assertEquals(6, mathService.gcd(12, 18), "gcd(12, 18) 应该等于 6");
        assertEquals(1, mathService.gcd(7, 13), "gcd(7, 13) 应该等于 1");
        assertEquals(5, mathService.gcd(0, 5), "gcd(0, 5) 应该等于 5");
        assertEquals(5, mathService.gcd(5, 0), "gcd(5, 0) 应该等于 5");
        assertEquals(0, mathService.gcd(0, 0), "gcd(0, 0) 应该等于 0");
        assertEquals(6, mathService.gcd(-12, 18), "gcd(-12, 18) 应该等于 6");
    }

    @Test
    @DisplayName("lcm - 最小公倍数测试")
    void testLcm() {
        assertEquals(36, mathService.lcm(12, 18), "lcm(12, 18) 应该等于 36");
        assertEquals(91, mathService.lcm(7, 13), "lcm(7, 13) 应该等于 91");
        assertEquals(0, mathService.lcm(0, 5), "lcm(0, 5) 应该等于 0");
        assertEquals(0, mathService.lcm(5, 0), "lcm(5, 0) 应该等于 0");
    }

    @Test
    @DisplayName("isPrime - 质数判断测试")
    void testIsPrime() {
        assertFalse(mathService.isPrime(0), "0 不是质数");
        assertFalse(mathService.isPrime(1), "1 不是质数");
        assertTrue(mathService.isPrime(2), "2 是质数");
        assertTrue(mathService.isPrime(3), "3 是质数");
        assertFalse(mathService.isPrime(4), "4 不是质数");
        assertTrue(mathService.isPrime(5), "5 是质数");
        assertFalse(mathService.isPrime(6), "6 不是质数");
        assertTrue(mathService.isPrime(7), "7 是质数");
        assertFalse(mathService.isPrime(8), "8 不是质数");
        assertFalse(mathService.isPrime(9), "9 不是质数");
        assertFalse(mathService.isPrime(10), "10 不是质数");
        assertTrue(mathService.isPrime(11), "11 是质数");
        assertTrue(mathService.isPrime(13), "13 是质数");
        assertTrue(mathService.isPrime(17), "17 是质数");
        assertTrue(mathService.isPrime(19), "19 是质数");
        assertFalse(mathService.isPrime(21), "21 不是质数");
    }

    // ==================== 几何函数测试 ====================

    @Test
    @DisplayName("circleArea - 圆的面积测试")
    void testCircleArea() {
        assertEquals(0.0, mathService.circleArea(0), 0.001, "半径为0的圆面积应该等于0");
        assertEquals(Math.PI, mathService.circleArea(1), 0.001, "半径为1的圆面积应该等于π");
        assertEquals(4 * Math.PI, mathService.circleArea(2), 0.001, "半径为2的圆面积应该等于4π");
        assertThrows(IllegalArgumentException.class, () -> mathService.circleArea(-1), "负数半径应该抛出异常");
    }

    @Test
    @DisplayName("circleCircumference - 圆的周长测试")
    void testCircleCircumference() {
        assertEquals(0.0, mathService.circleCircumference(0), 0.001, "半径为0的圆周长应该等于0");
        assertEquals(2 * Math.PI, mathService.circleCircumference(1), 0.001, "半径为1的圆周长应该等于2π");
        assertEquals(4 * Math.PI, mathService.circleCircumference(2), 0.001, "半径为2的圆周长应该等于4π");
        assertThrows(IllegalArgumentException.class, () -> mathService.circleCircumference(-1), "负数半径应该抛出异常");
    }

    @Test
    @DisplayName("rectangleArea - 矩形面积测试")
    void testRectangleArea() {
        assertEquals(0.0, mathService.rectangleArea(0, 5), 0.001, "长度为0的矩形面积应该等于0");
        assertEquals(0.0, mathService.rectangleArea(5, 0), 0.001, "宽度为0的矩形面积应该等于0");
        assertEquals(20.0, mathService.rectangleArea(4, 5), 0.001, "4x5的矩形面积应该等于20");
        assertThrows(IllegalArgumentException.class, () -> mathService.rectangleArea(-1, 5), "负数长度应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.rectangleArea(5, -1), "负数宽度应该抛出异常");
    }

    @Test
    @DisplayName("rectanglePerimeter - 矩形周长测试")
    void testRectanglePerimeter() {
        assertEquals(10.0, mathService.rectanglePerimeter(4, 1), 0.001, "4x1的矩形周长应该等于10");
        assertEquals(14.0, mathService.rectanglePerimeter(4, 3), 0.001, "4x3的矩形周长应该等于14");
        assertThrows(IllegalArgumentException.class, () -> mathService.rectanglePerimeter(-1, 5), "负数长度应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.rectanglePerimeter(5, -1), "负数宽度应该抛出异常");
    }

    @Test
    @DisplayName("triangleArea - 三角形面积测试")
    void testTriangleArea() {
        assertEquals(6.0, mathService.triangleArea(3, 4, 5), 0.001, "3-4-5三角形面积应该等于6");
        assertEquals(0.433, mathService.triangleArea(1, 1, 1), 0.001, "等边三角形面积测试");
        assertThrows(IllegalArgumentException.class, () -> mathService.triangleArea(-1, 2, 3), "负数边长应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.triangleArea(1, 2, 10), "不满足三角形不等式应该抛出异常");
    }

    // ==================== 统计函数测试 ====================

    @Test
    @DisplayName("average - 平均值测试")
    void testAverage() {
        assertEquals(2.0, mathService.average(1, 2, 3), 0.001, "1,2,3的平均值应该等于2");
        assertEquals(0.0, mathService.average(-1, 0, 1), 0.001, "-1,0,1的平均值应该等于0");
        assertEquals(5.0, mathService.average(5), 0.001, "单个数字的平均值应该等于它本身");
        assertThrows(IllegalArgumentException.class, () -> mathService.average(), "空数组应该抛出异常");
    }

    // ==================== 工具函数测试 ====================

    @Test
    @DisplayName("round - 四舍五入测试")
    void testRound() {
        assertEquals(3.14, mathService.round(3.14159, 2), 0.001, "3.14159四舍五入到2位小数应该等于3.14");
        assertEquals(3.142, mathService.round(3.14159, 3), 0.001, "3.14159四舍五入到3位小数应该等于3.142");
        assertEquals(3.0, mathService.round(3.14159, 0), 0.001, "3.14159四舍五入到0位小数应该等于3.0");
        assertThrows(IllegalArgumentException.class, () -> mathService.round(3.14, -1), "负数小数位数应该抛出异常");
    }

    @Test
    @DisplayName("ceil - 向上取整测试")
    void testCeil() {
        assertEquals(4.0, mathService.ceil(3.1), 0.001, "ceil(3.1) 应该等于 4");
        assertEquals(3.0, mathService.ceil(3.0), 0.001, "ceil(3.0) 应该等于 3");
        assertEquals(-3.0, mathService.ceil(-3.1), 0.001, "ceil(-3.1) 应该等于 -3");
    }

    @Test
    @DisplayName("floor - 向下取整测试")
    void testFloor() {
        assertEquals(3.0, mathService.floor(3.9), 0.001, "floor(3.9) 应该等于 3");
        assertEquals(3.0, mathService.floor(3.0), 0.001, "floor(3.0) 应该等于 3");
        assertEquals(-4.0, mathService.floor(-3.1), 0.001, "floor(-3.1) 应该等于 -4");
    }

    @Test
    @DisplayName("random - 随机数测试")
    void testRandom() {
        for (int i = 0; i < 100; i++) {
            double random = mathService.random();
            assertTrue(random >= 0.0 && random < 1.0, "随机数应该在[0,1)范围内");
        }
    }

    @Test
    @DisplayName("randomInt - 整数随机数测试")
    void testRandomInt() {
        for (int i = 0; i < 100; i++) {
            int random = mathService.randomInt(1, 10);
            assertTrue(random >= 1 && random <= 10, "随机整数应该在[1,10]范围内");
        }
        
        assertThrows(IllegalArgumentException.class, () -> mathService.randomInt(10, 1), "最小值大于最大值应该抛出异常");
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("边界值测试")
    void testBoundaryValues() {
        // 测试最大最小值
        assertEquals(Integer.MAX_VALUE, mathService.max(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, mathService.min(Integer.MAX_VALUE, Integer.MIN_VALUE));
        
        // 测试特殊值
        assertTrue(Double.isInfinite(mathService.log(0)));
        assertTrue(Double.isNaN(mathService.sqrt(-1)));
        assertTrue(Double.isNaN(mathService.log(-1)));
        
        // 测试零值
        assertEquals(0.0, mathService.sqrt(0), 0.001);
        assertEquals(0.0, mathService.cbrt(0), 0.001);
        assertEquals(1.0, mathService.power(0, 0), 0.001);
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("数学函数性能测试")
    void testMathFunctionsPerformance() {
        int iterations = 1000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            mathService.sqrt(i + 1);
            mathService.power(i + 1, 2);
            mathService.sin(i);
            mathService.factorial(Math.min(i % 10, 5));
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证性能在合理范围内
        assertTrue(duration < 500, "1000次数学函数调用应该在500ms内完成，实际耗时: " + duration + "ms");
    }

    // ==================== 组合数学函数测试 ====================

    @Test
    @DisplayName("combination - 组合数测试")
    void testCombination() {
        assertEquals(1, mathService.combination(5, 0), "C(5,0) 应该等于 1");
        assertEquals(5, mathService.combination(5, 1), "C(5,1) 应该等于 5");
        assertEquals(10, mathService.combination(5, 2), "C(5,2) 应该等于 10");
        assertEquals(10, mathService.combination(5, 3), "C(5,3) 应该等于 10");
        assertEquals(5, mathService.combination(5, 4), "C(5,4) 应该等于 5");
        assertEquals(1, mathService.combination(5, 5), "C(5,5) 应该等于 1");
        assertEquals(252, mathService.combination(10, 5), "C(10,5) 应该等于 252");
    }

    @Test
    @DisplayName("combination - 异常测试")
    void testCombinationExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.combination(-1, 2), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.combination(5, -1), "负数k应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.combination(3, 5), "k大于n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.combination(21, 10), "n过大应该抛出异常");
    }

    @Test
    @DisplayName("permutation - 排列数测试")
    void testPermutation() {
        assertEquals(1, mathService.permutation(5, 0), "P(5,0) 应该等于 1");
        assertEquals(5, mathService.permutation(5, 1), "P(5,1) 应该等于 5");
        assertEquals(20, mathService.permutation(5, 2), "P(5,2) 应该等于 20");
        assertEquals(60, mathService.permutation(5, 3), "P(5,3) 应该等于 60");
        assertEquals(120, mathService.permutation(5, 4), "P(5,4) 应该等于 120");
        assertEquals(120, mathService.permutation(5, 5), "P(5,5) 应该等于 120");
    }

    @Test
    @DisplayName("permutation - 异常测试")
    void testPermutationExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.permutation(-1, 2), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.permutation(5, -1), "负数k应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.permutation(3, 5), "k大于n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.permutation(21, 10), "n过大应该抛出异常");
    }

    // ==================== 特殊数列测试 ====================

    @Test
    @DisplayName("harmonicNumber - 调和数测试")
    void testHarmonicNumber() {
        assertEquals(1.0, mathService.harmonicNumber(1), 0.001, "H(1) 应该等于 1");
        assertEquals(1.5, mathService.harmonicNumber(2), 0.001, "H(2) 应该等于 1.5");
        assertEquals(1.833, mathService.harmonicNumber(3), 0.001, "H(3) 应该约等于 1.833");
        assertEquals(2.083, mathService.harmonicNumber(4), 0.001, "H(4) 应该约等于 2.083");
    }

    @Test
    @DisplayName("harmonicNumber - 异常测试")
    void testHarmonicNumberExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.harmonicNumber(0), "n=0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.harmonicNumber(-1), "负数n应该抛出异常");
    }

    @Test
    @DisplayName("bernoulliNumber - 伯努利数测试")
    void testBernoulliNumber() {
        assertEquals(1.0, mathService.bernoulliNumber(0), 0.001, "B(0) 应该等于 1");
        assertEquals(-0.5, mathService.bernoulliNumber(1), 0.001, "B(1) 应该等于 -0.5");
        assertEquals(1.0/6.0, mathService.bernoulliNumber(2), 0.001, "B(2) 应该等于 1/6");
        assertEquals(0.0, mathService.bernoulliNumber(3), 0.001, "B(3) 应该等于 0");
        assertEquals(-1.0/30.0, mathService.bernoulliNumber(4), 0.001, "B(4) 应该等于 -1/30");
    }

    @Test
    @DisplayName("bernoulliNumber - 异常测试")
    void testBernoulliNumberExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.bernoulliNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.bernoulliNumber(21), "n过大应该抛出异常");
    }

    @Test
    @DisplayName("eulerNumber - 欧拉数测试")
    void testEulerNumber() {
        assertEquals(1, mathService.eulerNumber(0), "E(0) 应该等于 1");
        assertEquals(0, mathService.eulerNumber(1), "E(1) 应该等于 0");
        assertEquals(-1, mathService.eulerNumber(2), "E(2) 应该等于 -1");
        assertEquals(0, mathService.eulerNumber(3), "E(3) 应该等于 0");
        assertEquals(5, mathService.eulerNumber(4), "E(4) 应该等于 5");
        assertEquals(0, mathService.eulerNumber(5), "E(5) 应该等于 0");
        assertEquals(-61, mathService.eulerNumber(6), "E(6) 应该等于 -61");
    }

    @Test
    @DisplayName("eulerNumber - 异常测试")
    void testEulerNumberExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.eulerNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.eulerNumber(11), "n过大应该抛出异常");
    }

    @Test
    @DisplayName("stirlingNumber - 斯特林数测试")
    void testStirlingNumber() {
        assertEquals(1, mathService.stirlingNumber(0, 0), "S(0,0) 应该等于 1");
        assertEquals(0, mathService.stirlingNumber(1, 0), "S(1,0) 应该等于 0");
        assertEquals(1, mathService.stirlingNumber(1, 1), "S(1,1) 应该等于 1");
        assertEquals(1, mathService.stirlingNumber(2, 1), "S(2,1) 应该等于 1");
        assertEquals(1, mathService.stirlingNumber(2, 2), "S(2,2) 应该等于 1");
        assertEquals(1, mathService.stirlingNumber(3, 1), "S(3,1) 应该等于 1");
        assertEquals(3, mathService.stirlingNumber(3, 2), "S(3,2) 应该等于 3");
        assertEquals(1, mathService.stirlingNumber(3, 3), "S(3,3) 应该等于 1");
    }

    @Test
    @DisplayName("stirlingNumber - 边界测试")
    void testStirlingNumberBoundary() {
        assertEquals(0, mathService.stirlingNumber(5, 0), "S(5,0) 应该等于 0");
        assertEquals(1, mathService.stirlingNumber(5, 1), "S(5,1) 应该等于 1");
        assertEquals(1, mathService.stirlingNumber(5, 5), "S(5,5) 应该等于 1");
        assertEquals(0, mathService.stirlingNumber(3, 5), "k>n时应该等于0");
    }

    @Test
    @DisplayName("catalanNumber - 卡特兰数测试")
    void testCatalanNumber() {
        assertEquals(1, mathService.catalanNumber(0), "C(0) 应该等于 1");
        assertEquals(1, mathService.catalanNumber(1), "C(1) 应该等于 1");
        assertEquals(2, mathService.catalanNumber(2), "C(2) 应该等于 2");
        assertEquals(5, mathService.catalanNumber(3), "C(3) 应该等于 5");
        assertEquals(14, mathService.catalanNumber(4), "C(4) 应该等于 14");
        assertEquals(42, mathService.catalanNumber(5), "C(5) 应该等于 42");
        assertEquals(132, mathService.catalanNumber(6), "C(6) 应该等于 132");
    }

    @Test
    @DisplayName("bellNumber - 贝尔数测试")
    void testBellNumber() {
        assertEquals(1, mathService.bellNumber(0), "B(0) 应该等于 1");
        assertEquals(1, mathService.bellNumber(1), "B(1) 应该等于 1");
        assertEquals(2, mathService.bellNumber(2), "B(2) 应该等于 2");
        assertEquals(5, mathService.bellNumber(3), "B(3) 应该等于 5");
        assertEquals(15, mathService.bellNumber(4), "B(4) 应该等于 15");
        assertEquals(52, mathService.bellNumber(5), "B(5) 应该等于 52");
    }

    @Test
    @DisplayName("partitionNumber - 分拆数测试")
    void testPartitionNumber() {
        assertEquals(1, mathService.partitionNumber(0), "P(0) 应该等于 1");
        assertEquals(1, mathService.partitionNumber(1), "P(1) 应该等于 1");
        assertEquals(2, mathService.partitionNumber(2), "P(2) 应该等于 2");
        assertEquals(3, mathService.partitionNumber(3), "P(3) 应该等于 3");
        assertEquals(5, mathService.partitionNumber(4), "P(4) 应该等于 5");
        assertEquals(7, mathService.partitionNumber(5), "P(5) 应该等于 7");
        assertEquals(11, mathService.partitionNumber(6), "P(6) 应该等于 11");
    }

    // ==================== 数论函数测试 ====================

    @Test
    @DisplayName("eulerTotient - 欧拉函数测试")
    void testEulerTotient() {
        assertEquals(1, mathService.eulerTotient(1), "φ(1) 应该等于 1");
        assertEquals(1, mathService.eulerTotient(2), "φ(2) 应该等于 1");
        assertEquals(2, mathService.eulerTotient(3), "φ(3) 应该等于 2");
        assertEquals(2, mathService.eulerTotient(4), "φ(4) 应该等于 2");
        assertEquals(4, mathService.eulerTotient(5), "φ(5) 应该等于 4");
        assertEquals(2, mathService.eulerTotient(6), "φ(6) 应该等于 2");
        assertEquals(6, mathService.eulerTotient(7), "φ(7) 应该等于 6");
        assertEquals(4, mathService.eulerTotient(8), "φ(8) 应该等于 4");
        assertEquals(6, mathService.eulerTotient(9), "φ(9) 应该等于 6");
        assertEquals(4, mathService.eulerTotient(10), "φ(10) 应该等于 4");
    }

    @Test
    @DisplayName("eulerTotient - 异常测试")
    void testEulerTotientExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.eulerTotient(0), "n=0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.eulerTotient(-1), "负数n应该抛出异常");
    }

    @Test
    @DisplayName("mobiusFunction - 莫比乌斯函数测试")
    void testMobiusFunction() {
        assertEquals(1, mathService.mobiusFunction(1), "μ(1) 应该等于 1");
        assertEquals(-1, mathService.mobiusFunction(2), "μ(2) 应该等于 -1");
        assertEquals(-1, mathService.mobiusFunction(3), "μ(3) 应该等于 -1");
        assertEquals(0, mathService.mobiusFunction(4), "μ(4) 应该等于 0");
        assertEquals(-1, mathService.mobiusFunction(5), "μ(5) 应该等于 -1");
        assertEquals(1, mathService.mobiusFunction(6), "μ(6) 应该等于 1");
        assertEquals(-1, mathService.mobiusFunction(7), "μ(7) 应该等于 -1");
        assertEquals(0, mathService.mobiusFunction(8), "μ(8) 应该等于 0");
        assertEquals(0, mathService.mobiusFunction(9), "μ(9) 应该等于 0");
        assertEquals(1, mathService.mobiusFunction(10), "μ(10) 应该等于 1");
    }

    @Test
    @DisplayName("mobiusFunction - 异常测试")
    void testMobiusFunctionExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.mobiusFunction(0), "n=0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.mobiusFunction(-1), "负数n应该抛出异常");
    }

    @Test
    @DisplayName("legendreSymbol - 勒让德符号测试")
    void testLegendreSymbol() {
        assertEquals(1, mathService.legendreSymbol(1, 3), "(1/3) 应该等于 1");
        assertEquals(-1, mathService.legendreSymbol(2, 3), "(2/3) 应该等于 -1");
        assertEquals(0, mathService.legendreSymbol(3, 3), "(3/3) 应该等于 0");
        assertEquals(1, mathService.legendreSymbol(1, 5), "(1/5) 应该等于 1");
        assertEquals(-1, mathService.legendreSymbol(2, 5), "(2/5) 应该等于 -1");
        assertEquals(-1, mathService.legendreSymbol(3, 5), "(3/5) 应该等于 -1");
        assertEquals(1, mathService.legendreSymbol(4, 5), "(4/5) 应该等于 1");
    }

    @Test
    @DisplayName("legendreSymbol - 异常测试")
    void testLegendreSymbolExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.legendreSymbol(1, 2), "p为偶数应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.legendreSymbol(1, 0), "p=0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.legendreSymbol(1, -3), "p为负数应该抛出异常");
    }

    @Test
    @DisplayName("jacobiSymbol - 雅可比符号测试")
    void testJacobiSymbol() {
        assertEquals(1, mathService.jacobiSymbol(1, 3), "(1/3) 应该等于 1");
        assertEquals(-1, mathService.jacobiSymbol(2, 3), "(2/3) 应该等于 -1");
        assertEquals(0, mathService.jacobiSymbol(3, 3), "(3/3) 应该等于 0");
        assertEquals(1, mathService.jacobiSymbol(1, 9), "(1/9) 应该等于 1");
        assertEquals(1, mathService.jacobiSymbol(2, 9), "(2/9) 应该等于 1");
        assertEquals(0, mathService.jacobiSymbol(3, 9), "(3/9) 应该等于 0");
    }

    @Test
    @DisplayName("jacobiSymbol - 异常测试")
    void testJacobiSymbolExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.jacobiSymbol(1, 2), "n为偶数应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.jacobiSymbol(1, 0), "n=0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.jacobiSymbol(1, -3), "n为负数应该抛出异常");
    }

    // ==================== 连分数测试 ====================

    @Test
    @DisplayName("continuedFraction - 连分数展开测试")
    void testContinuedFraction() {
        int[] cf1 = mathService.continuedFraction(Math.PI, 5);
        assertEquals(3, cf1[0], "π的连分数第一项应该等于3");
        assertEquals(7, cf1[1], "π的连分数第二项应该等于7");
        assertEquals(15, cf1[2], "π的连分数第三项应该等于15");
        
        int[] cf2 = mathService.continuedFraction(Math.sqrt(2), 5);
        assertEquals(1, cf2[0], "√2的连分数第一项应该等于1");
        assertEquals(2, cf2[1], "√2的连分数第二项应该等于2");
        assertEquals(2, cf2[2], "√2的连分数第三项应该等于2");
    }

    @Test
    @DisplayName("continuedFractionValue - 连分数值测试")
    void testContinuedFractionValue() {
        int[] coefficients = {3, 7, 15, 1, 292};
        double value = mathService.continuedFractionValue(coefficients);
        assertTrue(Math.abs(value - Math.PI) < 0.01, "连分数值应该接近π");
        
        int[] coefficients2 = {1, 2, 2, 2, 2};
        double value2 = mathService.continuedFractionValue(coefficients2);
        assertTrue(Math.abs(value2 - Math.sqrt(2)) < 0.01, "连分数值应该接近√2");
    }

    @Test
    @DisplayName("continuedFraction - 异常测试")
    void testContinuedFractionExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.continuedFraction(Math.PI, 0), "最大项数为0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.continuedFraction(Math.PI, -1), "最大项数为负数应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.continuedFractionValue(null), "系数数组为null应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.continuedFractionValue(new int[0]), "系数数组为空应该抛出异常");
    }

    // ==================== 特殊常数测试 ====================

    @Test
    @DisplayName("goldenRatio - 黄金分割比测试")
    void testGoldenRatio() {
        double phi = mathService.goldenRatio();
        assertEquals(1.618033988749895, phi, 0.000000000000001, "黄金分割比应该等于(1+√5)/2");
        assertTrue(Math.abs(phi * phi - phi - 1) < 1e-15, "黄金分割比应该满足φ²=φ+1");
    }

    @Test
    @DisplayName("silverRatio - 白银分割比测试")
    void testSilverRatio() {
        double silver = mathService.silverRatio();
        assertEquals(2.414213562373095, silver, 0.000000000000001, "白银分割比应该等于1+√2");
        assertTrue(Math.abs(silver * silver - 2 * silver - 1) < 1e-15, "白银分割比应该满足δ²=2δ+1");
    }

    @Test
    @DisplayName("bronzeRatio - 青铜分割比测试")
    void testBronzeRatio() {
        double bronze = mathService.bronzeRatio();
        assertEquals(3.302775637731995, bronze, 0.000000000000001, "青铜分割比应该等于(3+√13)/2");
    }

    // ==================== 数列测试 ====================

    @Test
    @DisplayName("fibonacciRatio - 斐波那契比值测试")
    void testFibonacciRatio() {
        double ratio = mathService.fibonacciRatio(10);
        assertTrue(Math.abs(ratio - mathService.goldenRatio()) < 0.1, "斐波那契比值应该接近黄金分割比");
        
        double ratio2 = mathService.fibonacciRatio(20);
        assertTrue(Math.abs(ratio2 - mathService.goldenRatio()) < 0.01, "更大的斐波那契比值应该更接近黄金分割比");
    }

    @Test
    @DisplayName("fibonacciRatio - 异常测试")
    void testFibonacciRatioExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.fibonacciRatio(0), "n=0应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.fibonacciRatio(1), "n=1应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.fibonacciRatio(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.fibonacciRatio(51), "n过大应该抛出异常");
    }

    @Test
    @DisplayName("lucasNumber - 卢卡斯数列测试")
    void testLucasNumber() {
        assertEquals(2, mathService.lucasNumber(0), "L(0) 应该等于 2");
        assertEquals(1, mathService.lucasNumber(1), "L(1) 应该等于 1");
        assertEquals(3, mathService.lucasNumber(2), "L(2) 应该等于 3");
        assertEquals(4, mathService.lucasNumber(3), "L(3) 应该等于 4");
        assertEquals(7, mathService.lucasNumber(4), "L(4) 应该等于 7");
        assertEquals(11, mathService.lucasNumber(5), "L(5) 应该等于 11");
        assertEquals(18, mathService.lucasNumber(6), "L(6) 应该等于 18");
        assertEquals(29, mathService.lucasNumber(7), "L(7) 应该等于 29");
    }

    @Test
    @DisplayName("pellNumber - 佩尔数列测试")
    void testPellNumber() {
        assertEquals(0, mathService.pellNumber(0), "P(0) 应该等于 0");
        assertEquals(1, mathService.pellNumber(1), "P(1) 应该等于 1");
        assertEquals(2, mathService.pellNumber(2), "P(2) 应该等于 2");
        assertEquals(5, mathService.pellNumber(3), "P(3) 应该等于 5");
        assertEquals(12, mathService.pellNumber(4), "P(4) 应该等于 12");
        assertEquals(29, mathService.pellNumber(5), "P(5) 应该等于 29");
        assertEquals(70, mathService.pellNumber(6), "P(6) 应该等于 70");
    }

    // ==================== 多边形数测试 ====================

    @Test
    @DisplayName("triangularNumber - 三角数测试")
    void testTriangularNumber() {
        assertEquals(0, mathService.triangularNumber(0), "T(0) 应该等于 0");
        assertEquals(1, mathService.triangularNumber(1), "T(1) 应该等于 1");
        assertEquals(3, mathService.triangularNumber(2), "T(2) 应该等于 3");
        assertEquals(6, mathService.triangularNumber(3), "T(3) 应该等于 6");
        assertEquals(10, mathService.triangularNumber(4), "T(4) 应该等于 10");
        assertEquals(15, mathService.triangularNumber(5), "T(5) 应该等于 15");
        assertEquals(21, mathService.triangularNumber(6), "T(6) 应该等于 21");
    }

    @Test
    @DisplayName("squareNumber - 平方数测试")
    void testSquareNumber() {
        assertEquals(0, mathService.squareNumber(0), "S(0) 应该等于 0");
        assertEquals(1, mathService.squareNumber(1), "S(1) 应该等于 1");
        assertEquals(4, mathService.squareNumber(2), "S(2) 应该等于 4");
        assertEquals(9, mathService.squareNumber(3), "S(3) 应该等于 9");
        assertEquals(16, mathService.squareNumber(4), "S(4) 应该等于 16");
        assertEquals(25, mathService.squareNumber(5), "S(5) 应该等于 25");
        assertEquals(36, mathService.squareNumber(6), "S(6) 应该等于 36");
    }

    @Test
    @DisplayName("cubeNumber - 立方数测试")
    void testCubeNumber() {
        assertEquals(0, mathService.cubeNumber(0), "C(0) 应该等于 0");
        assertEquals(1, mathService.cubeNumber(1), "C(1) 应该等于 1");
        assertEquals(8, mathService.cubeNumber(2), "C(2) 应该等于 8");
        assertEquals(27, mathService.cubeNumber(3), "C(3) 应该等于 27");
        assertEquals(64, mathService.cubeNumber(4), "C(4) 应该等于 64");
        assertEquals(125, mathService.cubeNumber(5), "C(5) 应该等于 125");
        assertEquals(216, mathService.cubeNumber(6), "C(6) 应该等于 216");
    }

    @Test
    @DisplayName("pentagonalNumber - 五边形数测试")
    void testPentagonalNumber() {
        assertEquals(0, mathService.pentagonalNumber(0), "P(0) 应该等于 0");
        assertEquals(1, mathService.pentagonalNumber(1), "P(1) 应该等于 1");
        assertEquals(5, mathService.pentagonalNumber(2), "P(2) 应该等于 5");
        assertEquals(12, mathService.pentagonalNumber(3), "P(3) 应该等于 12");
        assertEquals(22, mathService.pentagonalNumber(4), "P(4) 应该等于 22");
        assertEquals(35, mathService.pentagonalNumber(5), "P(5) 应该等于 35");
        assertEquals(51, mathService.pentagonalNumber(6), "P(6) 应该等于 51");
    }

    @Test
    @DisplayName("hexagonalNumber - 六边形数测试")
    void testHexagonalNumber() {
        assertEquals(0, mathService.hexagonalNumber(0), "H(0) 应该等于 0");
        assertEquals(1, mathService.hexagonalNumber(1), "H(1) 应该等于 1");
        assertEquals(6, mathService.hexagonalNumber(2), "H(2) 应该等于 6");
        assertEquals(15, mathService.hexagonalNumber(3), "H(3) 应该等于 15");
        assertEquals(28, mathService.hexagonalNumber(4), "H(4) 应该等于 28");
        assertEquals(45, mathService.hexagonalNumber(5), "H(5) 应该等于 45");
        assertEquals(66, mathService.hexagonalNumber(6), "H(6) 应该等于 66");
    }

    @Test
    @DisplayName("heptagonalNumber - 七边形数测试")
    void testHeptagonalNumber() {
        assertEquals(0, mathService.heptagonalNumber(0), "H(0) 应该等于 0");
        assertEquals(1, mathService.heptagonalNumber(1), "H(1) 应该等于 1");
        assertEquals(7, mathService.heptagonalNumber(2), "H(2) 应该等于 7");
        assertEquals(18, mathService.heptagonalNumber(3), "H(3) 应该等于 18");
        assertEquals(34, mathService.heptagonalNumber(4), "H(4) 应该等于 34");
        assertEquals(55, mathService.heptagonalNumber(5), "H(5) 应该等于 55");
        assertEquals(81, mathService.heptagonalNumber(6), "H(6) 应该等于 81");
    }

    @Test
    @DisplayName("octagonalNumber - 八边形数测试")
    void testOctagonalNumber() {
        assertEquals(0, mathService.octagonalNumber(0), "O(0) 应该等于 0");
        assertEquals(1, mathService.octagonalNumber(1), "O(1) 应该等于 1");
        assertEquals(8, mathService.octagonalNumber(2), "O(2) 应该等于 8");
        assertEquals(21, mathService.octagonalNumber(3), "O(3) 应该等于 21");
        assertEquals(40, mathService.octagonalNumber(4), "O(4) 应该等于 40");
        assertEquals(65, mathService.octagonalNumber(5), "O(5) 应该等于 65");
        assertEquals(96, mathService.octagonalNumber(6), "O(6) 应该等于 96");
    }

    // ==================== 多边形数异常测试 ====================

    @Test
    @DisplayName("多边形数异常测试")
    void testPolygonalNumberExceptions() {
        assertThrows(IllegalArgumentException.class, () -> mathService.triangularNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.squareNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.cubeNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.pentagonalNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.hexagonalNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.heptagonalNumber(-1), "负数n应该抛出异常");
        assertThrows(IllegalArgumentException.class, () -> mathService.octagonalNumber(-1), "负数n应该抛出异常");
    }

    // ==================== 综合测试 ====================

    @Test
    @DisplayName("数学函数关系测试")
    void testMathematicalRelations() {
        // 测试组合数和排列数的关系
        assertEquals(mathService.permutation(5, 3), 
                     mathService.combination(5, 3) * mathService.factorial(3), 
                     "P(n,k) = C(n,k) * k!");
        
        // 测试贝尔数和斯特林数的关系
        long bellSum = 0;
        for (int k = 0; k <= 5; k++) {
            bellSum += mathService.stirlingNumber(5, k);
        }
        assertEquals(mathService.bellNumber(5), bellSum, "B(n) = ΣS(n,k)");
        
        // 测试卡特兰数的递推关系
        long catalanSum = 0;
        for (int i = 0; i < 4; i++) {
            catalanSum += mathService.catalanNumber(i) * mathService.catalanNumber(3 - i);
        }
        assertEquals(mathService.catalanNumber(4), catalanSum, "C(n) = ΣC(i)*C(n-1-i)");
    }

    @Test
    @DisplayName("特殊数列关系测试")
    void testSpecialSequenceRelations() {
        // 测试斐波那契数列和卢卡斯数列的关系
        assertEquals(mathService.fibonacci(5) + mathService.fibonacci(7), 
                     mathService.lucasNumber(6), 
                     "L(n) = F(n-1) + F(n+1)");
        
        // 测试佩尔数列的递推关系
        assertEquals(mathService.pellNumber(4), 
                     2 * mathService.pellNumber(3) + mathService.pellNumber(2), 
                     "P(n) = 2*P(n-1) + P(n-2)");
    }

    @Test
    @DisplayName("数论函数关系测试")
    void testNumberTheoryRelations() {
        // 测试欧拉函数的性质
        assertEquals(1, mathService.eulerTotient(1), "φ(1) = 1");
        assertEquals(1, mathService.eulerTotient(2), "φ(2) = 1");
        
        // 测试莫比乌斯函数的性质
        assertEquals(1, mathService.mobiusFunction(1), "μ(1) = 1");
        assertEquals(-1, mathService.mobiusFunction(2), "μ(2) = -1");
        assertEquals(0, mathService.mobiusFunction(4), "μ(4) = 0");

    }

    @Test
    @DisplayName("高级数学函数性能测试")
    void testAdvancedMathFunctionsPerformance() {
        int iterations = 100;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            mathService.combination(10, 5);
            mathService.permutation(8, 4);
            mathService.catalanNumber(8);
            mathService.bellNumber(8);
            mathService.stirlingNumber(8, 4);
            mathService.eulerTotient(100);
            mathService.mobiusFunction(100);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证性能在合理范围内
        assertTrue(duration < 1000, "100次高级数学函数调用应该在1000ms内完成，实际耗时: " + duration + "ms");
    }
} 