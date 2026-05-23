# 高校餐饮服务质量感知与推荐系统

> **后续开发与维护**请阅读：[docs/项目开发交接文档.md](docs/项目开发交接文档.md)（含当前进度、完整使用说明、待完善清单、API 一览）

## 项目结构

```
├── backend/          # Spring Boot 后端 (端口 8080)
├── admin-web/        # Vue 3 管理端 (端口 5173)
├── miniprogram/      # 微信小程序
├── sql/              # 数据库脚本
└── docker-compose.yml
```

## 环境要求

| 软件 | 用途 | 说明 |
|------|------|------|
| JDK 8+ | 运行后端 | IDEA 运行时可自带 |
| MySQL 5.7+ | 数据库 | 见下方「数据库初始化」 |
| Redis 6+ | 缓存 | 后端启动前必须已运行 |
| IntelliJ IDEA | 运行后端（推荐） | 可代替命令行 `mvn` |
| Node.js 18+ | 运行管理端 | 首次需 `npm install` |
| 微信开发者工具 | 调试小程序 | 必选 |

可选：Maven 3.6+（命令行构建）、Docker Desktop（一键起 MySQL+Redis）

---

## 数据库初始化

`sql/` 目录脚本说明：

| 脚本 | 用途 | 何时执行 |
|------|------|----------|
| `schema.sql` | 建库建表（含用户协议、消息跳转、红黑榜、举报、RBAC、审计日志等完整结构） | **新环境必做** |
| `seed.sql` | 演示数据（管理员、菜品、评价、菜单权限等） | **新环境必做** |
| `migration_board_manage.sql` | 为 `dish` 表补 `board_sort`、`board_hidden`（红黑榜人工管理） | 仅**旧库升级** |
| `migration_message_related.sql` | 为 `message` 表补 `related_type`、`related_id`、`dish_id`（消息中心跳转） | 仅**旧库升级** |
| `migration_user_protocol.sql` | 为 `user` 表补 `protocol_agreed`、`protocol_agreed_at`（用户协议记录） | 仅**旧库升级** |
| `migration_report_seed.sql` | 举报表示例数据（可选） | 可选 |
| `reset_database.sql` | 删库重建（内含 `SOURCE` 路径需改成本机路径） | 需要完全重置时 |

### 场景 A：全新安装（推荐）

在 MySQL Workbench 或命令行**按顺序**执行：

```text
1. sql/schema.sql
2. sql/seed.sql
```

当前版 `schema.sql` 已包含上述迁移字段，**无需再跑 migration_*.sql**。

### 场景 B：已有旧版 catering 库（从早期代码升级）

若库是较早版本建的（没有消息跳转、协议、榜单字段等），在选中 `catering` 库后**依次执行**：

```text
1. sql/migration_board_manage.sql
2. sql/migration_message_related.sql
3. sql/migration_user_protocol.sql
4. sql/migration_report_seed.sql   （可选，补举报演示数据）
```

若某脚本报 `1060 Duplicate column name`，说明该字段已存在，**跳过该脚本即可**。

### 验证是否就绪

```sql
USE catering;
SHOW COLUMNS FROM user LIKE 'protocol_agreed';
SHOW COLUMNS FROM message LIKE 'related_type';
SHOW COLUMNS FROM dish LIKE 'board_sort';
SELECT COUNT(*) FROM dish;
SELECT COUNT(*) FROM menu;
```

### 数据库相关常见问题

| 现象 | 处理 |
|------|------|
| 红黑榜管理报 `Unknown column 'board_sort'` | 执行 `migration_board_manage.sql` |
| 消息中心有角标但无法跳转 / 后端 SQL 报错 | 执行 `migration_message_related.sql` |
| 同意用户协议后后端报错 | 执行 `migration_user_protocol.sql` |
| 管理端举报列表为空 | 可执行 `migration_report_seed.sql` 或自行在小程序提交举报 |
| `Error 1046 No database selected` | 先 `USE catering;` 或 Workbench 左侧双击选中库 |

---

## 推荐运行方式（IDEA + 图形界面，少敲命令）

适合本机已安装 **MySQL、Redis**，且数据库已按上文「数据库初始化」完成配置的情况。

### 运行前确认

1. **MySQL**、**Redis** 服务已启动（Windows「服务」中可设为开机自启）
2. Redis 自检：`redis-cli ping` 返回 `PONG`
3. 数据库账号与 `backend/src/main/resources/application-dev.yml` 一致（默认 `catering` / `catering123`，库名 `catering`）
4. 若从未建表，按「数据库初始化 → 场景 A」执行 `schema.sql` 和 `seed.sql`（只需做一次）

### 日常三步启动

```text
① MySQL + Redis 在后台运行（一般不用每天手动开 Workbench）
② IDEA 运行后端
③ 浏览器打开管理端 + 微信开发者工具编译小程序
```

#### 步骤 1：IDEA 启动后端

1. 用 **IntelliJ IDEA** 打开 `backend` 目录（或整个 `e:\软件工程`）
2. 等待 Maven 依赖下载完成
3. 打开 `com.catering.CateringApplication`
4. 右键 → **Run 'CateringApplication'**
5. 控制台出现 `Tomcat started on port(s): 8080` 即成功

