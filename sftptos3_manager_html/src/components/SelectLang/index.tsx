import { Select } from 'antd';
import { useIntl } from '@umijs/max';
import { setLocale } from 'umi';
import React, { useEffect } from 'react';
import './index.less';

// 语言选项
const LANGS = [
  {
    label: '中文',
    value: 'zh-CN',
  },
  {
    label: 'English',
    value: 'en-US',
  },
];

const SelectLang: React.FC = () => {
  const intl = useIntl();
  const [currentLang, setCurrentLang] = React.useState('zh-CN');

  // 从 localStorage 中获取当前语言设置
  useEffect(() => {
    const lang = localStorage.getItem('umi_locale') || 'zh-CN';
    setCurrentLang(lang);
  }, []);

  // 处理语言切换
  const handleLangChange = (value: string) => {
    setCurrentLang(value);
    // 设置 umi 的语言
    setLocale(value);
    // 存储到 localStorage
    localStorage.setItem('umi_locale', value);
    // 刷新页面以应用新语言
    window.location.reload();
  };

  return (
    <div className="select-lang">
      <Select
        size="small"
        className="select-lang-select"
        value={currentLang}
        onChange={handleLangChange}
        options={LANGS}
        style={{
          width: 100,
        }}
      />
    </div>
  );
};

export default SelectLang;