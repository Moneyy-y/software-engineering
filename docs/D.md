
# 组员D（后端）任务完成对接文档

---

## 📋 任务完成清单

| 顺序 | 任务编号 | 内容 | 状态 | 对接说明 |
|------|----------|------|------|----------|
| ① | D1、D6 | 用户管理 API + 看板待办细分 VO | ✅ 已完成 | 提供用户 CRUD、列表查询、状态管理接口 |
| ② | D5 | 帖子批量审核、拒绝原因、删帖 | ✅ 已完成 | 支持帖子审核、拒绝、批量操作 |
| ③ | D4、D2、D3 | 红黑榜干预 API + 协同过滤 + 定时任务 | ✅ 已完成 | 红黑榜计算、菜品统计、定时同步 |
| ④ | D7 | Excel 报表导出 | ✅ 已完成 | 支持菜品、评价数据导出 |
| ⑤ | D12、D11 | 图形验证码 + JWT 刷新 | ✅ 已完成 | 登录验证码、Token 刷新机制 |
| ⑥ | D8、D9 | 浏览记录、消息 API | ✅ 已完成 | 浏览历史记录、系统消息推送 |
| ⑦ | D10、D13 | 举报 + RBAC 简化版 | ✅ 已完成 | 举报功能、角色菜单权限控制 |
| ⑧ | D14～D17 | 限流、审计日志、点赞同步、手机号脱敏 | ✅ 已完成 | 系统增强功能 |

---

## 🔧 技术栈

- **框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0 + Redis
- **ORM**: MyBatis Plus
- **认证**: JWT
- **定时任务**: Spring Scheduler

---

## 📡 API 接口汇总

### 1. 用户管理 (D1)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/admin/user/list` | GET | 用户列表（分页） |
| `/api/admin/user/detail/{userId}` | GET | 用户详情 |
| `/api/admin/user/add` | POST | 添加用户 |
| `/api/admin/user/update` | PUT | 更新用户 |
| `/api/admin/user/delete/{userId}` | DELETE | 删除用户 |
| `/api/admin/user/status/{userId}` | PUT | 切换用户状态 |

### 2. 看板数据 (D6)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/dashboard/stats` | GET | 统计概览 |
| `/api/dashboard/todo` | GET | 待办事项 |
| `/api/dashboard/recent` | GET | 最近动态 |

### 3. 帖子管理 (D5)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/admin/post/list` | GET | 帖子列表 |
| `/api/admin/post/approve` | POST | 通过审核 |
| `/api/admin/post/reject` | POST | 拒绝审核（含拒绝原因） |
| `/api/admin/post/delete/{postId}` | DELETE | 删除帖子 |
| `/api/admin/post/batch` | POST | 批量审核 |

### 4. 红黑榜 (D4、D2、D3)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/admin/board/list` | GET | 红黑榜列表 |
| `/api/admin/board/intervene` | POST | 人工干预榜单 |
| `/api/admin/board/calculate` | POST | 手动触发计算 |

### 5. Excel 导出 (D7)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/admin/export/dish` | GET | 导出菜品数据 |
| `/api/admin/export/review` | GET | 导出评价数据 |

### 6. 验证码与认证 (D12、D11)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/admin/captcha` | GET | 获取图形验证码 |
| `/api/admin/login` | POST | 管理员登录 |
| `/api/admin/refresh` | POST | 刷新 Token |
| `/api/user/login` | POST | 用户登录（微信） |

### 7. 浏览记录与消息 (D8、D9)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/user/browse/list` | GET | 获取浏览记录 |
| `/api/user/browse/add` | POST | 添加浏览记录 |
| `/api/user/message/list` | GET | 获取消息列表 |
| `/api/user/message/read/{messageId}` | PUT | 标记已读 |
| `/api/user/message/delete/{messageId}` | DELETE | 删除消息 |

### 8. 举报功能 (D10)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/report/submit` | POST | 提交举报 |
| `/api/report/list` | GET | 举报列表 |
| `/api/report/my` | GET | 我的举报 |
| `/api/report/handle/{reportId}` | PUT | 处理举报 |
| `/api/report/delete/{reportId}` | DELETE | 删除举报 |

### 9. RBAC 权限 (D13)

| API 路径 | 方法 | 功能 |
|----------|------|------|
| `/api/permission/menus` | GET | 当前用户菜单（动态） |
| `/api/permission/current` | GET | 当前用户权限信息 |
| `/api/permission/menus/all` | GET | 所有菜单（管理端） |
| `/api/permission/menus/sub/{parentId}` | GET | 子菜单列表 |

### 10. 限流 (D14)

- **注解**: `@RateLimit(count = 10, seconds = 60)`
- **应用场景**: 评价提交、举报提交等高频接口

### 11. 审计日志 (D15)

- **注解**: `@AuditLog(operation = "操作名称")`
- **记录内容**: 用户ID、操作类型、方法名、参数、IP、时间
- **存储表**: `audit_log`

### 12. 点赞同步 (D16)

- **存储**: Redis Set（去重）
- **同步周期**: 每5分钟
- **同步任务**: `BoardCalculationTask.syncPostLikesFromRedisToDB()`

### 13. 手机号脱敏 (D17)

- **脱敏规则**: 中间4位替换为 `****`
- **示例**: `13800138000` → `138****8000`
- **应用位置**: `UserVO.of()` 方法

---

## 🗄️ 数据库表结构

### 新增/修改表

