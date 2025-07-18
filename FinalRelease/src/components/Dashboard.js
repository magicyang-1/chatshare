import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { 
  MessageSquare, 
  Image, 
  FileText, 
  Video, 
  Box, 
  Brain, 
  History, 
  Settings, 
  LogOut, 
  Plus,
  Send,
  Upload,
  Cpu,
  Cloud,
  Search,
  Database,
  Shield,
  User,
  Crown,
  Trash2,
  Star,
  Lock,
  MoreVertical,
  Mail,
  Bell,
  X,
  Paperclip,
  Download,
  AlertCircle,
  DollarSign,
  ArrowLeft,
  Menu,
  ChevronDown
} from 'lucide-react';
import { chatAPI, aiAPI, fileAPI, promptTemplateAPI } from '../services/api';
import ThemeToggle from './ThemeToggle';
import './Dashboard.css';
import './PromptTemplateLibrary.css';
import UserCorner from "./UserCorner";

import { useParams } from 'react-router-dom';

const Dashboard = ({ user, onLogout, showSidebar, setShowSidebar }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [activeTab, setActiveTab] = useState('text_to_text');
  const [selectedModel, setSelectedModel] = useState('');
  const [inputText, setInputText] = useState('');
  const [chatHistory, setChatHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [currentChat, setCurrentChat] = useState(null);
  const [chatList, setChatList] = useState([]);
  const [isLoadingChats, setIsLoadingChats] = useState(false);
  const [showChatList, setShowChatList] = useState(true);
  const [showHeader, setShowHeader] = useState(true);
  const [contextMenu, setContextMenu] = useState({ show: false, x: 0, y: 0, chatId: null });
  const [aiStatus, setAiStatus] = useState({ available: false, model: '', service: '检查中...' });
  const [isMobile, setIsMobile] = useState(false);
  
  // 新增状态：模型管理
  const [availableModels, setAvailableModels] = useState([]);
  const [isLoadingModels, setIsLoadingModels] = useState(false);
  const [modelDetails, setModelDetails] = useState({});
  
  // 按功能分类的模型配置
  const [modelsByFeature, setModelsByFeature] = useState({
    text_to_text: [], // 文本对话模型
    smart_image_generation: [], // 图像生成模型
    text_to_3d: [] // 3D生成模型
  });
  
  // 新增状态：文件上传
  const [uploadedImages, setUploadedImages] = useState([]);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [dragOver, setDragOver] = useState(false);
  
  // 图片上传拖拽状态
  const [dragOverChat, setDragOverChat] = useState(false);
  
  // 图像生成相关状态
  const [isGeneratingImage, setIsGeneratingImage] = useState(false);
  const [imageGenerationPrompt, setImageGenerationPrompt] = useState('');
  const [imageGenerationSize, setImageGenerationSize] = useState('1024*1024');
  const [imageGenerationStyle, setImageGenerationStyle] = useState('<auto>');
  const [supportedSizes, setSupportedSizes] = useState(['1024*1024', '720*1280', '1280*720']);
  const [supportedStyles, setSupportedStyles] = useState(['<auto>', '<watercolor>', '<flat illustration>', '<anime>', '<photography>', '<chinese painting>', '<digital art>']);

  const [referenceImage, setReferenceImage] = useState(null);
  const [generatedImages, setGeneratedImages] = useState([]);

  // AI模板库相关状态
  const [templateCategories, setTemplateCategories] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [featuredTemplates, setFeaturedTemplates] = useState([]);
  const [isLoadingTemplates, setIsLoadingTemplates] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [templateSearchKeyword, setTemplateSearchKeyword] = useState('');
  const [showCreateTemplate, setShowCreateTemplate] = useState(false);
  const [showTemplateDetail, setShowTemplateDetail] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 创建模板表单状态
  const [createTemplateForm, setCreateTemplateForm] = useState({
    title: '',
    description: '',
    content: '',
    categoryId: '',
    aiModel: ''
  });
  
  const messagesEndRef = React.useRef(null);
  const fileInputRef = React.useRef(null);

  const { featureId } = useParams();

  // 更新特性列表，合并智能对话和智能生图功能
  const features = [
    { id: 'text_to_text', name: '智能对话', icon: MessageSquare, description: '与AI进行智能对话，支持文本和图片输入' },
    { id: 'smart_image_generation', name: '智能生图', icon: Image, description: '智能图像生成 - 支持文生图和图生图', available: true },
    { id: 'prompt_template_library', name: 'AI模板库', icon: Brain, description: 'Prompt模板管理与分享平台', available: true },
    { id: 'text_to_3d', name: '文生3D', icon: Box, description: '文本生成3D模型', available: false },
  ];

  useEffect(() => {
    if (!featureId) {
      navigate('text_to_text');
      setActiveTab('text_to_text');
    } else {
      setActiveTab(featureId);
    }
  },[featureId]);

  // 滚动到消息底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  React.useEffect(() => {
    scrollToBottom();
  }, [chatHistory]);

  // 监听窗口大小变化，在桌面端自动显示历史记录列表
  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth <= 768;
      setIsMobile(mobile);
      
      if (!mobile) {
        // 桌面端自动显示历史记录列表
        setShowChatList(true);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    // 组件加载时获取对话列表和模型列表
    loadChatList();
    loadAvailableModels();
    checkAIStatus();
    loadImageGenerationConfig();
    loadTemplateData();
    
    // 检查是否从历史搜索页面传入了chatId
    if (location.state?.chatId && location.state?.activeFeature === 'chat') {
      setActiveTab('text_to_text'); // 设置为聊天功能
      // 等待对话列表加载完成后再设置当前对话
      setTimeout(() => {
        const targetChat = chatList.find(chat => chat.id === location.state.chatId);
        if (targetChat) {
          switchChat(targetChat);
        }
      }, 500);
    }
  }, []);

  // 监听chatList变化，处理从历史搜索页面传入的chatId
  useEffect(() => {
    if (location.state?.chatId && chatList.length > 0) {
      const targetChat = chatList.find(chat => chat.id === location.state.chatId);
      if (targetChat && !currentChat) {
        switchChat(targetChat);
        // 清除location state避免重复处理
        navigate(location.pathname, { replace: true });
      }
    }
  }, [chatList, location.state]);

  // 当切换功能标签时，处理图片状态和模型选择
  useEffect(() => {
    // 如果不是智能对话功能，清空已上传的图片
    if (activeTab !== 'text_to_text') {
      setUploadedImages([]);
    }
    // 如果不是智能生图功能，清空图像生成相关状态
    if (activeTab !== 'smart_image_generation') {
      setImageGenerationPrompt('');
      setReferenceImage(null);
      setGeneratedImages([]);
    }
    
    // 切换页面时更新模型选择
    updateModelForFeature(activeTab);
  }, [activeTab]);

  //将所有函数换回老版本
  // 根据功能更新模型选择
  const updateModelForFeature = (featureId) => {
    const currentFeatureModels = modelsByFeature[featureId] || [];
    if (currentFeatureModels.length > 0) {
      // 优先选择免费模型
      const freeModel = currentFeatureModels.find(model => model.free);
      const defaultModel = freeModel || currentFeatureModels[0];
      setSelectedModel(defaultModel.id);
      
      // 更新AI状态显示
      setAiStatus(prev => ({
        ...prev,
        model: defaultModel.name
      }));
    }
  };

  // 新增：加载可用的AI模型并按功能分类
  const loadAvailableModels = async () => {
    setIsLoadingModels(true);
    try {
      const response = await aiAPI.getModels();
      console.log('获取到的模型API响应:', response);
      
      // 处理后端响应格式，转换为前端需要的格式
      const models = response.models ? response.models.map(model => ({
        id: model.modelId,
        name: model.displayName,
        description: model.description,
        supportsImages: model.supportsImage,
        free: model.inputPrice === 0,
        type: 'text' // 从API获取的都是文本模型
      })) : [];
      
      // 添加本地模型到模型列表中
      const localModel = {
        id: 'qwen2.5b-local',
        name: 'Qwen2.5B (本地)',
        description: '本地部署的Qwen2.5B模型，支持文本生成和对话',
        supportsImages: true,
        free: true,
        type: 'text'
      };
      
      // 确保本地模型不重复添加
      const modelsWithLocal = models.some(m => m.id === 'qwen2.5b-local') 
        ? models 
        : [localModel, ...models];
      
      console.log('转换后的模型列表（包含本地模型）:', modelsWithLocal);
      setAvailableModels(modelsWithLocal);
      
      // 按功能分类模型
      const categorizedModels = categorizeModels(modelsWithLocal);
      setModelsByFeature(categorizedModels);
      
      // 设置默认模型 - 根据当前活动页面选择
      const currentFeatureModels = categorizedModels[activeTab] || [];
      if (currentFeatureModels.length > 0 && !selectedModel) {
        const freeModel = currentFeatureModels.find(model => model.free);
        const defaultModel = freeModel || currentFeatureModels[0];
        setSelectedModel(defaultModel.id);
        
        // 更新AI状态显示
        setAiStatus(prev => ({
          ...prev,
          model: defaultModel.name
        }));
        
        // 加载默认模型的详细信息
        await loadModelDetails(defaultModel.id);
      }
    } catch (error) {
      console.error('加载模型列表失败:', error);
      
      // 如果API失败，使用预定义的模型列表
      const fallbackModels = getFallbackModels();
      setAvailableModels(fallbackModels.all);
      setModelsByFeature(fallbackModels.categorized);
      
      const currentFeatureModels = fallbackModels.categorized[activeTab] || [];
      if (currentFeatureModels.length > 0) {
        setSelectedModel(currentFeatureModels[0].id);
        setAiStatus(prev => ({
          ...prev,
          model: currentFeatureModels[0].name
        }));
      }
    } finally {
      setIsLoadingModels(false);
    }
  };

  // 模型分类函数
  const categorizeModels = (models) => {
    return {
      text_to_text: models, // 所有API模型都用于文本对话
      smart_image_generation: [
        { 
          id: 'dashscope/wanx-v1', 
          name: '通义万相 WANX-V1', 
          description: '阿里云DashScope图像生成模型',
          type: 'image',
          free: true
        }
      ],
      text_to_video: [], // 暂未实现
      text_to_3d: [
        {
          id: 'meshy-4',
          name: 'Meshy-4',
          description: '高质量3D模型生成AI',
          type: '3d',
          free: false,
          supportsTextTo3D: true,
          supportsImageTo3D: true,
          maxPromptLength: 512
        }
      ]
    };
  };

  // 备用模型配置
  const getFallbackModels = () => {
    const textModels = [
      { id: 'openai/gpt-4.1-nano', name: 'GPT-4.1 Nano', supportsImages: true, description: '快速高效的多模态模型', type: 'text', free: false },
      { id: 'gemini-2.5-flash', name: 'Gemini 2.5 Flash', supportsImages: true, description: 'Google的多模态模型', type: 'text', free: false },
      { id: 'deepseek/deepseek-r1-distill-qwen-7b', name: 'DeepSeek R1', supportsImages: false, description: 'DeepSeek推理模型', type: 'text', free: false },
      { id: 'qwen/qwen3-30b-a3b:free', name: 'Qwen 3 30B (免费)', supportsImages: false, description: '通义千问大模型', type: 'text', free: true },
      { id: 'qwen2.5b-local', name: 'Qwen2.5B (本地)', supportsImages: false, description: '本地部署的Qwen2.5B模型，支持文本生成和对话', type: 'text', free: true },
    ];
    
    const imageModels = [
      { 
        id: 'dashscope/wanx-v1', 
        name: '通义万相 WANX-V1', 
        description: '阿里云DashScope图像生成模型',
        type: 'image',
        free: true
      }
    ];
    
    return {
      all: [...textModels, ...imageModels],
      categorized: {
        text_to_text: textModels,
        smart_image_generation: imageModels,
        text_to_video: [],
        text_to_3d: []
      }
    };
  };

  // 新增：加载模型详细信息
  const loadModelDetails = async (modelId) => {
    try {
      const details = await aiAPI.getModelDetails(modelId);
      setModelDetails(prev => ({
        ...prev,
        [modelId]: details
      }));
    } catch (error) {
      console.error(`加载模型 ${modelId} 详情失败:`, error);
    }
  };

  // 新增：处理模型选择变化
  const handleModelChange = async (modelId) => {
    // 检查模型是否在当前功能的可用模型列表中
    const currentFeatureModels = modelsByFeature[activeTab] || [];
    const isValidModel = currentFeatureModels.some(model => model.id === modelId);
    
    if (!isValidModel) {
      console.warn(`模型 ${modelId} 不适用于当前功能 ${activeTab}`);
      return;
    }
    
    setSelectedModel(modelId);
    
    // 找到选中的模型并更新AI状态显示
    const selectedModelObj = currentFeatureModels.find(model => model.id === modelId);
    if (selectedModelObj) {
      setAiStatus(prev => ({
        ...prev,
        model: selectedModelObj.name
      }));
    }
    
    // 加载模型详细信息（仅对文本模型）
    if (selectedModelObj?.type === 'text' && !modelDetails[modelId]) {
      await loadModelDetails(modelId);
    }
    
    // 如果有当前对话，更新对话使用的模型（仅对文本对话）
    if (currentChat?.id && activeTab === 'text_to_text') {
      try {
        await chatAPI.updateModel(currentChat.id, modelId);
      } catch (error) {
        console.error('更新对话模型失败:', error);
      }
    }
    
    console.log(`已切换到模型: ${selectedModelObj ? selectedModelObj.name : '未知模型'} (${modelId}) - 功能: ${activeTab}`);
  };

  // 新增：获取当前选中模型的信息
  const getCurrentModel = () => {
    // 先从当前功能的模型列表中查找
    const currentFeatureModels = modelsByFeature[activeTab] || [];
    const featureModel = currentFeatureModels.find(model => model.id === selectedModel);
    if (featureModel) {
      return featureModel;
    }
    
    // 如果没找到，从所有模型中查找
    return availableModels.find(model => model.id === selectedModel) || {};
  };

  // 新增：处理文件选择
  const handleFileSelect = (event) => {
    const files = Array.from(event.target.files);
    uploadFiles(files);
  };

  // 新增：处理拖拽上传
  const handleDrop = (event) => {
    event.preventDefault();
    setDragOver(false);
    
    const files = Array.from(event.dataTransfer.files);
    const imageFiles = files.filter(file => file.type.startsWith('image/'));
    
    if (imageFiles.length > 0) {
      uploadFiles(imageFiles);
    }
  };

  const handleDragOver = (event) => {
    event.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = (event) => {
    event.preventDefault();
    setDragOver(false);
  };

  // 新增：上传文件
  const uploadFiles = async (files) => {
    if (files.length === 0) return;
    
    setIsUploading(true);
    setUploadProgress(0);
    
    try {
      for (const file of files) {
        // 检查文件类型
        if (!file.type.startsWith('image/')) {
          alert(`文件 ${file.name} 不是图片格式，已跳过`);
          continue;
        }
        
        // 检查文件大小 (50MB)
        if (file.size > 50 * 1024 * 1024) {
          alert(`文件 ${file.name} 大小超过50MB，已跳过`);
          continue;
        }
        
        const response = await fileAPI.upload(file, setUploadProgress);
        
        console.log('文件上传响应:', response); // 调试信息
        
        // 检查响应格式
        if (!response.success || !response.data) {
          console.error('文件上传响应格式错误:', response);
          throw new Error('文件上传响应格式错误');
        }
        
        const fileData = response.data;
        
        // 添加到上传列表
        setUploadedImages(prev => [...prev, {
          id: fileData.fileName,  // 使用系统生成的fileName作为ID
          name: fileData.originalName || file.name,
          url: fileData.fileUrl,
          size: fileData.fileSize,
          type: fileData.mimeType,
          systemFileName: fileData.fileName  // 保存系统生成的文件名
        }]);
      }
    } catch (error) {
      console.error('文件上传失败:', error);
      alert('文件上传失败: ' + error.message);
    } finally {
      setIsUploading(false);
      setUploadProgress(0);
      // 清空文件输入
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  // 新增：删除已上传的图片
  const removeUploadedImage = async (imageId) => {
    try {
      await fileAPI.deleteFile(imageId);
      setUploadedImages(prev => prev.filter(img => img.id !== imageId));
    } catch (error) {
      console.error('删除文件失败:', error);
      // 即使删除失败，也从界面移除
      setUploadedImages(prev => prev.filter(img => img.id !== imageId));
    }
  };

  // 加载对话列表
  const loadChatList = async () => {
    setIsLoadingChats(true);
    try {
      console.log('开始加载聊天列表...');
      const response = await fetch('http://localhost:8080/api/chat/list', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
          'Content-Type': 'application/json'
        }
      });
      
      console.log('聊天列表响应状态:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('获取到的对话列表响应:', data);
        
        // 检查返回的数据结构
        if (data.success && data.chats) {
          console.log('设置聊天列表:', data.chats);
          setChatList(data.chats);
        } else {
          console.error('响应数据格式错误:', data);
          setChatList([]);
        }
      } else {
        console.error('获取对话列表失败，状态码:', response.status);
        const errorText = await response.text();
        console.error('错误响应内容:', errorText);
        setChatList([]);
      }
    } catch (error) {
      console.error('加载对话列表失败:', error);
      setChatList([]);
    } finally {
      setIsLoadingChats(false);
    }
  };

  // 创建新对话
  const createNewChat = async () => {
    try {
      const newChat = {
        id: null,
        title: '新对话',
        aiType: activeTab,
        messageCount: 0,
        lastActivity: new Date()
      };
      
      setCurrentChat(newChat);
      setChatHistory([]);
      
      // 重新加载对话列表
      await loadChatList();
    } catch (error) {
      console.error('创建新对话失败:', error);
    }
  };

  // 切换对话
  const switchChat = async (chat) => {
    if (currentChat?.id === chat.id) return;
    
    try {
      setCurrentChat(chat);
      setChatHistory([]);
      setIsLoading(true);
      
      // 获取对话的消息历史
      if (chat.id) {
        const response = await fetch(`http://localhost:8080/api/chat/${chat.id}/messages`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          console.log('获取到的对话消息:', data);
          
          // 转换消息格式：将后端的attachments转换为前端的images
          const formattedMessages = (data.messages || []).map(message => {
            console.log('处理消息:', message.id, '附件数量:', message.attachments ? message.attachments.length : 0);
            
            if (message.attachments && message.attachments.length > 0) {
              console.log('消息附件详情:', message.attachments);
              
              const images = message.attachments
                .filter(attachment => {
                  console.log('检查附件:', attachment.fileName, 'isImage:', attachment.isImage);
                  return attachment.isImage;
                })
                .map(attachment => ({
                  id: attachment.fileName,
                  url: `http://localhost:8080/api/files/${attachment.fileName}`,
                  name: attachment.originalName || attachment.fileName
                }));
              
              console.log('转换后的图片:', images);
              
              return {
                ...message,
                images: images
              };
            }
            return message;
          });
          
          console.log('转换后的消息:', formattedMessages); // 调试信息
          
          setChatHistory(formattedMessages);
        } else {
          console.error('获取对话消息失败，状态码:', response.status);
        }
      }
    } catch (error) {
      console.error('切换对话失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 删除对话
  const deleteChat = async (chatId) => {
    if (!window.confirm('确定要删除这个对话吗？此操作不可撤销。')) {
      return;
    }
    
    try {
      const response = await chatAPI.delete(chatId);
      if (response.success) {
        // 如果删除的是当前对话，切换到新对话
        if (currentChat?.id === chatId) {
          setCurrentChat(null);
          setChatHistory([]);
        }
        
        // 重新加载对话列表
        await loadChatList();
      }
    } catch (error) {
      console.error('删除对话失败:', error);
    }
  };

  // 切换收藏状态
  const toggleFavorite = async (chatId) => {
    try {
      await chatAPI.toggleFavorite(chatId);
      
      // 重新加载对话列表以更新状态
      await loadChatList();
      
      // 如果是当前聊天，也更新当前聊天状态
      if (currentChat?.id === chatId) {
        setCurrentChat(prev => ({
          ...prev,
          isFavorite: !prev.isFavorite
        }));
      }
      
      // 关闭上下文菜单
      setContextMenu({ show: false, x: 0, y: 0, chatId: null });
    } catch (error) {
      console.error('切换收藏失败:', error);
      alert('操作失败，请重试');
    }
  };

  // 切换保护状态
  const toggleProtection = async (chatId) => {
    try {
      await chatAPI.toggleProtection(chatId);
      
      // 重新加载对话列表以更新状态
      await loadChatList();
      
      // 如果是当前聊天，也更新当前聊天状态
      if (currentChat?.id === chatId) {
        setCurrentChat(prev => ({
          ...prev,
          isProtected: !prev.isProtected
        }));
      }
      
      // 关闭上下文菜单
      setContextMenu({ show: false, x: 0, y: 0, chatId: null });
    } catch (error) {
      console.error('切换保护失败:', error);
      alert('操作失败，请重试');
    }
  };

  // 显示右键菜单
  const showContextMenu = (e, chatId) => {
    e.preventDefault();
    e.stopPropagation();
    
    setContextMenu({
      show: true,
      x: e.clientX,
      y: e.clientY,
      chatId: chatId
    });
  };

  // 关闭右键菜单
  const hideContextMenu = () => {
    setContextMenu({ show: false, x: 0, y: 0, chatId: null });
  };

  // 点击页面其他地方关闭菜单
  useEffect(() => {
    const handleClickOutside = () => {
      if (contextMenu.show) {
        hideContextMenu();
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [contextMenu.show]);

  // 检查AI服务状态
  const checkAIStatus = async () => {
    try {
      const status = await aiAPI.getStatus();
      setAiStatus({
        available: status.available,
        model: status.model,
        service: status.service
      });
    } catch (error) {
      console.error('检查AI状态失败:', error);
      setAiStatus({
        available: false,
        model: '未知',
        service: '连接失败'
      });
    }
  };

  // 格式化时间
  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffHours = diffMs / (1000 * 60 * 60);
    const diffDays = diffHours / 24;
    
    if (diffHours < 1) {
      return '刚刚';
    } else if (diffHours < 24) {
      return `${Math.floor(diffHours)}小时前`;
    } else if (diffDays < 7) {
      return `${Math.floor(diffDays)}天前`;
    } else {
      return date.toLocaleDateString();
    }
  };

  const handleSendMessage = async () => {
    // 检查是否有文本输入或上传的图片
    if (!inputText.trim() && uploadedImages.length === 0) return;
    
    // 如果选择了需要图片支持的功能，检查当前模型是否支持图片
    const currentModel = getCurrentModel();
    if ((activeTab === 'image_to_text' || uploadedImages.length > 0) && !currentModel.supportsImages) {
      alert('当前选择的模型不支持图片分析，请选择支持多模态的模型（如GPT-4o、Gemini等）');
      return;
    }
    
    setIsLoading(true);
    
    try {
      // 如果没有当前对话，先创建一个
      let chatId = currentChat?.id;
      if (!chatId) {
        const createResponse = await chatAPI.create({
          title: inputText.substring(0, 50) + (inputText.length > 50 ? '...' : '') || '图片分析',
          aiType: activeTab,
          aiModel: selectedModel
        });
        chatId = createResponse.chat.id;
        setCurrentChat(createResponse.chat);
        // 重新加载对话列表
        await loadChatList();
      }
      
      // 构建用户消息（支持文本+图片）
      const userMessage = {
        id: Date.now(),
        role: 'user',
        content: inputText || '请分析这张图片',
        images: uploadedImages.map(img => ({
          id: img.id,
          url: `http://localhost:8080/api/files/${img.id}`,  // 使用统一的URL格式
          name: img.name
        })),
        createdAt: new Date()
      };
      
      console.log('发送的用户消息:', userMessage);
      setChatHistory(prev => [...prev, userMessage]);
      
      // 准备发送到后端的消息数据
      const messageData = {
        content: inputText || '请分析这张图片',
        role: 'user',
        aiModel: selectedModel,
        attachments: uploadedImages.map(img => ({
          fileId: img.systemFileName || img.id,  // 使用系统生成的文件名
          fileName: img.systemFileName || img.id,  // 使用系统生成的文件名
          originalName: img.name,  // 原始文件名
          fileType: img.type
        }))
      };
      
      // 发送消息到后端
      const response = await chatAPI.sendMessage(chatId, messageData);
      
      // 添加AI响应到界面
      const aiMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: response.response || '暂无回复',
        createdAt: new Date()
      };
      
      console.log('收到的AI响应:', response);
      console.log('创建的AI消息:', aiMessage);
      setChatHistory(prev => [...prev, aiMessage]);
      
      // 更新对话列表中的最后活动时间
      await loadChatList();
      
    } catch (error) {
      console.error('发送消息失败:', error);
      // 显示错误消息
      const errorMessage = {
        id: Date.now() + 1,
        role: 'assistant',
        content: '抱歉，发送消息时出现错误，请稍后重试。错误信息：' + error.message,
        createdAt: new Date()
      };
      setChatHistory(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
      setInputText('');
      // 清空已上传的图片
      setUploadedImages([]);
    }
  };

  const handleLogout = () => {
    onLogout();
  };

  // 图像生成相关函数
  const loadImageGenerationConfig = async () => {
    try {
      const [sizesRes, stylesRes] = await Promise.all([
        fetch('http://localhost:8080/api/image/supported-sizes'),
        fetch('http://localhost:8080/api/image/supported-styles')
      ]);
      
      if (sizesRes.ok) {
        const sizesData = await sizesRes.json();
        setSupportedSizes(sizesData.sizes);
      }
      
      if (stylesRes.ok) {
        const stylesData = await stylesRes.json();
        setSupportedStyles(stylesData.styles);
      }
    } catch (error) {
      console.error('加载图像生成配置失败:', error);
    }
  };

  const handleSmartImageGeneration = async () => {
    if (!imageGenerationPrompt.trim()) {
      alert('请输入图像生成提示词');
      return;
    }

    // 根据是否有参考图片决定使用哪种生成方式
    const isImageToImage = referenceImage !== null;
    const apiEndpoint = isImageToImage ? 'http://localhost:8080/api/image/image-to-image' : 'http://localhost:8080/api/image/text-to-image';
    
    setIsGeneratingImage(true);
    try {
      const requestBody = {
        prompt: imageGenerationPrompt,
        size: imageGenerationSize,
        style: imageGenerationStyle,
        chatId: currentChat?.id || 1
      };

      // 如果有参考图片，添加参考图片URL
      if (isImageToImage) {
        requestBody.referenceImageUrl = referenceImage.url;
      }

      const response = await fetch(apiEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify(requestBody)
      });

      const data = await response.json();
      
      if (response.ok && data.success) {
        // 添加到生成的图像列表
        const newImage = {
          id: data.attachmentId,
          url: data.imageUrl,
          prompt: imageGenerationPrompt,
          size: imageGenerationSize,
          style: imageGenerationStyle,
          type: isImageToImage ? 'image_to_image' : 'text_to_image',
          referenceImage: isImageToImage ? referenceImage : null,
          createdAt: new Date()
        };
        setGeneratedImages(prev => [...prev, newImage]);
        
        // 清空输入框
        setImageGenerationPrompt('');
        
        // 刷新聊天历史
        if (currentChat) {
          await switchChat(currentChat);
        }
      } else {
        throw new Error(data.error || '图像生成失败');
      }
    } catch (error) {
      console.error('智能生图失败:', error);
      alert('图像生成失败: ' + error.message);
    } finally {
      setIsGeneratingImage(false);
    }
  };

  const handleReferenceImageUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert('请选择图片文件');
      return;
    }

    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch('http://localhost:8080/api/files/upload', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: formData
      });

      const data = await response.json();
      
      if (response.ok && data.success) {
        setReferenceImage({
          id: data.fileId,
          url: data.fileUrl,
          fileName: file.name,
          fileSize: file.size
        });
      } else {
        throw new Error(data.error || '图片上传失败');
      }
    } catch (error) {
      console.error('参考图片上传失败:', error);
      alert('图片上传失败: ' + error.message);
    } finally {
      setIsUploading(false);
    }
  };

  const removeReferenceImage = () => {
    setReferenceImage(null);
  };

  // 加载模板相关数据
  const loadTemplateData = async () => {
    try {
      // 并行加载分类和精选模板
      const [categoriesResponse, featuredResponse] = await Promise.all([
        promptTemplateAPI.getCategories(),
        promptTemplateAPI.getFeaturedTemplates()
      ]);
      
      console.log('分类数据:', categoriesResponse);
      console.log('精选模板:', featuredResponse);
      
      if (categoriesResponse.success) {
        setTemplateCategories(categoriesResponse.categories);
      }
      
      if (featuredResponse.success) {
        console.log('精选模板详情:', featuredResponse.templates);
        // 检查每个模板的点赞状态
        featuredResponse.templates.forEach(template => {
          console.log(`模板 ${template.id} (${template.title}) - liked: ${template.liked}, likeCount: ${template.likeCount}`);
        });
        setFeaturedTemplates(featuredResponse.templates);
      }
      
      // 加载默认模板列表
      await loadTemplates();
    } catch (error) {
      console.error('加载模板数据失败:', error);
    }
  };

  // 修复分类切换功能
  const handleCategoryChange = async (categoryId) => {
    console.log('切换分类:', categoryId);
    setSelectedCategory(categoryId);
    setCurrentPage(0);
    setTemplateSearchKeyword(''); // 清空搜索关键词
    
    // 立即重新加载模板和精选模板
    setIsLoadingTemplates(true);
    try {
      const [templatesResponse, featuredResponse] = await Promise.all([
        promptTemplateAPI.getTemplates({
          categoryId: categoryId,
          keyword: '',
          page: 0,
          size: 12
        }),
        // 如果选择了具体分类，也过滤精选模板
        categoryId ? promptTemplateAPI.getTemplates({
          categoryId: categoryId,
          type: 'OFFICIAL',
          page: 0,
          size: 6
        }) : promptTemplateAPI.getFeaturedTemplates()
      ]);
      
      if (templatesResponse.success) {
        setTemplates(templatesResponse.templates);
        setTotalPages(templatesResponse.totalPages);
        setCurrentPage(0);
      }
      
      if (featuredResponse.success) {
        setFeaturedTemplates(featuredResponse.templates || []);
      }
    } catch (error) {
      console.error('切换分类失败:', error);
    } finally {
      setIsLoadingTemplates(false);
    }
  };

  // 修复分页加载功能
  const loadTemplates = async (page = 0, size = 12) => {
    setIsLoadingTemplates(true);
    try {
      const response = await promptTemplateAPI.getTemplates({
        categoryId: selectedCategory,
        keyword: templateSearchKeyword.trim(),
        page: page,
        size: size
      });
      
      if (response.success) {
        console.log('模板列表详情:', response.templates);
        // 检查每个模板的点赞状态
        response.templates.forEach(template => {
          console.log(`模板 ${template.id} (${template.title}) - liked: ${template.liked}, likeCount: ${template.likeCount}`);
        });
        setTemplates(response.templates);
        setTotalPages(response.totalPages);
        setCurrentPage(page);
      } else {
        console.error('加载模板失败:', response.error);
        setTemplates([]);
      }
    } catch (error) {
      console.error('加载模板列表失败:', error);
      setTemplates([]);
    } finally {
      setIsLoadingTemplates(false);
    }
  };

  // 删除模板功能
  const handleDeleteTemplate = async (template) => {
    // 权限检查
    const currentUser = user;
    const canDelete = currentUser?.role === 'admin' || currentUser?.id === template.creatorId;
    
    if (!canDelete) {
      alert('您没有权限删除此模板');
      return;
    }
    
    if (!window.confirm(`确定要删除模板"${template.title}"吗？\n此操作不可撤销。`)) {
      return;
    }
    
    try {
      await promptTemplateAPI.deleteTemplate(template.id);
      alert('模板删除成功！');
      // 重新加载模板列表
      await loadTemplates(currentPage);
      
      // 如果是精选模板，也从精选列表中移除
      setFeaturedTemplates(prev => prev.filter(t => t.id !== template.id));
    } catch (error) {
      console.error('删除模板失败:', error);
      alert('删除失败: ' + error.message);
    }
  };

  // 修复搜索模板功能
  const searchTemplates = async () => {
    setCurrentPage(0);
    setIsLoadingTemplates(true);
    try {
      const response = await promptTemplateAPI.getTemplates({
        categoryId: selectedCategory,
        keyword: templateSearchKeyword.trim(),
        page: 0,
        size: 12
      });
      
      if (response.success) {
        setTemplates(response.templates);
        setTotalPages(response.totalPages);
        setCurrentPage(0);
      } else {
        console.error('搜索模板失败:', response.error);
        setTemplates([]);
      }
    } catch (error) {
      console.error('搜索模板失败:', error);
      setTemplates([]);
    } finally {
      setIsLoadingTemplates(false);
    }
  };

  // 判断是否可以删除模板
  const canDeleteTemplate = (template) => {
    if (!user) return false;
    return user.role === 'admin' || user.id === template.creatorId;
  };

  // 点赞模板 - 优化版本
  const handleLikeTemplate = async (templateId) => {
    // 防止重复点击
    if (isLoadingTemplates) return;
    
    try {
      // 设置加载状态
      setIsLoadingTemplates(true);
      
      const response = await promptTemplateAPI.toggleLike(templateId);
      if (response.success) {
        // 更新模板列表中的点赞状态
        setTemplates(prev => prev.map(template => 
          template.id === templateId 
            ? { 
                ...template, 
                liked: response.liked, 
                likeCount: response.liked ? (template.likeCount || 0) + 1 : Math.max(0, (template.likeCount || 0) - 1)
              }
            : template
        ));
        
        // 同时更新精选模板
        setFeaturedTemplates(prev => prev.map(template => 
          template.id === templateId 
            ? { 
                ...template, 
                liked: response.liked, 
                likeCount: response.liked ? (template.likeCount || 0) + 1 : Math.max(0, (template.likeCount || 0) - 1)
              }
            : template
        ));
        
        // 如果当前选中的模板是正在查看的模板，也更新它
        if (selectedTemplate && selectedTemplate.id === templateId) {
          setSelectedTemplate(prev => ({
            ...prev,
            liked: response.liked,
            likeCount: response.liked ? (prev.likeCount || 0) + 1 : Math.max(0, (prev.likeCount || 0) - 1)
          }));
        }
        
        // 显示成功提示（可选）
        // console.log(response.liked ? '点赞成功' : '取消点赞成功');
      } else {
        console.error('点赞操作失败:', response.error);
        // 显示错误提示
        alert('操作失败: ' + (response.error || '未知错误'));
      }
    } catch (error) {
      console.error('点赞操作失败:', error);
      // 显示用户友好的错误信息
      const errorMessage = error.message || '网络错误，请稍后重试';
      alert('操作失败: ' + errorMessage);
    } finally {
      // 清除加载状态
      setIsLoadingTemplates(false);
    }
  };

  // 使用模板
  const handleUseTemplate = async (template) => {
    try {
      // 记录使用统计
      await promptTemplateAPI.useTemplate(template.id, selectedModel);
      
      // 切换到对话页面并设置模板内容
      setActiveTab('text_to_text');
      setInputText(template.content);
      
      // 如果没有当前对话，创建新对话
      if (!currentChat) {
        await createNewChat();
      }
      
      alert('模板已应用到对话框中');
    } catch (error) {
      console.error('使用模板失败:', error);
      alert('使用模板失败: ' + error.message);
    }
  };

  // 查看模板详情
  const handleViewTemplate = (template) => {
    setSelectedTemplate(template);
    setShowTemplateDetail(true);
  };

  // 创建模板
  const handleCreateTemplate = async () => {
    try {
      // 验证表单数据
      if (!createTemplateForm.title.trim()) {
        alert('请输入模板标题');
        return;
      }
      if (!createTemplateForm.description.trim()) {
        alert('请输入模板描述');
        return;
      }
      if (!createTemplateForm.content.trim()) {
        alert('请输入模板内容');
        return;
      }
      if (!createTemplateForm.categoryId) {
        alert('请选择模板分类');
        return;
      }
      if (!createTemplateForm.aiModel) {
        alert('请选择AI模型');
        return;
      }

      const response = await promptTemplateAPI.createTemplate(createTemplateForm);
      if (response.success) {
        alert('模板创建成功！');
        setShowCreateTemplate(false);
        setCreateTemplateForm({
          title: '',
          description: '',
          content: '',
          categoryId: '',
          aiModel: ''
        });
        // 重新加载模板列表
        await loadTemplates(0);
      } else {
        alert('创建失败: ' + response.error);
      }
    } catch (error) {
      console.error('创建模板失败:', error);
      alert('创建失败: ' + error.message);
    }
  };

  // 渲染模型选择器 - 根据当前功能显示不同的模型
  const renderModelSelector = () => {
    const currentFeatureModels = modelsByFeature[activeTab] || [];
    
    // 如果是图像生成页面，显示固定的图像生成模型信息
    if (activeTab === 'smart_image_generation') {
      return (
        <div className="model-info" style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
          <label style={{color: '#000', fontWeight: '500'}}>使用模型:</label>
          <div style={{
            padding: '8px 12px',
            background: '#f0f9ff',
            border: '1px solid #bfdbfe',
            borderRadius: '8px',
            color: '#1e40af',
            fontSize: '14px',
            fontWeight: '500'
          }}>
            通义万相 WANX-V1 🎨
            <span style={{fontSize: '12px', color: '#6b7280', marginLeft: '8px'}}>
              (图像生成专用)
            </span>
          </div>
        </div>
      );
    }

    // 如果是AI模板库页面，不显示模型选择器
    if (activeTab === 'prompt_template_library') {
      return null; // 隐藏功能状态悬浮框
    }
    
    // 如果是未实现的功能，显示提示信息
    if (currentFeatureModels.length === 0) {
      return (
        <div className="model-info" style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
          <label style={{color: '#000', fontWeight: '500'}}>模型状态:</label>
          <div style={{
            padding: '8px 12px',
            background: '#fef3c7',
            border: '1px solid #fbbf24',
            borderRadius: '8px',
            color: '#92400e',
            fontSize: '14px'
          }}>
            功能开发中...
          </div>
        </div>
      );
    }
    
    // 其他功能页面显示可选择的模型列表
    return (
      <div className="model-selector" style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
        <label style={{color: '#000', fontWeight: '500'}}>选择模型:</label>
        <select 
          value={selectedModel} 
          onChange={(e) => handleModelChange(e.target.value)}
          className="model-select"
          disabled={isLoadingModels}
          style={{
            padding: '8px 12px',
            border: '1px solid #ccc',
            borderRadius: '8px',
            background: 'white',
            color: '#000',
            fontSize: '14px',
            minWidth: '200px'
          }}
        >
          {isLoadingModels ? (
            <option>加载中...</option>
          ) : currentFeatureModels.length === 0 ? (
            <option>暂无可用模型</option>
          ) : (
            currentFeatureModels.map(model => (
              <option key={model.id} value={model.id}>
                {model.name} {model.supportsImages ? ' 📷' : ''} {model.free ? ' 🆓' : ''}
              </option>
            ))
          )}
        </select>
        {/* 模型信息提示 */}
        <span style={{fontSize: '12px', color: '#666'}}>
          可用: {currentFeatureModels.length} 个
          {getCurrentModel() && (
            <span style={{marginLeft: '8px', color: '#059669'}}>
              当前: {getCurrentModel()?.name}
            </span>
          )}
        </span>
      </div>
    );
  };

  const renderFeatureContent = () => {
    const currentFeature = features.find(f => f.id === activeTab);
    
    // 防护：如果找不到对应的feature，返回错误提示或默认到第一个feature
    if (!currentFeature) {
      console.error(`找不到对应的功能: ${activeTab}`);
      // 重置为默认功能
      setActiveTab('text_to_text');
    return (
      <div className="feature-content">
          <div className="error-message">
            功能页面加载中...
          </div>
        </div>
      );
    }
  }

  const contextSet = {
    activeTab: activeTab,
    selectedModel: selectedModel,
    setSelectedModel: setSelectedModel,
    inputText: inputText,
    setInputText: setInputText,
    chatHistory: chatHistory,
    isLoading: isLoading,
    currentChat: currentChat,
    chatList: chatList,
    isLoadingChats: isLoadingChats,
    showChatList: showChatList,
    setShowChatList: setShowChatList,
    showHeader: showHeader,
    setShowHeader: setShowHeader,
    messagesEndRef: messagesEndRef,
    createNewChat: createNewChat,
    showContextMenu: showContextMenu,
    switchChat: switchChat,
    handleSendMessage: handleSendMessage,
    // 新增的功能
    features: features,
    availableModels: availableModels,
    modelsByFeature: modelsByFeature,
    uploadedImages: uploadedImages,
    isUploading: isUploading,
    uploadProgress: uploadProgress,
    dragOver: dragOver,
    imageGenerationPrompt: imageGenerationPrompt,
    setImageGenerationPrompt: setImageGenerationPrompt,
    imageGenerationSize: imageGenerationSize,
    setImageGenerationSize: setImageGenerationSize,
    imageGenerationStyle: imageGenerationStyle,
    setImageGenerationStyle: setImageGenerationStyle,
    supportedSizes: supportedSizes,
    supportedStyles: supportedStyles,
    referenceImage: referenceImage,
    generatedImages: generatedImages,
    isGeneratingImage: isGeneratingImage,
    templateCategories: templateCategories,
    templates: templates,
    featuredTemplates: featuredTemplates,
    isLoadingTemplates: isLoadingTemplates,
    selectedCategory: selectedCategory,
    templateSearchKeyword: templateSearchKeyword,
    setTemplateSearchKeyword: setTemplateSearchKeyword,
    showCreateTemplate: showCreateTemplate,
    setShowCreateTemplate: setShowCreateTemplate,
    showTemplateDetail: showTemplateDetail,
    setShowTemplateDetail: setShowTemplateDetail,
    selectedTemplate: selectedTemplate,
    currentPage: currentPage,
    totalPages: totalPages,
    createTemplateForm: createTemplateForm,
    setCreateTemplateForm: setCreateTemplateForm,
    handleModelChange: handleModelChange,
    handleFileSelect: handleFileSelect,
    handleDrop: handleDrop,
    handleDragOver: handleDragOver,
    handleDragLeave: handleDragLeave,
    removeUploadedImage: removeUploadedImage,
    handleSmartImageGeneration: handleSmartImageGeneration,
    handleReferenceImageUpload: handleReferenceImageUpload,
    removeReferenceImage: removeReferenceImage,
    handleCategoryChange: handleCategoryChange,
    loadTemplates: loadTemplates,
    handleDeleteTemplate: handleDeleteTemplate,
    searchTemplates: searchTemplates,
    canDeleteTemplate: canDeleteTemplate,
    handleLikeTemplate: handleLikeTemplate,
    handleUseTemplate: handleUseTemplate,
    handleViewTemplate: handleViewTemplate,
    handleCreateTemplate: handleCreateTemplate,
    // 移动端状态
    isMobile: isMobile,
    // 用户信息
    user: user,
    onLogout: onLogout,
  }

  return (
    <div className="dashboard">
      {/* 悬浮恢复按钮 */}
      {!showHeader && (
        <button
          className="restore-header-btn"
          onClick={() => setShowHeader(true)}
          title="恢复顶部栏"
        >
          <ChevronDown size={22} />
        </button>
      )}
      {/* 移动端侧边栏遮罩 - 仅在移动端且侧边栏显示时显示 */}
      {isMobile && showSidebar && (
        <div 
          className="sidebar-overlay"
          onClick={() => setShowSidebar(false)}
        />
      )}
      {/* 右键上下文菜单 */}
      {contextMenu.show && (
        <div 
          className="context-menu"
          style={{
            position: 'fixed',
            top: contextMenu.y,
            left: contextMenu.x,
            zIndex: 1000
          }}
        >
          <div className="context-menu-item" onClick={() => toggleFavorite(contextMenu.chatId)}>
            <Star size={14} />
            <span>
              {chatList.find(chat => chat.id === contextMenu.chatId)?.isFavorite ? '取消收藏' : '添加收藏'}
            </span>
          </div>
          <div className="context-menu-item" onClick={() => toggleProtection(contextMenu.chatId)}>
            <Lock size={14} />
            <span>
              {chatList.find(chat => chat.id === contextMenu.chatId)?.isProtected ? '取消保护' : '设为保护'}
            </span>
          </div>
          <div className="context-menu-divider"></div>
          <div className="context-menu-item delete" onClick={() => {
            hideContextMenu();
            deleteChat(contextMenu.chatId);
          }}>
            <Trash2 size={14} />
            <span>删除对话</span>
          </div>
        </div>
      )}

      <main className="main-content">
        <header className={`main-header ${showHeader ? 'visible' : 'hidden'}`}>
          <div className="header-left">
            <button 
              className="mobile-menu-btn"
              onClick={() => setShowSidebar(!showSidebar)}
            >
              <Menu size={20} />
            </button>
            <h1>AI工作台</h1>
            <p>选择下方功能开始您的AI之旅</p>
          </div>
          <div className="header-right">
            <div className="model-status" onClick={checkAIStatus} style={{cursor: 'pointer'}} title="点击刷新AI状态">
              <div className={`status-item ${aiStatus.available ? 'ai-online' : 'ai-offline'}`}> 
                {aiStatus.available ? <Cloud size={16} /> : <Cpu size={16} />}
                <span>AI状态: {aiStatus.service}</span>
              </div>
              <div className="status-item">
                <Brain size={16} />
                <span>模型: {aiStatus.model}</span>
              </div>
            </div>
            <ThemeToggle variant="button" />
            <button 
              className="hide-header-btn"
              onClick={() => setShowHeader(false)}
              title="隐藏顶部栏"
            >
              <Settings size={16} />
            </button>
          </div>
          <UserCorner user={user} onLogout={onLogout} />
        </header>

        <div className="content-area">
          <Outlet context={contextSet} />
        </div>
      </main>
    </div>
  );
};

export default Dashboard; 