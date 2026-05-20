# 成员 C — 管理端 Vue（后台页面）任务交接文档

> **对照范围**：详细设计说明书 §9.3、§9.5；P0 #3；P1 #7 / #9 / #10 / #12  
> **负责模块**：`admin-web/`（Vue 3 + Vite + Element Plus）  
> **协作依赖**：组员 D 提供 Spring Boot 后端 API、RBAC、举报/榜单/审核等接口

---

## 一、任务完成总览（与分工表一致）

| 任务 | 当前状态 | 实现位置 / 说明 |
|------|----------|-----------------|
| 用户管理页（列表、停用、改角色） | ✅ 已完成 | `UserManage.vue` + `/api/admin/user/*` |
| 红黑榜人工管理（置顶、隐藏等，原只读） | ✅ 已完成 | `RedBlack.vue` + `/api/admin/board/*`；需执行 `migration_board_manage.sql` |
| 帖子批量审核 | ✅ 已完成 | `PostAudit.vue` 多选批量通过/拒绝 |
| 帖子拒绝原因填写 | ✅ 已完成 | 单条/批量拒绝弹窗必填原因 |
| 管理端删帖 | ✅ 已完成 | 单条删除 + 批量删除 |
| 看板：评价/帖子待办分开 + 刷新按钮 | ✅ 已完成 | `Dashboard.vue` 分卡统计 + 刷新 + 点击跳转 |
| 报表 Excel 导出按钮（原仅 CSV） | ✅ 已完成 | 看板「导出 Excel」→ `/api/statistics/export/excel` |
| 图形验证码登录 UI | ✅ 已完成 | `Login.vue` + `/api/admin/captcha` |
| 食堂/菜品图片上传表单完善 | ✅ 已完成 | `ShopManage.vue` Logo；`DishManage.vue` 封面 |
| 举报处理页 | ✅ 已完成 | `ReportManage.vue`；筛选已修复；小程序举报入口已联调 |
| 审核员 auditor 菜单区分（与 D 的 RBAC 联调） | ✅ 已完成 | 动态菜单 + 路由守卫 + `PermissionManage.vue` |

**成员 C 分工表内任务：11/11 已完成。**

---

## 二、各任务实现说明

### 2.1 用户管理页 ✅

- **页面**：`admin-web/src/views/UserManage.vue`
- **功能**：分页列表；按用户名/角色/状态筛选；启用/停用（Switch）；修改角色（admin/auditor/student）
- **接口**：`GET /api/admin/user/list`、`POST /api/admin/user/save`、`POST /api/admin/user/changeRole`
- **菜单**：`menu_id=11`，路径 `/user-manage`（仅 admin 默认分配）

### 2.2 红黑榜人工管理 ✅

- **页面**：`admin-web/src/views/RedBlack.vue`（由原只读改为可干预）
- **功能**：红/黑榜分栏；置顶/取消置顶、上移下移、隐藏/显示、移出榜单、人工加入红/黑榜、重新计算榜单
- **接口**：
  - `GET /api/admin/board/list` — 管理端榜单（含隐藏项）
  - `POST /api/admin/board/intervene?dishId=&action=` — 干预动作见下表
  - `POST /api/admin/board/calculate` — 手动触发榜单计算
- **公开榜单**：小程序/前台 `GET /api/recommend/redblack` 已与人工干预逻辑统一（`BoardService`）

| action | 含义 |
|--------|------|
| `add_red` / `add_black` | 加入红/黑榜 |
| `remove_red` / `remove_black` | 移出榜单 |
| `hide` / `show` | 隐藏 / 恢复展示 |
| `pin_red` / `pin_black` / `unpin` | 置顶 / 取消置顶 |
| `move_up_red` / `move_down_red` 等 | 排序调整 |

- **数据库**：需有字段 `board_sort`、`board_hidden`（见 `sql/migration_board_manage.sql`）

### 2.3 帖子审核增强 ✅

- **页面**：`admin-web/src/views/PostAudit.vue`
- **功能**：
  - 状态筛选：待审核 / 已通过 / 已拒绝 / 全部（变量名 `filterStatus`，避免与 Element Plus 表单 `status` 冲突）
  - 批量通过、批量拒绝（拒绝必填原因）
  - 单条通过/拒绝、单条/批量删帖
  - 分页
- **接口**：
  - `GET /api/admin/post/pending?status=&page=&size=`（与 `/post/list` 同路由）
  - `POST /api/admin/post/approve`、`/approve/batch`
  - `POST /api/admin/post/reject`、`/reject/batch`
  - `POST /api/admin/post/delete`、`/delete/batch`

### 2.4 数据看板 ✅

- **页面**：`admin-web/src/views/Dashboard.vue`
- **功能**：
  - 待办拆分：「待审核评价」「待审核帖子」独立统计（可点击跳转 `/audit`、`/post-audit`）
  - 「刷新数据」按钮
  - 导出 CSV + 导出 Excel
  - ECharts：评分趋势、投诉分布、热门菜品 TOP10

