# Exception 包单元测试

## 概述

本目录包含 `exception` 包中所有类的单元测试，使用 Mock 技术进行测试。

## 测试文件

### 1. BusinessExceptionTest.java
测试 `BusinessException` 类的所有构造函数和功能。

**测试覆盖：**
- 所有构造函数的正确性
- getter 方法的返回值
- 异常继承关系
- null 值和空值处理
- 边界情况测试

**测试方法：**
- `testConstructorWithMessage()` - 测试带消息的构造函数
- `testConstructorWithCodeAndMessage()` - 测试带代码和消息的构造函数
- `testConstructorWithMessageAndCause()` - 测试带消息和原因的构造函数
- `testConstructorWithCodeMessageAndData()` - 测试带代码、消息和数据的构造函数
- `testGetCode()` - 测试获取代码
- `testGetData()` - 测试获取数据
- `testExceptionInheritance()` - 测试异常继承关系
- `testNullValues()` - 测试空值处理
- `testEmptyValues()` - 测试空字符串处理

### 2. GlobalExceptionHandlerTest.java
测试 `GlobalExceptionHandler` 类的异常处理功能。

**测试覆盖：**
- 验证异常处理
- 业务异常处理
- 全局异常处理
- 运行时异常处理
- 空值和边界情况
- 通过控制器的异常处理

**测试方法：**
- `testHandleValidationExceptions()` - 测试验证异常处理
- `testHandleBusinessException()` - 测试业务异常处理
- `testHandleGlobalException()` - 测试全局异常处理
- `testHandleRuntimeException()` - 测试运行时异常处理
- `testHandleBusinessExceptionWithNullMessage()` - 测试空消息的业务异常
- `testHandleGlobalExceptionWithNullMessage()` - 测试空消息的全局异常
- `testHandleRuntimeExceptionWithNullMessage()` - 测试空消息的运行时异常
- `testHandleValidationExceptionsWithEmptyErrors()` - 测试空验证错误
- `testHandleBusinessExceptionWithData()` - 测试带数据的业务异常
- `testBusinessExceptionThroughController()` - 通过控制器测试业务异常
- `testRuntimeExceptionThroughController()` - 通过控制器测试运行时异常
- `testGeneralExceptionThroughController()` - 通过控制器测试一般异常

## 测试技术

### Mock 技术使用
- 使用 `Mockito` 创建模拟对象
- 模拟 `MethodArgumentNotValidException` 和 `BindingResult`
- 模拟 `WebRequest` 对象
- 使用 `MockMvc` 测试控制器异常处理

### 断言验证
- 验证 HTTP 状态码
- 验证响应体内容
- 验证异常消息
- 验证 CORS 头部设置

## 运行测试

```bash
# 运行所有异常测试
mvn test -Dtest="*ExceptionTest"

# 运行特定测试类
mvn test -Dtest="BusinessExceptionTest"
mvn test -Dtest="GlobalExceptionHandlerTest"
```

## 测试覆盖率

- **BusinessException**: 100% 方法覆盖率
- **GlobalExceptionHandler**: 100% 方法覆盖率
- 包含正常流程、异常流程和边界情况测试

## 注意事项

1. `GlobalExceptionHandlerTest` 使用了 `@WebMvcTest` 注解，需要 Spring 测试上下文
2. 测试中包含了内部测试控制器来验证异常处理
3. 所有测试都使用中文错误消息进行验证
4. Mock 对象确保了测试的隔离性和可重复性 