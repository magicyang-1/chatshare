import axios from 'axios';

// 创建axios实例
export const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 自动添加token
api.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method?.toUpperCase(), config.baseURL + config.url, config.data);
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 显示提示信息的工具函数
const showTokenExpiredNotification = () => {
  // 创建一个简单的通知提示
  const notification = document.createElement('div');
  notification.innerHTML = `
    <div style="
      position: fixed;
      top: 20px;
      right: 20px;
      background: #ff4757;
      color: white;
      padding: 16px 20px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      z-index: 9999;
      font-family: system-ui, -apple-system, sans-serif;
      font-size: 14px;
      max-width: 320px;
      animation: slideIn 0.3s ease-out;
    ">
      <div style="display: flex; align-items: center; gap: 8px;">
        <span style="font-size: 18px;">⚠️</span>
        <div>
          <div style="font-weight: 600; margin-bottom: 4px;">登录已过期</div>
          <div style="opacity: 0.9; font-size: 13px;">为了您的安全，请重新登录</div>
        </div>
      </div>
    </div>
  `;

  // 添加CSS动画
  const style = document.createElement('style');
  style.textContent = `
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `;
  document.head.appendChild(style);

  document.body.appendChild(notification);
  
  // 3秒后移除通知并跳转
  setTimeout(() => {
    if (notification.parentNode) {
      notification.remove();
    }
    // 清除认证信息并跳转到登录页
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }, 3000);
};

// 响应拦截器 - 统一错误处理
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error('API Error:', error.response?.data);
    
    // 处理认证失败
    if (error.response?.status === 401) {
      // 401通常表示未认证或token无效
      showTokenExpiredNotification();
      return Promise.reject(new Error('登录已过期，即将跳转到登录页面'));
    }
    
    // 处理403权限不足错误
    if (error.response?.status === 403) {
      const errorMsg = error.response?.data?.message || error.response?.data?.error || '';
      
      // 只有明确提到token过期的情况才当作认证过期处理
      if (errorMsg.includes('expired') || errorMsg.includes('过期') || 
          errorMsg.includes('token') || errorMsg.includes('jwt') ||
          errorMsg.includes('Expired')) {
        showTokenExpiredNotification();
        return Promise.reject(new Error('登录已过期，即将跳转到登录页面'));
      }
      
      // 其他403错误当作权限不足处理
      const message = errorMsg || '权限不足，无法访问此资源';
      return Promise.reject(new Error(message));
    }
    
    // 返回更友好的错误信息
    const message = error.response?.data?.message || error.response?.data?.error || error.message || '网络错误';
    return Promise.reject(new Error(message));
  }
);

// 认证相关API
export const authAPI = {
  // 用户注册
  register: (userData) => api.post('/auth/register', userData),
  
  // 用户登录
  login: (credentials) => api.post('/auth/login', credentials),
  
  // 验证token（通过获取当前用户信息）
  verify: () => api.get('/auth/me'),
  
  // 注销
  logout: () => api.post('/auth/logout'),
  
  // 修改密码
  changePassword: (passwords) => api.post('/auth/change-password', passwords),
};

// 聊天相关API
export const chatAPI = {
  // 创建新对话
  create: (chatData) => api.post('/chat/create', chatData),
  
  // 发送消息 - 支持模型选择和文件附件
  sendMessage: (chatId, messageData) => api.post(`/chat/${chatId}/message`, messageData),
  
  // 获取对话消息
  getMessages: (chatId, params = {}) => api.get(`/chat/${chatId}/messages`, { params }),
  
  // 删除对话
  delete: (chatId) => api.delete(`/chat/${chatId}`),
  
  // 更新对话标题
  updateTitle: (chatId, title) => api.patch(`/chat/${chatId}/title`, { title }),
  
  // 切换收藏状态
  toggleFavorite: (chatId) => api.patch(`/chat/${chatId}/favorite`),
  
  // 切换保护状态
  toggleProtection: (chatId) => api.patch(`/chat/${chatId}/protect`),
  
  // 新增：更新对话使用的AI模型
  updateModel: (chatId, aiModel) => api.patch(`/chat/${chatId}/model`, { aiModel }),
};

