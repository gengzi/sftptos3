import { useState, useEffect, useRef } from 'react';
import { Modal, message, Drawer, Button, Space } from 'antd';
import { ProTable, ProColumns } from '@ant-design/pro-components';
import { PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { queryUsers, createUser, updateUser, deleteUser, getUserDetails } from '@/services/user';
import UserForm from '../components/UserForm';

const { confirm } = Modal;

// 定义用户数据类型
interface User {
  id: string;
  username: string;
  userRootPath: string;
  storageType: 'local' | 's3';
  s3Link?: string;
  clientPublicKey?: string;
  createdAt: string;
}

const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  const [editDrawerVisible, setEditDrawerVisible] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [refreshKey, setRefreshKey] = useState<number>(0);
  const actionRef = useRef<any>(null);
  const createUserFormRef = useRef<any>(null);

  // 获取用户列表
  const fetchUsers = async (params?: { username?: string; current?: number; pageSize?: number }) => {
    setLoading(true);
    try {
      const response = await queryUsers(params);
      if (response.success) {
        setUsers(response.data || []);
        // 直接返回符合ProTable要求的数据结构
        return {
          data: response.data || [],
          total: response.total || 0,
          success: true,
        };
      } else {
        // 显示后端返回的错误信息
        message.error(response.message || '获取用户列表失败');
        return {
          data: [],
          total: 0,
          success: false,
        };
      }
    } catch (error) {
      message.error('获取用户列表失败');
      return {
        data: [],
        total: 0,
        success: false,
      };
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
        // 刷新表格
        if (actionRef.current) {
          actionRef.current.reload();
        }
      } else {
        // 显示后端返回的错误信息
        message.error(response.message || '添加用户失败');
      }
    } catch (error) {
      message.error('添加用户失败');
    }
  };

  // 编辑用户
  const handleEditUser = async (values: any) => {
    try {
      const response = await updateUser(currentUser!.id, values);
      if (response.success) {
        message.success('编辑用户成功');
        setEditDrawerVisible(false);
        // 刷新表格
        if (actionRef.current) {
          actionRef.current.reload();
        }
      } else {
        message.error(response.message || '编辑用户失败');
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
              // 刷新表格
              if (actionRef.current) {
                actionRef.current.reload();
              }
              resolve(undefined);
            } else {
              message.error(response.message || '删除用户失败');
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
  const showEditDrawer = async (record: User) => {
    try {
      // 先设置加载状态
      setLoading(true);
      // 调用用户详情接口获取最新数据
      const response = await getUserDetails(record.id);
      if (response.success) {
        console.log('获取用户详情成功:', response.data);
        setCurrentUser(response.data);
        setEditDrawerVisible(true);
      } else {
        message.error(response.message || '获取用户详情失败');
        // 如果获取详情失败，仍然使用列表中的数据作为回退
        console.warn('使用列表中的用户数据作为回退');
        setCurrentUser(record);
        setEditDrawerVisible(true);
      }
    } catch (error) {
      message.error('获取用户详情失败');
      console.error('获取用户详情异常:', error);
      // 如果发生异常，仍然使用列表中的数据作为回退
      setCurrentUser(record);
      setEditDrawerVisible(true);
    } finally {
      // 无论成功失败，都要设置加载状态为false
      setLoading(false);
    }
  };

  // 列配置
  const columns: ProColumns<User>[] = [
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      valueType: 'text',
      sorter: true,
      hideInSearch: false,
    },
    {
      title: '用户根目录',
      dataIndex: 'userRootPath',
      key: 'userRootPath',
      valueType: 'text',
      hideInSearch: true,
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
      hideInSearch: true,
    },
    {
      title: 's3存储链接',
      dataIndex: 's3Link',
      key: 's3Link',
      valueType: 'text',
      render: (_, record) => {
        // 只有当存储类型为s3时才显示s3链接
        return record.storageType === 's3' ? record.s3Link : '-';
      },
      hideInSearch: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      valueType: 'dateTime',
      hideInSearch: true,
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

      <ProTable<User>
        columns={columns}
        actionRef={actionRef}
        request={fetchUsers}
        rowKey="id"
        loading={loading}
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
          formIdPrefix="create-user-form"
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
            formIdPrefix="edit-user-form"
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