| 表名 | 说明 | 状态 |
|------|------|------|
| `report` | 举报表 | ✅ 新增 |
| `menu` | 菜单表 | ✅ 新增 |
| `role_menu` | 角色菜单关联表 | ✅ 新增 |
| `audit_log` | 审计日志表 | ✅ 新增 |
| `user` | 用户表（新增mobile字段） | ✅ 修改 |

---

## 🔐 角色权限说明

| 角色 | 权限说明 | 可访问菜单 |
|------|----------|------------|
| `admin` | 系统管理员 | 全部10个菜单 |
| `auditor` | 审核员 | 数据看板、评价审核、帖子审核、敏感词库、举报管理（5个） |
| `student` | 普通用户 | 仅数据看板（1个） |

---

## 🔌 接口调用注意事项

### 1. 认证方式

所有管理端接口需要在 Header 中携带 Token：
```
Authorization: Bearer <token>
```

### 2. 分页参数

列表接口统一分页参数：
- `pageNum`: 页码（从1开始）
- `pageSize`: 每页条数

### 3. 错误码说明

| 错误码 | 说明 |
|--------|------|
| `0` | 成功 |
| `1001` | 未登录 |
| `1002` | 无权限 |
| `3001` | 服务器错误 |
| `429` | 请求过于频繁（限流） |

---

## 📁 项目结构

```
backend/
├── src/main/java/com/catering/
│   ├── controller/          # REST API 控制层
│   ├── service/             # 业务逻辑层
│   ├── mapper/              # 数据访问层
│   ├── entity/              # 数据库实体
│   ├── vo/                  # 视图对象
│   ├── dto/                 # 数据传输对象
│   ├── annotation/          # 自定义注解
│   │   ├── AuditLog.java    # 审计日志注解
│   │   └── RateLimit.java   # 限流注解
│   ├── aspect/              # AOP 切面
│   │   └── AuditLogAspect.java
│   ├── interceptor/         # 拦截器
│   │   └── RateLimitInterceptor.java
│   ├── task/                # 定时任务
│   │   └── BoardCalculationTask.java
│   └── config/              # 配置类
└── src/main/resources/
    ├── application.yml      # 应用配置
    └── mapper/              # MyBatis XML
```

---

## 🚀 启动方式

### 环境要求

- Java 1.8+
- MySQL 8.0+
- Redis 6.0+

### 启动命令

```bash
cd backend
mvn spring-boot:run
```

### 数据库初始化

1. 执行 `sql/schema.sql` 创建表结构
2. 执行 `sql/seed.sql` 插入初始数据

---

## 📞 联系人

- **组员D**: [你的姓名]
- **负责模块**: 后端全部功能

---

## 📝 修改记录与数据说明

### 一、代码修改

| 修改文件 | 修改内容 | 说明 |
|----------|----------|------|
| `task/BoardCalculationTask.java` | 添加点赞同步定时任务 `syncPostLikesFromRedisToDB()` | 每5分钟将Redis点赞数同步到数据库 |
| `aspect/AuditLogAspect.java` | 实现审计日志切面 | 通过`@AuditLog`注解记录管理员操作 |
| `annotation/AuditLog.java` | 新增审计日志注解 | 标记需要记录日志的方法 |
| `annotation/RateLimit.java` | 新增限流注解 | 控制接口访问频率 |
| `interceptor/RateLimitInterceptor.java` | 实现限流拦截器 | 基于Redis实现接口限流 |
| `vo/UserVO.java` | 添加手机号脱敏方法 `maskMobile()` | 返回用户时自动脱敏手机号 |
| `controller/PermissionController.java` | 实现RBAC权限接口 | 动态返回当前用户菜单 |

### 二、数据库修改

| 修改内容 | 文件位置 | 说明 |
|----------|----------|------|
| 新增 `audit_log` 表 | `sql/schema.sql` | 存储审计日志记录 |
| 新增 `report` 表 | `sql/schema.sql` | 存储举报信息 |
| 新增 `menu` 表 | `sql/schema.sql` | 存储系统菜单 |
| 新增 `role_menu` 表 | `sql/schema.sql` | 角色菜单关联 |
| `user` 表新增 `mobile` 字段 | `sql/schema.sql` | 存储用户手机号 |
| 用户初始化数据添加手机号 | `sql/seed.sql` | 便于测试脱敏功能 |

### 三、需要提供给其他组员的数据

#### 1. 测试用户账号

| 角色 | 用户名 | 密码 | 手机号（脱敏后） |
|------|--------|------|------------------|
| admin | admin | admin123 | 138****8000 |
| auditor | auditor | admin123 | 139****9000 |
| student | - | - (微信登录) | 137****5678 |
| student | - | - (微信登录) | 136****9999 |

#### 2. 角色权限配置

| 角色 | 可访问菜单ID | 菜单名称 |
|------|-------------|----------|
| admin | 1-10 | 全部10个菜单 |
| auditor | 1,4,5,6,9 | 数据看板、评价审核、帖子审核、敏感词库、举报管理 |
| student | 1 | 数据看板 |

#### 3. 数据库初始化步骤

```bash
# 1. 创建数据库和表结构
mysql -u root -p < sql/schema.sql

# 2. 插入初始数据
mysql -u root -p < sql/seed.sql
```

#### 4. Redis 配置要求

- Redis 版本：6.0+
- 用于存储：点赞数据、限流计数器、验证码

### 四、接口调用示例

#### 登录获取 Token

```bash
# 管理员登录
POST /api/admin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "captchaKey": "xxx",
  "captcha": "abcd"
}
```

#### 带 Token 请求

```bash
GET /api/permission/menus
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

**文档版本**: v1.1  
**生成日期**: 2026-05-20  
**项目**: 校园餐饮评价系统