// 历史记录相关API
export const historyAPI = {
  // 获取对话列表
  getChats: (params = {}) => api.get('/history/chats', { params }),
  
  // 获取对话详情
  getChatDetail: (chatId) => api.get(`/history/chats/${chatId}`),
  
  // 获取搜索建议
  getSearchSuggestions: (query) => api.get('/history/search-suggestions', { params: { query } }),
  
  // 获取用户统计信息
  getStats: () => api.get('/history/stats'),
  
  // 批量操作对话
  batchOperation: (operation, chatIds) => api.post('/history/batch-operation', { operation, chatIds }),
};

// 数据管理相关API
export const dataAPI = {
  // 获取用户设置
  getSettings: () => api.get('/data/settings'),
  
  // 更新用户设置
  updateSettings: (settings) => api.put('/data/settings', settings),
  
  // 获取数据统计
  getStatistics: () => api.get('/data/statistics'),
  
  // 立即清理过期数据
  cleanup: () => api.post('/data/cleanup'),
  
  // 删除所有数据
  deleteAll: (confirmText) => api.delete('/data/all', { data: { confirmText } }),
  
  // 导出数据
  exportData: () => api.get('/data/export'),
};

// 用户相关API
export const userAPI = {
  // 获取用户profile
  getProfile: () => api.get('/user/profile'),
  
  // 更新用户profile
  updateProfile: (userData) => api.put('/user/profile', userData),
  
  // 获取用户权限
  getPermissions: () => api.get('/user/permissions'),
  
  // 获取使用统计
  getUsageStats: () => api.get('/user/usage-stats'),
  
  // 获取活动日志
  getActivityLogs: (params = {}) => api.get('/user/activity-logs', { params }),
  
  // 消息相关
  getMessages: () => api.get('/user/messages'),
  markMessageAsRead: (messageId) => api.patch(`/user/messages/${messageId}/read`),
  deleteMessage: (messageId) => api.delete(`/user/messages/${messageId}`),
  
  // 客服对话
  getSupportChat: () => api.get('/user/support/chat'),
  sendToSupport: (messageData) => api.post('/user/support/message', messageData),
  
  // 获取客服人员列表
  getSupportStaff: () => api.get('/user/support/staff'),
};

// 管理员相关API
export const adminAPI = {
  // 获取所有用户
  getUsers: (params = {}) => api.get('/admin/users', { params }),
  
  // 更新用户状态
  updateUserStatus: (userId, status) => api.patch(`/admin/users/${userId}/status`, { status }),
  
  // 更新用户权限
  updateUserPermissions: (userId, permissions) => api.put(`/admin/users/${userId}/permissions`, permissions),
  
  // 修改用户角色
  updateUserRole: (userId, roleData) => api.put(`/admin/users/${userId}/role`, roleData),
  
  // 发送消息给指定用户
  sendMessage: (userId, messageData) => api.post(`/admin/users/${userId}/message`, messageData),
  
  // 获取发送的消息历史
  getSentMessages: (params = {}) => api.get('/admin/messages/sent', { params }),
  
  // 获取系统统计
  getStatistics: () => api.get('/admin/statistics'),
  
  // 获取系统日志
  getLogs: (params = {}) => api.get('/admin/logs', { params }),
  
  // 客服工作台相关
  getCustomerChats: () => api.get('/admin/support/customer-chats'),
  replyToCustomer: (replyData) => api.post('/admin/support/reply', replyData),
};

// AI服务相关API
export const aiAPI = {
  // 获取AI服务状态
  getStatus: () => api.get('/ai/status'),
  
  // 测试AI回复
  testAI: (message = '你好') => api.post('/ai/test', { message }),
  
  // 获取AI配置信息
  getConfig: () => api.get('/ai/config'),
  
  // 获取聊天AI状态
  getChatStatus: () => api.get('/ai/chat/status'),
  
  // 新增：获取所有可用的AI模型
  getModels: () => api.get('/ai/models'),
  
  // 新增：获取支持图片的AI模型
  getImageModels: () => api.get('/ai/models/image-supported'),
  
  // 新增：获取特定模型的详细信息
  getModelDetails: (modelName) => api.get(`/ai/models/${encodeURIComponent(modelName)}`),
};