> Maven 未配置 PATH 时，仍可在 IDEA 内正常运行，无需命令行 `mvn`。

API 地址：http://localhost:8080（浏览器显示 404 属正常）

#### 步骤 2：浏览器打开管理端

**首次：**

1. IDEA 或 VS Code 打开 `admin-web`
2. 终端执行一次：`npm install`

**每次运行：**

- 终端执行：`npm run dev`  
- 或在 IDEA 的 npm 工具窗口双击 **dev** 脚本

浏览器访问：**http://localhost:5173**  
账号：`admin` / `admin123`

#### 步骤 3：微信开发者工具运行小程序

1. 打开微信开发者工具 → **导入项目** → 目录：`miniprogram`
2. AppID 选 **测试号** 即可
3. **详情 → 本地设置** → 勾选 **「不校验合法域名、web-view、TLS…」**
4. 确认 `miniprogram/utils/config.js` 中：

```javascript
baseUrl: 'http://localhost:8080'
```

> ⚠️ **真机调试**：手机扫码后无法访问 `localhost`。需将 `baseUrl` 改为电脑的局域网 IP（如 `http://192.168.1.100:8080`），并确保手机与电脑在同一 WiFi 下。  
> 查看本机 IP：命令行输入 `ipconfig`，找到 `IPv4 地址`。

5. 点击 **编译** 运行

### 快速自测

| 步骤 | 操作 |
|------|------|
| 1 | 小程序首页能看到菜品 |
| 2 | 提交一条评价（至少 10 字） |
| 3 | 管理端 → 评价审核 → 通过 |
| 4 | 小程序刷新详情可见评价 |

---

## 其他启动方式

### 方式 A：命令行（适合熟悉终端）

**1. 数据库（Docker）**

```bash
cd e:\软件工程
docker-compose up -d
```

MySQL: `localhost:3306`，库 `catering`，用户 `catering` / `catering123`  
Redis: `localhost:6379`

**2. 后端**

```bash
cd backend
mvn spring-boot:run
```

若 `mvn` 未加入 PATH，可使用：

```text
D:\catering-dev-tools\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```

**3. 管理端**

```bash
cd admin-web
npm install
npm run dev
```

**4. 小程序** — 同上文「步骤 3」

### 方式 B：不用 Docker（本机 MySQL + Redis）

1. 安装并启动 MySQL、Redis
2. 按「数据库初始化」完成建表与种子数据（新库用场景 A，旧库升级用场景 B）
3. 若不用默认账号，修改 `application-dev.yml` 中的 `username`、`password`
4. 按「推荐运行方式」或「方式 A」第 2～4 步启动三端

---

## 默认账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | admin123 |
| 学生 | 微信 mock 登录 | 传任意 code |

---

## 常见问题

| 现象 | 处理 |
|------|------|
| 后端报 Redis 连接失败 | 先启动 Redis 服务 |
| 后端报 MySQL 连接失败 | 检查库名、账号密码是否与 `application-dev.yml` 一致 |
| 管理端登录失败 | 确认后端已启动；使用 `admin` / `admin123` |
| 登录成功但页面全 401、数据为 0 | **先启动 Redis**；重启后端后再登录 |
| 小程序网络错误 | 后端先启动；勾选「不校验合法域名」 |
| 首页无菜品 | 是否已执行 `seed.sql` |
| 评价提交后看不到 | 管理端需 **评价审核 → 通过** |
| 红黑榜/消息/协议相关 SQL 报错 | 见「数据库初始化」与「数据库相关常见问题」 |
| 小程序消息页白屏 | 开发者工具清缓存并重新编译；确认 `ignoreDevUnusedFiles: false` |

---

## 核心 API

| 接口 | 说明 |
|------|------|
| POST /api/user/login | 微信 mock 登录 |
| POST /api/admin/login | 管理端登录 |
| GET /api/dish/list | 菜品列表 |
| POST /api/review/submit | 提交评价 |
| GET /api/audit/review/pending | 待审核评价 |
| GET /api/recommend/list | 个性化推荐 |
| GET /api/statistics/dashboard | 数据看板 |

---

## 技术说明

- 微信登录为开发期 mock（`wx.login` 的 code 映射为 openId）
- 图片上传使用本地 `uploads/` 目录
- 评价默认待审核，管理端通过后前台可见
- 管理员账号由应用启动时自动初始化（`admin` / `admin123`）

## 已完善功能（近期更新）

- 敏感词 **DFA** 过滤；管理端敏感词库维护
- 评价 `user_id` **AES** 加密存储（兼容旧 seed 数据 `enc_*`）
- 管理端：食堂/档口管理、帖子审核、数据看板 **CSV/Excel 导出**、红黑榜人工干预、举报处理、RBAC 动态菜单
- 小程序：首页**高级筛选**与**双列瀑布流**、**浏览记录**、**消息中心**、评价草稿/被拒重提、论坛分区与发帖带图、**用户协议/隐私政策弹窗**
- 收藏列表返回菜品详情；论坛点赞使用 **Redis**
- 数据库：`message` 跳转字段、`user` 协议同意字段、`dish` 榜单管理字段（见 `sql/` 迁移脚本）
