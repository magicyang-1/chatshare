import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  MessageSquare, 
  Bell, 
  Users, 
  Send, 
  Clock, 
  CheckCircle, 
  ArrowLeft,
  Trash2,
  Search,
  Mail,
  User,
  Shield,
  Phone,
  MessageCircle,
  UserCircle,
  Headphones,
  Check,
  RefreshCw,
  AlertCircle
} from 'lucide-react';
import { userAPI, adminAPI } from '../services/api';
import './MessageCenter.css';

const MessageCenter = ({ user, onLogout }) => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('messages');
  const [messages, setMessages] = useState([]);
  const [supportChat, setSupportChat] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [supportStaff, setSupportStaff] = useState([]);
  const [selectedSupport, setSelectedSupport] = useState(null);

  const [notification, setNotification] = useState(null);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [showCustomerList, setShowCustomerList] = useState(true);
  
  // 如果是客服账户，加载客户对话列表
  const [customerChats, setCustomerChats] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);

  // 显示通知
  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  useEffect(() => {
    console.log('MessageCenter - 当前用户信息:', user);
    console.log('MessageCenter - 用户角色:', user?.role);
    
    loadMessages();
    
    // 普通用户和管理员都可以使用客服对话功能
    if (user?.role === 'user' || user?.role === 'admin') {
      console.log('MessageCenter - 加载客服人员列表 (用户/管理员)');
      loadSupportStaff();
    }
    
    // 只有客服才加载客服工作台数据
    if (user?.role === 'support') {
      console.log('MessageCenter - 加载客服工作台数据 (客服用户)');
      loadCustomerChats();
    }
    
    // 调试：如果用户角色不是预期的，显示警告
    if (user && !['user', 'support', 'admin'].includes(user.role)) {
      console.warn('MessageCenter - 未知的用户角色:', user.role);
    }
  }, [user]);

  // 监听窗口大小变化
  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth <= 768;
      setIsMobile(mobile);
      // 如果切换到桌面端，重置显示状态
      if (!mobile) {
        setShowCustomerList(true);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 当选择的客服改变时，重新加载对话
  useEffect(() => {
    if (user?.role === 'user' || user?.role === 'admin') {
      if (selectedSupport) {
    loadSupportChat();
      } else {
        // 没有选择客服时，清空对话记录
        setSupportChat([]);
      }
    }
  }, [selectedSupport, user]);

  // 加载接收到的消息
  const loadMessages = async () => {
    try {
      setIsLoading(true);
      const response = await userAPI.getMessages();
      console.log('加载的消息数据:', response);
      
      if (response.content) {
        setMessages(response.content);
      } else if (Array.isArray(response)) {
        setMessages(response);
      } else {
        setMessages([]);
      }
    } catch (error) {
      console.error('加载消息失败:', error);
      setMessages([]);
    } finally {
      setIsLoading(false);
    }
  };

  // 加载客服人员列表
  const loadSupportStaff = async () => {
    try {
      console.log('正在从后端加载客服人员列表...');
      console.log('当前用户:', user);
      console.log('API调用: /user/support/staff');
      
      const response = await userAPI.getSupportStaff();
      console.log('后端客服人员原始响应:', response);
      console.log('响应类型:', typeof response);
      console.log('响应是否为数组:', Array.isArray(response));
      
      // 处理不同的响应格式
      let staffList = [];
      if (Array.isArray(response)) {
        staffList = response;
        console.log('使用响应作为数组');
      } else if (response && response.data && Array.isArray(response.data)) {
        staffList = response.data;
        console.log('使用response.data作为数组');
      } else if (response && Array.isArray(response.content)) {
        staffList = response.content;
        console.log('使用response.content作为数组');
      } else {
        console.log('响应格式不匹配，staffList保持为空数组');
      }
      
      console.log('提取的staffList:', staffList);
      
      // 为每个客服添加在线状态（从后端获取或默认）
      staffList = staffList.map(staff => ({
        ...staff,
        status: staff.status || 'online' // 默认在线状态
      }));
      
      setSupportStaff(staffList);
      console.log('最终设置的客服人员列表:', staffList);
      
      if (staffList.length === 0) {
        showNotification('暂无可用的客服人员，请检查后端数据', 'error');
      } else {
        showNotification(`成功加载 ${staffList.length} 位客服人员`, 'success');
      }
      
    } catch (error) {
      console.error('从后端加载客服人员失败:', error);
      console.error('错误详情:', error.response?.data);
      console.error('错误状态:', error.response?.status);
      showNotification('加载客服人员失败: ' + error.message, 'error');
      setSupportStaff([]); // 只设置空数组，不使用模拟数据
    }
  };

  // 加载客服对话 - 根据选中的客服过滤
  const loadSupportChat = async () => {
    try {
      const response = await userAPI.getSupportChat();
      console.log('客服对话数据:', response);
      
      let allChats = Array.isArray(response) ? response : [];
      
      // 如果选择了特定客服，只显示与该客服的对话
      if (selectedSupport) {
        console.log('过滤对话，选中的客服:', selectedSupport);
        console.log('所有对话:', allChats);
        
        const filteredChats = allChats.filter(chat => {
          console.log('检查消息:', chat);
          
          // 显示用户发送给该客服的消息
          if (chat.senderType === 'USER') {
            // 如果没有指定客服ID，或者指定的客服ID匹配选中的客服
            const chatSupportId = chat.supportId;
            const selectedSupportId = selectedSupport.id;
            
            console.log('用户消息 - chatSupportId:', chatSupportId, 'selectedSupportId:', selectedSupportId);
            
            return chatSupportId === selectedSupportId || 
                   String(chatSupportId) === String(selectedSupportId) ||
                   chatSupportId === null; // 如果没有指定客服，也显示（兼容旧数据）
          }
          
          // 显示该客服回复的消息
          if (chat.senderType === 'SUPPORT') {
            const chatSupportId = chat.supportId || (chat.fromUser && chat.fromUser.id);
            const selectedSupportId = selectedSupport.id;
            
            console.log('客服消息 - chatSupportId:', chatSupportId, 'selectedSupportId:', selectedSupportId);
            
            return chatSupportId === selectedSupportId || 
                   String(chatSupportId) === String(selectedSupportId);
          }
          
          return false;
        });
        
        console.log('过滤后的对话:', filteredChats);
        setSupportChat(filteredChats);
      } else {
        // 没有选择客服时，不显示任何对话
        console.log('没有选择客服，清空对话列表');
        setSupportChat([]);
      }
    } catch (error) {
      console.error('加载客服对话失败:', error);
      setSupportChat([]);
    }
  };

  // 客服加载客户对话列表
  const loadCustomerChats = async () => {
    // 确保只有客服角色才能调用此API
    if (user?.role !== 'support') {
      console.warn('loadCustomerChats - 当前用户不是客服角色，跳过加载:', user?.role);
      return;
    }
    
    try {
      console.log('loadCustomerChats - 开始加载客户对话列表');
      console.log('当前用户:', user);
      console.log('API调用: /admin/support/customer-chats');
      
      // 调用客服专用API获取客户对话
      const response = await adminAPI.getCustomerChats();
      console.log('loadCustomerChats - 原始响应:', response);
      console.log('响应类型:', typeof response);
      console.log('响应是否为数组:', Array.isArray(response));
      
      const customerChatsList = Array.isArray(response) ? response : [];
      setCustomerChats(customerChatsList);
      
      console.log('设置的客户对话列表:', customerChatsList);
      if (customerChatsList.length === 0) {
        showNotification('暂无客户对话记录', 'info');
      } else {
        showNotification(`成功加载 ${customerChatsList.length} 个客户对话`, 'success');
      }
      
    } catch (error) {
      console.error('加载客户对话失败:', error);
      console.error('错误详情:', error.response?.data);
      console.error('错误状态:', error.response?.status);
      
      // 如果是权限错误，显示友好提示
      if (error.message.includes('权限不足') || error.response?.status === 403) {
        showNotification('您没有权限访问客服工作台，请检查用户角色', 'error');
      } else {
        showNotification('加载客户对话失败: ' + error.message, 'error');
      }
      
      setCustomerChats([]);
    }
  };

  // 发送消息给客服
  const sendToSupport = async () => {
    if (!newMessage.trim() || !selectedSupport) {
      if (!selectedSupport) {
        alert('请先选择一位客服人员');
        return;
      }
      return;
    }

    try {
      setIsLoading(true);
      
      await userAPI.sendToSupport({
        content: newMessage,
        supportId: selectedSupport.id
      });

      // 立即在本地添加用户发送的消息
      const newChatMessage = {
        id: Date.now(),
        messageType: 'SUPPORT',
        content: newMessage,
        createdAt: new Date().toISOString(),
        senderType: 'USER',
        fromUserId: user.id,
        fromUser: user,
        isRead: true
      };

      setSupportChat(prev => [...prev, newChatMessage]);
      setNewMessage('');
      
      // 显示成功通知
      showNotification(`消息已发送给 ${selectedSupport.username}`);
      
      // 重新加载对话以获取最新状态
      setTimeout(loadSupportChat, 500);
      
    } catch (error) {
      console.error('发送消息失败:', error);
      showNotification('发送消息失败: ' + error.message, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // 客服回复客户
  const replyToCustomer = async () => {
    if (!newMessage.trim() || !selectedCustomer) return;

    try {
      setIsLoading(true);
      
      await adminAPI.replyToCustomer({
        customerId: selectedCustomer.id,
        content: newMessage
      });

      // 立即在本地添加客服发送的消息
      const newMessageObj = {
        id: Date.now(),
        content: newMessage,
        createdAt: new Date().toISOString(),
        isFromCustomer: false,  // 客服发送的消息
        senderType: 'SUPPORT'
      };

      // 更新当前选中客户的消息列表
      setSelectedCustomer(prev => ({
        ...prev,
        messages: [...(prev.messages || []), newMessageObj]
      }));

      setNewMessage('');
      showNotification('消息发送成功', 'success');
      
      // 刷新客户对话列表（更新最后消息时间等）
      await loadCustomerChats();
      
    } catch (error) {
      console.error('回复客户失败:', error);
      showNotification('回复失败: ' + error.message, 'error');
    } finally {
      setIsLoading(false);
    }
  };

  // 标记消息为已读
  const markAsRead = async (messageId) => {
    try {
      await userAPI.markMessageAsRead(messageId);
      setMessages(prev => prev.map(msg => 
        msg.id === messageId ? { ...msg, isRead: true } : msg
      ));
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  };

  // 删除消息
  const deleteMessage = async (messageId) => {
    if (!window.confirm('确定要删除这条消息吗？')) return;

    try {
      console.log('正在删除消息:', messageId);
      await userAPI.deleteMessage(messageId);
      console.log('消息删除成功');
      
      // 更新本地状态
      setMessages(prev => prev.filter(msg => msg.id !== messageId));
      if (selectedMessage?.id === messageId) {
        setSelectedMessage(null);
      }
      
      // 显示成功通知
      showNotification('消息删除成功');
    } catch (error) {
      console.error('删除消息失败:', error);
      showNotification(`删除消息失败: ${error.message}`, 'error');
    }
  };

  // 格式化时间
  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 24 * 60 * 60 * 1000) { // 24小时内
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    } else if (diff < 7 * 24 * 60 * 60 * 1000) { // 7天内
      return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString('zh-CN');
    }
  };

  // 获取发信人信息
  const getSenderInfo = (message) => {
    if (message.fromUser) {
      return {
        name: message.fromUser.username || '用户',
        avatar: message.fromUser.username?.charAt(0) || 'U',
        role: message.fromUser.role || 'user'
      };
    }
    
    // 根据消息类型推断
    if (message.messageType === 'BROADCAST') {
      return { name: '系统管理员', avatar: 'S', role: 'admin' };
    } else if (message.messageType === 'SUPPORT') {
      return { name: '客服团队', avatar: 'C', role: 'support' };
    } else {
      return { name: '系统', avatar: 'S', role: 'system' };
    }
  };

  // 获取未读消息数量
  const unreadCount = messages.filter(msg => !msg.isRead).length;

  return (
    <div className="message-center">
      {/* 通知组件 */}
      {notification && (
        <div className={`notification ${notification.type}`}>
          <div className="notification-content">
            {notification.type === 'success' && <CheckCircle size={16} />}
            {notification.type === 'error' && <AlertCircle size={16} />}
            <span>{notification.message}</span>
          </div>
        </div>
      )}
      
      <div className="message-center-header">
        <div className="header-left">
          <div className="header-icon">
            <MessageSquare size={28} />
          </div>
          <div className="header-text">
          <h1>消息中心</h1>
            <p>查看系统消息{(user?.role === 'user' || user?.role === 'admin') ? '和客服对话' : user?.role === 'support' ? '和客服工作台' : ''}</p>
          </div>
        </div>
        <button className="back-btn" onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} />
          返回主页
        </button>
      </div>

      <div className="message-center-content">
        {/* 标签导航 */}
        <div className="tabs-header">
          <button 
            className={`tab-btn ${activeTab === 'messages' ? 'active' : ''}`}
            onClick={() => setActiveTab('messages')}
          >
            <Bell size={20} />
            <span>系统消息</span>
            {unreadCount > 0 && <span className="badge">{unreadCount}</span>}
          </button>
          
          {/* 普通用户和管理员都能看到客服对话 */}
          {(user?.role === 'user' || user?.role === 'admin') && (
          <button 
            className={`tab-btn ${activeTab === 'support' ? 'active' : ''}`}
            onClick={() => setActiveTab('support')}
          >
            <MessageSquare size={20} />
              <span>客服对话</span>
            </button>
          )}
          
          {/* 只有客服才能看到客服工作台 */}
          {user?.role === 'support' && (
            <button 
              className={`tab-btn ${activeTab === 'support-desk' ? 'active' : ''}`}
              onClick={() => setActiveTab('support-desk')}
            >
              <Headphones size={20} />
              <span>客服工作台</span>
          </button>
          )}
        </div>

        {/* 系统消息 */}
        {activeTab === 'messages' && (
          <div className="tab-content">

            {isLoading ? (
              <div className="loading-state">
                <div className="loading-spinner"></div>
                <span>加载消息中...</span>
              </div>
            ) : messages.length === 0 ? (
              <div className="empty-state">
                <Bell size={48} />
                <h3>暂无消息</h3>
                <p>您还没有收到任何系统消息</p>
              </div>
            ) : (
              <div className="messages-list">
                {messages.map(message => {
                  const senderInfo = getSenderInfo(message);
                  return (
                  <div 
                  key={message.id} 
                    className={`message-item ${!message.isRead ? 'unread' : ''}`}
                    >
                      <div className="message-avatar">
                        <div className={`avatar ${senderInfo.role}`}>
                          {senderInfo.avatar}
                        </div>
                        <div className="sender-info">
                          <div className="sender-name">{senderInfo.name}</div>
                          <div className="sender-role">
                            {senderInfo.role === 'admin' && <Shield size={12} />}
                            {senderInfo.role === 'support' && <Headphones size={12} />}
                            {senderInfo.role === 'user' && <User size={12} />}
                            {senderInfo.role === 'system' && <Bell size={12} />}
                            <span>
                              {senderInfo.role === 'admin' ? '管理员' : 
                               senderInfo.role === 'support' ? '客服' : 
                               senderInfo.role === 'user' ? '用户' : '系统'}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="message-main">
                    <div className="message-header">
                      <h4 className="message-title">
                        {message.subject || '系统通知'}
                      </h4>
                      <div className="message-meta">
                        <span className="message-time">
                              <Clock size={14} />
                          {formatTime(message.createdAt)}
                        </span>
                        <span className={`message-status ${message.isRead ? 'read' : 'unread'}`}>
                              {message.isRead ? (
                                <>
                                  <CheckCircle size={14} />
                                  已读
                                </>
                              ) : (
                                <>
                                  <Mail size={14} />
                                  未读
                                </>
                              )}
                        </span>
                      </div>
                    </div>
                        
                    <div className="message-content">
                      {message.content}
                    </div>
                        
                    <div className="message-actions">
                      {!message.isRead && (
                        <button 
                          className="action-btn mark-read"
                              onClick={() => markAsRead(message.id)}
                              title="标记为已读"
                        >
                          <CheckCircle size={14} />
                          标记已读
                        </button>
                      )}
                      <button 
                        className="action-btn delete"
                            onClick={() => deleteMessage(message.id)}
                            title="删除此消息"
                      >
                        <Trash2 size={14} />
                        删除
                      </button>
                    </div>
                  </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* 客服对话（普通用户和管理员） */}
        {activeTab === 'support' && (user?.role === 'user' || user?.role === 'admin') && (
          <div className="tab-content">
            <div className="support-chat-container">
              {/* 客服选择侧边栏 */}
              <div className="support-sidebar">
                <div className="sidebar-header">
                  <h3>
                    <Users size={18} />
                    选择客服人员
                  </h3>
                  <div className="online-count">
                    <div className="status-indicator online"></div>
                    在线: {supportStaff.filter(s => s.status === 'online').length} 人
                  </div>
                </div>
                
                {supportStaff.length === 0 ? (
                  <div className="sidebar-empty">
                    <Headphones size={24} />
                    <p>暂无客服在线</p>
                    <button className="retry-btn" onClick={loadSupportStaff}>
                      刷新
                    </button>
                  </div>
                ) : (
                  <div className="support-list">
                    {supportStaff.map(staff => (
                      <div 
                        key={staff.id}
                        className={`support-item ${selectedSupport?.id === staff.id ? 'active' : ''}`}
                        onClick={() => {
                          setSelectedSupport(staff);
                          loadSupportChat();
                        }}
                      >
                        <div className="support-avatar">
                          <span>{staff.username?.charAt(0) || 'S'}</span>
                          <div className={`status-dot ${staff.status}`}></div>
                        </div>
                        <div className="support-info">
                          <div className="support-name">{staff.username}</div>
                          <div className="support-status">
                            {staff.status === 'online' ? '在线' : 
                             staff.status === 'busy' ? '忙碌' : '离线'}
                          </div>
                        </div>
                        {selectedSupport?.id === staff.id && (
                          <div className="selected-indicator">
                            <Check size={16} />
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* 对话主区域 */}
              <div className="chat-main">
                {selectedSupport ? (
                  <>
                    {/* 聊天头部 */}
                    <div className="chat-header">
                      <div className="chat-title">
                        <div className="support-avatar-small">
                          <span>{selectedSupport.username?.charAt(0) || 'S'}</span>
                          <div className={`status-dot ${selectedSupport.status}`}></div>
                        </div>
                        <div className="title-info">
                          <h4>{selectedSupport.username}</h4>
                          <span className="role-tag">客服专员</span>
                        </div>
                      </div>
                      <div className="chat-actions">
                        <button className="action-btn" title="刷新对话">
                          <RefreshCw size={16} />
                        </button>
                      </div>
                    </div>
                    {/* 1. 客服在自己视角里发言在右侧，用户在左侧 */}
                    {/* 2. 美化消息框*/}
                    {/* 消息区域 */}
                    <div className="chat-messages">
                      {supportChat.length === 0 ? (
                        <div className="chat-empty">
                          <MessageCircle size={48} />
                          <h3>开始与 {selectedSupport.username} 对话</h3>
                          <p>请输入您的问题，我们将尽快为您解答</p>
                        </div>
                      ) : (
                        <div className="messages-list">
                          {supportChat.map((msg, index) => (
                            <div 
                              key={msg.id || index} 
                              className={`message ${msg.senderType === 'USER' ? 'own' : 'other'}`}
                              style={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: msg.senderType === 'USER' ? 'flex-end' : 'flex-start',
                                gap: '0px'
                              }}
                            >
                              <div className="message-container" style={{ 
                                display: 'flex', 
                                alignItems: 'flex-end', 
                                gap: '12px',
                                flexDirection: msg.senderType === 'USER' ? 'row' : 'row-reverse'
                              }}>
                                <div className="message-bubble" 
                                style={{ 
                                  flex: 1,
                                  maxWidth: '100%',
                                  wordBreak: 'break-word',
                                  padding: '8px 10px',
                                  border: msg.senderType === 'USER' ? '1px solid #4caf50' : '1px solid #2196f3',
                                  borderRadius: '18px',
                                  backgroundColor: msg.senderType === 'USER' ? '#e8f5e8' : '#e3f2fd',
                                  
                                  color: '#000000',
                                }}>
                                  <div className="message-text" 
                                  style={{ 
                                    backgroundColor: 'transparent',
                                    padding: '10px 12px' ,
                                    fontSize: '14px',
                                    fontWeight: '500',
                                    color: '#000000'
                                    }}>
                                    {msg.content}
                                  </div>
                                </div>
                                <div 
                                  style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '32px',
                                    height: '32px',
                                    borderRadius: '50%',
                                    backgroundColor: msg.senderType === 'USER' ? '#10b981' : '#3b82f6',
                                    color: 'white',
                                    fontSize: '14px',
                                    fontWeight: 'bold',
                                    flexShrink: 0
                                  }}
                                 > 
                                  {msg.senderType === 'USER' ? 
                                    (user?.username?.charAt(0) || 'U') : 
                                    (selectedSupport.username?.charAt(0) || 'S')
                                  }
                                </div>
                              </div>
                              <div className="message-time" style={{ 
                                fontSize: '12px', 
                                color: '#6b7280', 
                                marginTop: '4px',
                                alignSelf: msg.senderType === 'USER' ? 'flex-end' : 'flex-start',
                                paddingRight: msg.senderType === 'USER' ? '44px' : '0',
                                paddingLeft: msg.senderType === 'USER' ? '0' : '44px'
                              }}>
                                {formatTime(msg.createdAt)}
                              </div>
                            </div>
                          ))}
                        </div>
                )}
              </div>

                    {/* 输入区域（用户/管理员视角） */}
                    <div className="chat-input-bar">
                      <textarea
                        className="chat-textarea"
                        value={newMessage}
                        onChange={(e) => setNewMessage(e.target.value)}
                        placeholder={selectedSupport ? `发送消息...` : '回复客户...'}
                        rows="1"
                        disabled={isLoading}
                        onKeyPress={(e) => {
                          if (e.key === 'Enter' && !e.shiftKey) {
                            e.preventDefault();
                            sendToSupport();
                          }
                        }}
                        onInput={(e) => {
                          e.target.style.height = 'auto';
                          e.target.style.height = Math.min(e.target.scrollHeight, 80) + 'px';
                        }}
                      />
                      <button 
                        className="send-button"
                        onClick={sendToSupport}
                        disabled={!newMessage.trim() || isLoading}
                      >
                        {isLoading ? <div className="loading-spinner"></div> : <Send size={18} />}
                      </button>
                    </div>
                  </>
                ) : (
                  <div className="no-selection">
                    <MessageSquare size={64} />
                    <h3>选择客服人员</h3>
                    <p>请从左侧选择一位客服人员开始对话</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* 客服工作台 */}
        {activeTab === 'support-desk' && user?.role === 'support' && (
          <div className="tab-content">
            <div className="support-chat-container" style={{ height: 'calc(100vh - 200px)' }}>
              <div 
                className="support-sidebar"
                style={{
                  width: isMobile ? '100%' : '300px',
                  height: isMobile ? (showCustomerList ? '100%' : '0') : '100%',
                  display: isMobile && !showCustomerList ? 'none' : 'flex',
                  flexDirection: 'column',
                  overflow: 'hidden',
                  transition: 'all 0.3s ease'
                }}
              >
                <div className="sidebar-header">
                  <h3>
                    <Users size={18} />
                    客户列表
                  </h3>
                  <div className="online-count">
                    <div className="status-indicator online"></div>
                    在线: {customerChats.length} 位客户
                  </div>
                </div>
                {customerChats.length === 0 ? (
                  <div className="sidebar-empty">
                    <MessageSquare size={24} />
                    <p>暂无客户对话</p>
                    <button className="retry-btn" onClick={loadCustomerChats}>
                      刷新
                    </button>
                  </div>
                ) : (
                  <div className="support-list">
                    {customerChats.map(chat => (
                      <div 
                        key={chat.id}
                        className={`support-item ${selectedCustomer?.id === chat.id ? 'active' : ''}`}
                        onClick={() => {
                          setSelectedCustomer(chat);
                          // 在手机端选择客户后隐藏客户列表
                          if (isMobile) {
                            setShowCustomerList(false);
                          }
                        }}
                      >
                        <div className="support-avatar">
                          <span>{chat.customerName?.charAt(0) || 'C'}</span>
                          <div className="status-dot online"></div>
                        </div>
                        <div className="support-info">
                          <div className="support-name">
                            {chat.customerName}
                          </div>
                          <div className="support-status">
                            {chat.lastMessage || '暂无消息'}
                          </div>
                        </div>
                        {selectedCustomer?.id === chat.id && (
                          <div className="selected-indicator">
                            <Check size={16} />
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div 
                className="chat-main"
                style={{
                  flex: 1,
                  width: isMobile ? '100%' : 'auto',
                  display: isMobile && showCustomerList ? 'none' : 'flex',
                  flexDirection: 'column',
                  height: '100%',
                  overflow: 'hidden'
                }}
              >
                {selectedCustomer ? (
                  <>
                    <div className="chat-header">
                      <div className="chat-title">
                        {isMobile && (
                          <button
                            onClick={() => setShowCustomerList(true)}
                            style={{
                              background: 'none',
                              border: 'none',
                              padding: '4px',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              color: '#3b82f6',
                              marginRight: '8px'
                            }}
                          >
                            <ArrowLeft size={20} />
                          </button>
                        )}
                        <div className="support-avatar-small">
                          <span>{selectedCustomer.customerName?.charAt(0) || 'C'}</span>
                          <div className="status-dot online"></div>
                        </div>
                        <div className="title-info">
                          <h4>与 {selectedCustomer.customerName} 的对话</h4>
                          <span className="role-tag">客户对话</span>
                        </div>
                      </div>
                      <div className="chat-actions">
                        <button className="action-btn" title="刷新对话" onClick={loadCustomerChats}>
                          <RefreshCw size={16} />
                        </button>
                      </div>
                    </div>

                    <div className="chat-messages">
                      {selectedCustomer.messages?.length === 0 ? (
                        <div className="chat-empty">
                          <MessageCircle size={48} />
                          <h3>开始与 {selectedCustomer.customerName} 对话</h3>
                          <p>客户尚未发送任何消息</p>
                        </div>
                      ) : (
                        <div className="messages-list">
                          {selectedCustomer.messages?.map((msg, index) => (
                            <div 
                              key={msg.id || index} 
                              className={`message ${msg.isFromCustomer ? 'other' : 'own'}`}
                              style={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: msg.isFromCustomer ? 'flex-start' : 'flex-end',
                                gap: '0px'
                              }}
                            >
                              <div className="message-container" style={{ 
                                display: 'flex', 
                                alignItems: 'flex-end', 
                                gap: '12px',
                                flexDirection: msg.isFromCustomer ? 'row-reverse' : 'row'
                              }}>
                                <div className="message-bubble" 
                                style={{ 
                                  flex: 1,
                                  maxWidth: '100%',
                                  wordBreak: 'break-word',
                                  padding: '8px 10px',
                                  border: msg.isFromCustomer ? '1px solid #2196f3' : '1px solid #4caf50',
                                  borderRadius: '18px',
                                  backgroundColor: msg.isFromCustomer ? '#e3f2fd' : '#e8f5e8',
                                  color: '#000000',
                                }}>
                                  <div className="message-text" 
                                  style={{ 
                                    backgroundColor: 'transparent',
                                    padding: '10px 12px',
                                    fontSize: '14px',
                                    fontWeight: '500',
                                    color: '#000000'
                                  }}>
                                    {msg.content}
                                  </div>
                                </div>
                                <div 
                                  style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    width: '32px',
                                    height: '32px',
                                    borderRadius: '50%',
                                    backgroundColor: msg.isFromCustomer ? '#3b82f6' : '#10b981',
                                    color: 'white',
                                    fontSize: '14px',
                                    fontWeight: 'bold',
                                    flexShrink: 0
                                  }}
                                > 
                                  {msg.isFromCustomer ? 
                                    (selectedCustomer.customerName?.charAt(0) || 'C') : 
                                    (user?.username?.charAt(0) || 'S')
                                  }
                                </div>
                              </div>
                              <div className="message-time" style={{ 
                                fontSize: '12px', 
                                color: '#6b7280', 
                                marginTop: '4px',
                                alignSelf: msg.isFromCustomer ? 'flex-start' : 'flex-end',
                                paddingRight: msg.isFromCustomer ? '0' : '44px',
                                paddingLeft: msg.isFromCustomer ? '44px' : '0'
                              }}>
                                {formatTime(msg.createdAt)}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>

                    <div className="chat-input">
                      <div className="input-container">
                        <div className="chat-input-bar">
                          <textarea
                            className="chat-textarea"
                            value={newMessage}
                            onChange={(e) => setNewMessage(e.target.value)}
                            placeholder="回复客户..."
                            rows="1"
                            disabled={isLoading}
                            onKeyPress={(e) => {
                              if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                replyToCustomer();
                              }
                            }}
                            onInput={(e) => {
                              e.target.style.height = 'auto';
                              e.target.style.height = Math.min(e.target.scrollHeight, 80) + 'px';
                            }}
                          />
                          <button 
                            className="send-button"
                            onClick={replyToCustomer}
                            disabled={!newMessage.trim() || isLoading}
                          >
                            {isLoading ? <div className="loading-spinner"></div> : <Send size={18} />}
                          </button>
                        </div>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="no-selection">
                    <Users size={64} />
                    <h3>选择客户开始对话</h3>
                    <p>
                      {isMobile ? '请选择一个客户来查看对话历史或发送消息' : '从左侧客户列表中选择一个客户来查看对话历史或发送消息'}
                    </p>
                    {isMobile && (
                      <button
                        onClick={() => setShowCustomerList(true)}
                        style={{
                          backgroundColor: '#3b82f6',
                          color: 'white',
                          border: 'none',
                          borderRadius: '8px',
                          padding: '12px 24px',
                          fontSize: '14px',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '8px',
                          margin: '16px auto 0'
                        }}
                      >
                        <Users size={16} />
                        查看客户列表
                      </button>
                    )}
                    {customerChats.length === 0 && (
                      <div style={{ 
                        backgroundColor: '#e0f2fe', 
                        border: '1px solid #0ea5e9',
                        borderRadius: '8px',
                        padding: '16px',
                        marginTop: '20px'
                      }}>
                        <p style={{ margin: 0, fontSize: '13px', color: '#0369a1' }}>
                          💡 提示：目前没有客户对话记录。当用户通过客服对话功能联系时，会在左侧列表中显示。
                        </p>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MessageCenter;