### 2.5 举报管理 ✅

- **页面**：`admin-web/src/views/ReportManage.vue`
- **功能**：待处理 / 已处理（`approved`+`rejected`）/ 全部筛选；通过、驳回；展示处理说明
- **接口**：`GET /api/report/list?status=`、`PUT /api/report/handle/{id}`
- **小程序联调**（与 C 页面配套）：`pages/report/report`；帖子详情、评价旁「举报」入口 → `POST /api/report/submit`
- **种子数据**：`seed.sql` 举报示例 + `sql/migration_report_seed.sql`

### 2.6 登录验证码 ✅

- **页面**：`admin-web/src/views/Login.vue`
- **功能**：验证码输入框、图片展示、点击刷新；登录携带 `captchaKey` + `captcha`
- **接口**：`GET /api/admin/captcha`、`POST /api/admin/login`

### 2.7 图片上传 ✅

- **食堂 Logo**：`ShopManage.vue` → `POST /api/file/upload`
- **菜品封面**：`DishManage.vue` → 同上
- **注意**：`application.yml` 中 `file.upload-dir: ${user.dir}/uploads`；访问路径 `/uploads/**`

### 2.8 RBAC 与 auditor 菜单 ✅

- **动态菜单**：`Layout.vue` 读取 `userStore.menus`（登录后 `fetchMenus()`）
- **路由守卫**：`router/index.js` 按 `localStorage.menus` 路径白名单拦截
- **权限配置**：`PermissionManage.vue`（admin 专用菜单）
- **角色菜单数**：admin 11 项；auditor 5 项（看板、评价审核、帖子审核、敏感词库、举报管理）

---

## 三、修改 / 新增文件清单

### 3.1 管理端前端（本轮重点）

| 文件 | 说明 |
|------|------|
| `src/views/RedBlack.vue` | 红黑榜人工管理（重写） |
| `src/views/PostAudit.vue` | 帖子批量审核、拒绝原因、删帖 |
| `src/views/Dashboard.vue` | 待办拆分、刷新、Excel 导出 |
| `src/views/ReportManage.vue` | 举报筛选修复、状态展示优化 |
| `src/views/UserManage.vue` | 用户管理（已有） |
| `src/views/Login.vue` | 图形验证码（已有） |
| `src/views/ShopManage.vue` | Logo 上传（已有） |
| `src/views/DishManage.vue` | 封面上传（已有） |
| `src/views/PermissionManage.vue` | 角色-菜单分配（已有） |
| `src/layout/Layout.vue` | 动态菜单 + 角色展示 |
| `src/router/index.js` | 路由与守卫 |
| `src/stores/user.js` | token、menus、fetchMenus |
| `src/utils/iconMap.js` | 菜单图标映射 |

### 3.2 后端（组员 D，C 联调依赖）

| 文件 | 说明 |
|------|------|
| `service/BoardService.java` | 榜单构建与干预（新增） |
| `service/PostService.java` | `listForAdmin`、拒绝原因 |
| `service/ReportService.java` | `listReports` 支持 all/handled |
| `controller/AdminManageController.java` | 帖子/榜单管理 API |
| `entity/Dish.java` | `boardSort`、`boardHidden` |

### 3.3 小程序（举报入口，与举报页联调）

| 文件 | 说明 |
|------|------|
| `miniprogram/pages/report/*` | 举报提交页 |
| `miniprogram/pages/forum-detail/*` | 帖子举报入口 |
| `miniprogram/pages/detail/*` | 评价举报入口 |

### 3.4 SQL

| 文件 | 说明 |
|------|------|
| `sql/migration_board_manage.sql` | 红黑榜字段 |
| `sql/migration_report_seed.sql` | 举报示例数据 |
| `sql/seed.sql` | 菜单、帖子、举报等种子更新 |

---

## 四、数据库补充（已有库必做）

```sql
-- 1. 红黑榜字段（未执行会报 Unknown column 'board_sort'）
-- 见 sql/migration_board_manage.sql

-- 2. 用户管理菜单（若缺失）
INSERT INTO `menu` (`menu_id`, `name`, `path`, `icon`, `parent_id`, `sort_order`, `roles`, `status`)
VALUES (11, '用户管理', '/user-manage', 'User', 0, 11, '', 1)
ON DUPLICATE KEY UPDATE `name`='用户管理';

INSERT IGNORE INTO `role_menu` (`role`, `menu_id`) VALUES ('admin', 11);

-- 3. 举报示例（可选）
-- 见 sql/migration_report_seed.sql
```

执行后 **重新登录管理端** 以刷新菜单。

---

## 五、角色权限对照

| 角色 | 可见菜单 | 数量 |
|------|----------|------|
| **admin** | 数据看板、食堂档口、菜品管理、评价审核、帖子审核、敏感词库、反馈处理、红黑榜、举报管理、权限管理、用户管理 | 11 |
| **auditor** | 数据看板、评价审核、帖子审核、敏感词库、举报管理 | 5 |
| **student** | 仅数据看板（管理端一般不登录） | 1 |

