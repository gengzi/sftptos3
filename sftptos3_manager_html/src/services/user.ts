import { request } from '@umijs/max';

// Mock数据，实际项目中应替换为真实API调用
const mockUsers = [
  {
    id: '1',
    username: 'admin',
    email: 'admin@example.com',
    passwd: 'admin123',
    access: 'admin',
    userRootPath: '/admin',
    storageType: 'local',
    s3Link: '',
    clientPublicKey: 'sample-public-key-for-admin',
    createdAt: '2023-01-01T00:00:00Z',
  },
  {
    id: '2',
    username: 'user1',
    email: 'user1@example.com',
    passwd: 'user123',
    access: 'user',
    userRootPath: '/users/user1',
    storageType: 's3',
    s3Link: 's3://bucket1',
    clientPublicKey: 'sample-public-key-for-user1',
    createdAt: '2023-01-02T00:00:00Z',
  },
  {
    id: '3',
    username: 'user2',
    email: 'user2@example.com',
    passwd: 'user123',
    access: 'user',
    userRootPath: '/users/user2',
    storageType: 'local',
    s3Link: '',
    clientPublicKey: '',
    createdAt: '2023-01-03T00:00:00Z',
  },
];

// 获取用户列表
export async function queryUsers(params?: { username?: string; current?: number; pageSize?: number }) {
  try {
    // 调用真实的API接口
    const response = await request('/api/user/list', {
      method: 'GET',
      headers: {
        'accept': '*/*',
      },
      params: {
        username: params?.username,
        page: params?.current ? params.current - 1 : 0,
        size: params?.pageSize || 10,
        sort: 'createTime',
      },
    });
    
    // 根据响应返回结果
    if (response.success || response.code === 200) {
      // 转换后端返回的用户数据格式，使其符合前端需求
      const transformedData = response.data.content.map((user: any) => ({
        id: user.id.toString(),
        username: user.username,
        userRootPath: user.userRootPath,
        storageType: user.accessStorageType,
        s3Link: user.accessStorageType === 's3' ? user.accessStorageInfo : '',
        clientPublicKey: user.secretKey || '',
        createdAt: user.createTime,
      }));
      
      return {
        success: true,
        data: transformedData,
        total: response.data.totalElements,
      };
    } else {
      // 当code非200时，返回后端提供的错误信息
      return {
        success: false,
        code: response.code,
        message: response.message || '获取用户列表失败',
      };
    }
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
    const response = await request('/api/user/create', {
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
        clientPublicKey: params.clientPublicKey || '',
      },
    });
    
    // 根据响应返回结果
    if (response.success || response.code === 200) {
      return {
        success: true,
        data: response.data,
      };
    } else {
      // 当code非200时，返回后端提供的错误信息
      return {
        success: false,
        code: response.code,
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
    // 调用真实的API接口
    const response = await request('/api/user/update', {
      method: 'POST',
      headers: {
        'accept': '*/*',
        'Content-Type': 'application/json',
      },
      data: {
        id: parseInt(id), // 转换为数字类型
        username: params.username,
        passwd: params.passwd || '', // 允许不修改密码
        userRootPath: params.userRootPath,
        accessStorageType: params.storageType,
        accessStorageInfo: params.storageType === 's3' ? params.s3Link : '',
        secretKey: params.clientPublicKey || '',
      },
    });
    
    // 根据响应返回结果
    if (response.success || response.code === 200) {
      return {
        success: true,
        data: response.data,
      };
    } else {
      // 当code非200时，返回后端提供的错误信息
      return {
        success: false,
        code: response.code,
        message: response.message || '更新用户失败',
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

// 获取用户详情
export async function getUserDetails(id: string) {
  try {
    // 调用真实的API接口
    const response = await request('/api/user/details', {
      method: 'GET',
      headers: {
        'accept': '*/*',
      },
      params: {
        id: id,
      },
    });
    
    // 根据响应返回结果
    if (response.success || response.code === 200) {
      // 转换后端返回的用户数据格式，使其符合前端需求
      const userData = {
        id: response.data.id.toString(),
        username: response.data.username,
        userRootPath: response.data.userRootPath,
        storageType: response.data.accessStorageType,
        s3Link: response.data.accessStorageType === 's3' ? response.data.accessStorageInfo : '',
        clientPublicKey: response.data.secretKey || '',
        createdAt: response.data.createTime,
      };
      
      return {
        success: true,
        data: userData,
      };
    } else {
      // 当code非200时，返回后端提供的错误信息
      return {
        success: false,
        code: response.code,
        message: response.message || '获取用户详情失败',
      };
    }
  } catch (error) {
    console.error('获取用户详情失败:', error);
    return {
      success: false,
      message: '获取用户详情失败',
    };
  }
}

// 删除用户
export async function deleteUser(id: string) {
  try {
    // 调用真实的API接口，使用POST请求到/api/user/remove
    const response = await request('/api/user/remove', {
      method: 'POST',
      headers: {
        'accept': '*/*',
      },
      params: {
        id: id,
      },
      data: '', // 空的请求体
    });
    
    // 根据响应返回结果
    if (response.success || response.code === 200) {
      return {
        success: true,
        data: response.data,
      };
    } else {
      // 当code非200时，返回后端提供的错误信息
      return {
        success: false,
        code: response.code,
        message: response.message || '删除用户失败',
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