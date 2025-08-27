# 科研KPI成果收集系统

## 项目简介

这是一个基于Vue3 + Element Plus的科研KPI成果收集系统前端项目，支持普通用户和管理员两种角色。

## 功能特性

### 普通用户功能
- 登录系统
- 提交KPI成果（包含三级联动下拉选择）
- 保存草稿
- 查看提交记录
- 修改密码
- 退出登录

### 管理员功能
- 成果审核（查看、通过、退回）
- 成果统计（提交量概览、考核领域统计、负责人统计）
- 成果导出（条件查询、Excel导出）
- 用户管理（增删改查、批量导入）

## 技术栈

- **前端框架**: Vue 3
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **图标库**: @element-plus/icons-vue

## 项目结构

```
src/
├── api/                 # API接口
│   ├── index.js        # API配置
│   ├── user.js         # 用户相关接口
│   ├── admin.js        # 管理员相关接口
│   └── common.js       # 公共接口
├── components/          # 公共组件
│   └── AdminLayout.vue # 管理员布局组件
├── router/             # 路由配置
│   └── index.js       # 路由定义
├── views/              # 页面组件
│   ├── Login.vue       # 登录页面
│   ├── UserHome.vue    # 普通用户首页
│   └── admin/          # 管理员页面
│       ├── Audit.vue   # 成果审核
│       ├── Stats.vue   # 成果统计
│       ├── Export.vue  # 成果导出
│       └── Users.vue   # 用户管理
├── App.vue             # 根组件
└── main.js             # 入口文件
```

## 安装和运行

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

## 开发说明

### 代理配置
项目已配置代理到本地8080端口，所有`/api`开头的请求会自动转发到`http://localhost:8080`。

### 三级联动
考核领域、关键指标、评价标准采用三级联动设计，选择上级选项后会自动加载下级选项。

### 状态管理
- 0: 草稿
- 1: 审核中
- 2: 审核通过
- 3: 已退回

### 评价标准显示
超过20个字符的评价标准会显示省略号，点击"展开"按钮可查看完整内容。

## 接口规范

所有接口返回格式统一为：
```json
{
  "code": 1,        // 1表示成功，0表示失败
  "msg": "success", // 响应消息
  "data": {}        // 响应数据
}
```

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT License
