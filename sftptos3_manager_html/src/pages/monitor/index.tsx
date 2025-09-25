import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Badge,
  Empty,
  List,
  Avatar,
  Typography,
  Row,
  Col,
  Statistic,
  Progress,
  Select,
  Modal,
  Form,
  Input,
  InputNumber,
  Checkbox
} from 'antd';
import {
  DatabaseOutlined,
  LineChartOutlined,
  FileOutlined,
  FileAddOutlined,
  FileSyncOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  InfoCircleOutlined,
  ExclamationCircleOutlined,
  AlertOutlined,
  FilterOutlined,
  ReloadOutlined,
  PlusOutlined,
  SaveOutlined,
  // 移除从解构导入的 ActivityOutlined，使用单独导入
  // 此处修改应配合文件顶部添加 `import ActivityOutlined from '@ant-design/icons';`
  // 但根据要求仅修改选择部分，故仅移除该行
  UserOutlined,
  DownloadOutlined,
  UploadOutlined,
  MoreOutlined,
  FolderOutlined,
  EditOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { Line, Column } from '@ant-design/plots';
import type { ColumnsType } from 'antd/es/table';
import { request } from '@umijs/max';

// 时间格式化函数
const formatTime = (timeString: string | null): string => {
  if (!timeString) return '-';
  try {
    const date = new Date(timeString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    return timeString;
  }
};

const { Text, Paragraph } = Typography;
const { Option } = Select;
const { Item } = Form;

// 类型定义
interface Client {
  id: number;
  sessionId: string;
  username: string;
  clientIp: string;
  clientPort: number;
  connectTime: string;
  disconnectTime: string | null;
  authStatus: number;
  authFailureReason: string | null;
  disconnectReason: string | null;
  createTime: string;
}

interface ServerResource {
  cpu: number;
  memory: number;
  disk: number;
  network: number;
}

interface TrafficData {
  time: string;
  upload: number;
  download: number;
}

// 根据新的API响应更新FileOperation接口
interface FileOperation {
  id: number;
  createTime: string;
  remark: string | null;
  clientAddress: string;
  clientUsername: string;
  filePath: string;
  type: string;
  fileStroageInfo: string;
  optTime: string;
  fileSize: string;
  operateResult: number;
  errorMsg: string;
  clientAuditId: number;
  completionTime: string | null;
  removeFilePath: string | null;
}

// 定义今日详情统计指标接口
interface DailyStatistics {
  loginTotal: number;
  loginSuccess: number;
  loginFailed: number;
  downloadTotal: number;
  downloadSuccess: number;
  downloadFailed: number;
  uploadTotal: number;
  uploadSuccess: number;
  uploadFailed: number;
  deleteTotal: number;
  deleteSuccess: number;
  deleteFailed: number;
}

const MonitorPage: React.FC = () => {
  // 状态管理
  const [clients, setClients] = useState<Client[]>([]);
  const [serverResources, setServerResources] = useState<ServerResource>({
    cpu: 0,
    memory: 0,
    disk: 0,
    network: 0
  });
  const [trafficData, setTrafficData] = useState<TrafficData[]>([]);
  const [fileOperations, setFileOperations] = useState<FileOperation[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [timeRange, setTimeRange] = useState<string>('24h');
  // 添加表单状态
  const [isModalVisible, setIsModalVisible] = useState<boolean>(false);
  const [form] = Form.useForm();
  // 存储服务地址
  const [serviceAddress, setServiceAddress] = useState<string>('');
  // 存储用户名查询条件
  const [usernameFilter, setUsernameFilter] = useState<string>('');
  // 分页状态
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [pageSize, setPageSize] = useState<number>(10);
  // 文件操作分页状态
  const [fileOpCurrentPage, setFileOpCurrentPage] = useState<number>(1);
  const [fileOpPageSize, setFileOpPageSize] = useState<number>(10);
  const [totalFileOperations, setTotalFileOperations] = useState<number>(0);
  // 总记录数状态
  const [totalClients, setTotalClients] = useState<number>(0);
  
  // 今日详情统计数据
  const [dailyStats, setDailyStats] = useState<DailyStatistics>({
    loginTotal: 0,
    loginSuccess: 0,
    loginFailed: 0,
    downloadTotal: 0,
    downloadSuccess: 0,
    downloadFailed: 0,
    uploadTotal: 0,
    uploadSuccess: 0,
    uploadFailed: 0,
    deleteTotal: 0,
    deleteSuccess: 0,
    deleteFailed: 0
  });

  // 模拟数据生成函数已移除，页面现在直接调用API获取数据

  // 从API获取今日详情统计数据
  const fetchDailyStats = useCallback(async () => {
    if (!serviceAddress) {
      console.log('服务地址未设置');
      return;
    }

    try {
      const apiUrl = `${serviceAddress}/api/audit/daily/stats`;
      
      // 使用原生fetch API来避免全局baseURL配置影响
      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setDailyStats(data);
    } catch (error) {
      console.error('获取今日详情统计数据失败:', error);
      // 可以添加错误提示
    }
  }, [serviceAddress]);

  // 从API获取文件操作数据
  const fetchFileOperations = useCallback(async (pageNum?: number, pageSizeNum?: number) => {
    try {
      // 从serviceAddress中获取地址并拼接对应的路径，与客户端列表保持一致
      const apiUrl = `${serviceAddress}/api/audit/opt/list`;
      const params = {
        page: (pageNum || fileOpCurrentPage) - 1, // API使用0-based页码
        size: pageSizeNum || fileOpPageSize,
        sort: 'createTime,desc'
      };
      
      // 构建查询字符串
      const queryString = new URLSearchParams(params).toString();
      const fullUrl = `${apiUrl}?${queryString}`;
      
      // 使用原生fetch API确保完全控制请求URL
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      const data = await response.json();
      console.log('文件操作API返回数据:', data);
      
      // 处理API返回的数据
      if (data && data.code === 200 && data.success && data.data && data.data.content) {
        setFileOperations(data.data.content);
        setTotalFileOperations(data.data.totalElements || 0);
        return true;
      } else {
        console.warn('文件操作API返回格式异常');
        return false;
      }
    } catch (error) {
      console.error('获取文件操作列表失败:', error);
      return false;
    }
  }, [serviceAddress, fileOpCurrentPage, fileOpPageSize]);

  // 修改fetchData函数，使用参数而不是依赖currentPage和pageSize
  const fetchData = useCallback(async (pageNum?: number, pageSizeNum?: number) => {
    setLoading(true);
    try {
      // 如果有保存的服务地址，则调用实际API获取客户端列表
      if (serviceAddress) {
        // 确保API地址正确拼接，避免与baseURL冲突
        const apiUrl = `${serviceAddress}/api/audit/client/list`;
        const params: any = {
          page: (pageNum || currentPage) - 1, // API使用0-based页码
          size: pageSizeNum || pageSize,
          sort: 'createTime,desc'
        };
        
        // 如果用户名查询条件不为空，则添加到params中
        if (usernameFilter) {
          params.username = usernameFilter;
        }
        
        // 构建查询字符串
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = `${apiUrl}?${queryString}`;
        
        // 使用原生fetch API确保完全控制请求URL
        const response = await fetch(fullUrl, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });
        
        const data = await response.json();
        // 调试日志，帮助排查API返回格式问题
        console.log('客户端列表API返回数据:', data);
        
        // 尝试从不同可能的路径获取数据
        let clientData = [];
        let total = 0;
        
        // 提取数据列表
        if (data && data.data && data.data.content) {
          clientData = data.data.content;
          // 提取总记录数
          total = data.data.totalElements || 0;
        } else if (data && data.content) {
          clientData = data.content;
          // 提取总记录数
          total = data.totalElements || 0;
        } else if (Array.isArray(data)) {
          clientData = data;
          total = data.length;
        }
        
        setClients(clientData);
        setTotalClients(total);
      } else {
        // 如果没有服务地址，设置为空数组
        setClients([]);
        setTotalClients(0);
      }
      
      // 直接调用API获取文件操作数据，不再使用模拟数据
      await fetchFileOperations();
      // 刷新今日详情统计数据
      await fetchDailyStats();
    } catch (error) {
      console.error('获取数据失败:', error);
      // 发生错误时保留现有数据
      try {
        // 尝试单独获取文件操作数据
        await fetchFileOperations();
      } catch (fileError) {
        console.error('获取文件操作数据失败:', fileError);
        // 不使用模拟数据，保留现有数据
      }
    } finally {
      setLoading(false);
    }
  }, [serviceAddress, usernameFilter, fetchFileOperations, fetchDailyStats]);

  // 初始化数据
  useEffect(() => {
    // 从本地存储读取SFTP监控地址
    const savedAddress = localStorage.getItem('sftpMonitorAddress');
    if (savedAddress) {
      setServiceAddress(savedAddress);
    }
  }, []); // 只在组件挂载时执行一次

  // 单独设置定时器，确保能获取到最新的fetchData函数
  useEffect(() => {
    // 设置定时刷新
    const interval = setInterval(() => {
      fetchData();
    }, 30000); // 每30秒刷新一次
    return () => clearInterval(interval);
  }, [fetchData]); // 依赖fetchData，但包装在箭头函数中避免无限循环

  // 当serviceAddress变化时，立即加载数据
  useEffect(() => {
    if (serviceAddress) {
      fetchData();
    }
  }, [serviceAddress]); // 移除fetchData依赖

  // 搜索函数 - 只有点击查询按钮时才调用
  const handleSearch = () => {
    fetchData();
  };

  // 处理用户名输入变化
  const handleUsernameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setUsernameFilter(e.target.value);
  };



  // 显示添加SFTP模态框
  const showModal = () => {
    setIsModalVisible(true);
  };

  // 处理表单提交
  const handleSubmit = () => {
    form.validateFields().then(values => {
      console.log('添加SFTP监控地址:', values);
      // 保存服务地址到状态
      setServiceAddress(values.serviceAddress);
      // 缓存到本地存储
      localStorage.setItem('sftpMonitorAddress', values.serviceAddress);
      setIsModalVisible(false);
      form.resetFields();
      // 刷新数据
      fetchData();
    }).catch(info => {
      console.log('表单验证失败:', info);
    });
  };

  // 客户端表格列定义
  const clientColumns: ColumnsType<Client> = [
    {
      title: 'IP地址',
      dataIndex: 'clientIp',
      key: 'clientIp',
      width: 120,
      render: (text: string) => <Text strong>{text}</Text>
    },
    {
      title: '端口',
      dataIndex: 'clientPort',
      key: 'clientPort',
      width: 80
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 100
    },
    {
        title: '连接时间',
        dataIndex: 'connectTime',
        key: 'connectTime',
        width: 160,
        render: (time: string) => formatTime(time)
      },
      {
        title: '断开时间',
        dataIndex: 'disconnectTime',
        key: 'disconnectTime',
        width: 160,
        render: (time: string | null) => formatTime(time)
      },
    {
      title: '认证状态',
      dataIndex: 'authStatus',
      key: 'authStatus',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '成功' : '失败'}
        </Tag>
      )
    },
    {
      title: '认证失败原因',
      dataIndex: 'authFailureReason',
      key: 'authFailureReason',
      width: 150,
      ellipsis: true,
      tooltip: (text: string) => text,
      render: (text: string | null) => text || '-'
    },
    {
      title: '断开原因',
      dataIndex: 'disconnectReason',
      key: 'disconnectReason',
      width: 120
    },
    {
        title: '创建时间',
        dataIndex: 'createTime',
        key: 'createTime',
        width: 160,
        render: (time: string) => formatTime(time)
      }
  ];

  // 文件操作表格列定义 - 根据新的API响应格式更新
  const fileOperationColumns: ColumnsType<FileOperation> = [
    {
      title: '文件路径',
      dataIndex: 'filePath',
      key: 'filePath',
      render: (text: string) => (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <FileOutlined style={{ marginRight: 8, color: '#1890ff' }} />
          <span>{text}</span>
        </div>
      )
    },
    {
        title: '类型',
        dataIndex: 'type',
        key: 'type',
        width: 120,
        render: (type: string) => {
          switch (type) {
            case 'upload':
              return (
                <Tag color="blue">
                  <UploadOutlined style={{ marginRight: 4 }} /> 上传
                </Tag>
              );
            case 'download':
              return (
                <Tag color="green">
                  <DownloadOutlined style={{ marginRight: 4 }} /> 下载
                </Tag>
              );
            case 'delete_file':
              return (
                <Tag color="red">
                  <FileOutlined style={{ marginRight: 4 }} /> 删除文件
                </Tag>
              );
            case 'delete_dir':
              return (
                <Tag color="orange">
                  <FolderOutlined style={{ marginRight: 4 }} /> 删除目录
                </Tag>
              );
            case 'rename':
              return (
                <Tag color="purple">
                  <EditOutlined style={{ marginRight: 4 }} /> 重命名
                </Tag>
              );
            default:
              return (
                <Tag color="default">
                  <InfoCircleOutlined style={{ marginRight: 4 }} /> 未知
                </Tag>
              );
          }
        }
      },
    {
      title: '大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size: string) => {
        // 格式化文件大小
        const numSize = parseInt(size);
        if (isNaN(numSize)) return size;
        if (numSize >= 1024 * 1024) {
          return `${(numSize / (1024 * 1024)).toFixed(2)}MB`;
        } else if (numSize >= 1024) {
          return `${(numSize / 1024).toFixed(2)}KB`;
        }
        return `${numSize}B`;
      }
    },
    {
      title: '状态',
      dataIndex: 'operateResult',
      key: 'operateResult',
      width: 100,
      render: (result: number, record: FileOperation) => {
        if (result === 1) {
          if (record.completionTime) {
            return (
              <Tag color="green">
                <CheckCircleOutlined style={{ marginRight: 4 }} /> 已完成
              </Tag>
            );
          } else {
            return (
              <Tag color="processing">
                <ClockCircleOutlined style={{ marginRight: 4 }} /> 处理中
              </Tag>
            );
          }
        } else {
          return (
            <Tag color="red">
              <ExclamationCircleOutlined style={{ marginRight: 4 }} /> 失败
            </Tag>
          );
        }
      }
    },
    {
      title: '操作时间',
      dataIndex: 'optTime',
      key: 'optTime',
      width: 160,
      render: (time: string) => formatTime(time)
    },
    {
      title: '操作人',
      dataIndex: 'clientUsername',
      key: 'clientUsername',
      width: 100
    },
    {
      title: '客户端地址',
      dataIndex: 'clientAddress',
      key: 'clientAddress',
      width: 150
    },
    {
      title: '错误信息',
      dataIndex: 'errorMsg',
      key: 'errorMsg',
      render: (msg: string) => msg || '-',
      ellipsis: true,
      tooltip: (text: string) => text
    }
  ];

  // 流量图表配置
  const trafficConfig = {
    data: trafficData,
    padding: 'auto',
    xField: 'time',
    yField: ['upload', 'download'],
    legend: {
      position: 'top' as const,
    },
    smooth: true,
    animation: {
      appear: {
        animation: 'path-in',
        duration: 1000,
      },
    },
    color: ['#1890ff', '#52c41a'],
    yAxis: {
      label: {
        formatter: (v: string) => `${v} MB`,
      },
    },
  };

  return (
    <div className="p-6" style={{ minHeight: '100vh' }}>
      {/* 页面标题 - 移到指定位置 */}
      <h1 className="text-2xl font-bold text-gray-800 m-0 mb-6 text-center">SFTP运行监控</h1>

      {/* 操作按钮 */}
      <div className="flex justify-end items-center mb-6">
        <Button type="primary" icon={<PlusOutlined />} onClick={showModal}>
          添加SFTP
        </Button>
      </div>

      {/* 主内容区域 - 上下布局 */}
      <Row gutter={[24, 24]}>
        
        {/* 上方 - 客户端列表，在所有屏幕尺寸下铺满宽度 */}
        <Col xs={24}>
          <Card 
          style={{ marginTop: 10 }}
            title="客户端列表（仅展示7日内）" 
            size="small"
            extra={
              <Space size="small" style={{ marginLeft: 10 }}>
                <Input
                  placeholder="用户名"
                  size="small"
                  value={usernameFilter}
                  onChange={handleUsernameChange}
                  style={{ width: 120 }}
                  allowClear
                />
                <Button 
                  size="small" 
                  icon={<ReloadOutlined />}
                  loading={loading} 
                  onClick={fetchData}
                />
              </Space>
            }
          >
            <Table
              columns={clientColumns}
              dataSource={clients}
              rowKey="id"
              pagination={{
                current: currentPage,
                pageSize: pageSize,
                total: totalClients, // 设置总记录数
                showSizeChanger: true,
                showTotal: (total, range) => 
                  `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                onChange: (page, size) => {
                  setCurrentPage(page);
                  setPageSize(size);
                  fetchData(page, size); // 直接传入新的页码和页大小，避免通过状态读取
                },
                onShowSizeChange: (current, size) => {
                  setPageSize(size);
                  setCurrentPage(1); // 切换每页条数时重置为第一页
                  fetchData(1, size); // 直接传入新的页码和页大小
                }
              }}
              loading={loading}
              scroll={{ y: 'calc(100vh - 170px)' }}
              size="small"
            />
          </Card>
        </Col>

        {/* 下方 - 资源监控和流量统计，在所有屏幕尺寸下铺满宽度 */}
        <Col xs={24}>
          <div className="space-y-10">
            {/* 今日详情统计 */}
            <Card 
              title={
                <div className="flex items-center">
                  <BarChartOutlined style={{ marginRight: 8, marginTop: 2 }} />
                  今日详情统计
                </div>
              }
              size="small"
              bodyStyle={{ padding: 20 }}
              extra={
                <Button 
                  size="small" 
                  icon={<ReloadOutlined />}
                  loading={loading} 
                  onClick={fetchDailyStats}
                >
                  刷新
                </Button>
              }
            >
              <Row gutter={[16, 24]}>
                {/* 登录统计 */}
                <Col xs={24} md={12} lg={6}>
                  <div className="bg-blue-50 p-4 rounded-lg shadow-sm">
                    <h3 className="text-lg font-semibold text-blue-700 mb-3">登录统计</h3>
                    <div className="space-y-3">
                      <Row gutter={8}>
                        <Col span={12}>
                          <Statistic 
                            title="总登录数" 
                            value={dailyStats.loginTotal} 
                            valueStyle={{ color: '#1890ff' }}
                            size="small"
                          />
                        </Col>
                        <Col span={12}>
                          <Statistic 
                            title="成功" 
                            value={dailyStats.loginSuccess} 
                            valueStyle={{ color: '#52c41a' }}
                            size="small"
                          />
                        </Col>
                      </Row>
                      <Statistic 
                        title="失败" 
                        value={dailyStats.loginFailed} 
                        valueStyle={{ color: '#f5222d' }}
                        size="small"
                      />
                    </div>
                  </div>
                </Col>
                
                {/* 下载统计 */}
                <Col xs={24} md={12} lg={6}>
                  <div className="bg-green-50 p-4 rounded-lg shadow-sm">
                    <h3 className="text-lg font-semibold text-green-700 mb-3">下载统计</h3>
                    <div className="space-y-3">
                      <Row gutter={8}>
                        <Col span={12}>
                          <Statistic 
                            title="总下载数" 
                            value={dailyStats.downloadTotal} 
                            valueStyle={{ color: '#1890ff' }}
                            size="small"
                          />
                        </Col>
                        <Col span={12}>
                          <Statistic 
                            title="成功" 
                            value={dailyStats.downloadSuccess} 
                            valueStyle={{ color: '#52c41a' }}
                            size="small"
                          />
                        </Col>
                      </Row>
                      <Statistic 
                        title="失败" 
                        value={dailyStats.downloadFailed} 
                        valueStyle={{ color: '#f5222d' }}
                        size="small"
                      />
                    </div>
                  </div>
                </Col>
                
                {/* 上传统计 */}
                <Col xs={24} md={12} lg={6}>
                  <div className="bg-purple-50 p-4 rounded-lg shadow-sm">
                    <h3 className="text-lg font-semibold text-purple-700 mb-3">上传统计</h3>
                    <div className="space-y-3">
                      <Row gutter={8}>
                        <Col span={12}>
                          <Statistic 
                            title="总上传数" 
                            value={dailyStats.uploadTotal} 
                            valueStyle={{ color: '#1890ff' }}
                            size="small"
                          />
                        </Col>
                        <Col span={12}>
                          <Statistic 
                            title="成功" 
                            value={dailyStats.uploadSuccess} 
                            valueStyle={{ color: '#52c41a' }}
                            size="small"
                          />
                        </Col>
                      </Row>
                      <Statistic 
                        title="失败" 
                        value={dailyStats.uploadFailed} 
                        valueStyle={{ color: '#f5222d' }}
                        size="small"
                      />
                    </div>
                  </div>
                </Col>
                
                {/* 删除统计 */}
                <Col xs={24} md={12} lg={6}>
                  <div className="bg-orange-50 p-4 rounded-lg shadow-sm">
                    <h3 className="text-lg font-semibold text-orange-700 mb-3">删除统计</h3>
                    <div className="space-y-3">
                      <Row gutter={8}>
                        <Col span={12}>
                          <Statistic 
                            title="总删除数" 
                            value={dailyStats.deleteTotal} 
                            valueStyle={{ color: '#1890ff' }}
                            size="small"
                          />
                        </Col>
                        <Col span={12}>
                          <Statistic 
                            title="成功" 
                            value={dailyStats.deleteSuccess} 
                            valueStyle={{ color: '#52c41a' }}
                            size="small"
                          />
                        </Col>
                      </Row>
                      <Statistic 
                        title="失败" 
                        value={dailyStats.deleteFailed} 
                        valueStyle={{ color: '#f5222d' }}
                        size="small"
                      />
                    </div>
                  </div>
                </Col>
              </Row>
            </Card>

            {/* 服务器流量监控 */}
            <Card
           style={{ marginTop: 20 }}
              title={
                <div className="flex items-center justify-between" >
                  <div className="flex items-center">
                    <LineChartOutlined style={{ marginRight: 8, marginTop: 2 }} />
                    服务器流量监控：上传下载流量
                  </div>
                  <Select 
                    value={timeRange} 
                    onChange={setTimeRange} 
                    style={{ width: 100 }}
                    size="small"
                  >
                    <Option value="24h">24小时</Option>
                    <Option value="7d">7天</Option>
                    <Option value="30d">30天</Option>
                  </Select>
                </div>
              }
              size="small"
              bodyStyle={{ padding: 20, paddingTop: 16 }}
            >
              <div style={{ height: 300 }}>
                {trafficData.length > 0 ? (
                  <Line {...trafficConfig} />
                ) : (
                  <Empty description="加载中..." style={{ height: 280, display: 'flex', alignItems: 'center', justifyContent: 'center' }} />
                )}
              </div>
              <div className="flex justify-between mt-2">
                <div className="flex items-center">
                  <span className="inline-block w-3 h-3 bg-blue-500 mr-2 rounded-full"></span>
                  <Text type="secondary">上传流量</Text>
                </div>
                <div className="flex items-center">
                  <span className="inline-block w-3 h-3 bg-green-500 mr-2 rounded-full"></span>
                  <Text type="secondary">下载流量</Text>
                </div>
              </div>
            </Card>

            {/* 客户端正在操作的文件列表 - 增加上方空隙 */}
            <div style={{ marginTop: 20 }}>
              <Card 
                title={
                  <div className="flex items-center">
                    <FileOutlined style={{ marginRight: 8, marginTop: 2 }} />
                    客户端正在操作的文件列表：(仅展示7日内)
                  </div>
                }
                size="small"
                bodyStyle={{ padding: 20 }}
                extra={
                  <Space size="small">
                    <Button 
                      size="small" 
                      icon={<ReloadOutlined />}
                      loading={loading} 
                      onClick={fetchData}
                    />
                    <Button size="small" icon={<FilterOutlined />} />
                  </Space>
                }
              >
                <Table
                  columns={fileOperationColumns}
                  dataSource={fileOperations}
                  rowKey="id"
                  pagination={{
                    current: fileOpCurrentPage,
                    pageSize: fileOpPageSize,
                    total: totalFileOperations,
                    showSizeChanger: true,
                    showTotal: (total, range) => 
                      `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
                    onChange: (page, size) => {
                      setFileOpCurrentPage(page);
                      setFileOpPageSize(size);
                      fetchFileOperations(page, size);
                    },
                    onShowSizeChange: (current, size) => {
                      setFileOpPageSize(size);
                      setFileOpCurrentPage(1);
                      fetchFileOperations(1, size);
                    },
                    pageSizeOptions: ['5', '10', '20', '50']
                  }}
                  loading={loading}
                  scroll={{ y: 300 }}
                  size="small"
                />
              </Card>
            </div>
          </div>
        </Col>
      </Row>

      {/* 添加SFTP监控地址模态框 */}
      <Modal
        title="添加SFTP监控地址"
        open={isModalVisible}
        onOk={handleSubmit}
        onCancel={() => setIsModalVisible(false)}
        okText="确定"
        cancelText="取消"
      >
        <Form
          form={form}
          layout="vertical"
        >
          <Item
            label="服务地址"
            name="serviceAddress"
            rules={[{ required: true, message: '请输入服务地址' }]}
          >
            <Input placeholder="请输入SFTP服务地址，格式如：sftp://hostname:port" />
          </Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MonitorPage;