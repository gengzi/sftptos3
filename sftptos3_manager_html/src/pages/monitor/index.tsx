import React, { useState, useEffect } from 'react';
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
} from '@ant-design/icons';
import { Line, Column } from '@ant-design/plots';
import type { ColumnsType } from 'antd/es/table';

const { Text, Paragraph } = Typography;
const { Option } = Select;
const { Item } = Form;

// 类型定义
interface Client {  
  id: string;
  ip: string;
  port: string;
  username: string;
  connectedTime: string;
  status: 'active' | 'idle';
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

interface FileOperation {
  id: string;
  filename: string;
  type: 'upload' | 'download';
  size: string;
  progress: number;
  status: 'processing' | 'completed' | 'failed';
  startTime: string;
  username: string;
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
  const [selectedFtpServer, setSelectedFtpServer] = useState<string>('all');
  const [timeRange, setTimeRange] = useState<string>('24h');
  // 添加表单状态
  const [isModalVisible, setIsModalVisible] = useState<boolean>(false);
  const [form] = Form.useForm();

  // 生成模拟客户端数据
  const generateClientData = (): Client[] => {
    const statuses: ('active' | 'idle')[] = ['active', 'idle'];
    const clients: Client[] = [];
    
    for (let i = 1; i <= 8; i++) {
      const status = statuses[Math.floor(Math.random() * statuses.length)];
      const hour = Math.floor(Math.random() * 24).toString().padStart(2, '0');
      const minute = Math.floor(Math.random() * 60).toString().padStart(2, '0');
      const second = Math.floor(Math.random() * 60).toString().padStart(2, '0');
      
      clients.push({
        id: `client-${i}`,
        ip: `192.168.1.${Math.floor(Math.random() * 255)}`,
        port: `${Math.floor(Math.random() * 5000) + 1000}`,
        username: `user${i}`,
        connectedTime: `2023-10-20 ${hour}:${minute}:${second}`,
        status
      });
    }
    
    return clients;
  };

  // 生成模拟服务器资源数据
  const generateServerResourceData = (): ServerResource => {
    return {
      cpu: Math.floor(Math.random() * 70) + 10, // 10-80%
      memory: Math.floor(Math.random() * 60) + 20, // 20-80%
      disk: Math.floor(Math.random() * 50) + 10, // 10-60%
      network: Math.floor(Math.random() * 80) + 5 // 5-85%
    };
  };

  // 生成模拟流量数据
  const generateTrafficData = (): TrafficData[] => {
    const data: TrafficData[] = [];
    const now = new Date();
    
    // 根据时间范围生成不同数量的数据点
    const points = timeRange === '24h' ? 24 : timeRange === '7d' ? 7 : 30;
    const interval = timeRange === '24h' ? 60 * 60 * 1000 : 24 * 60 * 60 * 1000;
    
    for (let i = points - 1; i >= 0; i--) {
      const time = new Date(now.getTime() - i * interval);
      let timeStr = '';
      
      if (timeRange === '24h') {
        timeStr = `${time.getHours().toString().padStart(2, '0')}:00`;
      } else {
        timeStr = `${time.getMonth() + 1}/${time.getDate()}`;
      }
      
      data.push({
        time: timeStr,
        upload: Math.floor(Math.random() * 500) + 100,
        download: Math.floor(Math.random() * 400) + 50
      });
    }
    
    return data;
  };

