import React, { useState, useRef } from 'react';
import { Button, Modal, Form, Input, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { ProTable, ProColumns } from '@ant-design/pro-components';
import { queryAdmins, createAdmin, updateAdmin, deleteAdmin } from '@/services/admin';

// 管理员类型定义
interface Admin {
  id: string;
  username: string;
  password: string;
}

const AdminListPage: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [isAddModalVisible, setIsAddModalVisible] = useState<boolean>(false);
  const [isEditModalVisible, setIsEditModalVisible] = useState<boolean>(false);
  const [currentAdmin, setCurrentAdmin] = useState<Admin | null>(null);
  const [addForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const actionRef = useRef<any>(null);

  // 获取管理员列表数据
  const fetchAdmins = async (params?: { username?: string; current?: number; pageSize?: number }) => {
    setLoading(true);
    try {
      const response = await queryAdmins(params);
      if (response.success) {
        // 为每个管理员添加密码占位符
        const adminsWithPassword = response.data.map((admin: any) => ({
          ...admin,
          password: '******',
        }));
        // 直接返回符合ProTable要求的数据结构
        return {
          data: adminsWithPassword,
          total: response.total || 0,
          success: true,
        };
      } else {
        message.error(response.message || '获取管理员列表失败');
        return {
          data: [],
          total: 0,
          success: false,
        };
      }
    } catch (error) {
      console.error('获取管理员列表失败:', error);
      message.error('获取管理员列表失败');
      return {
        data: [],
        total: 0,
        success: false,
      };
    } finally {
      setLoading(false);
    }
  };

  // 显示添加管理员模态框
  const showAddModal = () => {
    addForm.resetFields();
    setIsAddModalVisible(true);
  };

  // 显示编辑管理员模态框
  const showEditModal = (admin: Admin) => {
    setCurrentAdmin(admin);
    editForm.setFieldsValue({
      username: admin.username,
      password: '', // 编辑时密码框为空
    });
    setIsEditModalVisible(true);
  };

  // 刷新表格
  const refreshTable = () => {
    if (actionRef.current) {
      actionRef.current.reload();
    }
  };

  // 处理添加管理员
  const handleAddAdmin = () => {
    addForm.validateFields().then(values => {
      setLoading(true);
      createAdmin({
        username: values.username,
        passwd: values.password,
      })
        .then((response) => {
          if (response.success) {
            setIsAddModalVisible(false);
            message.success('管理员添加成功');
            // 重新获取管理员列表
            refreshTable();
          } else {
            message.error(response.message || '管理员添加失败');
          }
        })
        .catch((error) => {
          console.error('管理员添加失败:', error);
          message.error('管理员添加失败');
        })
        .finally(() => {
          setLoading(false);
        });
    });
  };

  // 处理编辑管理员
  const handleEditAdmin = () => {
    editForm.validateFields().then(values => {
      if (!currentAdmin) return;
      
      setLoading(true);
      updateAdmin(currentAdmin.id, {
        passwd: values.password,
      })
        .then((response) => {
          if (response.success) {
            setIsEditModalVisible(false);
            message.success('管理员密码修改成功');
            // 重新获取管理员列表
            refreshTable();
          } else {
            message.error(response.message || '管理员密码修改失败');
          }
        })
        .catch((error) => {
          console.error('管理员密码修改失败:', error);
          message.error('管理员密码修改失败');
        })
        .finally(() => {
          setLoading(false);
        });
    });
  };

  // 处理删除管理员
  const handleDeleteAdmin = (id: string) => {
    setLoading(true);
    deleteAdmin(id)
      .then((response) => {
        if (response.success) {
          message.success('管理员删除成功');
          // 重新获取管理员列表
          refreshTable();
        } else {
          message.error(response.message || '管理员删除失败');
        }
      })
      .catch((error) => {
        console.error('管理员删除失败:', error);
        message.error('管理员删除失败');
      })
      .finally(() => {
        setLoading(false);
      });
  };

  // 表格列定义
  const columns: ProColumns<Admin>[] = [
    {
      title: '账户',
      dataIndex: 'username',
      key: 'username',
      width: 200,
      sorter: true,
      hideInSearch: false,
    },
    {
      title: '密码',
      dataIndex: 'password',
      key: 'password',
      width: 200,
      hideInSearch: true,
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="link" 
            icon={<EditOutlined />} 
            onClick={() => showEditModal(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个管理员吗？"
            onConfirm={() => handleDeleteAdmin(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button 
              type="link" 
              danger 
              icon={<DeleteOutlined />}
              disabled={record.username === 'admin'} // 系统默认管理员不可删除
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="p-6">
      <div className="mb-6" style={{ marginBottom: '24px' }}>
        <div className="flex justify-between items-center">
          <Button type="primary" icon={<PlusOutlined />} onClick={showAddModal}>
            添加管理员
          </Button>
        </div>
      </div>

      <ProTable<Admin>
        columns={columns}
        actionRef={actionRef}
        request={fetchAdmins}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
          showSizeChanger: true,
          showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
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

      {/* 添加管理员模态框 */}
      <Modal
        title="添加管理员"
        open={isAddModalVisible}
        onOk={handleAddAdmin}
        onCancel={() => setIsAddModalVisible(false)}
        okText="确定"
        cancelText="取消"
        confirmLoading={loading}
      >
        <Form
          form={addForm}
          layout="vertical"
          initialValues={{
            username: '',
            password: '',
          }}
        >
          <Form.Item
            label="账户"
            name="username"
            rules={[
              { required: true, message: '请输入账户名' },
              { min: 3, message: '账户名长度至少为3个字符' },
            ]}
          >
            <Input placeholder="请输入账户名" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码长度至少为6个字符' },
            ]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑管理员模态框 */}
      <Modal
        title="编辑管理员"
        open={isEditModalVisible}
        onOk={handleEditAdmin}
        onCancel={() => setIsEditModalVisible(false)}
        okText="确定"
        cancelText="取消"
        confirmLoading={loading}
      >
        <Form
          form={editForm}
          layout="vertical"
        >
          <Form.Item
            label="账户"
            name="username"
          >
            <Input disabled placeholder="账户名不可修改" />
          </Form.Item>
          <Form.Item
            label="新密码"
            name="password"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码长度至少为6个字符' },
            ]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminListPage;