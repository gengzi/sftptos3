import React from 'react';
import { Modal, Form, Input, Button, Space, Alert } from 'antd';

interface S3FormProps {
  visible: boolean;
  initialValues?: any;
  onFinish: (values: any) => void;
  onCancel: () => void;
}

const S3Form: React.FC<S3FormProps> = ({
  visible,
  initialValues,
  onFinish,
  onCancel,
}) => {
  const [form] = Form.useForm();

  React.useEffect(() => {
    if (visible) {
      if (initialValues) {
        form.setFieldsValue(initialValues);
      } else {
        form.resetFields();
      }
    }
  }, [visible, initialValues, form]);

  const handleFinish = (values: any) => {
    onFinish(values);
    form.resetFields();
  };

  return (
    <Modal
      title={initialValues ? '编辑S3存储' : '添加S3存储'}
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={600}
    >
      <Form
          form={form}
          onFinish={handleFinish}
          labelCol={{ span: 6 }}
          wrapperCol={{ span: 16 }}
        >
          {initialValues && (
            <Alert
              message="配置生效说明"
              description="更新的配置信息需用户重新连接SFTP服务才会生效，存量正在连接的用户将继续按照既往配置运行。"
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
        <Form.Item
          label="名称标识"
          name="s3Name"
          rules={[{ required: true, message: '请输入名称标识!' }]}
        >
          <Input 
            placeholder="请输入S3存储的名称标识" 
            disabled={!!initialValues} 
          />
        </Form.Item>

        <Form.Item
          label="请求地址"
          name="endpoint"
          rules={[{ required: true, message: '请输入请求地址!' }]}
        >
          <Input placeholder="请输入S3请求地址，例如：https://s3.amazonaws.com" />
        </Form.Item>

        <Form.Item
          label="账户"
          name="accessKey"
          rules={[{ required: true, message: '请输入账户!' }]}
        >
          <Input placeholder="请输入访问密钥ID" />
        </Form.Item>

        <Form.Item
          label="密码"
          name="accessSecret"
          rules={[{ required: true, message: '请输入密码!' }]}
        >
          <Input.Password placeholder="请输入秘密访问密钥" />
        </Form.Item>

        <Form.Item
          label="桶"
          name="bucket"
          rules={[{ required: true, message: '请输入桶名称!' }]}
        >
          <Input placeholder="请输入S3桶名称" />
        </Form.Item>

        <Form.Item
          label="Region"
          name="region"
          tooltip="指定API请求的地区，例如: us-east-1"
        >
          <Input placeholder="请输入地区" />
        </Form.Item>

        <Form.Item wrapperCol={{ offset: 6, span: 16 }}>
          <Space>
            <Button type="primary" htmlType="submit">
              {initialValues ? '更新' : '添加'}
            </Button>
            <Button onClick={onCancel}>取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default S3Form;