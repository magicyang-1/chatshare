import React, { useRef, useState, useEffect } from "react";
import { useOutletContext } from "react-router-dom";
import {
    History,
    Lock,
    MessageSquare,
    MoreVertical,
    Plus,
    Send,
    Settings,
    Star,
    Upload,
    Image,
    Brain,
    Search,
    Heart,
    Eye,
    Download,
    Trash2,
    X,
    ArrowLeft,
    FileText,
    User,
    Crown,
    AlertCircle
} from "lucide-react";
import formatTime from "../utils/formatTime";
import { fileAPI,threeDGenerationAPI } from '../services/api';
import '../styles/text-to-3d.css';

const FeatureContent = () => {
    const {
        activeTab,
        selectedModel,
        setSelectedModel,
        inputText,
        setInputText,
        chatHistory,
        isLoading,
        currentChat,
        chatList,
        isLoadingChats,
        showChatList,
        setShowChatList,
        showHeader,
        setShowHeader,
        messagesEndRef,
        createNewChat,
        showContextMenu,
        switchChat,
        handleSendMessage,
        // 新增的功能
        features,
        availableModels,
        modelsByFeature,
        dragOver,
        imageGenerationPrompt,
        setImageGenerationPrompt,
        imageGenerationSize,
        setImageGenerationSize,
        imageGenerationStyle,
        setImageGenerationStyle,
        supportedSizes,
        supportedStyles,
        referenceImage,
        generatedImages,
        isGeneratingImage,
        templateCategories,
        templates,
        featuredTemplates,
        isLoadingTemplates,
        selectedCategory,
        templateSearchKeyword,
        setTemplateSearchKeyword,
        showCreateTemplate,
        setShowCreateTemplate,
        showTemplateDetail,
        setShowTemplateDetail,
        selectedTemplate,
        currentPage,
        totalPages,
        createTemplateForm,
        setCreateTemplateForm,
        handleModelChange,
        handleDrop,
        handleDragOver,
        handleDragLeave,
        handleSmartImageGeneration,
        handleReferenceImageUpload,
        removeReferenceImage,
        handleCategoryChange,
        loadTemplates,
        handleDeleteTemplate,
        searchTemplates,
        canDeleteTemplate,
        handleLikeTemplate,
        handleUseTemplate,
        handleViewTemplate,
        handleCreateTemplate,
        setActiveTab,
        isMobile,
        // 用户信息
        user,
        onLogout,
    } = useOutletContext();

    const fileInputRef = useRef(null);
    const [uploadedImages, setUploadedImages] = React.useState([]);
    const [isUploading, setIsUploading] = React.useState(false);
    const [uploadProgress, setUploadProgress] = React.useState(0);
    
    // 检查管理员权限
    const isAdmin = user && user.role === 'admin';
    
    // 移动端检测函数
    const isMobileDevice = () => window.innerWidth <= 768;

    // 3D生成相关状态
    const [prompt, setPrompt] = useState('');
    const [mode, setMode] = useState('realistic');
    const [artStyle, setArtStyle] = useState('realistic');
    const [shouldRemesh, setShouldRemesh] = useState(true);
    const [seed, setSeed] = useState(12345);
    const [taskId, setTaskId] = useState('');
    const [taskStatus, setTaskStatus] = useState('');
    const [taskProgress, setTaskProgress] = useState(0);
    const [taskResult, setTaskResult] = useState(null);
    const [taskError, setTaskError] = useState(null);
    const [is3DLoading, setIs3DLoading] = useState(false);
    const [isRefining, setIsRefining] = useState(false);
    const [isDownloading, setIsDownloading] = useState(false);
    const [isGenerating, setIsGenerating] = useState(false);
    const [statusInterval, setStatusInterval] = useState(null);
    const [manualTaskId, setManualTaskId] = useState('');
    const [isQuerying, setIsQuerying] = useState(false);
    // 新增：3D模式切换状态
    const [threeDMode, setThreeDMode] = useState('generate'); // 'generate' 或 'refine' 或 'history'
    
    // 3D历史记录相关状态
    const [historySearchKeyword, setHistorySearchKeyword] = useState('');
    const [historyRecords, setHistoryRecords] = useState([]);
    const [isLoadingHistory, setIsLoadingHistory] = useState(false);
    const [filteredHistory, setFilteredHistory] = useState([]);
    const [selectedHistoryItem, setSelectedHistoryItem] = useState(null);


    // 清理定时器
    useEffect(() => {
        return () => {
            if (statusInterval) {
                clearInterval(statusInterval);
            }
        };
    }, [statusInterval]);

    // 初始化历史记录
    useEffect(() => {
        if (threeDMode === 'history') {
            loadHistoryRecords();
        }
    }, [threeDMode]);

    // 搜索关键词变化时过滤历史记录
    useEffect(() => {
        filterHistoryRecords();
    }, [historySearchKeyword, historyRecords]);

    // 加载历史记录 - 调用后端API
    const loadHistoryRecords = async () => {
        setIsLoadingHistory(true);
        try {
            const response = await threeDGenerationAPI.getHistory();
            if (response.history && Array.isArray(response.history)) {
                setHistoryRecords(response.history);
            } else {
                setHistoryRecords([]);
            }
        } catch (error) {
            console.error('加载历史记录失败:', error);
            setHistoryRecords([]);
        } finally {
            setIsLoadingHistory(false);
        }
    };

    // 过滤历史记录
    const filterHistoryRecords = () => {
        if (!historySearchKeyword.trim()) {
            setFilteredHistory(historyRecords);
            return;
        }

        const keyword = historySearchKeyword.toLowerCase();
        const filtered = historyRecords.filter(record => 
            (record.prompt && record.prompt.toLowerCase().includes(keyword)) ||
            (record.id && record.id.toLowerCase().includes(keyword)) ||
            (record.mode && record.mode.toLowerCase().includes(keyword)) ||
            (record.art_style && record.art_style.toLowerCase().includes(keyword))
        );
        setFilteredHistory(filtered);
    };

    // 搜索历史记录
    const searchHistoryRecords = (keyword) => {
        setHistorySearchKeyword(keyword);
    };

    // 选择历史记录项
    const selectHistoryItem = (item) => {
        setSelectedHistoryItem(item);
    };

    // 重新生成历史记录项
    const regenerateHistoryItem = (item) => {
        setPrompt(item.prompt || '');
        setArtStyle(item.art_style || 'realistic');
        setSeed(12345); // 使用默认种子
        setThreeDMode('generate');
    };



    // 上传文件选择
    const handleFileSelect = async (event) => {
        const files = Array.from(event.target.files);
        await uploadFiles(files);
    };

    // 上传文件核心逻辑
    const uploadFiles = async (files) => {
        if (!files.length) return;
        setIsUploading(true);
        setUploadProgress(0);
        try {
            for (const file of files) {
                if (!file.type.startsWith('image/')) {
                    alert(`文件 ${file.name} 不是图片格式，已跳过`);
                    continue;
                }
                if (file.size > 50 * 1024 * 1024) {
                    alert(`文件 ${file.name} 大小超过50MB，已跳过`);
                    continue;
                }
                const response = await fileAPI.upload(file, setUploadProgress);
                if (!response.data || !response.data.data) {
                    alert('文件上传失败');
                    continue;
                }
                const fileData = response.data.data;
                setUploadedImages(prev => [...prev, {
                    id: fileData.fileName,
                    name: fileData.originalName || file.name,
                    url: fileData.fileUrl,
                    size: fileData.fileSize,
                    type: fileData.mimeType,
                    systemFileName: fileData.fileName
                }]);
            }
        } catch (e) {
            alert('文件上传失败: ' + e.message);
        } finally {
            setIsUploading(false);
            setUploadProgress(0);
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    // 删除图片
    const removeUploadedImage = async (imageId) => {
        try {
            await fileAPI.deleteFile(imageId);
        } catch (e) {
            // 即使删除失败也从界面移除
        }
        setUploadedImages(prev => prev.filter(img => img.id !== imageId));
    };

    const currentFeature = features.find(f => f.id === activeTab);

    // 渲染模型选择器
    const renderModelSelector = () => {
        const currentFeatureModels = modelsByFeature[activeTab] || [];

    return (
                <div className="model-selector">
                    <label>选择模型:</label>
                    <select
                        value={selectedModel}
                    onChange={(e) => handleModelChange(e.target.value)}
                        className="model-select"
                    >
                    {currentFeatureModels.map(model => (
                            <option key={model.id} value={model.id}>
                            {model.name} {model.free && '(免费)'}
                            </option>
                        ))}
                    </select>
                </div>
        );
    };

    // 渲染智能对话功能
    const renderTextToText = () => (
                <div className="chat-layout">
                    {/* 对话列表侧边栏 */}
                    <div className={`chat-list-sidebar ${showChatList ? 'visible' : 'hidden'}`}>
                        <div className="chat-list-header">
                            <h3>对话历史</h3>
                            <div className="chat-list-actions">
                                <button
                                    className="new-chat-btn"
                                    onClick={() => {
                                        createNewChat();
                                        // 移动端点击新建对话后隐藏侧边栏
                                        if (isMobileDevice()) {
                                            setShowChatList(false);
                                        }
                                    }}
                                    title="新建对话"
                                >
                                    <Plus size={16} />
                                </button>
                                <button
                                    className="hide-sidebar-btn"
                                    onClick={() => setShowChatList(false)}
                                    title="隐藏侧边栏"
                                >
                                    <History size={16} />
                                </button>
                            </div>
                        </div>

                        <div className="chat-list">
                            {isLoadingChats ? (
                                <div className="loading-chats">加载中...</div>
                            ) : chatList.length === 0 ? (
                                <div className="empty-chats">
                                    <MessageSquare size={24} />
                                    <p>还没有对话记录</p>
                                    <button 
                                        onClick={() => {
                                            createNewChat();
                                            // 移动端点击开始对话后隐藏侧边栏
                                            if (isMobileDevice()) {
                                                setShowChatList(false);
                                            }
                                        }} 
                                        className="start-chat-btn"
                                    >
                                        开始对话
                                    </button>
                                </div>
                            ) : (
                                chatList.map(chat => (
                                    <div
                                        key={chat.id}
                                        className={`chat-item ${currentChat?.id === chat.id ? 'active' : ''}`}
                                        onClick={() => {
                                            switchChat(chat);
                                            // 移动端点击历史记录后隐藏侧边栏
                                            if (isMobileDevice()) {
                                                setShowChatList(false);
                                            }
                                        }}
                                        onContextMenu={(e) => showContextMenu(e, chat.id)}
                                    >
                                        <div className="chat-item-content">
                                            <div className="chat-title">
                                                {chat.title || '新对话'}
                                            </div>
                                            <div className="chat-meta">
                          <span className="message-count">
                            {chat.messageCount || 0} 条消息
                          </span>
                                                <span className="last-activity">
                            {formatTime(chat.lastActivity)}
                          </span>
                                            </div>
                                        </div>

                                        <div className="chat-actions">
                                            {chat.isFavorite && <Star size={12} className="favorite-icon" />}
                                            {chat.isProtected && <Lock size={12} className="protected-icon" />}
                                            <button
                                                className="more-actions-btn"
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    showContextMenu(e, chat.id);
                                                }}
                                                title="更多操作"
                                            >
                                                <MoreVertical size={12} />
                                            </button>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>

                    {/* 对话区域 */}
                    <div className="chat-container">
                        <div className="chat-header">
                            <div className="chat-header-left">
                                <button
                                    className={`toggle-chat-list ${!showChatList ? 'prominent' : ''}`}
                                    onClick={() => setShowChatList(!showChatList)}
                                    title={showChatList ? '隐藏对话列表' : '显示对话列表'}
                                >
                                    <History size={16} />
                                    {!showChatList && <span className="toggle-text">显示历史</span>}
                                </button>
                            </div>
                            <div className="current-chat-info">
                                <h4>{currentChat?.title || '新对话'}</h4>
                                {currentChat?.messageCount > 0 && (
                                    <span className="chat-message-count">
                      {currentChat.messageCount} 条消息
                    </span>
                                )}
                            </div>
                        </div>



                        <div className="chat-messages">
                            {chatHistory.length === 0 && !isLoading && (
                                <div className="empty-chat">
                                    <MessageSquare size={48} />
                                    <h3>开始新的对话</h3>
                                    <p>在下方输入框中输入您的问题，开始与AI对话</p>
                                </div>
                            )}

                            {chatHistory.map(message => (
                                <div key={message.id} className={`message ${message.role}`}>
                                    <div className="message-content">
                                        {message.content}
                                    </div>
                                    <div className="message-meta">
                      <span className="timestamp">
                        {new Date(message.createdAt).toLocaleTimeString()}
                      </span>
                                    </div>
                                </div>
                            ))}

                            {isLoading && (
                                <div className="message assistant loading">
                                    <div className="typing-indicator">
                                        <span></span>
                                        <span></span>
                                        <span></span>
                                    </div>
                                </div>
                            )}

                            {/* 用于滚动到底部的隐藏元素 */}
                            <div ref={messagesEndRef} />
                        </div>

                        {/* 图片预览区 */}
                        {uploadedImages.length > 0 && (
                            <div className="uploaded-images" style={{ display: 'flex', gap: '8px', padding: '8px 0' }}>
                                {uploadedImages.map(image => (
                                    <div key={image.id} className="uploaded-image" style={{ position: 'relative' }}>
                                        <img src={image.url} alt={image.name} style={{ width: 60, height: 60, borderRadius: 8, objectFit: 'cover' }} />
                                        <button className="remove-image-btn" style={{ position: 'absolute', top: 2, right: 2, background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: 20, height: 20, cursor: 'pointer' }} onClick={() => removeUploadedImage(image.id)}><X size={12} /></button>
                                    </div>
                                ))}
                            </div>
                        )}
                        <div className="chat-input">
                            <input
                                type="text"
                                value={inputText}
                                onChange={(e) => setInputText(e.target.value)}
                                placeholder="输入您的问题..."
                                onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                                disabled={isLoading}
                            />
                            <input
                                type="file"
                                ref={fileInputRef}
                                style={{ display: 'none' }}
                                accept="image/*"
                                multiple
                                onChange={handleFileSelect}
                            />
                            <button
                                onClick={() => fileInputRef.current?.click()}
                                className="upload-button"
                                title="上传图片"
                                disabled={isUploading}
                            >
                                {isUploading ? `${uploadProgress}%` : <Upload size={20} />}
                            </button>
                            <button
                                onClick={handleSendMessage}
                                disabled={isLoading || !inputText.trim()}
                                className="send-button"
                            >
                                <Send size={20} />
                            </button>
                        </div>
            </div>
        </div>
    );

    // 渲染智能生图功能
    const renderSmartImageGeneration = () => (
        <div className="image-generation-layout">
            <div className="image-generation-header">
                <h3>智能图像生成</h3>
                <p>输入提示词，AI将为您生成精美的图像</p>
            </div>

            <div className="image-generation-content">
                <div className="prompt-section">
                    <div className="prompt-input">
                        <textarea
                            value={imageGenerationPrompt}
                            onChange={(e) => setImageGenerationPrompt(e.target.value)}
                            placeholder="描述您想要生成的图像，例如：一只可爱的小猫坐在花园里"
                            rows={4}
                        />
                    </div>

                    <div className="generation-options">
                        <div className="option-group">
                            <label>图像尺寸:</label>
                            <select
                                value={imageGenerationSize}
                                onChange={(e) => setImageGenerationSize(e.target.value)}
                            >
                                {supportedSizes.map(size => (
                                    <option key={size} value={size}>{size}</option>
                                ))}
                            </select>
                        </div>

                        <div className="option-group">
                            <label>图像风格:</label>
                            <select
                                value={imageGenerationStyle}
                                onChange={(e) => setImageGenerationStyle(e.target.value)}
                            >
                                {supportedStyles.map(style => (
                                    <option key={style} value={style}>
                                        {style === '<auto>' ? '自动' : style.replace(/[<>]/g, '')}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="reference-image-section">
                        <label>参考图片 (可选):</label>
                        <div className="reference-image-upload">
                            {referenceImage ? (
                                <div className="reference-image">
                                    <img src={referenceImage.url} alt="参考图片" />
                                    <button onClick={removeReferenceImage}>
                                        <X size={16} />
                                    </button>
                                </div>
                            ) : (
                                <div className="upload-area">
                                    <input
                                        type="file"
                                        onChange={handleReferenceImageUpload}
                                        accept="image/*"
                                        style={{ display: 'none' }}
                                        id="reference-image-input"
                                    />
                                    <label htmlFor="reference-image-input">
                                        <Upload size={24} />
                                        <span>上传参考图片</span>
                                    </label>
                                </div>
                            )}
                        </div>
                    </div>

                    <button
                        className="generate-btn"
                        onClick={handleSmartImageGeneration}
                        disabled={isGeneratingImage || !imageGenerationPrompt.trim()}
                    >
                        {isGeneratingImage ? '生成中...' : '生成图像'}
                    </button>
                </div>

                <div className="generated-images">
                    <h4>生成的图像</h4>
                    {generatedImages.length === 0 ? (
                        <div className="empty-images">
                            <Image size={48} />
                            <p>还没有生成的图像</p>
                        </div>
                    ) : (
                        <div className="image-grid">
                            {generatedImages.map((image, index) => (
                                <div key={index} className="generated-image">
                                    <img src={image.url} alt={`生成的图像 ${index + 1}`} />
                                    <div className="image-actions">
                                        <button onClick={() => window.open(image.url, '_blank')}>
                                            <Eye size={16} />
                                        </button>
                                        <button onClick={() => {
                                            const link = document.createElement('a');
                                            link.href = image.url;
                                            link.download = `generated-image-${index + 1}.png`;
                                            link.click();
                                        }}>
                                            <Download size={16} />
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );

    // 渲染AI模板库功能 - 优化布局和深色模式支持
    const renderPromptTemplateLibrary = () => (
        <div className="prompt-template-library">
            {/* 标题栏 - 调整为非固定位置，放在sidebar下面 */}
            <div className="template-library-header">
                <div className="library-title">
                    <h1>AI模板库</h1>
                    <p>精选优质提示词模板，提升您的AI对话体验</p>
                </div>
                <div className="library-actions">
                    <button 
                        className="create-template-btn" 
                        onClick={() => setShowCreateTemplate(true)}
                        title="创建新模板"
                    >
                        <Plus size={16} />
                        创建模板
                    </button>
                </div>
            </div>

            {/* 搜索栏 - 只在点击/回车时触发搜索 */}
            <div className="template-search-container">
                <div className="template-search-bar">
                    <input
                        type="text"
                        className="template-search-input"
                        placeholder="搜索模板标题、描述或内容..."
                        value={templateSearchKeyword}
                        onChange={(e) => setTemplateSearchKeyword(e.target.value)}
                        onKeyPress={(e) => {
                            if (e.key === 'Enter') {
                                e.preventDefault();
                                searchTemplates();
                            }
                        }}
                        onFocus={(e) => {
                            e.target.select();
                        }}
                    />
                    <button 
                        className="template-search-btn" 
                        onClick={() => {
                            if (templateSearchKeyword.trim()) {
                                searchTemplates();
                            } else {
                                loadTemplates(0);
                            }
                        }}
                        title="搜索模板"
                    >
                        <Search size={16} />
                    </button>
                </div>
            </div>

            {/* 分类选择 - 优化布局 */}
            <div className="template-categories">
                <button
                    className={`category-btn ${!selectedCategory ? 'active' : ''}`}
                    onClick={() => handleCategoryChange(null)}
                    title="显示所有模板"
                >
                    全部
                </button>
                {templateCategories.map(category => (
                    <button
                        key={category.id}
                        className={`category-btn ${selectedCategory === category.id ? 'active' : ''}`}
                        onClick={() => handleCategoryChange(category.id)}
                        title={`筛选 ${category.name} 分类`}
                    >
                        {category.name}
                    </button>
                ))}
            </div>

            {/* 模板内容区独立滚动 */}
            <div className="template-sections">
                {/* 搜索状态提示 */}
                {templateSearchKeyword.trim() && (
                    <div className="search-status">
                        <Search size={16} />
                        <span>搜索关键词: "{templateSearchKeyword}"</span>
                        <button 
                            onClick={() => {
                                setTemplateSearchKeyword('');
                                loadTemplates(0);
                            }}
                        >
                            清除搜索
                        </button>
                    </div>
                )}

                {/* 精选模板 - 优化显示逻辑 */}
                {featuredTemplates.length > 0 && !templateSearchKeyword.trim() && (
                    <div className="template-section">
                        <h3>⭐ 精选模板</h3>
                        <div className="template-grid">
                            {featuredTemplates.map(template => (
                                <div key={template.id} className="template-card">
                                    <div className="template-header">
                                        <h4 title={template.title}>{template.title}</h4>
                                        <span className={template.isOfficial ? 'official-badge' : 'user-badge'}>
                                            {template.isOfficial ? '官方' : '用户'}
                                        </span>
                                    </div>
                                    <p className="template-description" title={template.description}>
                                        {template.description}
                                    </p>
                                    <div className="template-meta">
                                        <span className="category-tag" title={`分类: ${template.categoryName}`}>
                                            {template.categoryName}
                                        </span>
                                        <span className="model-tag" title={`推荐模型: ${template.aiModel}`}>
                                            {template.aiModel}
                                        </span>
                                    </div>
                                    <div className="template-stats">
                                        <span className={template.liked ? 'liked' : ''} title="点赞数">
                                            ❤️ {template.likeCount || 0}
                                        </span>
                                        <span title="使用次数">👁️ {template.useCount || 0}</span>
                                    </div>
                                    <div className="template-actions">
                                        <button 
                                            className={`like-btn ${template.liked ? 'liked' : ''} ${isLoadingTemplates ? 'loading' : ''}`} 
                                            onClick={() => handleLikeTemplate(template.id)}
                                            title={template.liked ? '取消点赞' : '点赞'}
                                            disabled={isLoadingTemplates}
                                        >
                                            <Heart size={14} />
                                        </button>
                                        <button 
                                            className="view-btn" 
                                            onClick={() => handleViewTemplate(template)}
                                            title="查看详情"
                                        >
                                            <Eye size={14} />
                                        </button>
                                        <button 
                                            className="use-btn" 
                                            onClick={() => handleUseTemplate(template)}
                                            title="使用此模板"
                                        >
                                            使用
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* 模板列表 - 优化显示逻辑 */}
                <div className="template-section">
                    <h3>
                        {templateSearchKeyword.trim() ? '🔍 搜索结果' : '📚 模板库'}
                    </h3>
                    {isLoadingTemplates ? (
                        <div className="loading-templates">
                            <p>正在加载模板...</p>
                        </div>
                    ) : templates.length === 0 ? (
                        <div className="empty-state">
                            <Brain size={64} />
                            <h4>
                                {templateSearchKeyword.trim() 
                                    ? `未找到包含"${templateSearchKeyword}"的模板` 
                                    : '暂无模板'
                                }
                            </h4>
                            <p>
                                {templateSearchKeyword.trim() 
                                    ? '尝试使用不同的关键词搜索，或清除搜索条件查看所有模板'
                                    : '该分类下还没有模板，快来创建第一个吧！'
                                }
                            </p>
                            {!templateSearchKeyword.trim() && (
                                <button 
                                    className="create-template-btn" 
                                    onClick={() => setShowCreateTemplate(true)}
                                    style={{ marginTop: '15px' }}
                                >
                                    <Plus size={16} />
                                    创建模板
                                </button>
                            )}
                        </div>
                    ) : (
                        <>
                            <div className="template-grid">
                                {templates.map(template => (
                                    <div key={template.id} className="template-card">
                                        <div className="template-header">
                                            <h4 title={template.title}>{template.title}</h4>
                                            <span className={template.isOfficial ? 'official-badge' : 'user-badge'}>
                                                {template.isOfficial ? '官方' : '用户'}
                                            </span>
                                        </div>
                                        <p className="template-description" title={template.description}>
                                            {template.description}
                                        </p>
                                        <div className="template-meta">
                                            <span className="category-tag" title={`分类: ${template.categoryName}`}>
                                                {template.categoryName}
                                            </span>
                                            <span className="model-tag" title={`推荐模型: ${template.aiModel}`}>
                                                {template.aiModel}
                                            </span>
                                        </div>
                                        <div className="template-stats">
                                                                                    <span className={template.liked ? 'liked' : ''} title="点赞数">
                                            ❤️ {template.likeCount || 0}
                                        </span>
                                            <span title="使用次数">👁️ {template.useCount || 0}</span>
                                        </div>
                                        <div className="template-actions">
                                            <button 
                                                className={`like-btn ${template.liked ? 'liked' : ''} ${isLoadingTemplates ? 'loading' : ''}`} 
                                                onClick={() => handleLikeTemplate(template.id)}
                                                title={template.liked ? '取消点赞' : '点赞'}
                                                disabled={isLoadingTemplates}
                                            >
                                                <Heart size={14} />
                                            </button>
                                            <button 
                                                className="view-btn" 
                                                onClick={() => handleViewTemplate(template)}
                                                title="查看详情"
                                            >
                                                <Eye size={14} />
                                            </button>
                                            <button 
                                                className="use-btn" 
                                                onClick={() => handleUseTemplate(template)}
                                                title="使用此模板"
                                            >
                                                使用
                                            </button>
                                            {canDeleteTemplate(template) && (
                                                <button 
                                                    className="delete-btn" 
                                                    onClick={() => handleDeleteTemplate(template)}
                                                    title="删除模板"
                                                >
                                                    <Trash2 size={12} />
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                            
                            {/* 分页组件 */}
                            {totalPages > 1 && (
                                <div className="pagination">
                                    <button 
                                        className="pagination-btn"
                                        onClick={() => loadTemplates(currentPage - 1)}
                                        disabled={currentPage === 0}
                                        title="上一页"
                                    >
                                        ← 上一页
                                    </button>
                                    
                                    <div className="pagination-numbers">
                                        {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                            let pageNum;
                                            if (totalPages <= 5) {
                                                pageNum = i;
                                            } else if (currentPage < 3) {
                                                pageNum = i;
                                            } else if (currentPage >= totalPages - 3) {
                                                pageNum = totalPages - 5 + i;
                                            } else {
                                                pageNum = currentPage - 2 + i;
                                            }
                                            
                                            return (
                                                <button
                                                    key={pageNum}
                                                    className={`pagination-number ${currentPage === pageNum ? 'active' : ''}`}
                                                    onClick={() => loadTemplates(pageNum)}
                                                    title={`第 ${pageNum + 1} 页`}
                                                >
                                                    {pageNum + 1}
                                                </button>
                                            );
                                        })}
                                    </div>
                                    
                                    <button 
                                        className="pagination-btn"
                                        onClick={() => loadTemplates(currentPage + 1)}
                                        disabled={currentPage >= totalPages - 1}
                                        title="下一页"
                                    >
                                        下一页 →
                                    </button>
                                    
                                    <div className="pagination-info">
                                        第 {currentPage + 1} 页，共 {totalPages} 页
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );

    // 渲染模板详情模态框 - 优化深色模式支持
    const renderTemplateDetailModal = () => {
        if (!showTemplateDetail || !selectedTemplate) return null;

        return (
            <div className="template-modal-overlay" onClick={() => setShowTemplateDetail(false)}>
                <div className="template-modal" onClick={(e) => e.stopPropagation()}>
                    <button 
                        className="modal-close-btn" 
                        onClick={() => setShowTemplateDetail(false)}
                        title="关闭详情"
                    >
                        <X size={20} />
                    </button>
                    
                    <div className="modal-template-content">
                        <h2 className="modal-template-title">{selectedTemplate.title}</h2>
                        
                        <div className="modal-template-meta">
                            {selectedTemplate.isOfficial ? (
                                <span className="official-badge">官方</span>
                            ) : (
                                <span className="user-badge">用户</span>
                            )}
                            <span className="category-tag">{selectedTemplate.categoryName}</span>
                            <span className="model-tag">{selectedTemplate.aiModel}</span>
                        </div>
                        
                        <p className="modal-template-description">{selectedTemplate.description}</p>
                        
                        <div className="modal-template-prompt">
                            <h4>📝 Prompt内容</h4>
                            <pre>{selectedTemplate.content}</pre>
                        </div>
                        
                        <div className="template-stats" style={{ marginBottom: '20px' }}>
                            <span className={selectedTemplate.liked ? 'liked' : ''} title="点赞数">
                                ❤️ {selectedTemplate.likeCount || 0} 人点赞
                            </span>
                            <span title="使用次数">👁️ {selectedTemplate.useCount || 0} 次使用</span>
                        </div>
                    </div>
                    
                    <div className="modal-actions">
                        <button 
                            className={`like-btn ${selectedTemplate.liked ? 'liked' : ''} ${isLoadingTemplates ? 'loading' : ''}`}
                            onClick={() => handleLikeTemplate(selectedTemplate.id)}
                            title={selectedTemplate.liked ? '取消点赞' : '点赞'}
                            disabled={isLoadingTemplates}
                        >
                            {selectedTemplate.liked ? '❤️ 已点赞' : '👍 点赞'}
                        </button>
                        
                        <button 
                            className="use-btn"
                            onClick={() => {
                                handleUseTemplate(selectedTemplate);
                                setShowTemplateDetail(false);
                            }}
                            title="使用此模板"
                        >
                            使用模板
                        </button>
                        
                        {canDeleteTemplate(selectedTemplate) && (
                            <button 
                                className="delete-btn"
                                onClick={() => {
                                    handleDeleteTemplate(selectedTemplate);
                                    setShowTemplateDetail(false);
                                }}
                                title="删除模板"
                            >
                                🗑️ 删除
                            </button>
                        )}
                    </div>
                </div>
            </div>
        );
    };

    // 渲染创建模板模态框 - 优化深色模式支持
    const renderCreateTemplateModal = () => {
        if (!showCreateTemplate) return null;

        return (
            <div className="template-modal-overlay" onClick={() => setShowCreateTemplate(false)}>
                <div className="create-template-modal" onClick={(e) => e.stopPropagation()}>
                    <button 
                        className="modal-close-btn" 
                        onClick={() => setShowCreateTemplate(false)}
                        title="关闭创建"
                    >
                        <X size={20} />
                    </button>
                    
                    <h2 style={{ fontSize: '1.8rem', fontWeight: '700', marginBottom: '20px' }}>创建新模板</h2>
                    
                    <div className="create-template-form">
                        <div className="form-group">
                            <label>模板标题 *</label>
                            <input
                                type="text"
                                value={createTemplateForm.title}
                                onChange={(e) => setCreateTemplateForm(prev => ({ ...prev, title: e.target.value }))}
                                placeholder="请输入模板标题"
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>模板描述 *</label>
                            <textarea
                                value={createTemplateForm.description}
                                onChange={(e) => setCreateTemplateForm(prev => ({ ...prev, description: e.target.value }))}
                                placeholder="请描述模板的用途和特点"
                                style={{ minHeight: '80px' }}
                            />
                        </div>
                        
                        <div className="form-group">
                            <label>模板内容 *</label>
                            <textarea
                                value={createTemplateForm.content}
                                onChange={(e) => setCreateTemplateForm(prev => ({ ...prev, content: e.target.value }))}
                                placeholder="请输入提示词模板内容"
                                style={{ minHeight: '120px' }}
                            />
                        </div>
                        
                        <div style={{ display: 'flex', gap: '15px' }}>
                            <div className="form-group" style={{ flex: 1 }}>
                                <label>模板分类 *</label>
                                <select
                                    value={createTemplateForm.categoryId}
                                    onChange={(e) => setCreateTemplateForm(prev => ({ ...prev, categoryId: e.target.value }))}
                                >
                                    <option value="">请选择分类</option>
                                    {templateCategories
                                        .filter(cat => cat.id !== null)
                                        .map(category => (
                                        <option key={category.id} value={category.id}>
                                            {category.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            
                            <div className="form-group" style={{ flex: 1 }}>
                                <label>推荐AI模型 *</label>
                                <select
                                    value={createTemplateForm.aiModel}
                                    onChange={(e) => setCreateTemplateForm(prev => ({ ...prev, aiModel: e.target.value }))}
                                >
                                    <option value="">请选择模型</option>
                                    <option value="qwen">qwen</option>
                                    <option value="deepseek">deepseek</option>
                                    <option value="gpt">gpt</option>
                                    <option value="gemini">gemini</option>
                                    <option value="文生图">文生图</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    
                    <div className="form-actions">
                        <button 
                            className="cancel-btn"
                            onClick={() => setShowCreateTemplate(false)}
                        >
                            取消
                        </button>
                        <button 
                            className="submit-btn"
                            onClick={handleCreateTemplate}
                        >
                            创建模板
                        </button>
                    </div>
                </div>
            </div>
        );
    };

    const render3DGeneration = () => {
        // 检查管理员权限
        if (!isAdmin) {
            return (
                <div className="text-to-3d-container">
                    <div className="permission-denied">
                        <div className="permission-denied-content">
                            <AlertCircle size={64} className="permission-icon" />
                            <h2>权限不足</h2>
                            <p>文生3D功能仅对管理员开放</p>
                            <p>您当前的角色是：{user?.role || '未知'}</p>
                        </div>
                    </div>
                </div>
            );
        }

        // 轮询任务状态
        const startStatusPolling = (taskId) => {
            const interval = setInterval(async () => {
                try {
                    const response = await threeDGenerationAPI.getTextTo3DStatus(taskId);
                    if (response.status) {
                        const statusData = JSON.parse(response.status);
                        
                        if (statusData.status === 'SUCCEEDED') {
                            setTaskStatus('SUCCEEDED');
                            setTaskProgress(100);
                            setTaskResult(statusData); // 直接存整个对象
                            clearInterval(interval);
                            setStatusInterval(null);
                        } else if (statusData.status === 'FAILED') {
                            setTaskStatus('FAILED');
                            setTaskError('生成失败');
                            clearInterval(interval);
                            setStatusInterval(null);
                        } else if (statusData.status === 'PENDING' || statusData.status === 'IN_PROGRESS') {
                            setTaskStatus('IN_PROGRESS');
                            setTaskProgress(Math.min(90, taskProgress + 10));
                        }
                    }
                } catch (error) {
                    console.error('查询状态失败:', error);
                }
            }, 3000); // 每3秒查询一次

            setStatusInterval(interval);
        };

        // 手动查询任务状态
        const queryTaskStatus = async () => {
            if (!manualTaskId.trim()) {
                alert('请输入任务ID');
                return;
            }

            setIsQuerying(true);
            setTaskError(null);
            setTaskResult(null);

            try {
                const response = await threeDGenerationAPI.getTextTo3DStatus(manualTaskId.trim());
                if (response.status) {
                    const statusData = JSON.parse(response.status);
                    
                    setTaskId(manualTaskId.trim());
                    setTaskStatus(statusData.status);
                    console.log('statusData:', statusData);
                    console.log('statusData.status:', statusData.status);
                    
                    if (statusData.status === 'SUCCEEDED') {
                        setTaskProgress(100);
                        setTaskResult(statusData);
                    } else if (statusData.status === 'FAILED') {
                        setTaskError('任务失败');
                    } else if (statusData.status === 'PENDING' || statusData.status === 'IN_PROGRESS') {
                        setTaskProgress(50); // 假设进行中
                        // 开始轮询
                        startStatusPolling(manualTaskId.trim());
                    }
                } else {
                    setTaskError('查询失败，请检查任务ID是否正确');
                }
            } catch (error) {
                console.error('查询任务状态失败:', error);
                setTaskError('查询失败，请检查任务ID是否正确');
            } finally {
                setIsQuerying(false);
            }
        };

        // 生成3D模型
        const generate3D = async () => {
            if (!prompt.trim()) {
                alert('请输入提示词');
                return;
            }

            setIsGenerating(true);
            setTaskError(null);
            setTaskResult(null);

            try {
                const response = await threeDGenerationAPI.createTextTo3D(prompt.trim(), 'preview', artStyle, shouldRemesh, seed);
                console.log('API响应:', response); // 调试信息
                
                if (response.success) {
                    console.log('response.taskId:', response.taskId); // 调试信息
                    if (response.taskId) {
                        setTaskId(response.taskId);
                        setTaskStatus('IN_PROGRESS');
                        setTaskProgress(10);
                        
                        // 开始轮询任务状态
                        startStatusPolling(response.taskId);
                    } else {
                        console.error('响应数据结构异常:', response);
                        setTaskError('响应数据结构异常');
                    }
                } else {
                    setTaskError(response.error || '生成失败');
                }
            } catch (error) {
                console.error('生成3D模型失败:', error);
                setTaskError('网络错误，请重试');
            } finally {
                setIsGenerating(false);
            }
        };



        // 精炼3D模型
        const refine3D = async () => {
            if (!taskId) return;

            setIsRefining(true);
            setTaskError(null);
            setTaskResult(null);
            
            try {
                console.log('taskId:', taskId);
                console.log('prompt:', prompt);
                const response = await threeDGenerationAPI.refineTextTo3D(taskId, prompt.trim());
                if (response.success) {
                    if (response.taskId) {
                        setTaskId(response.taskId);
                        setTaskStatus('IN_PROGRESS');
                        setTaskProgress(10);
                        startStatusPolling(response.taskId);
                    } else {
                        setTaskError('精炼失败：未获取到任务ID');
                    }
                } else {
                    setTaskError(response.error || '精炼失败');
                }
            } catch (error) {
                console.error('精炼失败:', error);
                setTaskError('精炼失败：' + error.message);
            } finally {
                setIsRefining(false);
            }
        };

        // 下载3D模型
        const download3D = async () => {
            if (!taskResult?.output?.model_url) return;

            setIsDownloading(true);
            try {
                const link = document.createElement('a');
                link.href = taskResult.output.model_url;
                link.download = 'model.glb';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } catch (error) {
                console.error('下载失败:', error);
                setTaskError('下载失败');
            } finally {
                setIsDownloading(false);
            }
        };



        return (
            <div className="text-to-3d-container">
                {/* 模式切换器 */}
                <div className="mode-switcher">
                    <div className="mode-tabs">
                        <button
                            className={`mode-tab ${threeDMode === 'generate' ? 'active' : ''}`}
                            onClick={() => setThreeDMode('generate')}
                        >
                            <Plus size={16} />
                            生成新模型
                        </button>
                        <button
                            className={`mode-tab ${threeDMode === 'refine' ? 'active' : ''}`}
                            onClick={() => setThreeDMode('refine')}
                        >
                            <Settings size={16} />
                            精炼模型
                        </button>
                                <button
                            className={`mode-tab ${threeDMode === 'history' ? 'active' : ''}`}
                            onClick={() => setThreeDMode('history')}
                                >
                            <History size={16} />
                            历史记录
                                </button>
                    </div>
                </div>

                {/* 主要内容区域 */}
                <div className="threed-main-content">
                    {/* 左侧表单区域 */}
                    <div className={`threed-form-section ${threeDMode === 'history' ? 'history-mode' : ''}`}>
                {/* 根据模式显示不同的表单 */}
                {threeDMode === 'generate' ? (
                            <>
                        <h3>生成3D模型</h3>
                                <div className="threed-form-group">
                        <label>提示词 *</label>
                        <textarea
                            value={prompt}
                            onChange={(e) => setPrompt(e.target.value)}
                            placeholder="描述您想要生成的3D模型，例如：一个红色的汽车"
                            rows={3}
                        />
                    </div>

                                <div className="threed-form-row">
                                    <div className="threed-form-group">
                            <label>艺术风格</label>
                            <select value={artStyle} onChange={(e) => setArtStyle(e.target.value)}>
                                <option value="realistic">写实</option>
                                <option value="sculpture">雕塑</option>
                            </select>
                    </div>

                                    <div className="threed-form-group">
                            <label>随机种子</label>
                            <input
                                type="number"
                                value={seed}
                                onChange={(e) => setSeed(parseInt(e.target.value) || 12345)}
                                min="1"
                                max="999999"
                            />
                        </div>
                    </div>

                                <div className="threed-form-group">
                                    <div className="threed-checkbox-group">
                                        <input
                                            type="checkbox"
                                            checked={shouldRemesh}
                                            onChange={(e) => setShouldRemesh(e.target.checked)}
                                        />
                                        <label>重新网格化</label>
                                    </div>
                                </div>

                        <button
                                    className={`threed-btn threed-btn-primary ${isGenerating ? 'threed-generating' : ''}`}
                            onClick={generate3D}
                            disabled={isGenerating || !prompt.trim()}
                        >
                            {isGenerating ? '生成中...' : '生成3D模型'}
                        </button>
                            </>
                        ) : threeDMode === 'refine' ? (
                            <>
                        <h3>精炼3D模型</h3>
                                <div className="threed-form-group">
                            <label>任务ID *</label>
                            <input
                                type="text"
                                value={taskId}
                                onChange={(e) => setTaskId(e.target.value)}
                                placeholder="输入要精炼的任务ID"
                            />
                        </div>
                                <div className="threed-form-group">
                            <label>精炼提示词 *</label>
                            <textarea
                                value={prompt}
                                onChange={(e) => setPrompt(e.target.value)}
                                placeholder="描述您希望如何精炼这个3D模型"
                                rows={3}
                            />
                        </div>
                            <button
                                    className={`threed-btn threed-btn-primary ${isRefining ? 'threed-generating' : ''}`}
                                onClick={refine3D}
                                disabled={isRefining || !prompt.trim() || !taskId.trim()}
                            >
                                {isRefining ? '精炼中...' : '精炼模型'}
                            </button>
                            </>
                        ) : (
                            <>
                                <h3>历史记录</h3>
                                {/* 搜索栏 */}
                                <div className="threed-form-group">
                                    <label>搜索历史记录</label>
                                    <div className="threed-search-input">
                                        <input
                                            type="text"
                                            value={historySearchKeyword}
                                            onChange={(e) => searchHistoryRecords(e.target.value)}
                                            placeholder="搜索提示词、记录ID、模式或风格..."
                                        />
                                        <Search size={16} className="search-icon" />
                        </div>
                    </div>

                                {/* 历史记录列表 */}
                                <div className="threed-history-list">
                                    {isLoadingHistory ? (
                                        <div className="threed-loading">
                                            <div className="loading-spinner"></div>
                                            <p>加载历史记录中...</p>
                                        </div>
                                    ) : filteredHistory.length === 0 ? (
                                        <div className="threed-empty-state">
                                            <FileText size={32} />
                                            <p>
                                                {historySearchKeyword.trim() ? '没有找到匹配的记录' : '暂无历史记录'}
                                            </p>
                                        </div>
                                    ) : (
                                        filteredHistory.map((item) => (
                                            <div 
                                                key={item.id} 
                                                className={`threed-history-item ${selectedHistoryItem?.id === item.id ? 'selected' : ''}`}
                                                onClick={() => selectHistoryItem(item)}
                                            >
                                                <div className="history-item-header">
                                                    <span className="history-item-id">{item.id}</span>
                                                    <span className={`history-item-status status-${item.mode || 'unknown'}`}>
                                                        {item.mode === 'preview' ? '预览' : 
                                                         item.mode === 'refine' ? '精炼' : '未知'}
                                                    </span>
                                                </div>
                                                <div className="history-item-content">
                                                    <p className="history-item-prompt">{item.prompt || '无提示词'}</p>
                                                    <div className="history-item-meta">
                                                        <span>模型: Meshy-4</span>
                                                        <span>模式: {item.mode || '未知'}</span>
                                                        <span>风格: {item.art_style || '未知'}</span>
                                                        <span>时间: {item.created_at ? new Date(item.created_at).toLocaleString() : '未知时间'}</span>
                                                    </div>
                                                </div>
                                                <div className="history-item-actions">
                                                    <button 
                                                        className="threed-btn-secondary"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            regenerateHistoryItem(item);
                                                        }}
                                                    >
                                                        <ArrowLeft size={12} />
                                                        复用
                                                    </button>
                                                </div>
                                            </div>
                                        ))
                                    )}
                                </div>
                            </>
                        )}
                    </div>

                    {/* 右侧状态区域 */}
                    <div className="threed-status-section">
                        {threeDMode === 'history' ? (
                            <>
                                {/* 历史记录模式下的右侧内容 */}
                                <div className="threed-status-card">
                                    <h4>当前模型</h4>
                                    <div className="status-info-item">
                                        <span className="label">模型:</span>
                                        <span className="value">Meshy-4</span>
                                    </div>
                                    <div className="status-info-item">
                                        <span className="label">总记录数:</span>
                                        <span className="value">{historyRecords.length}</span>
                                    </div>
                                    <div className="status-info-item">
                                        <span className="label">筛选结果:</span>
                                        <span className="value">{filteredHistory.length}</span>
                                    </div>
                                </div>

                                {/* 选中历史记录项的详细信息 */}
                                {selectedHistoryItem && (
                                    <div className="threed-result-card">
                                        <h4>记录详情</h4>
                                        <div className="status-info-item">
                                            <span className="label">记录ID:</span>
                                            <span className="value">{selectedHistoryItem.id}</span>
                                        </div>
                                        <div className="status-info-item">
                                            <span className="label">模式:</span>
                                            <span className={`value status-${selectedHistoryItem.mode || 'unknown'}`}>
                                                {selectedHistoryItem.mode === 'preview' ? '预览' : 
                                                 selectedHistoryItem.mode === 'refine' ? '精炼' : '未知'}
                                            </span>
                                        </div>
                                        <div className="status-info-item">
                                            <span className="label">艺术风格:</span>
                                            <span className="value">{selectedHistoryItem.art_style || '未知'}</span>
                                        </div>
                                        <div className="status-info-item">
                                            <span className="label">创建时间:</span>
                                            <span className="value">{selectedHistoryItem.created_at ? new Date(selectedHistoryItem.created_at).toLocaleString() : '未知时间'}</span>
                                        </div>
                                        
                                        <div className="status-info-item">
                                            <span className="label">提示词:</span>
                                            <span className="value">{selectedHistoryItem.prompt || '无提示词'}</span>
                                        </div>
                                        
                                        <div className="status-info-item">
                                            <span className="label">使用说明:</span>
                                            <span className="value">点击"复用"按钮可以使用该记录的参数重新生成</span>
                                        </div>
                                    </div>
                                )}
                            </>
                        ) : (
                            <>
                                {/* 任务查询组件 */}
                                <div className="task-query-compact">
                                    <h4>快速查询</h4>
                                    <div className="task-query-input">
                                        <input
                                            type="text"
                                            value={manualTaskId}
                                            onChange={(e) => setManualTaskId(e.target.value)}
                                            placeholder="输入任务ID查询"
                                        />
                                        <button
                                            onClick={queryTaskStatus}
                                            disabled={isQuerying || !manualTaskId.trim()}
                                        >
                                            {isQuerying ? '查询中...' : '查询'}
                                        </button>
                                    </div>
                                </div>

                {/* 任务状态显示 */}
                {taskId && (
                                    <div className="threed-status-card">
                                        <h4>任务状态</h4>
                                        <div className="status-info-item">
                                            <span className="label">任务ID:</span>
                                            <span className="value">{taskId}</span>
                                        </div>
                                        <div className="status-info-item">
                                            <span className="label">状态:</span>
                                            <span className="value">
                                                {taskStatus === 'PENDING' ? '等待中' : 
                                      taskStatus === 'IN_PROGRESS' ? '处理中' : 
                                      taskStatus === 'SUCCEEDED' ? '已完成' : 
                                                 taskStatus === 'FAILED' ? '失败' : '未知'}
                                            </span>
                                        </div>
                            
                            {(taskStatus === 'PENDING' || taskStatus === 'IN_PROGRESS') && (
                                            <>
                                                <div className="threed-progress">
                                    <div 
                                                        className="threed-progress-fill" 
                                        style={{ width: `${taskProgress}%` }}
                                    ></div>
                                </div>
                                                <div className="threed-progress-text">{taskProgress}%</div>
                                            </>
                            )}

                            {taskError && (
                                            <div className="threed-error">
                                    {taskError}
                                            </div>
                                        )}
                                </div>
                            )}

                                {/* 生成结果显示 */}
                            {taskStatus === 'SUCCEEDED' && taskResult && (
                                    <div className="threed-result-card">
                                    <h4>生成结果</h4>
                                        <div className="threed-result-preview">
                                            <img 
                                                src={taskResult.thumbnail_url} 
                                                alt="3D模型预览" 
                                            />
                                    </div>
                                        <div className="threed-result-actions">
                                            <a 
                                                href={taskResult.model_urls.glb} 
                                                download 
                                                className="threed-download-btn"
                                            >
                                                <Download size={14} />
                                                下载GLB
                                            </a>
                                            <a 
                                                href={taskResult.model_urls.fbx} 
                                                download 
                                                className="threed-download-btn"
                                            >
                                                <Download size={14} />
                                                下载FBX
                                            </a>
                                        </div>
                                </div>
                                )}
                            </>
                            )}
                        </div>
                    </div>
            </div>
        );
    }

    return (
        <div className="feature-content" data-feature={activeTab}>
            {/* 在AI模板库页面时不显示feature-header */}
            {activeTab !== 'prompt_template_library' && (
                <div className="feature-header">
                    <div className="feature-title">
                        <currentFeature.icon className="feature-icon" />
                        <div>
                            <h2>{currentFeature.name}</h2>
                            <p>{currentFeature.description}</p>
                        </div>
                    </div>

                    {renderModelSelector()}
                </div>
            )}

            {/* 根据当前功能渲染不同内容 */}
            {activeTab === 'text_to_text' && renderTextToText()}
            {activeTab === 'smart_image_generation' && renderSmartImageGeneration()}
            {activeTab === 'prompt_template_library' && renderPromptTemplateLibrary()}
            {activeTab === 'text_to_3d' && render3DGeneration()}
            
            {/* 其他功能显示占位符 */}
            {!['text_to_text', 'smart_image_generation', 'prompt_template_library', 'text_to_3d'].includes(activeTab) && (
                <div className="feature-placeholder">
                    <div className="placeholder-content">
                        <Upload size={48} />
                        <h3>功能正在开发中</h3>
                        <p>
                            {activeTab === 'text_to_3d' && '文本生成3D模型功能即将上线'}
                        </p>
                        <p>敬请期待！</p>
                    </div>
                </div>
            )}

            {/* 模态框 */}
            {renderTemplateDetailModal()}
            {renderCreateTemplateModal()}
        </div>
    );
};

export default FeatureContent;