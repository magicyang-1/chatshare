package com.aiplatform.service;

public class MathService {
    
    // ========== 基础数学函数 ==========
    
    /**
     * 计算两个数的最大值
     */
    public int max(int a, int b) {
        return Math.max(a, b);
    }
    
    /**
     * 计算两个数的最小值
     */
    public int min(int a, int b) {
        return Math.min(a, b);
    }
    
    /**
     * 计算绝对值
     */
    public int abs(int a) {
        return Math.abs(a);
    }
    
    /**
     * 计算幂运算
     */
    public double power(double base, double exponent) {
        return Math.pow(base, exponent);
    }
    
    /**
     * 计算平方根
     */
    public double sqrt(double a) {
        return Math.sqrt(a);
    }
    
    /**
     * 计算立方根
     */
    public double cbrt(double a) {
        return Math.cbrt(a);
    }
    
    /**
     * 计算自然对数
     */
    public double log(double a) {
        return Math.log(a);
    }
    
    /**
     * 计算以10为底的对数
     */
    public double log10(double a) {
        return Math.log10(a);
    }
    
    /**
     * 计算正弦值
     */
    public double sin(double a) {
        return Math.sin(a);
    }
    
    /**
     * 计算余弦值
     */
    public double cos(double a) {
        return Math.cos(a);
    }
    
    /**
     * 计算正切值
     */
    public double tan(double a) {
        return Math.tan(a);
    }
    
    /**
     * 计算反正弦值
     */
    public double asin(double a) {
        return Math.asin(a);
    }
    
    /**
     * 计算反余弦值
     */
    public double acos(double a) {
        return Math.acos(a);
    }
    
    /**
     * 计算反正切值
     */
    public double atan(double a) {
        return Math.atan(a);
    }
    
    /**
     * 计算双曲正弦值
     */
    public double sinh(double a) {
        return Math.sinh(a);
    }
    
    /**
     * 计算双曲余弦值
     */
    public double cosh(double a) {
        return Math.cosh(a);
    }
    
    /**
     * 计算双曲正切值
     */
    public double tanh(double a) {
        return Math.tanh(a);
    }
    
    // ========== 高级数学函数 ==========
    
