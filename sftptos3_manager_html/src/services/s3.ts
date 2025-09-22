import { request } from '@umijs/max';

// S3存储配置接口
export interface S3Config {
  id?: string;
  s3Name: string;
  endpoint: string;
  accessKey: string;
  accessSecret: string;
  bucket: string;
  region?: string;
}

// 创建S3存储配置
export async function createS3Storage(params: S3Config) {
  try {
    const response = await request('/s3/storage/create', {
      method: 'POST',
      data: params,
    });
    
    // 处理响应格式
    if (response && (response.success || response.code === 200)) {
      return {
        success: true,
        data: response,
      };
    } else {
      return {
        success: false,
        message: response.message || '创建S3存储配置失败',
      };
    }
  } catch (error) {
    console.error('创建S3存储配置失败:', error);
    return {
      success: false,
      message: '创建S3存储配置失败',
    };
  }
}

// 获取S3存储配置列表
export async function queryS3Storages(params?: { s3Name?: string; page?: number; size?: number; sort?: string }) {
  try {
    const response = await request('/s3/storage/list', {
      method: 'GET',
      params: {
        ...params,
      },
    });
    
    // 处理分页响应格式
    if (response && response.code === 200 && response.success) {
      return {
        success: true,
        data: response.data.content || [],
        total: response.data.totalElements || 0,
      };
    } else {
      return {
        success: false,
        message: response.message || '获取S3存储配置列表失败',
      };
    }
  } catch (error) {
    console.error('获取S3存储配置列表失败:', error);
    return {
      success: false,
      message: '获取S3存储配置列表失败',
    };
  }
}

// 更新S3存储配置
export async function updateS3Storage(params: S3Config) {
  try {
    const response = await request('/s3/storage/update', {
      method: 'POST',
      data: params,
    });
    
    // 处理响应格式
    if (response && (response.success || response.code === 200)) {
      return {
        success: true,
        data: response,
      };
    } else {
      return {
        success: false,
        message: response.message || '更新S3存储配置失败',
      };
    }
  } catch (error) {
    console.error('更新S3存储配置失败:', error);
    return {
      success: false,
      message: '更新S3存储配置失败',
    };
  }
}

// 删除S3存储配置
export async function deleteS3Storage(id: string) {
  try {
    const response = await request('/s3/storage/remove', {
      method: 'POST',
      params: {
        id,
      },
      data: '',
    });
    console.log('删除S3存储配置响应:', response);
    // 处理响应格式
    if (response && (response.success || response.code === 200)) {
      return {
        success: true,
        data: response,
      };
    } else {
      // 确保正确传递后端返回的错误信息
      // 特别是code为1007且message为"当前配置正在使用中"的情况
      return {
        success: false,
        message: response.message || '删除S3存储配置失败',
        code: response.code,
      };
    }
  } catch (error) {
    console.error( error);
    return {
      success: false,
      message: error.message || '删除S3存储配置失败',
      code: error.code,
    };
  }
}

// 获取S3名称列表
export async function getS3Names() {
  try {
    const response = await request('/s3/storage/get/s3names', {
      method: 'POST',
    });
    
    // 处理响应格式
    if (response && response.code === 200 && response.success) {
      // 返回完整的对象数组，包含id和s3Name字段
      return {
        success: true,
        data: response.data || [],
      };
    } else {
      return {
        success: false,
        message: response.message || '获取S3名称列表失败',
      };
    }
  } catch (error) {
    console.error('获取S3名称列表失败:', error);
    return {
      success: false,
      message: '获取S3名称列表失败',
    };
  }
}