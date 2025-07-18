// src/setupTests.js

// 1. 引入 jest-dom 提供的自定义断言
import '@testing-library/jest-dom';

// 2. 自动 mock axios，避免 ESM 语法报错
jest.mock('axios');

// 3. Stub localStorage
const _storage = {};
const localStorageMock = {
  getItem: (key) => (_storage[key] ?? null),
  setItem: (key, val) => { _storage[key] = String(val); },
  removeItem: (key) => { delete _storage[key]; },
  clear: () => { Object.keys(_storage).forEach(k => delete _storage[k]); },
};
Object.defineProperty(global, 'localStorage', { value: localStorageMock });

// 4. Stub window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),    // deprecated, for older listeners
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// 5. Stub window.location（如果你在测试中需要操控跳转）
delete window.location;
window.location = {
  href: 'http://localhost/',
  pathname: '/',
  search: '',
  hash: '',
  origin: 'http://localhost',
  reload: jest.fn(),
  replace: jest.fn(),
};

// 6. 降低测试中的 React 警告噪音
const _origError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    const [msg] = args;
    if (typeof msg === 'string' && msg.startsWith('Warning:')) {
      return;
    }
    _origError.call(console, ...args);
  };
});
afterAll(() => {
  console.error = _origError;
});

// 7. 全局辅助函数
global.createMockUser = (overrides = {}) => ({
  id: 1,
  email: 'test@example.com',
  name: 'Test User',
  role: 'user',
  ...overrides,
});
global.createMockToken = () => 'mock-jwt-token';

// 8. 增加超时时间 & 清理 mocks
jest.setTimeout(10000);
afterEach(() => {
  jest.clearAllMocks();
  localStorage.clear();
  jest.useRealTimers();
});