    /**
     * 计算阶乘
     */
    public long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("阶乘不能计算负数");
        }
        if (n > 20) {
            throw new IllegalArgumentException("阶乘数值过大，可能导致溢出");
        }
        if (n == 0 || n == 1) {
            return 1;
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * 计算斐波那契数列第n项
     */
    public long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("斐波那契数列不能计算负数项");
        }
        if (n > 92) {
            throw new IllegalArgumentException("斐波那契数列数值过大，可能导致溢出");
        }
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
    
    /**
     * 计算最大公约数
     */
    public int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * 计算最小公倍数
     */
    public int lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return Math.abs(a * b) / gcd(a, b);
    }
    
    /**
     * 判断是否为质数
     */
    public boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 计算圆的面积
     */
    public double circleArea(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("半径不能为负数");
        }
        return Math.PI * radius * radius;
    }
    
    /**
     * 计算圆的周长
     */
    public double circleCircumference(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("半径不能为负数");
        }
        return 2 * Math.PI * radius;
    }
    
    /**
     * 计算矩形面积
     */
    public double rectangleArea(double length, double width) {
        if (length < 0 || width < 0) {
            throw new IllegalArgumentException("长度和宽度不能为负数");
        }
        return length * width;
    }
    
    /**
     * 计算矩形周长
     */
    public double rectanglePerimeter(double length, double width) {
        if (length < 0 || width < 0) {
            throw new IllegalArgumentException("长度和宽度不能为负数");
        }
        return 2 * (length + width);
    }
    
    /**
     * 计算三角形面积（海伦公式）
     */
    public double triangleArea(double a, double b, double c) {
        if (a <= 0 || b <= 0 || c <= 0) {
            throw new IllegalArgumentException("三角形边长必须为正数");
        }
        if (a + b <= c || a + c <= b || b + c <= a) {
            throw new IllegalArgumentException("三角形边长不满足三角形不等式");
        }
        double s = (a + b + c) / 2;
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }
    
    /**
     * 计算平均值
     */
    public double average(double... numbers) {
        if (numbers.length == 0) {
            throw new IllegalArgumentException("至少需要一个数字");
        }
        double sum = 0;
        for (double num : numbers) {
            sum += num;
        }
        return sum / numbers.length;
    }
    
    /**
     * 计算标准差
     */
    public double standardDeviation(double... numbers) {
        if (numbers.length < 2) {
            throw new IllegalArgumentException("至少需要两个数字来计算标准差");
        }
        double avg = average(numbers);
        double sumSquaredDiff = 0;
        for (double num : numbers) {
            sumSquaredDiff += Math.pow(num - avg, 2);
        }
        return Math.sqrt(sumSquaredDiff / (numbers.length - 1));
    }
    
    /**
     * 计算方差
     */
    public double variance(double... numbers) {
        if (numbers.length < 2) {
            throw new IllegalArgumentException("至少需要两个数字来计算方差");
        }
        double avg = average(numbers);
        double sumSquaredDiff = 0;
        for (double num : numbers) {
            sumSquaredDiff += Math.pow(num - avg, 2);
        }
        return sumSquaredDiff / (numbers.length - 1);
    }
    
    /**
     * 四舍五入到指定小数位
     */
    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("小数位数不能为负数");
        }
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
    
    /**
     * 向上取整
     */
    public double ceil(double a) {
        return Math.ceil(a);
    }
    
    /**
     * 向下取整
     */
    public double floor(double a) {
        return Math.floor(a);
    }
    
    /**
     * 随机数生成（0到1之间）
     */
    public double random() {
        return Math.random();
    }
    
    /**
     * 随机数生成（指定范围内）
     */
    public int randomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("最小值不能大于最大值");
        }
        return min + (int) (Math.random() * (max - min + 1));
    }

    // ========== 高级数学函数扩展 ==========

    /**
     * 计算组合数 C(n,k)
     */
    public long combination(int n, int k) {
        if (n < 0 || k < 0) {
            throw new IllegalArgumentException("参数不能为负数");
        }
        if (k > n) {
            throw new IllegalArgumentException("k不能大于n");
        }
        if (n > 20) {
            throw new IllegalArgumentException("n值过大，可能导致溢出");
        }
        
        if (k == 0 || k == n) {
            return 1;
        }
        if (k > n - k) {
            k = n - k; // 利用对称性
        }
        
        long result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }

    /**
     * 计算排列数 P(n,k)
     */
    public long permutation(int n, int k) {
        if (n < 0 || k < 0) {
            throw new IllegalArgumentException("参数不能为负数");
        }
        if (k > n) {
            throw new IllegalArgumentException("k不能大于n");
        }
        if (n > 20) {
            throw new IllegalArgumentException("n值过大，可能导致溢出");
        }
        
        long result = 1;
        for (int i = 0; i < k; i++) {
            result *= (n - i);
        }
        return result;
    }

    /**
     * 计算调和数 H(n)
     */
    public double harmonicNumber(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n必须为正整数");
        }
        
        double result = 0.0;
        for (int i = 1; i <= n; i++) {
            result += 1.0 / i;
        }
        return result;
    }

    /**
     * 计算伯努利数 B(n)
     */
    public double bernoulliNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        if (n > 20) {
            throw new IllegalArgumentException("n值过大，计算复杂");
        }
        
        if (n == 0) return 1.0;
        if (n == 1) return -0.5;
        if (n % 2 == 1) return 0.0; // 奇数项为0
        
        // 简化的伯努利数计算（仅计算前几个偶数项）
        switch (n) {
            case 2: return 1.0/6.0;
            case 4: return -1.0/30.0;
            case 6: return 1.0/42.0;
            case 8: return -1.0/30.0;
            case 10: return 5.0/66.0;
            case 12: return -691.0/2730.0;
            case 14: return 7.0/6.0;
            case 16: return -3617.0/510.0;
            case 18: return 43867.0/798.0;
            case 20: return -174611.0/330.0;
            default: throw new IllegalArgumentException("暂不支持计算B(" + n + ")");
        }
    }

    /**
     * 计算欧拉数 E(n)
     */
    public long eulerNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        if (n > 10) {
            throw new IllegalArgumentException("n值过大，计算复杂");
        }
        
        // 前几个欧拉数
        switch (n) {
            case 0: return 1;
            case 1: return 0;
            case 2: return -1;
            case 3: return 0;
            case 4: return 5;
            case 5: return 0;
            case 6: return -61;
            case 7: return 0;
            case 8: return 1385;
            case 9: return 0;
            case 10: return -50521;
            default: throw new IllegalArgumentException("暂不支持计算E(" + n + ")");
        }
    }

    /**
     * 计算斯特林数 S(n,k) - 第二类斯特林数
     */
    public long stirlingNumber(int n, int k) {
        if (n < 0 || k < 0) {
            throw new IllegalArgumentException("参数不能为负数");
        }
        if (k > n) {
            return 0;
        }
        if (n > 20) {
            throw new IllegalArgumentException("n值过大，可能导致溢出");
        }
        
        if (k == 0) {
            return n == 0 ? 1 : 0;
        }
        if (k == 1) {
            return 1;
        }
        if (k == n) {
            return 1;
        }
        
        // 使用递推公式 S(n,k) = k*S(n-1,k) + S(n-1,k-1)
        long[][] dp = new long[n + 1][k + 1];
        dp[0][0] = 1;
        
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= k; j++) {
                dp[i][j] = j * dp[i - 1][j] + dp[i - 1][j - 1];
            }
        }
        
        return dp[n][k];
    }

    /**
     * 计算卡特兰数 C(n)
     */
    public long catalanNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        if (n > 20) {
            throw new IllegalArgumentException("n值过大，可能导致溢出");
        }
        
        if (n == 0) return 1;
        
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result = result * (4 * i - 2) / (i + 1);
        }
        return result;
    }

    /**
     * 计算贝尔数 B(n)
     */
    public long bellNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        if (n > 20) {
            throw new IllegalArgumentException("n值过大，可能导致溢出");
        }
        
        if (n == 0) return 1;
        
        long result = 0;
        for (int k = 0; k <= n; k++) {
            result += stirlingNumber(n, k);
        }
        return result;
    }

    /**
     * 计算分拆数 P(n)
     */
    public long partitionNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        if (n > 30) {
            throw new IllegalArgumentException("n值过大，计算复杂");
        }
        
        if (n == 0) return 1;
        
        // 使用动态规划计算分拆数
        long[] dp = new long[n + 1];
        dp[0] = 1;
        
        for (int i = 1; i <= n; i++) {
            for (int j = i; j <= n; j++) {
                dp[j] += dp[j - i];
            }
        }
        
        return dp[n];
    }

    /**
     * 计算欧拉函数 φ(n)
     */
    public int eulerTotient(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n必须为正整数");
        }
        
        int result = n;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                while (n % i == 0) {
                    n /= i;
                }
                result -= result / i;
            }
        }
        if (n > 1) {
            result -= result / n;
        }
        return result;
    }

    /**
     * 计算莫比乌斯函数 μ(n)
     */
    public int mobiusFunction(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n必须为正整数");
        }
        
        if (n == 1) return 1;
        
        int count = 0;
        int squareFree = 1;
        
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                count++;
                int power = 0;
                while (n % i == 0) {
                    n /= i;
                    power++;
                }
                if (power > 1) {
                    return 0; // 有平方因子
                }
                squareFree *= i;
            }
        }
        
        if (n > 1) {
            count++;
            squareFree *= n;
        }
        
        return count % 2 == 0 ? 1 : -1;
    }

    /**
     * 计算勒让德符号 (a/p)
     */
    public int legendreSymbol(int a, int p) {
        if (p <= 0 || p % 2 == 0) {
            throw new IllegalArgumentException("p必须为正奇数");
        }
        
        if (a % p == 0) return 0;
        
        int result = 1;
        if (a < 0) {
            a = -a;
            if (p % 4 == 3) {
                result = -result;
            }
        }
        
        while (a != 0) {
            while (a % 2 == 0) {
                a /= 2;
                if (p % 8 == 3 || p % 8 == 5) {
                    result = -result;
                }
            }
            
            if (a % 4 == 3 && p % 4 == 3) {
                result = -result;
            }
            
            int temp = a;
            a = p % temp;
            p = temp;
        }
        
        return p == 1 ? result : 0;
    }

    /**
     * 计算雅可比符号 (a/n)
     */
    public int jacobiSymbol(int a, int n) {
        if (n <= 0 || n % 2 == 0) {
            throw new IllegalArgumentException("n必须为正奇数");
        }
        
        if (gcd(a, n) != 1) return 0;
        
        int result = 1;
        while (a != 0) {
            while (a % 2 == 0) {
                a /= 2;
                if (n % 8 == 3 || n % 8 == 5) {
                    result = -result;
                }
            }
            
            if (a % 4 == 3 && n % 4 == 3) {
                result = -result;
            }
            
            int temp = a;
            a = n % temp;
            n = temp;
        }
        
        return n == 1 ? result : 0;
    }

    /**
     * 计算连分数展开
     */
    public int[] continuedFraction(double x, int maxTerms) {
        if (maxTerms <= 0) {
            throw new IllegalArgumentException("最大项数必须为正数");
        }
        
        int[] result = new int[maxTerms];
        double current = x;
        
        for (int i = 0; i < maxTerms; i++) {
            int integerPart = (int) Math.floor(current);
            result[i] = integerPart;
            
            double fractionalPart = current - integerPart;
            if (Math.abs(fractionalPart) < 1e-10) {
                // 已经得到精确的整数
                break;
            }
            current = 1.0 / fractionalPart;
        }
        
        return result;
    }

    /**
     * 计算连分数的收敛值
     */
    public double continuedFractionValue(int[] coefficients) {
        if (coefficients == null || coefficients.length == 0) {
            throw new IllegalArgumentException("系数数组不能为空");
        }
        
        double result = coefficients[coefficients.length - 1];
        for (int i = coefficients.length - 2; i >= 0; i--) {
            result = coefficients[i] + 1.0 / result;
        }
        
        return result;
    }

    /**
     * 计算黄金分割比
     */
    public double goldenRatio() {
        return (1 + Math.sqrt(5)) / 2;
    }

    /**
     * 计算白银分割比
     */
    public double silverRatio() {
        return 1 + Math.sqrt(2);
    }

    /**
     * 计算青铜分割比
     */
    public double bronzeRatio() {
        return (3 + Math.sqrt(13)) / 2;
    }

    /**
     * 计算斐波那契数列的比值（相邻两项的比值）
     */
    public double fibonacciRatio(int n) {
        if (n <= 1) {
            throw new IllegalArgumentException("n必须大于1");
        }
        if (n > 50) {
            throw new IllegalArgumentException("n值过大，可能导致精度问题");
        }
        
        long fibN = fibonacci(n);
        long fibNMinus1 = fibonacci(n - 1);
        
        return (double) fibN / fibNMinus1;
    }

    /**
     * 计算卢卡斯数列第n项
     */
    public long lucasNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("卢卡斯数列不能计算负数项");
        }
        if (n > 50) {
            throw new IllegalArgumentException("卢卡斯数列数值过大，可能导致溢出");
        }
        
        if (n == 0) return 2;
        if (n == 1) return 1;
        
        long a = 2, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }

    /**
     * 计算佩尔数列第n项
     */
    public long pellNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("佩尔数列不能计算负数项");
        }
        if (n > 40) {
            throw new IllegalArgumentException("佩尔数列数值过大，可能导致溢出");
        }
        
        if (n == 0) return 0;
        if (n == 1) return 1;
        
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = 2 * b + a;
            a = b;
            b = temp;
        }
        return b;
    }

    /**
     * 计算三角数 T(n)
     */
    public long triangularNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * (n + 1) / 2;
    }

    /**
     * 计算平方数 S(n)
     */
    public long squareNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * n;
    }

    /**
     * 计算立方数 C(n)
     */
    public long cubeNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * n * n;
    }

    /**
     * 计算五边形数 P(n)
     */
    public long pentagonalNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * (3 * n - 1) / 2;
    }

    /**
     * 计算六边形数 H(n)
     */
    public long hexagonalNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * (2 * n - 1);
    }

    /**
     * 计算七边形数 H(n)
     */
    public long heptagonalNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * (5 * n - 3) / 2;
    }

    /**
     * 计算八边形数 O(n)
     */
    public long octagonalNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n不能为负数");
        }
        return (long) n * (3 * n - 2);
    }

int plus1(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        
        return a + b;
    }
    int minus1(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a - b;
    }
    int plus3(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
    int minus3(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a - b;
    }
    int plus4(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
    int minus4(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a - b;
    }
    int plus5(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
    int plus6(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
    int plus9(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
    int plus7(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
    int plus8(int a, int b) {
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        a = a;
        return a + b;
    }
}
