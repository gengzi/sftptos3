import { useState, useEffect, useRef } from 'react';
import {
  Modal,
  message,
  Drawer,
  Button,
  Space,
} from 'antd';
import { ProTable, ProDescriptions } from '@ant-design/pro-components';
import { PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { queryUsers, createUser, updateUser, deleteUser } from '@/services/user';
import UserForm from '../components/UserForm';

const { confirm } = Modal;

const UserList = () => {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  const [editDrawerVisible, setEditDrawerVisible] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [refreshKey, setRefreshKey] = useState<number>(0);
  const createUserFormRef = useRef<any>(null);

  // 获取用户列表
  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await queryUsers();
      if (response.success) {
        setUsers(response.data || []);
      } else {
        message.error('获取用户列表失败');
      }
    } catch (error) {
      message.error('获取用户列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 添加用户
  const handleAddUser = async (values: any) => {
    try {
      const response = await createUser(values);
      if (response.success) {
        message.success('添加用户成功');
        setCreateModalVisible(false);
        // 重置表单
        if (createUserFormRef.current) {
          createUserFormRef.current.resetFields();
        }
        fetchUsers();
      } else {
        message.error('添加用户失败');
      }
    } catch (error) {
      message.error('添加用户失败');
    }
  };

  // 编辑用户
  const handleEditUser = async (values: any) => {
    try {
      const response = await updateUser(currentUser.id, values);
      if (response.success) {
        message.success('编辑用户成功');
        setEditDrawerVisible(false);
        fetchUsers();
      } else {
        message.error('编辑用户失败');
      }
    } catch (error) {
      message.error('编辑用户失败');
    }
  };

  // 删除用户
  const handleDeleteUser = (id: string) => {
    confirm({
      title: '确定要删除这个用户吗？',
      icon: <ExclamationCircleOutlined />,
      onOk() {
        return new Promise(async (resolve, reject) => {
          try {
            const response = await deleteUser(id);
            if (response.success) {
              message.success('删除用户成功');
              fetchUsers();
              resolve(undefined);
            } else {
              message.error('删除用户失败');
              reject(new Error('删除失败'));
            }
          } catch (error) {
            message.error('删除用户失败');
            reject(error);
          }
        });
      },
    });
  };

  // 显示编辑抽屉
  const showEditDrawer = (record: any) => {
    setCurrentUser(record);
    setEditDrawerVisible(true);
  };

  // 列配置
  const columns = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      valueType: 'text',
    },
    {
      title: '用户根目录',
      dataIndex: 'userRootPath',
      key: 'userRootPath',
      valueType: 'text',
    },
    {
      title: '存储类型',
      dataIndex: 'storageType',
      key: 'storageType',
      valueType: 'select',
      valueEnum: {
        local: {
          text: '本地文件',
          status: 'Default',
        },
        s3: {
          text: 's3存储',
          status: 'Success',
        },
      },
    },
    {
      title: 's3存储链接',
      dataIndex: 's3Link',
      key: 's3Link',
      valueType: 'text',
      render: (text: string, record: any) => {
        // 只有当存储类型为s3时才显示s3链接
        return record.storageType === 's3' ? text : '-';
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      valueType: 'dateTime',
    },
    {
      title: '操作',
      valueType: 'option',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => showEditDrawer(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteUser(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  // 初始加载用户列表
  useEffect(() => {
    fetchUsers();
  }, [refreshKey]);

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setCreateModalVisible(true)}
        >
          添加用户
        </Button>
      </Space>

      <ProTable
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 10,
        }}
      />

      {/* 添加用户模态框 */}
      <Modal
        title="添加用户"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        footer={null}
        afterClose={() => {
          // 模态框关闭后重置表单
          if (createUserFormRef.current) {
            createUserFormRef.current.resetFields();
          }
        }}
      >
        <UserForm 
          ref={createUserFormRef}
          onFinish={handleAddUser} 
          onCancel={() => setCreateModalVisible(false)} 
        />
      </Modal>

      {/* 编辑用户抽屉 */}
      <Drawer
        title="编辑用户"
        width={720}
        placement="right"
        onClose={() => setEditDrawerVisible(false)}
        open={editDrawerVisible}
      >
        {currentUser && (
          <UserForm
            initialValues={currentUser}
            onFinish={handleEditUser}
            onCancel={() => setEditDrawerVisible(false)}
          />
        )}
      </Drawer>
    </div>
  );
};

export default UserList;