// 新增：文件上传相关API
export const fileAPI = {
  // 上传文件
  upload: (file, progressCallback) => {
    const formData = new FormData();
    formData.append('file', file);
    
    return api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (progressCallback) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          progressCallback(percentCompleted);
        }
      },
    });
  },
  
  // 获取文件信息
  getFileInfo: (fileId) => api.get(`/files/${fileId}`),
  
  // 删除文件
  deleteFile: (fileId) => api.delete(`/files/${fileId}`),
  
  // 获取文件下载链接
  getDownloadUrl: (fileId) => api.get(`/files/${fileId}/download`),
};

// 工具函数
export const apiUtils = {
  // 设置认证token
  setAuthToken: (token) => {
    localStorage.setItem('authToken', token);
  },
  
  // 清除认证token
  clearAuthToken: () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
  },
  
  // 获取当前token
  getAuthToken: () => {
    return localStorage.getItem('authToken');
  },
  
  // 检查是否已登录
  isAuthenticated: () => {
    return !!localStorage.getItem('authToken');
  },
  
  // 获取当前用户信息
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  
  // 保存用户信息
  setCurrentUser: (user) => {
    localStorage.setItem('user', JSON.stringify(user));
  },
  
  // 检查用户角色
  hasRole: (role) => {
    const user = apiUtils.getCurrentUser();
    return user?.role === role;
  },
  
  // 检查用户权限
  hasPermission: (permission) => {
    const user = apiUtils.getCurrentUser();
    return user?.permissions?.[permission] === true;
  },
};

// Prompt模板相关API
export const promptTemplateAPI = {
  // 获取所有分类
  getCategories: () => api.get('/prompt-templates/categories'),
  
  // 获取模板列表
  getTemplates: (params = {}) => api.get('/prompt-templates', { params }),
  
  // 获取精选模板
  getFeaturedTemplates: () => api.get('/prompt-templates/featured'),
  
  // 获取热门模板
  getPopularTemplates: () => api.get('/prompt-templates/popular'),
  
  // 获取最新模板
  getLatestTemplates: () => api.get('/prompt-templates/latest'),
  
  // 根据AI模型推荐模板
  getRecommendedTemplates: (aiModel) => api.get('/prompt-templates/recommended', { params: { aiModel } }),
  
  // 获取模板详情
  getTemplateById: (id) => api.get(`/prompt-templates/${id}`),
  
  // 创建模板
  createTemplate: (templateData) => api.post('/prompt-templates', templateData),
  
  // 更新模板
  updateTemplate: (id, templateData) => api.put(`/prompt-templates/${id}`, templateData),
  
  // 删除模板
  deleteTemplate: (id) => api.delete(`/prompt-templates/${id}`),
  
  // 点赞/取消点赞模板
  toggleLike: (id) => api.post(`/prompt-templates/${id}/like`),
  
  // 使用模板（记录使用统计）
  useTemplate: (id, aiModel) => api.post(`/prompt-templates/${id}/use`, { aiModel }),
  
  // 搜索模板
  searchTemplates: (keyword, categoryId = null, page = 0, size = 20) => {
    const params = { keyword, page, size };
    if (categoryId) params.categoryId = categoryId;
    return api.get('/prompt-templates', { params });
  }
};

// 3D生成相关API
export const threeDGenerationAPI = {
  // 创建3D生成任务
  createTextTo3D: (prompt, mode, art_style, should_remesh, seed) => api.post('/3d/text-to-3d', { prompt, mode, art_style, should_remesh, seed }),
  
  // 获取3D生成任务状态
  getTextTo3DStatus: (taskId) => api.get(`/3d/get-text-to-3d-status/${taskId}`),

  // 细化3D生成任务
  refineTextTo3D: (taskId, prompt) => api.post(`/3d/text-to-3d-refine`, { taskId, prompt }),

  //查历史
  getHistory: () => api.get(`/3d/search-history`),
};
// 导出默认api实例
export default api; 