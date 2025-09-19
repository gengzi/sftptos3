import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { Form, Input, Select, Button, Space } from 'antd';
import type { RuleObject } from 'antd/es/form';
import { getS3Names } from '@/services/s3';

const { Option } = Select;

interface UserFormProps {
  initialValues?: any;
  onFinish: (values: any) => void;
  onCancel: () => void;
}

const UserForm = forwardRef(({ initialValues, onFinish, onCancel }: UserFormProps, ref) => {
  const [form] = Form.useForm();
  const [s3Links, setS3Links] = useState<Array<{ value: string; label: string }>>([]);
  const [loadingS3Links, setLoadingS3Links] = useState<boolean>(false);

  // 暴露表单实例给父组件
  useImperativeHandle(ref, () => ({
    resetFields: () => form.resetFields(),
    setFieldsValue: (values: any) => form.setFieldsValue(values),
  }));


  // 从接口获取s3存储链接
  const fetchS3Links = async () => {
    setLoadingS3Links(true);
    try {
      const response = await getS3Names();
      if (response.success) {
        // 将返回的数据转换为Select选项格式，使用id作为value，s3Name作为label
        const links = response.data.map((item: any) => ({
          value: item.id,
          label: item.s3Name
        }));
        setS3Links(links);
      } else {
        console.error('获取s3存储链接失败:', response.message);
        // 如果获取失败，设置为空数组
        setS3Links([]);
      }
    } catch (error) {
      console.error('获取s3存储链接失败:', error);
      setS3Links([]);
    } finally {
      setLoadingS3Links(false);
    }
  };

  // 监听存储类型变化，当选择s3存储时获取s3链接
  useEffect(() => {
    const storageType = form.getFieldValue('storageType');
    if (storageType === 's3') {
      fetchS3Links();
    }
  }, []);

  // 表单验证规则
  const validatePasswd = (_: RuleObject, value: string) => {
    if (!value && !initialValues) {
      return Promise.reject(new Error('请输入密码'));
    }
    if (value && value.length < 6) {
      return Promise.reject(new Error('密码长度至少为6位'));
    }
    return Promise.resolve();
  };

  // 重置表单
  const handleReset = () => {
    form.resetFields();
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={initialValues}
      onFinish={onFinish}
    >
      <Form.Item
        name="username"
        label="用户名"
        rules={[
          {
            required: true,
            message: '请输入用户名',
          },
          {
            min: 3,
            message: '用户名长度至少为3位',
          },
        ]}
      >
        <Input placeholder="请输入用户名" />
      </Form.Item>



      <Form.Item
        name="passwd"
        label="密码"
        rules={[
          {
            validator: validatePasswd,
          },
        ]}
        tooltip="添加用户时必须输入密码，编辑用户时可选择不修改密码"
      >
        <Input.Password placeholder="请输入密码" />
      </Form.Item>

      <Form.Item
        name="userRootPath"
        label="用户根目录"
        rules={[
          {
            required: true,
            message: '请输入用户根目录',
          },
        ]}
      >
        <Input placeholder="请输入用户根目录路径" />
      </Form.Item>

      <Form.Item
        name="storageType"
        label="存储类型"
        rules={[
          {
            required: true,
            message: '请选择存储类型',
          },
        ]}
      >
        <Select 
          placeholder="请选择存储类型"
          onChange={(value) => {
            if (value === 's3') {
              fetchS3Links();
            } else {
              // 如果切换为其他存储类型，清除s3链接字段的值
              form.setFieldValue('s3Link', undefined);
            
            }
          }}
        >
          <Option value="local">本地文件</Option>
          <Option value="s3">s3存储</Option>
        </Select>
      </Form.Item>

      <Form.Item
        noStyle
        shouldUpdate={(prevValues, curValues) => prevValues.storageType !== curValues.storageType}
      >
        {({ getFieldValue }) => {
          return getFieldValue('storageType') === 's3' ? (
            <Form.Item
              name="s3Link"
              label="s3存储链接"
              rules={[
                {
                  required: true,
                  message: '请选择s3存储链接',
                },
              ]}
            >
              <Select 
                placeholder="请选择s3存储链接"
                loading={loadingS3Links}
              >
                {s3Links.map(link => (
                  <Option key={link.value} value={link.value}>{link.label}</Option>
                ))}
              </Select>
            </Form.Item>
          ) : null;
        }}
      </Form.Item>



      <Form.Item>
        <Space>
          <Button type="primary" htmlType="submit">
            {initialValues ? '更新' : '创建'}
          </Button>
          <Button onClick={handleReset}>
            重置
          </Button>
          <Button onClick={onCancel}>
            取消
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
});

export default UserForm;