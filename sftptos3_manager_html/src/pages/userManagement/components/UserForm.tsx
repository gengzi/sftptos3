import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { Form, Input, Select, Button, Space } from 'antd';
import type { RuleObject } from 'antd/es/form';
import { getS3Names } from '@/services/s3';

const { Option } = Select;

interface UserFormProps {
  initialValues?: any;
  onFinish: (values: any) => void;
  onCancel: () => void;
  formIdPrefix?: string;
}

const UserForm = forwardRef(({ initialValues, onFinish, onCancel, formIdPrefix = 'user-form' }: UserFormProps, ref) => {
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
        console.log('获取S3链接成功:', links);
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

  // 专门监听initialValues变化，确保编辑模式下表单值正确更新
  useEffect(() => {
    if (initialValues) {
      // 处理s3Link值，确保与下拉框选项匹配
      const formattedValues = { ...initialValues };
      
      // 确保s3Link字段存在且有值时，先获取S3链接列表
      if (formattedValues.storageType === 's3' && formattedValues.s3Link) {
        // 先获取S3链接列表，然后再设置表单值
        fetchS3Links().then(() => {
          form.setFieldsValue(formattedValues);
          console.log('表单值已更新:', formattedValues);
        });
      } else {
        // 当initialValues存在且发生变化时，使用setFieldsValue更新表单值
        form.setFieldsValue(formattedValues);
        console.log('表单值已更新:', formattedValues);
      }
    }
  }, [initialValues, form]);

  // 为了安全考虑，密码字段在编辑模式下不回显是正常的
  // 这是一个安全最佳实践，防止密码泄露

  // 组件挂载时，如果是添加用户模式(没有initialValues)，强制重置表单避免自动填充
  useEffect(() => {
    if (!initialValues) {
      // 重置表单，确保添加用户时表单是空的
      form.resetFields();
      console.log('表单已重置');
    }
  }, [initialValues, form]);

  // 监听存储类型变化，确保能获取S3链接
  useEffect(() => {
    // 检查当前的存储类型或初始值中的存储类型
    const storageType = form.getFieldValue('storageType') || (initialValues?.storageType);
    
    console.log('存储类型检查:', storageType);
    
    // 如果存储类型为s3，立即获取S3链接
    if (storageType === 's3') {
      console.log('开始获取S3链接...');
      fetchS3Links();
    }
  }, [form]); // 只监听form变化，避免与initialValues的useEffect重复触发

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
      onFinish={(values) => {
        // 先调用父组件的onFinish回调
        onFinish(values);
        // 然后重置表单（仅在编辑模式下）
        if (initialValues) {
          form.resetFields();
          console.log('编辑提交完成，表单已清空');
        }
      }}
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
        <Input placeholder="请输入用户名" id={`${formIdPrefix}-username`} disabled={!!initialValues} />
      </Form.Item>

      <Form.Item
        name="passwd"
        label="密码"
        rules={[
          {
            validator: validatePasswd,
          },
        ]}
        tooltip="添加用户时必须输入密码，编辑用户时密码不填写不会覆盖原密码"
      >
        <Input.Password placeholder="请输入密码" id={`${formIdPrefix}-password`} />
      </Form.Item>

      <Form.Item
        name="clientPublicKey"
        label="客户端公钥"
        tooltip="用于API调用认证的客户端公钥"
      >
        <Input.TextArea rows={4} placeholder="请输入客户端公钥" id={`${formIdPrefix}-clientPublicKey`} />
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
        <Input placeholder="请输入用户根目录路径" id={`${formIdPrefix}-userRootPath`} />
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
            console.log('存储类型变更为:', value);
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
          const storageType = getFieldValue('storageType');
          console.log('当前存储类型:', storageType);
          
          return storageType === 's3' ? (
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