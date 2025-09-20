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
    // 禁用错误抛出，让业务代码自己处理错误
    errorThrower: () => {
      // 不抛出错误，让业务代码自己处理
    },
    // 错误接收及处理
    errorHandler: (error: any, opts: any) => {
      if (opts?.skipErrorHandler) throw error;
      // 只处理网络错误和请求错误，不处理业务逻辑错误
      if (error.name === 'BizError') {
        // 不在这里显示业务错误信息，让业务代码自己处理
        return;
      } else if (error.response) {
        // Axios 的错误
        // 请求成功发出且服务器也响应了状态码，但状态代码超出了 2xx 的范围
        message.error(`网络请求错误: ${error.response.status}`);
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
      // 从 localStorage 获取 token 信息
      const token = localStorage.getItem('token');
      const tokenType = localStorage.getItem('token_type');
      
      // 拦截请求配置，添加认证信息
      if (token && tokenType) {
        // 添加认证头部
        const headers = {
          ...config.headers,
          Authorization: `${tokenType} ${token}`,
        };
        
        return { ...config, headers };
      }
      
      return config;
    },
  ],

  // 响应拦截器
  responseInterceptors: [
    (response) => {
      // 拦截响应数据，进行个性化处理
      const { data, status } = response;

      // 处理 401 未授权错误
      if (status === 401) {
        // 清除存储的用户信息
        localStorage.removeItem('userInfo');
        // 重定向到登录页面
        window.location.href = '/user/login';
      }

      // 不在这里处理业务错误，让业务代码自己处理
      
      return response;
    },
  ],
};
