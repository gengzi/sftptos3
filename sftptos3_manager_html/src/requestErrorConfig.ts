import type { RequestConfig, RequestOptions } from '@umijs/max';
import { message, notification } from 'antd';

// 错误处理方案： 错误类型
enum ErrorShowType {
  SILENT = 0,
  WARN_MESSAGE = 1,
  ERROR_MESSAGE = 2,
  NOTIFICATION = 3,
  REDIRECT = 9,
}
// 与后端约定的响应数据格式
interface ResponseStructure {
  success: boolean;
  data: any;
  code?: number;
  message?: string;
  showType?: ErrorShowType;
}

/**
 * @name 错误处理
 * pro 自带的错误处理， 可以在这里做自己的改动
 * @doc https://umijs.org/docs/max/request#配置
 */
export const errorConfig: RequestConfig = {
  // 错误处理： umi@3 的错误处理方案。

  errorConfig: {
    // 错误抛出，让业务代码能捕获到原始错误
    errorThrower: (error) => {
      console.log('ErrorThrower called with error:', error);
      throw error;
    },
    // 错误接收及处理
    errorHandler: (error: any, opts: any) => {
      console.log('ErrorHandler called with error:', error);
      if (opts?.skipErrorHandler) throw error;
      
      // 处理网络错误和请求错误
      if (error.response) {
        // Axios 的错误 - 请求成功发出且服务器也响应了状态码
        console.log('Error response status:', error.response.status);
        
        // 专门处理401/403错误，确保能重定向到登录页
        if (error.response.status === 401 || error.response.status === 403) {
          console.log('Processing 401/403 error in errorHandler, clearing auth data and redirecting');
          
          try {
            // 清除存储的用户信息和认证数据
            localStorage.removeItem('userInfo');
            localStorage.removeItem('token');
            localStorage.removeItem('token_type');
            
            console.log('Auth data cleared successfully');
            
            // 显示错误提示
            message.error('登录已过期或权限不足，请重新登录');
            
            // 延迟重定向，确保用户看到错误提示
            setTimeout(() => {
              console.log('Redirecting to login page after 1000ms delay');
              window.location.href = '/user/login';
            }, 1000);
          } catch (handlerError) {
            console.error('Error during 401/403 error handling:', handlerError);
          }
        } else {
          message.error(`网络请求错误: ${error.response.status}`);
        }
      } else if (error.request) {
        // 请求已经成功发起，但没有收到响应
        message.error('服务器无响应，请重试');
      } else {
        // 发送请求时出了点问题
        message.error('请求错误，请重试');
      }
    },
  },

  // 请求拦截器
  requestInterceptors: [
    (config: RequestOptions) => {
      console.log('Request Interceptor - URL:', config.url);
      // 从 localStorage 获取 token 信息
      const token = localStorage.getItem('token');
      const tokenType = localStorage.getItem('token_type');
      
      console.log('Token available:', !!token, 'Token type available:', !!tokenType);
      
      // 拦截请求配置，添加认证信息
      if (token && tokenType) {
        // 添加认证头部
        const headers = {
          ...config.headers,
          Authorization: `${tokenType} ${token}`,
        };
        
        console.log('Authorization header added:', `${tokenType} ${token.substring(0, 10)}...`);
        return { ...config, headers };
      }
      
      return config;
    },
  ],

  // 响应拦截器 - 处理成功响应
  responseInterceptors: [
    (response) => {
      // 拦截响应数据，进行个性化处理
      const { data, status, config } = response;
      console.log('Response Interceptor - URL:', config?.url, 'Status:', status);

      return response;
    },
  ],
};