---

## 六、API 速查（管理端常用）

### 登录与权限

| API | 方法 | 用途 |
|-----|------|------|
| `/api/admin/captcha` | GET | 图形验证码 |
| `/api/admin/login` | POST | 登录 |
| `/api/permission/menus` | GET | 当前角色菜单 |
| `/api/permission/menus/all` | GET | 全部菜单 |
| `/api/permission/role/menus/{role}` | GET/POST | 角色菜单分配 |

### 红黑榜

| API | 方法 | 用途 |
|-----|------|------|
| `/api/admin/board/list` | GET | 管理端红/黑榜 |
| `/api/admin/board/intervene` | POST | 人工干预 |
| `/api/admin/board/calculate` | POST | 重新计算 |

### 帖子审核

| API | 方法 | 用途 |
|-----|------|------|
| `/api/admin/post/pending` | GET | 列表（`status` 筛选） |
| `/api/admin/post/approve` | POST | 通过 |
| `/api/admin/post/approve/batch` | POST | 批量通过 |
| `/api/admin/post/reject` | POST | 拒绝（带 `reason`） |
| `/api/admin/post/reject/batch` | POST | 批量拒绝 |
| `/api/admin/post/delete` | POST | 删帖 |
| `/api/admin/post/delete/batch` | POST | 批量删帖 |

### 看板与报表

| API | 方法 | 用途 |
|-----|------|------|
| `/api/statistics/dashboard` | GET | 看板数据 |
| `/api/statistics/export` | GET | CSV |
| `/api/statistics/export/excel` | GET | Excel |

### 举报

| API | 方法 | 用途 |
|-----|------|------|
| `/api/report/list?status=` | GET | `pending` / `handled` / `all` |
| `/api/report/handle/{id}` | PUT | 处理举报 |

### 用户与上传

| API | 方法 | 用途 |
|-----|------|------|
| `/api/admin/user/list` | GET | 用户列表 |
| `/api/admin/user/save` | POST | 保存/停用 |
| `/api/admin/user/changeRole` | POST | 改角色 |
| `/api/file/upload` | POST | 图片上传 |

---

## 七、启动与验收

### 7.1 启动

```bash
# 后端（组员 D）
cd backend
# 确保 MySQL、Redis 已启动，且已执行 migration
mvn spring-boot:run

# 管理端
cd admin-web
npm install
npm run dev
# http://localhost:5173
```

**默认账号**：`admin` / `admin123`（需输入图形验证码）

### 7.2 建议验收步骤

| 步骤 | 操作 | 预期 |
|------|------|------|
| 1 | admin 登录 | 验证码显示；侧边栏 11 个菜单 |
| 2 | auditor 登录 | 仅 5 个菜单 |
| 3 | 数据看板 → 刷新 | 待审核评价/帖子数字更新；可点击跳转 |
| 4 | 导出 Excel | 下载 `.xlsx` |
| 5 | 帖子审核 → 批量通过/拒绝 | 拒绝需填原因；列表按状态筛选 |
| 6 | 红黑榜 → 置顶后取消置顶 | 置顶标签消失 |
| 7 | 举报管理 → 全部/已处理 | 能查到不同状态记录 |
| 8 | 用户管理 → 停用/改角色 | 保存成功 |

---

## 八、已知问题与注意事项

1. **后端必须重启**：新增 `/api/admin/post/pending` 的 `status` 参数、`BoardService`、举报 `listReports` 等需重新编译运行。
2. **红黑榜字段**：未执行 `migration_board_manage.sql` 会 SQL 报错。
3. **帖子审核下拉框**：`filterStatus` 勿命名为 `status`，否则 Element Plus 表单项会导致选中项不显示。
4. **取消置顶**：后端需用 `LambdaUpdateWrapper` 将 `board_sort` 置 `NULL`（已修复 `updateById` 忽略 null 问题）。
5. **菜单缓存**：改角色或菜单后需退出重新登录。
6. **代理**：`vite.config.js` 已将 `/api`、`/uploads` 代理到 `8080`。

---

## 九、仍属需求但未纳入成员 C 本轮范围

以下在 §9.3 / 总体需求中提及，**非本次 C 分工表任务**，如需继续做请另排期：

| 项 | 说明 |
|----|------|
| 红黑榜规则配置页 | 阈值、公式可配置（当前规则在 `BoardService` / 定时任务中写死） |
| 系统运营管理 | 备份、监控、消息推送配置等 |
| 处理举报后自动下架内容 | 当前仅更新举报状态，不联动删帖/删评价 |

---

## 十、联系人

| 角色 | 负责 |
|------|------|
| 成员 C | Vue 管理端页面与联调 |
| 成员 D | 后端 API、RBAC、数据库、小程序业务接口 |

---

**文档版本**：v2.0  
**更新日期**：2026-05-20  
**项目**：高校餐饮服务质量感知与推荐系统 — 管理端
