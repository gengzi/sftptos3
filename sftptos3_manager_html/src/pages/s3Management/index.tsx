import React, { useState, useEffect, useRef } from 'react';
import { Button, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { ProTable, ProColumns } from '@ant-design/pro-components';
import S3Form from './components/S3Form';
import { queryS3Storages, createS3Storage, updateS3Storage, deleteS3Storage } from '@/services/s3';

interface S3Config {
  id: string;
  s3Name: string;
  endpoint: string;
  accessKey: string;
  secretKey: string;
  bucket: string;
  region?: string;
}

const S3Management: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [currentConfig, setCurrentConfig] = useState<S3Config | null>(null);
  const actionRef = useRef();

  // 获取S3配置列表
  const fetchS3Configs = async (params: { s3Name?: string; current?: number; pageSize?: number; sort?: string }) => {
    try {
      const response = await queryS3Storages({
        s3Name: params.s3Name,
        page: params.current ? params.current - 1 : 0, // 转换为0基索引
        size: params.pageSize || 10,
        sort: params.sort || 'createTime'
      });
      
      if (response.success) {
        // 转换数据格式以匹配表格显示
        const formattedData = response.data.map((item: any) => ({
          ...item,
          secretKey: item.accessSecret // 将accessSecret映射为secretKey以匹配表单字段
        }));
        
        return {
          data: formattedData,
          success: true,
          total: response.total
        };
      } else {
        message.error(response.message || '获取S3配置列表失败');
        return {
          data: [],
          success: false,
          total: 0
        };
      }
    } catch (error) {
      message.error('获取S3配置列表失败');
      return {
        data: [],
        success: false,
        total: 0
      };
    }
  };

  // 添加S3配置
  const handleAddS3Config = async (values: any) => {
    try {
      // 转换表单字段名以匹配API要求
      const requestData = {
        s3Name: values.s3Name,
        endpoint: values.endpoint,
        accessKey: values.accessKey,
        accessSecret: values.secretKey, // 将secretKey映射为accessSecret以匹配API要求
        bucket: values.bucket,
        region: values.region
      };
      
      const response = await createS3Storage(requestData);
      if (response.success) {
        message.success('添加S3配置成功');
        setModalVisible(false);
        // 刷新表格数据
        if (actionRef.current) {
          actionRef.current.reload();
        }
      } else {
        message.error(response.message || '添加S3配置失败');
      }
    } catch (error) {
      message.error('添加S3配置失败');
    }
  };

  // 编辑S3配置
  const handleEditS3Config = async (values: any) => {
    try {
      // 转换表单字段名以匹配API要求
      const requestData = {
        s3Name: values.s3Name,
        endpoint: values.endpoint,
        accessKey: values.accessKey,
        accessSecret: values.secretKey, // 将secretKey映射为accessSecret以匹配API要求
        bucket: values.bucket,
        region: values.region
      };
      
      if (currentConfig && currentConfig.id) {
        const response = await updateS3Storage(currentConfig.id, requestData);
        if (response.success) {
          message.success('编辑S3配置成功');
          setModalVisible(false);
          // 刷新表格数据
          if (actionRef.current) {
            actionRef.current.reload();
          }
        } else {
          message.error(response.message || '编辑S3配置失败');
        }
      } else {
        message.error('编辑S3配置失败：缺少配置ID');
      }
    } catch (error) {
      message.error('编辑S3配置失败');
    }
  };

  // 删除S3配置
  const handleDeleteS3Config = async (id: string) => {
    try {
      const response = await deleteS3Storage(id);
      if (response.success) {
        message.success('删除S3配置成功');
        // 刷新表格数据
        if (actionRef.current) {
          actionRef.current.reload();
        }
      } else {
        message.error(response.message || '删除S3配置失败');
      }
    } catch (error) {
      message.error('删除S3配置失败');
    }
  };

  // 显示编辑模态框
  const showEditModal = (record: S3Config) => {
    setCurrentConfig(record);
    setModalVisible(true);
  };

  // 显示添加模态框
  const showAddModal = () => {
    setCurrentConfig(null);
    setModalVisible(true);
  };

  

  // 列配置
  const columns = [
    {
      title: '名称标识',
      dataIndex: 's3Name',
      key: 's3Name',
      valueType: 'text',
      sorter: true,
      hideInSearch: false,
    },
    {
      title: '请求地址',
      dataIndex: 'endpoint',
      key: 'endpoint',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: '账户',
      dataIndex: 'accessKey',
      key: 'accessKey',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: '桶',
      dataIndex: 'bucket',
      key: 'bucket',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: 'Region',
      dataIndex: 'region',
      key: 'region',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_: any, record: S3Config) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => showEditModal(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个S3配置吗？"
            onConfirm={() => handleDeleteS3Config(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={showAddModal}
        >
          添加S3存储
        </Button>
      </Space>

      <ProTable
        columns={columns}
        actionRef={actionRef}
        request={fetchS3Configs}
        rowKey="id"
        pagination={{
          pageSize: 10,
        }}
        search={{
          labelWidth: 'auto',
        }}
        options={{
          setting: {
            listsHeight: 400,
          },
        }}
      />

      {/* 添加/编辑S3配置模态框 */}
      <S3Form
        visible={modalVisible}
        initialValues={currentConfig}
        onFinish={currentConfig ? handleEditS3Config : handleAddS3Config}
        onCancel={() => setModalVisible(false)}
      />
    </div>
  );
};

export default S3Management;