import React, { useState, useEffect, useRef } from 'react';
import { Button, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { ProTable, ProColumns } from '@ant-design/pro-components';
import { useIntl } from '@umijs/max';
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
  const intl = useIntl();

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
        accessSecret: values.accessSecret, // 直接使用accessSecret字段
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
      // 转换表单字段名以匹配API要求并添加id
      const requestData = {
        s3Name: values.s3Name,
        endpoint: values.endpoint,
        accessKey: values.accessKey,
        accessSecret: values.accessSecret, // 直接使用accessSecret字段
        bucket: values.bucket,
        region: values.region,
        id: currentConfig?.id // 添加id字段
      };
      
      if (currentConfig && currentConfig.id) {
        const response = await updateS3Storage(requestData);
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
        // 确保正确显示后端返回的错误信息
        // 特别是当code为1007且message为"当前配置正在使用中"的情况
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
      title: intl.formatMessage({ id: 'pages.s3Management.columnName' }),
      dataIndex: 's3Name',
      key: 's3Name',
      valueType: 'text',
      sorter: true,
      hideInSearch: false,
    },
    {
      title: intl.formatMessage({ id: 'pages.s3Management.columnEndpoint' }),
      dataIndex: 'endpoint',
      key: 'endpoint',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: intl.formatMessage({ id: 'pages.s3Management.columnAccessKey' }),
      dataIndex: 'accessKey',
      key: 'accessKey',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: intl.formatMessage({ id: 'pages.s3Management.columnBucket' }),
      dataIndex: 'bucket',
      key: 'bucket',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: intl.formatMessage({ id: 'pages.s3Management.columnRegion' }),
      dataIndex: 'region',
      key: 'region',
      valueType: 'text',
      hideInSearch: true,
    },
    {
      title: intl.formatMessage({ id: 'pages.s3Management.columnAction' }),
      valueType: 'option',
      render: (_: any, record: S3Config) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => showEditModal(record)}
          >
            {intl.formatMessage({ id: 'pages.s3Management.editButton' })}
          </Button>
          <Popconfirm
            title={intl.formatMessage({ id: 'pages.s3Management.deleteConfirmTitle' })}
            onConfirm={() => handleDeleteS3Config(record.id)}
            okText={intl.formatMessage({ id: 'pages.common.confirm' })}
            cancelText={intl.formatMessage({ id: 'pages.common.cancel' })}
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              {intl.formatMessage({ id: 'pages.s3Management.deleteButton' })}
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
          {intl.formatMessage({ id: 'pages.s3Management.addButton' })}
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