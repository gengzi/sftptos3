import React from 'react';
import { Card, Typography } from 'antd';
import { SmileOutlined } from '@ant-design/icons';

const { Title, Paragraph } = Typography;

const IndexPage: React.FC = () => {
  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Title level={2} style={{ textAlign: 'center' }}>
          <SmileOutlined /> 欢迎使用 Ant Design Pro
        </Title>
        <Paragraph style={{ textAlign: 'center', marginTop: '24px' }}>
          这是一个测试页面，用于检查应用是否正常运行。
        </Paragraph>
      </Card>
    </div>
  );
};

export default IndexPage;