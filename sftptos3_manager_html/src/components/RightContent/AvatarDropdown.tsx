import { LogoutOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Button } from 'antd';
import React from 'react';
import { flushSync } from 'react-dom';
import { outLogin } from '@/services/ant-design-pro/api';

/**
 * 退出登录按钮组件
 */
export const LogoutButton: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  
  /**
   * 退出登录，并且将当前的 url 保存
   */
  const loginOut = async () => {
    try {
      // 尝试调用退出登录API
      await outLogin();
    } catch (error) {
      console.error('退出登录API调用失败:', error);
      // 即使API调用失败，仍然继续退出登录流程
    }
    
    // 清除localStorage中的用户信息
    localStorage.removeItem('token');
    localStorage.removeItem('token_type');
    localStorage.removeItem('username');
    localStorage.removeItem('userInfo');
    
    flushSync(() => {
      setInitialState((s) => ({ ...s, currentUser: undefined }));
    });
    
    const { search, pathname } = window.location;
    const urlParams = new URL(window.location.href).searchParams;
    const searchParams = new URLSearchParams({
      redirect: pathname + search,
    });
    
    /** 此方法会跳转到 redirect 参数所在的位置 */
    const redirect = urlParams.get('redirect');
    // Note: There may be security issues, please note
    if (window.location.pathname !== '/user/login' && !redirect) {
      // 使用replace而不是push，避免用户可以通过浏览器返回按钮回到已退出的页面
      history.replace({
        pathname: '/user/login',
        search: searchParams.toString(),
      });
    }
    
    // 强制刷新页面，确保所有状态都被重置
    window.location.reload();
  };
  
  return (
    <Button 
      type="text" 
      icon={<LogoutOutlined />} 
      onClick={loginOut}
    >
      退出登录
    </Button>
  );
};

export default LogoutButton;