  // 生成模拟文件操作数据
  const generateFileOperationData = (): FileOperation[] => {
    const types: ('upload' | 'download')[] = ['upload', 'download'];
    const statuses: ('processing' | 'completed' | 'failed')[] = ['processing', 'completed', 'failed'];
    const filenames = ['项目计划.docx', '财务报表.xlsx', '产品设计图.png', '用户调研报告.pdf', '数据分析.zip'];
    const sizes = ['1.2MB', '3.5MB', '8.7MB', '2.3MB', '5.6MB'];
    
    const operations: FileOperation[] = [];
    
    for (let i = 1; i <= 10; i++) {
      const type = types[Math.floor(Math.random() * types.length)];
      const status = statuses[Math.floor(Math.random() * statuses.length)];
      const progress = status === 'processing' ? Math.floor(Math.random() * 100) : status === 'completed' ? 100 : 0;
      
      operations.push({
        id: `file-${i}`,
        filename: filenames[Math.floor(Math.random() * filenames.length)],
        type,
        size: sizes[Math.floor(Math.random() * sizes.length)],
        progress,
        status,
        startTime: `2023-10-20 1${Math.floor(Math.random() * 2)}:${Math.floor(Math.random() * 60).toString().padStart(2, '0')}:00`,
        username: `user${Math.floor(Math.random() * 8) + 1}`
      });
    }
    
    return operations;
  };

  // 初始化数据
  useEffect(() => {
    fetchData();
    // 设置定时刷新
    const interval = setInterval(fetchData, 30000); // 每30秒刷新一次
    return () => clearInterval(interval);
  }, [timeRange, selectedFtpServer]);

  // 获取数据
  const fetchData = () => {
    setLoading(true);
    setTimeout(() => {
      setClients(generateClientData());
      setServerResources(generateServerResourceData());
      setTrafficData(generateTrafficData());
      setFileOperations(generateFileOperationData());
      setLoading(false);
    }, 800);
  };

  // 显示添加SFTP模态框
  const showModal = () => {
    setIsModalVisible(true);
  };

  // 处理表单提交
  const handleSubmit = () => {
    form.validateFields().then(values => {
      console.log('添加SFTP监控地址:', values);
      // 这里可以添加实际的API调用逻辑
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
      dataIndex: 'ip',
      key: 'ip',
      width: 120,
      render: (text: string) => <Text strong>{text}</Text>
    },
    {
      title: '端口',
      dataIndex: 'port',
      key: 'port',
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
      dataIndex: 'connectedTime',
      key: 'connectedTime'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : 'orange'}>
          {status === 'active' ? '活跃' : '空闲'}
        </Tag>
      )
    }
  ];

