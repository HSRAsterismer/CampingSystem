# Camping System

基于 Spring Boot + Vite React 的野外露营点与生态观察记录平台。

## 目录结构
- `server`：后端工程
- `frontend`：前端工程
- `docs/详细设计.md`：详细设计文档
- `db/camping_system.sql`：MySQL 初始化脚本

## 技术栈
- 后端：Java 17、Spring Boot、Spring Data JPA、MySQL
- 前端：Vite、React JSX、Ant Design、MobX、ECharts、Leaflet

## 数据库配置
- 主机：`127.0.0.1`
- 端口：`3306`
- 用户：`root`
- 密码：`rustgopy`
- 数据库：`camping_system`

## 启动方式
### 1. 初始化数据库
可选执行：

```sql
source db/camping_system.sql;
```

后端也已开启 `createDatabaseIfNotExist=true` 与自动种子数据逻辑，首次启动若库为空会自动建表并插入演示数据。

### 2. 启动后端

```powershell
cd server
.\mvnw.cmd spring-boot:run
```

默认地址：`http://127.0.0.1:8080`

### 3. 启动前端

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

默认地址：`http://127.0.0.1:5173`

## 演示账号
- 管理员：`admin / admin123`
- 露营用户：`camper / camper123`
- 生态观察员：`eco / eco123`

## 已实现功能
- 露营点管理
- 地图查询
- 在线预订
- 防刷单管控
- 支付结算
- 生态观察记录
- 数据可视化驾驶舱
