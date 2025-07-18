import React, { useState, useEffect } from 'react';
import { 
  Users, Settings, Shield, Ban, 
  CheckCircle, XCircle, Search, Eye, MessageCircle,
  UserCheck, UserX, AlertTriangle, Crown, MessageSquare,
  CheckSquare, Square, Filter, MoreHorizontal
} from 'lucide-react';
import { adminAPI } from '../services/api';
import './AdminPanel.css';

const AdminPanel = () => {
  const [activeTab, setActiveTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [messageText, setMessageText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showRoleModal, setShowRoleModal] = useState(false);
  const [roleUpdateUser, setRoleUpdateUser] = useState(null);
  const [newRole, setNewRole] = useState('');
  const [roleChangeReason, setRoleChangeReason] = useState('');
  
  // 批量操作状态 - 移除删除相关状态
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [selectAll, setSelectAll] = useState(false);
  const [showBatchActions, setShowBatchActions] = useState(false);
  const [batchActionType, setBatchActionType] = useState('');
  const [showBatchModal, setShowBatchModal] = useState(false);
  
  // 移动端适配状态
  const [isMobile, setIsMobile] = useState(false);
  const [isNarrow, setIsNarrow] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filterRole, setFilterRole] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');
  
  // 权限管理专用状态
  const [permissionSearchQuery, setPermissionSearchQuery] = useState('');
  const [selectedPermissionUsers, setSelectedPermissionUsers] = useState([]);
  const [selectAllPermissions, setSelectAllPermissions] = useState(false);
  const [showPermissionFilters, setShowPermissionFilters] = useState(false);
  const [permissionFilterRole, setPermissionFilterRole] = useState('all');
  const [permissionFilterStatus, setPermissionFilterStatus] = useState('all');

  // 屏幕尺寸检测
  useEffect(() => {
    const checkScreenSize = () => {
      setIsMobile(window.innerWidth <= 768);
      setIsNarrow(window.innerWidth <= 1024);
    };
    
    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  // 加载用户数据
  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setIsLoading(true);
      console.log('开始加载用户数据...');
      const response = await adminAPI.getUsers({
        keyword: searchQuery,
        page: 0,
        size: 50
      });
      console.log('用户数据响应:', response);
      
      // 后端返回的是Spring Page对象，用户数据在content字段中
      const userList = response.content || [];
      setUsers(userList);
      console.log('设置用户列表:', userList);
    } catch (error) {
      console.error('加载用户数据失败:', error);
      alert('加载用户数据失败: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  // 过滤用户
  const filteredUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = filterRole === 'all' || user.role === filterRole;
    const matchesStatus = filterStatus === 'all' || user.status === filterStatus;
    
    return matchesSearch && matchesRole && matchesStatus;
  });
  
  // 权限管理界面过滤用户
  const filteredPermissionUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(permissionSearchQuery.toLowerCase()) ||
                         user.email.toLowerCase().includes(permissionSearchQuery.toLowerCase());
    const matchesRole = permissionFilterRole === 'all' || user.role === permissionFilterRole;
    const matchesStatus = permissionFilterStatus === 'all' || user.status === permissionFilterStatus;
    
    return matchesSearch && matchesRole && matchesStatus;
  });

  // 批量操作相关函数 - 移除删除功能
  const handleSelectAll = () => {
    if (selectAll) {
      setSelectedUsers([]);
    } else {
      setSelectedUsers(filteredUsers.map(user => user.id));
    }
    setSelectAll(!selectAll);
  };

  const handleUserSelect = (userId) => {
    setSelectedUsers(prev => {
      if (prev.includes(userId)) {
        return prev.filter(id => id !== userId);
      } else {
        return [...prev, userId];
      }
    });
  };

  const handleBatchAction = async (actionType) => {
    if (selectedUsers.length === 0) {
      alert('请先选择要操作的用户');
      return;
    }
    
    setBatchActionType(actionType);
    setShowBatchModal(true);
  };

  const confirmBatchAction = async () => {
    try {
      setIsLoading(true);
      
      switch (batchActionType) {
        case 'ban':
          await Promise.all(selectedUsers.map(userId => 
            adminAPI.updateUserStatus(userId, 'banned')
          ));
          break;
        case 'activate':
          await Promise.all(selectedUsers.map(userId => 
            adminAPI.updateUserStatus(userId, 'active')
          ));
          break;
        // 移除删除功能
        default:
          break;
      }
      
      setSelectedUsers([]);
      setSelectAll(false);
      setShowBatchModal(false);
      loadUsers();
      
    } catch (error) {
      console.error('批量操作失败:', error);
      alert('批量操作失败: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  // 切换用户状态
  const toggleUserStatus = async (userId) => {
    try {
      setIsLoading(true);
      const user = users.find(u => u.id === userId);
      const newStatus = user.status === 'active' ? 'banned' : 'active';
      
      await adminAPI.updateUserStatus(userId, newStatus);
      
      setUsers(prev => prev.map(user => 
        user.id === userId 
          ? { ...user, status: newStatus }
          : user
      ));
    } catch (error) {
      console.error('更新用户状态失败:', error);
      alert('更新用户状态失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 切换用户权限
  const togglePermission = async (userId, permission) => {
    try {
      setIsLoading(true);
      const user = users.find(u => u.id === userId);
      const currentPermissions = getStandardPermissions(user);
      const newPermissions = {
        ...currentPermissions,
        [permission]: !currentPermissions[permission]
      };
      
      console.log('更新权限:', userId, permission, '当前权限:', currentPermissions, '新权限:', newPermissions);
      
      await adminAPI.updateUserPermissions(userId, newPermissions);
      
      // 更新本地状态，将权限保存为JSON字符串格式（与后端一致）
      setUsers(prev => prev.map(user => 
        user.id === userId 
          ? { ...user, permissions: JSON.stringify(newPermissions) }
          : user
      ));
      
      console.log('权限更新成功');
    } catch (error) {
      console.error('更新用户权限失败:', error);
      
      let errorMessage = '更新用户权限失败';
      if (error.response) {
        const errorData = error.response.data;
        if (typeof errorData === 'object' && errorData.error) {
          errorMessage = errorData.error;
          if (errorData.invalidKeys) {
            errorMessage += `\n无效的权限字段: ${errorData.invalidKeys.join(', ')}`;
          }
        } else if (typeof errorData === 'string') {
          errorMessage = errorData;
        }
      } else if (error.message) {
        errorMessage += ': ' + error.message;
      }
      
      alert(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // 权限管理界面批量操作函数
  const handlePermissionSelectAll = () => {
    if (selectAllPermissions) {
      setSelectedPermissionUsers([]);
    } else {
      setSelectedPermissionUsers(filteredPermissionUsers.map(user => user.id));
    }
    setSelectAllPermissions(!selectAllPermissions);
  };

  const handlePermissionUserSelect = (userId) => {
    setSelectedPermissionUsers(prev => {
      if (prev.includes(userId)) {
        return prev.filter(id => id !== userId);
      } else {
        return [...prev, userId];
      }
    });
  };

  const handleBatchPermissionToggle = async (permission, enable) => {
    if (selectedPermissionUsers.length === 0) {
      alert('请先选择要操作的用户');
      return;
    }
    
    try {
      setIsLoading(true);
      
      await Promise.all(selectedPermissionUsers.map(userId => {
        const user = users.find(u => u.id === userId);
        const currentPermissions = getStandardPermissions(user);
        const newPermissions = {
          ...currentPermissions,
          [permission]: enable
        };
        return adminAPI.updateUserPermissions(userId, newPermissions);
      }));
      
      // 更新本地状态
      setUsers(prev => prev.map(user => {
        if (selectedPermissionUsers.includes(user.id)) {
          const currentPermissions = getStandardPermissions(user);
          const newPermissions = {
            ...currentPermissions,
            [permission]: enable
          };
          return { ...user, permissions: JSON.stringify(newPermissions) };
        }
        return user;
      }));
      
      setSelectedPermissionUsers([]);
      setSelectAllPermissions(false);
      
      console.log(`批量${enable ? '启用' : '禁用'}权限成功:`, permission);
    } catch (error) {
      console.error('批量权限操作失败:', error);
      alert('批量权限操作失败: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const sendMessageToUser = async () => {
    if (!messageText.trim()) {
      alert('请输入消息内容');
      return;
    }
    
    try {
      setIsLoading(true);
      await adminAPI.sendMessageToUser(selectedUser.id, messageText);
      alert('消息发送成功');
      setSelectedUser(null);
      setMessageText('');
    } catch (error) {
      console.error('发送消息失败:', error);
      alert('发送消息失败: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const openRoleModal = (user) => {
    setRoleUpdateUser(user);
    setNewRole(user.role);
    setRoleChangeReason('');
    setShowRoleModal(true);
  };

  const updateUserRole = async () => {
    if (!newRole || !roleChangeReason.trim()) {
      alert('请填写完整信息');
      return;
    }
    
    try {
      setIsLoading(true);
      await adminAPI.updateUserRole(roleUpdateUser.id, newRole, roleChangeReason);
      alert('角色更新成功');
      setShowRoleModal(false);
      loadUsers();
    } catch (error) {
      console.error('更新用户角色失败:', error);
      alert('更新用户角色失败: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const getRoleName = (role) => {
    const names = {
      admin: '管理员',
      support: '客服',
      user: '用户'
    };
    return names[role] || role;
  };

  const getRoleIcon = (role) => {
    const icons = {
      admin: <Crown size={16} />,
      support: <UserCheck size={16} />,
      user: <Users size={16} />
    };
    return icons[role] || <Users size={16} />;
  };

  const getStatusColor = (status) => {
    return status === 'active' ? '#10b981' : '#ef4444';
  };

  // 获取权限名称
  const getPermissionName = (key) => {
    const names = {
      text_to_text: '智能对话',
      smart_image_generation: '智能生图',
      prompt_template_library: 'AI模板库',
      text_to_3d: '文生3D',
      chat: '基础聊天',
      file_upload: '文件上传',
      data_export: '数据导出'
    };
    return names[key] || key;
  };

  // 获取标准权限列表（处理后端返回的权限数据）
  const getStandardPermissions = (user) => {
    // 如果permissions是对象且不为null，直接返回
    if (typeof user.permissions === 'object' && user.permissions !== null) {
      return user.permissions;
    }

    // 如果permissions是JSON字符串，尝试解析
    if (typeof user.permissions === 'string' && user.permissions.trim().startsWith('{')) {
      try {
        return JSON.parse(user.permissions);
      } catch (e) {
        console.warn('无法解析用户权限JSON:', user.permissions);
      }
    }

    // 如果permissions是其他字符串格式或解析失败，根据角色返回标准权限
    const rolePermissions = {
      admin: {
        text_to_text: true,
        smart_image_generation: true,
        prompt_template_library: true,
        text_to_3d: true,
        chat: true,
        file_upload: true,
        data_export: true
      },
      support: {
        text_to_text: true,
        smart_image_generation: false,
        prompt_template_library: true,
        text_to_3d: false,
        chat: true,
        file_upload: true,
        data_export: false
      },
      user: {
        text_to_text: true,
        smart_image_generation: false,
        prompt_template_library: false,
        text_to_3d: false,
        chat: true,
        file_upload: false,
        data_export: false
      }
    };

    return rolePermissions[user.role] || rolePermissions.user;
  };
  
  // 搜索变化时重新加载数据
  useEffect(() => {
    const delayedSearch = setTimeout(() => {
      loadUsers();
    }, 500);
    
    return () => clearTimeout(delayedSearch);
  }, [searchQuery]);

  // 更新全选状态
  useEffect(() => {
    if (filteredUsers.length > 0) {
      setSelectAll(selectedUsers.length === filteredUsers.length);
    } else {
      setSelectAll(false);
    }
  }, [selectedUsers, filteredUsers]);

  // 更新权限管理全选状态
  useEffect(() => {
    if (filteredPermissionUsers.length > 0) {
      setSelectAllPermissions(selectedPermissionUsers.length === filteredPermissionUsers.length);
    } else {
      setSelectAllPermissions(false);
    }
  }, [selectedPermissionUsers, filteredPermissionUsers]);

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>管理员面板</h2>
        <p>用户管理、权限控制和系统监控</p>
      </div>

      {/* 标签导航 */}
      <div className="admin-tabs">
        <button 
          className={`admin-tab ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          <Users size={20} />
          用户管理
        </button>
        <button 
          className={`admin-tab ${activeTab === 'permissions' ? 'active' : ''}`}
          onClick={() => setActiveTab('permissions')}
        >
          <Shield size={20} />
          权限管理
        </button>
      </div>

      {/* 用户管理 */}
      {activeTab === 'users' && (
        <div className="tab-content">
          <div className="content-header">
            <div className="header-left">
            <h3>用户管理</h3>
              {selectedUsers.length > 0 && (
                <span className="selected-count">
                  已选择 {selectedUsers.length} 个用户
                </span>
              )}
            </div>
            <div className="header-right">
              {/* 筛选按钮 */}
              <button
                className={`filter-btn ${showFilters ? 'active' : ''}`}
                onClick={() => setShowFilters(!showFilters)}
              >
                <Filter size={18} />
                筛选
              </button>
              
              {/* 搜索栏 */}
            <div className="search-bar">
              <Search size={20} />
              <input
                type="text"
                placeholder="搜索用户..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              </div>
            </div>
          </div>

          {/* 筛选器 */}
          {showFilters && (
            <div className="filters-panel">
              <div className="filter-group">
                <label>角色</label>
                <select value={filterRole} onChange={(e) => setFilterRole(e.target.value)}>
                  <option value="all">所有角色</option>
                  <option value="admin">管理员</option>
                  <option value="support">客服</option>
                  <option value="user">用户</option>
                </select>
              </div>
              <div className="filter-group">
                <label>状态</label>
                <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                  <option value="all">所有状态</option>
                  <option value="active">正常</option>
                  <option value="banned">禁用</option>
                </select>
              </div>
            </div>
          )}

          {/* 批量操作栏 - 移除删除功能 */}
          {selectedUsers.length > 0 && (
            <div className="batch-actions">
              <div className="batch-info">
                <span>已选择 {selectedUsers.length} 个用户</span>
              </div>
              <div className="batch-buttons">
                <button
                  className="batch-btn activate"
                  onClick={() => handleBatchAction('activate')}
                  disabled={isLoading}
                >
                  <CheckCircle size={16} />
                  批量激活
                </button>
                <button
                  className="batch-btn ban"
                  onClick={() => handleBatchAction('ban')}
                  disabled={isLoading}
                >
                  <Ban size={16} />
                  批量禁用
                </button>
              </div>
            </div>
          )}

          <div className={`users-container ${isMobile || isNarrow ? 'card-view' : 'table-view'}`}>
            {/* 表格视图 - 仅在宽屏时显示，移除对话数列 */}
            {!isMobile && !isNarrow && (
          <div className="users-table">
            {filteredUsers.length > 0 && (
              <div className="table-header">
                    <div className="select-column">
                      <input
                        type="checkbox"
                        checked={selectAll}
                        onChange={handleSelectAll}
                        className="select-checkbox"
                      />
                    </div>
                <div>用户信息</div>
                <div>角色</div>
                <div>状态</div>
                <div>最后登录</div>
                <div>操作</div>
              </div>
            )}
            
            {filteredUsers.map(user => (
              <div key={user.id} className="table-row">
                    <div className="select-column">
                      <input
                        type="checkbox"
                        checked={selectedUsers.includes(user.id)}
                        onChange={() => handleUserSelect(user.id)}
                        className="select-checkbox"
                      />
                    </div>
                <div className="user-info">
                  <div className="user-avatar">
                    {user.username.charAt(0)}
                  </div>
                  <div className="user-details">
                    <div className="user-name">{user.username}</div>
                    <div className="user-email">{user.email}</div>
                  </div>
                </div>
                
                <div className="user-role">
                  <span className={`role-badge ${user.role}`}>
                    {getRoleIcon(user.role)}
                    {getRoleName(user.role)}
                  </span>
                </div>
                
                <div className="user-status">
                  <span 
                    className={`status-indicator ${user.status}`}
                    style={{ backgroundColor: getStatusColor(user.status) }}
                  >
                    {user.status === 'active' ? '正常' : '禁用'}
                  </span>
                </div>
                
                <div className="user-login">{user.lastLogin}</div>
                
                <div className="user-actions">
                  <button 
                    className={`action-btn ${user.status === 'active' ? 'ban' : 'unban'}`}
                    onClick={() => toggleUserStatus(user.id)}
                    disabled={isLoading}
                  >
                    {user.status === 'active' ? <Ban size={16} /> : <CheckCircle size={16} />}
                    {user.status === 'active' ? '禁用' : '解禁'}
                  </button>
                  <button 
                    className="action-btn role"
                    onClick={() => openRoleModal(user)}
                    disabled={isLoading}
                  >
                    <Crown size={16} />
                    改角色
                  </button>
                  <button 
                    className="action-btn message"
                    onClick={() => setSelectedUser(user)}
                  >
                    <MessageSquare size={16} />
                    发消息
                  </button>
                </div>
              </div>
            ))}
              </div>
            )}

            {/* 卡片视图 - 在移动端或窄屏时显示，移除对话数显示 */}
            {(isMobile || isNarrow) && (
              <div className="users-cards">
                {filteredUsers.map(user => (
                  <div key={user.id} className="user-card">
                    <div className="card-header">
                      <input
                        type="checkbox"
                        checked={selectedUsers.includes(user.id)}
                        onChange={() => handleUserSelect(user.id)}
                        className="select-checkbox"
                      />
                      <div className="user-info">
                        <div className="user-avatar">
                          {user.username.charAt(0)}
                        </div>
                        <div className="user-details">
                          <div className="user-name">{user.username}</div>
                          <div className="user-email">{user.email}</div>
                        </div>
                      </div>
                      <button className="card-menu-btn">
                        <MoreHorizontal size={16} />
                      </button>
                    </div>
                    
                    <div className="card-content">
                      <div className="card-item">
                        <span className="label">角色</span>
                        <span className={`role-badge ${user.role}`}>
                          {getRoleIcon(user.role)}
                          {getRoleName(user.role)}
                        </span>
                      </div>
                      
                      <div className="card-item">
                        <span className="label">状态</span>
                        <span 
                          className={`status-indicator ${user.status}`}
                          style={{ backgroundColor: getStatusColor(user.status) }}
                        >
                          {user.status === 'active' ? '正常' : '禁用'}
                        </span>
                      </div>
                      
                      <div className="card-item">
                        <span className="label">最后登录</span>
                        <span>{user.lastLogin}</span>
                      </div>
                    </div>
                    
                    <div className="card-actions">
                      <button 
                        className={`action-btn ${user.status === 'active' ? 'ban' : 'unban'}`}
                        onClick={() => toggleUserStatus(user.id)}
                        disabled={isLoading}
                      >
                        {user.status === 'active' ? <Ban size={16} /> : <CheckCircle size={16} />}
                        {user.status === 'active' ? '禁用' : '解禁'}
                      </button>
                      <button 
                        className="action-btn role"
                        onClick={() => openRoleModal(user)}
                        disabled={isLoading}
                      >
                        <Crown size={16} />
                        改角色
                      </button>
                      <button 
                        className="action-btn message"
                        onClick={() => setSelectedUser(user)}
                      >
                        <MessageSquare size={16} />
                        发消息
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* 空状态 */}
            {users.length === 0 && !isLoading && (
              <div className="no-users-message">
                <p>暂无用户数据</p>
                <p>用户总数: {users.length}</p>
                <p>过滤后用户数: {filteredUsers.length}</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 权限管理 */}
      {activeTab === 'permissions' && (
        <div className="tab-content">
          <div className="content-header">
            <div className="header-left">
            <h3>权限管理</h3>
              {selectedPermissionUsers.length > 0 && (
                <span className="selected-count">
                  已选择 {selectedPermissionUsers.length} 个用户
                </span>
              )}
          </div>
            <div className="header-right">
              {/* 筛选按钮 */}
              <button
                className={`filter-btn ${showPermissionFilters ? 'active' : ''}`}
                onClick={() => setShowPermissionFilters(!showPermissionFilters)}
              >
                <Filter size={18} />
                筛选
              </button>
              
              {/* 搜索栏 */}
              <div className="search-bar">
                <Search size={20} />
                <input
                  type="text"
                  placeholder="搜索用户..."
                  value={permissionSearchQuery}
                  onChange={(e) => setPermissionSearchQuery(e.target.value)}
                />
              </div>
            </div>
          </div>

          {/* 筛选器 */}
          {showPermissionFilters && (
            <div className="filters-panel">
              <div className="filter-group">
                <label>角色</label>
                <select value={permissionFilterRole} onChange={(e) => setPermissionFilterRole(e.target.value)}>
                  <option value="all">所有角色</option>
                  <option value="admin">管理员</option>
                  <option value="support">客服</option>
                  <option value="user">用户</option>
                </select>
              </div>
              <div className="filter-group">
                <label>状态</label>
                <select value={permissionFilterStatus} onChange={(e) => setPermissionFilterStatus(e.target.value)}>
                  <option value="all">所有状态</option>
                  <option value="active">正常</option>
                  <option value="banned">禁用</option>
                </select>
              </div>
            </div>
          )}

          {/* 批量操作栏 */}
          {selectedPermissionUsers.length > 0 && (
            <div className="batch-permissions-actions">
              <div className="batch-info">
                <span>已选择 {selectedPermissionUsers.length} 个用户</span>
              </div>
              <div className="batch-permission-buttons">
                <div className="permission-actions-group">
                  <span>批量操作：</span>
                  {['text_to_text', 'smart_image_generation', 'prompt_template_library', 'text_to_3d', 'chat', 'file_upload', 'data_export'].map(permission => (
                    <div key={permission} className="permission-action-item">
                      <span className="permission-action-name">{getPermissionName(permission)}</span>
                      <div className="permission-action-buttons">
                        <button
                          className="batch-permission-btn enable"
                          onClick={() => handleBatchPermissionToggle(permission, true)}
                          disabled={isLoading}
                        >
                          <CheckCircle size={14} />
                          启用
                        </button>
                        <button
                          className="batch-permission-btn disable"
                          onClick={() => handleBatchPermissionToggle(permission, false)}
                          disabled={isLoading}
                        >
                          <XCircle size={14} />
                          禁用
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          <div className="permissions-grid">
            {filteredPermissionUsers.length > 0 && (
              <div className="permissions-header">
                <div className="permissions-select-all">
                  <input
                    type="checkbox"
                    checked={selectAllPermissions}
                    onChange={handlePermissionSelectAll}
                    className="select-checkbox"
                  />
                  <span>全选</span>
                </div>
                <div className="permissions-summary">
                  共 {filteredPermissionUsers.length} 个用户
                </div>
              </div>
            )}
            
            {filteredPermissionUsers.map(user => (
              <div key={user.id} className={`permission-card ${selectedPermissionUsers.includes(user.id) ? 'selected' : ''}`}>
                <div className="card-header">
                  <div className="permission-card-select">
                    <input
                      type="checkbox"
                      checked={selectedPermissionUsers.includes(user.id)}
                      onChange={() => handlePermissionUserSelect(user.id)}
                      className="select-checkbox"
                    />
                  </div>
                  <div className="user-info">
                    <div className="user-avatar">
                      {user.username.charAt(0)}
                    </div>
                    <div className="user-details">
                      <div className="user-name">{user.username}</div>
                      <div className="user-email">{user.email}</div>
                    </div>
                  </div>
                  <span className={`status-badge ${user.status}`}>
                    {user.status === 'active' ? '正常' : '禁用'}
                  </span>
                </div>

                <div className="permissions-list">
                  {Object.entries(getStandardPermissions(user)).map(([key, enabled]) => (
                    <div key={key} className="permission-item">
                      <span className="permission-name">
                        {getPermissionName(key)}
                      </span>
                      <button
                        className={`permission-toggle ${enabled ? 'enabled' : 'disabled'}`}
                        onClick={() => togglePermission(user.id, key)}
                        disabled={isLoading || user.status === 'banned'}
                      >
                        {enabled ? <CheckCircle size={16} /> : <XCircle size={16} />}
                        {enabled ? '已启用' : '已禁用'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
            
            {/* 空状态 */}
            {filteredPermissionUsers.length === 0 && !isLoading && (
              <div className="no-permissions-message">
                <p>暂无符合条件的用户</p>
                <p>总用户数: {users.length}</p>
                <p>过滤后用户数: {filteredPermissionUsers.length}</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 批量操作确认弹窗 - 移除删除相关逻辑 */}
      {showBatchModal && (
        <div className="modal-overlay">
          <div className="batch-modal">
            <div className="modal-header">
              <h3>确认批量操作</h3>
              <button 
                className="close-btn"
                onClick={() => setShowBatchModal(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-content">
              <p>
                您确定要对选中的 {selectedUsers.length} 个用户执行
                {batchActionType === 'ban' && '禁用'}
                {batchActionType === 'activate' && '激活'}
                操作吗？
              </p>
            </div>
            <div className="modal-actions">
              <button 
                className="btn-secondary"
                onClick={() => setShowBatchModal(false)}
                disabled={isLoading}
              >
                取消
              </button>
              <button 
                className="btn-primary"
                onClick={confirmBatchAction}
                disabled={isLoading}
              >
                {isLoading ? '处理中...' : '确认'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 修改角色弹窗 */}
      {showRoleModal && roleUpdateUser && (
        <div className="modal-overlay">
          <div className="role-modal">
            <div className="modal-header">
              <h3>修改用户角色</h3>
              <button 
                className="close-btn"
                onClick={() => setShowRoleModal(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-content">
              <div className="user-info-section">
                <div className="user-avatar">
                  {roleUpdateUser.username.charAt(0)}
                </div>
                <div>
                  <div className="user-name">{roleUpdateUser.username}</div>
                  <div className="user-email">{roleUpdateUser.email}</div>
                  <div className="current-role">
                    当前角色: <span className={`role-badge ${roleUpdateUser.role}`}>
                      {getRoleIcon(roleUpdateUser.role)}
                      {getRoleName(roleUpdateUser.role)}
                    </span>
                  </div>
                </div>
              </div>

              <div className="form-group">
                <label>新角色</label>
                <select 
                  value={newRole} 
                  onChange={(e) => setNewRole(e.target.value)}
                  className="role-select"
                >
                  <option value="user">普通用户</option>
                  <option value="support">客服</option>
                  <option value="admin">管理员</option>
                </select>
              </div>

              <div className="form-group">
                <label>修改原因 *</label>
                <textarea
                  placeholder="请说明修改角色的原因..."
                  value={roleChangeReason}
                  onChange={(e) => setRoleChangeReason(e.target.value)}
                  rows="3"
                  className="reason-textarea"
                />
              </div>
            </div>
            <div className="modal-actions">
              <button 
                className="cancel-btn"
                onClick={() => setShowRoleModal(false)}
              >
                取消
              </button>
              <button 
                className="confirm-btn"
                onClick={updateUserRole}
                disabled={!newRole || !roleChangeReason.trim() || isLoading}
              >
                <Crown size={16} />
                确认修改
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 发送消息弹窗 */}
      {selectedUser && (
        <div className="message-modal-overlay">
          <div className="message-modal">
            <div className="modal-header">
              <h3>发送消息给 {selectedUser.username}</h3>
              <button 
                className="close-btn"
                onClick={() => setSelectedUser(null)}
              >
                ×
              </button>
            </div>
            <div className="modal-content">
              <textarea
                placeholder="输入消息内容..."
                value={messageText}
                onChange={(e) => setMessageText(e.target.value)}
                rows="4"
              />
            </div>
            <div className="modal-actions">
              <button 
                className="cancel-btn"
                onClick={() => setSelectedUser(null)}
              >
                取消
              </button>
              <button 
                className="send-btn"
                onClick={sendMessageToUser}
                disabled={!messageText.trim() || isLoading}
              >
                <MessageSquare size={16} />
                发送消息
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 加载状态 */}
      {isLoading && (
        <div className="loading-overlay">
          <div className="loading-spinner"></div>
        </div>
      )}
    </div>
  );
};

export default AdminPanel; 