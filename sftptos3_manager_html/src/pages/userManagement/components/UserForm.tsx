import React, { useState, useEffect, forwardRef, useImperativeHandle } from 'react';
import { Form, Input, Select, Button, Space, Alert } from 'antd';
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
          value: item.id.toString(), // 确保value是字符串类型
          label: item.s3Name
        }));
        setS3Links(links);
        console.log('获取S3链接成功，返回数据:', response.data);
        console.log('转换后的S3链接选项:', links);
        
        // 如果当前是编辑模式且存储类型为s3，检查并纠正s3Link值
        if (initialValues && initialValues.storageType === 's3' && initialValues.s3Link) {
          // 检查initialValues中的s3Link值
          console.log('检查initialValues中的s3Link值:', initialValues.s3Link);
          
          // 检查s3Link是否已存在于s3Links列表中
          const isValidS3Link = links.some(link => link.value === initialValues.s3Link.toString());
          
          if (!isValidS3Link) {
            console.warn('s3Link值不存在于S3链接列表中:', initialValues.s3Link);
            // 尝试查找与s3Link值相关的S3配置
            const matchingS3Link = links.find(link => 
              link.label === initialValues.s3Link || 
              link.value === initialValues.s3Link.toString() ||
              link.value === initialValues.s3Link
            );
            
            if (matchingS3Link) {
              // 如果找到了匹配项，使用其ID
              console.log('找到匹配的S3链接，ID:', matchingS3Link.value, '名称:', matchingS3Link.label);
              const updatedValues = { ...initialValues, s3Link: matchingS3Link.value };
              form.setFieldsValue(updatedValues);
              console.log('已自动纠正并设置s3Link值:', updatedValues);
            } else {
              // 如果没有找到匹配项，设置表单值但保留原始s3Link值
              console.log('未找到匹配的S3链接，使用原始值:', initialValues.s3Link);
              form.setFieldsValue(initialValues);
            }
          } else {
            console.log('s3Link值有效，直接设置表单值:', initialValues);
            form.setFieldsValue(initialValues);
          }
        }
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
      console.log('initialValues发生变化:', initialValues);
      // 处理s3Link值，确保与下拉框选项匹配
      const formattedValues = { ...initialValues };
      
      // 确保s3Link字段存在且有值时，先获取S3链接列表
      if (formattedValues.storageType === 's3' && formattedValues.s3Link) {
        console.log('检测到存储类型为s3且s3Link有值，准备获取S3链接列表...');
        // 先获取S3链接列表
        fetchS3Links();
        // 注意：表单值的设置现在移到了fetchS3Links函数内部，确保在数据加载完成后再设置
      } else {
        // 当存储类型不是s3或s3Link没有值时，直接设置表单值
        console.log('存储类型不是s3或s3Link没有值，直接设置表单值:', formattedValues);
        form.setFieldsValue(formattedValues);
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
         tooltip="请使用对应操作系统的路径格式，s3存储根目录为空可以设置为 / ,如果存在真实根目录可以设置 dir/dir1/ 即可"
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
                // 添加onBlur事件监听，用于调试和日志记录
                onBlur={(value) => {
                  console.log('s3Link输入框失焦，当前值:', value);
                }}
                // 添加showSearch属性，允许用户搜索选项
                showSearch
                // 自定义选项过滤逻辑，提高匹配成功率
                filterOption={(input, option) =>
                  option?.label.toLowerCase().includes(input.toLowerCase()) ||
                  option?.value.toString().includes(input)
                }
                // 设置maxTagCount为0，确保始终显示完整选项
                maxTagCount={0}
              >
                {/* 渲染S3链接选项 */}
                {s3Links.map(link => (
                  <Option key={link.value} value={link.value}>{link.label}</Option>
                ))}
                {/* 如果initialValues中有s3Link值但不在选项列表中，添加一个临时选项 */}
                {initialValues && initialValues.s3Link && !s3Links.some(link => link.value === initialValues.s3Link.toString()) && (
                  <Option key="temp-option" value={initialValues.s3Link.toString()}>
                    {`[系统值] ${initialValues.s3Link}`}
                  </Option>
                )}
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