  // 文件操作表格列定义
  const fileOperationColumns: ColumnsType<FileOperation> = [
    {
      title: '文件名',
      dataIndex: 'filename',
      key: 'filename',
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
      width: 100,
      render: (type: string) => (
        <Tag color={type === 'upload' ? 'blue' : 'green'}>
          {type === 'upload' ? (
            <><UploadOutlined style={{ marginRight: 4 }} /> 上传</>
          ) : (
            <><DownloadOutlined style={{ marginRight: 4 }} /> 下载</>
          )}
        </Tag>
      )
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
      width: 100
    },
    {
      title: '进度',
      dataIndex: 'progress',
      key: 'progress',
      render: (_: number, record: FileOperation) => {
        if (record.status === 'processing') {
          return (
            <div>
              <Progress percent={record.progress} size="small" status="active" />
              <Text type="secondary" style={{ fontSize: 12, float: 'right' }}>{record.progress}%</Text>
            </div>
          );
        } else if (record.status === 'completed') {
          return (
            <div>
              <Progress percent={100} size="small" status="success" />
              <Text type="secondary" style={{ fontSize: 12, float: 'right' }}>100%</Text>
            </div>
          );
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
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 140
    },
    {
      title: '操作人',
      dataIndex: 'username',
      key: 'username',
      width: 100
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
        <Space>
          <Select 
            value={selectedFtpServer} 
            style={{ width: 120 }} 
            onChange={setSelectedFtpServer}
          >
            <Option value="all">所有服务器</Option>
            <Option value="sftp1">SFTP服务器1</Option>
            <Option value="sftp2">SFTP服务器2</Option>
            <Option value="sftp3">SFTP服务器3</Option>
          </Select>
          <Button type="primary" icon={<PlusOutlined />} onClick={showModal}>
            添加SFTP
          </Button>
        </Space>
      </div>

      {/* 主内容区域 */}
      <Row gutter={[24, 24]}>
        
        {/* 左侧 - 客户端列表 */}
        <Col xs={24} lg={8}>
          <Card 
          style={{ marginTop: 10 }}
            title="正在连接的客户端列表" 
            size="small"
            extra={
              <Space size="small" style={{ marginLeft: 10 }}>
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
              columns={clientColumns}
              dataSource={clients}
              rowKey="id"
              pagination={{
                pageSize: 10,
                showSizeChanger: false,
                showTotal: (total, range) => 
                  `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
              }}
              loading={loading}
              scroll={{ y: 'calc(100vh - 170px)' }}
              size="small"
            />
          </Card>
        </Col>

        {/* 右侧 - 资源监控和流量统计 */}
        <Col xs={24} lg={16}>
          <div className="space-y-10">
            {/* 服务器资源监控 */}
            <Card 
              title={
                <div className="flex items-center">
                  <SaveOutlined style={{ marginRight: 8, marginTop: 2 }} />
                  服务器资源监控
                </div>
              }
              size="small"
              bodyStyle={{ padding: 20 }}
            >
              <Row gutter={[16, 24]}>
                <Col xs={24} sm={12} lg={6}>
                  <Statistic 
                    title="CPU使用率" 
                    value={serverResources.cpu} 
                    suffix="%" 
                    valueStyle={{ color: serverResources.cpu > 70 ? '#f5222d' : '#3f8600' }}
                    style={{ marginBottom: 8 }}
                  />
                  <Progress percent={serverResources.cpu} size="small" status="active" />
                </Col>
                <Col xs={24} sm={12} lg={6}>
                  <Statistic 
                    title="内存使用率" 
                    value={serverResources.memory} 
                    suffix="%" 
                    valueStyle={{ color: serverResources.memory > 70 ? '#f5222d' : '#3f8600' }}
                    style={{ marginBottom: 8 }}
                  />
                  <Progress percent={serverResources.memory} size="small" status="active" />
                </Col>
                <Col xs={24} sm={12} lg={6}>
                  <Statistic 
                    title="磁盘使用率" 
                    value={serverResources.disk} 
                    suffix="%" 
                    valueStyle={{ color: serverResources.disk > 70 ? '#f5222d' : '#3f8600' }}
                    style={{ marginBottom: 8 }}
                  />
                  <Progress percent={serverResources.disk} size="small" status="active" />
                </Col>
                <Col xs={24} sm={12} lg={6}>
                  <Statistic 
                    title="网络使用率" 
                    value={serverResources.network} 
                    suffix="%" 
                    valueStyle={{ color: serverResources.network > 70 ? '#f5222d' : '#3f8600' }}
                    style={{ marginBottom: 8 }}
                  />
                  <Progress percent={serverResources.network} size="small" status="active" />
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
                    客户端正在操作的文件列表：上传，下载
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
                    pageSize: 5,
                    showSizeChanger: false,
                    showTotal: (total, range) => 
                      `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
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
          initialValues={{
            port: 22,
            enableMonitoring: true
          }}
        >
          <Item
            label="服务器名称"
            name="serverName"
            rules={[{ required: true, message: '请输入服务器名称' }]}
          >
            <Input placeholder="请输入服务器名称" />
          </Item>
          <Item
            label="IP地址"
            name="ipAddress"
            rules={[{ required: true, message: '请输入IP地址' }]}
          >
            <Input placeholder="请输入IP地址" />
          </Item>
          <Item
            label="端口号"
            name="port"
            rules={[{ required: true, message: '请输入端口号' }]}
          >
            <InputNumber min={1} max={65535} style={{ width: '100%' }} />
          </Item>
          <Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" />
          </Item>
          <Item
            label="备注信息"
            name="remark"
          >
            <Input.TextArea rows={3} placeholder="请输入备注信息（可选）" />
          </Item>
          <Item name="enableMonitoring" valuePropName="checked">
            <Checkbox defaultChecked>启用监控</Checkbox>
          </Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MonitorPage;