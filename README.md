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
| MySQL 5.7+ | 数据库 | 需执行 `sql/` 下脚本（仅首次） |
| Redis 6+ | 缓存 | 后端启动前必须已运行 |
| IntelliJ IDEA | 运行后端（推荐） | 可代替命令行 `mvn` |
| Node.js 18+ | 运行管理端 | 首次需 `npm install` |
| 微信开发者工具 | 调试小程序 | 必选 |

可选：Maven 3.6+（命令行构建）、Docker Desktop（一键起 MySQL+Redis）

---

## 推荐运行方式（IDEA + 图形界面，少敲命令）

适合本机已安装 **MySQL、Redis**，且数据库已导入 `schema.sql`、`seed.sql`、`migration_board_manage.sql` 、`migration_report_seed.sql`情况。

### 运行前确认

1. **MySQL**、**Redis** 服务已启动（Windows「服务」中可设为开机自启）
2. Redis 自检：`redis-cli ping` 返回 `PONG`
3. 数据库账号与 `backend/src/main/resources/application-dev.yml` 一致（默认 `catering` / `catering123`，库名 `catering`）
4. 若从未建表，用 **MySQL Workbench** 执行 `sql/schema.sql` 和 `sql/seed.sql`（只需做一次）

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
2. 在 MySQL Workbench 执行 `sql/schema.sql`、`sql/seed.sql`、`migration_board_manage.sql` 、`migration_report_seed.sql`
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
- 管理端：食堂/档口管理、帖子审核、数据看板 **CSV 导出**
- 小程序：评价 **图片上传**、我的收藏/我的评价、论坛点赞与评论
- 收藏列表返回菜品详情；论坛点赞使用 **Redis**
