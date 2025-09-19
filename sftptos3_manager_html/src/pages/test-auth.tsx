import { Button, Card, Typography, message } from 'antd';
import { history, useModel } from '@umijs/max';
import { LogoutOutlined } from '@ant-design/icons';
import React from 'react';

const { Title, Paragraph } = Typography;

const TestAuthPage: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  const handleLogout = () => {
    // 清除存储的用户信息
    localStorage.removeItem('userInfo');
    
    // 更新全局状态
    setInitialState((s) => ({
      ...s,
      currentUser: undefined,
    }));
    
    message.success('已成功登出');
    
    // 重定向到登录页面
    history.push('/user/login');
  };

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Title level={3}>认证测试页面</Title>
        
        {currentUser ? (
          <div>
            <Paragraph>你已成功登录系统！</Paragraph>
            <Paragraph>用户信息：</Paragraph>
            <ul>
              <li>用户名：{currentUser.name}</li>
              <li>用户ID：{currentUser.userid}</li>
              <li>邮箱：{currentUser.email}</li>
              <li>角色：{currentUser.access}</li>
            </ul>
            <Button 
              type="primary" 
              danger 
              icon={<LogoutOutlined />} 
              onClick={handleLogout}
            >
              登出
            </Button>
          </div>
        ) : (
          <Paragraph>未登录，请先登录系统。</Paragraph>
        )}
      </Card>
    </div>
  );
};

export default TestAuthPage;