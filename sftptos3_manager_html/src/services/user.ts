import { request } from '@umijs/max';

// Mock数据，实际项目中应替换为真实API调用
const mockUsers = [
  {
    id: '1',
    username: 'admin',
    email: 'admin@example.com',
    passwd: 'admin123', // 实际项目中应使用加密存储
    access: 'admin',
    userRootPath: '/admin',
    storageType: 'local',
    s3Link: '',
    createdAt: '2023-01-01T00:00:00Z',
  },
  {
    id: '2',
    username: 'user1',
    email: 'user1@example.com',
    passwd: 'user123', // 实际项目中应使用加密存储
    access: 'user',
    userRootPath: '/users/user1',
    storageType: 's3',
    s3Link: 's3://bucket1',
    createdAt: '2023-01-02T00:00:00Z',
  },
  {
    id: '3',
    username: 'user2',
    email: 'user2@example.com',
    passwd: 'user123', // 实际项目中应使用加密存储
    access: 'user',
    userRootPath: '/users/user2',
    storageType: 'local',
    s3Link: '',
    createdAt: '2023-01-03T00:00:00Z',
  },
];

// 获取用户列表
export async function queryUsers() {
  try {
    // 实际项目中使用真实API调用
    // const response = await request('/api/users', { method: 'GET' });
    
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 返回mock数据
    return {
      success: true,
      data: mockUsers,
    };
  } catch (error) {
    console.error('获取用户列表失败:', error);
    return {
      success: false,
      message: '获取用户列表失败',
    };
  }
}

// 创建用户
export async function createUser(params: any) {
  try {
    // 调用真实的API接口
    const response = await request('http://127.0.0.1:9977/api/user/create', {
      method: 'POST',
      headers: {
        'accept': '*/*',
        'Content-Type': 'application/json',
      },
      data: {
        username: params.username,
        passwd: params.passwd,
        userRootPath: params.userRootPath,
        accessStorageType: params.storageType,
        accessStorageInfo: params.s3Link || '',
      },
    });
    
    // 根据响应返回结果
    if (response.success || response.code === 200) {
      return {
        success: true,
        data: response.data,
      };
    } else {
      return {
        success: false,
        message: response.message || '创建用户失败',
      };
    }
  } catch (error) {
    console.error('创建用户失败:', error);
    return {
      success: false,
      message: '创建用户失败',
    };
  }
}

// 更新用户
export async function updateUser(id: string, params: any) {
  try {
    // 实际项目中使用真实API调用
    // const response = await request(`/api/users/${id}`, { method: 'PUT', data: params });
    
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 模拟更新成功
    const index = mockUsers.findIndex(user => user.id === id);
    if (index !== -1) {
      mockUsers[index] = {
        ...mockUsers[index],
        ...params,
        // 不更新创建时间
        createdAt: mockUsers[index].createdAt,
      };
      
      return {
        success: true,
        data: mockUsers[index],
      };
    } else {
      return {
        success: false,
        message: '用户不存在',
      };
    }
  } catch (error) {
    console.error('更新用户失败:', error);
    return {
      success: false,
      message: '更新用户失败',
    };
  }
}

// 删除用户
export async function deleteUser(id: string) {
  try {
    // 实际项目中使用真实API调用
    // const response = await request(`/api/users/${id}`, { method: 'DELETE' });
    
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // 模拟删除成功
    const index = mockUsers.findIndex(user => user.id === id);
    if (index !== -1) {
      mockUsers.splice(index, 1);
      return {
        success: true,
      };
    } else {
      return {
        success: false,
        message: '用户不存在',
      };
    }
  } catch (error) {
    console.error('删除用户失败:', error);
    return {
      success: false,
      message: '删除用户失败',
    };
  }
}