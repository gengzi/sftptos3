import React from 'react';
import { Modal, Form, Input, Button, Space, Alert } from 'antd';
import { useIntl } from '@umijs/max';

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
  const intl = useIntl();

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
      title={initialValues ? intl.formatMessage({ id: 'pages.s3Management.editModalTitle' }) : intl.formatMessage({ id: 'pages.s3Management.addModalTitle' })}
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
              message={intl.formatMessage({ id: 'pages.s3Management.configEffectDescription' })}
              description={intl.formatMessage({ id: 'pages.s3Management.configEffectDetails' })}
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
          )}
        <Form.Item
          label={intl.formatMessage({ id: 'pages.s3Management.nameLabel' })}
          name="s3Name"
          rules={[{ required: true, message: intl.formatMessage({ id: 'pages.s3Management.pleaseInputName' }) }]}
        >
          <Input 
            placeholder={intl.formatMessage({ id: 'pages.s3Management.placeholderName' })}
            disabled={!!initialValues} 
          />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({ id: 'pages.s3Management.endpointLabel' })}
          name="endpoint"
          rules={[{ required: true, message: intl.formatMessage({ id: 'pages.s3Management.pleaseInputEndpoint' }) }]}
        >
          <Input placeholder={intl.formatMessage({ id: 'pages.s3Management.placeholderEndpoint' })} />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({ id: 'pages.s3Management.accessKeyLabel' })}
          name="accessKey"
          rules={[{ required: true, message: intl.formatMessage({ id: 'pages.s3Management.pleaseInputAccessKey' }) }]}
        >
          <Input placeholder={intl.formatMessage({ id: 'pages.s3Management.placeholderAccessKey' })} />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({ id: 'pages.s3Management.secretKeyLabel' })}
          name="accessSecret"
          rules={[{ required: true, message: intl.formatMessage({ id: 'pages.s3Management.pleaseInputSecretKey' }) }]}
        >
          <Input.Password placeholder={intl.formatMessage({ id: 'pages.s3Management.placeholderSecretKey' })} />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({ id: 'pages.s3Management.bucketLabel' })}
          name="bucket"
          rules={[{ required: true, message: intl.formatMessage({ id: 'pages.s3Management.pleaseInputBucket' }) }]}
        >
          <Input placeholder={intl.formatMessage({ id: 'pages.s3Management.placeholderBucket' })} />
        </Form.Item>

        <Form.Item
          label={intl.formatMessage({ id: 'pages.s3Management.regionLabel' })}
          name="region"
          tooltip={intl.formatMessage({ id: 'pages.s3Management.regionTooltip' })}
        >
          <Input placeholder={intl.formatMessage({ id: 'pages.s3Management.placeholderRegion' })} />
        </Form.Item>

        <Form.Item wrapperCol={{ offset: 6, span: 16 }}>
          <Space>
            <Button type="primary" htmlType="submit">
              {initialValues ? intl.formatMessage({ id: 'pages.s3Management.updateButtonText' }) : intl.formatMessage({ id: 'pages.s3Management.addButtonText' })}
            </Button>
            <Button onClick={onCancel}>{intl.formatMessage({ id: 'pages.s3Management.cancelButtonText' })}</Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default S3Form;