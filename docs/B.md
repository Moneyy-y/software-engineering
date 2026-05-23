# 成员 B — 微信小程序（师生端）任务交接文档

> **对照范围**：项目开发交接文档 §9.4、§9.11（P0 #2；P2 #16～#18）；UI 设计说明书个人中心与首页相关章节  
> **负责模块**：`miniprogram/`（微信原生小程序）  
> **协作依赖**：组员 D 提供浏览记录、消息中心、帖子重提、评价重提、举报等后端 API

---

## 一、任务完成总览（与分工表一致）

| 任务 | 当前状态 | 实现位置 / 说明 |
|------|----------|-----------------|
| 首页高级筛选（价格、菜系、距离、排序） | ✅ 已完成 | `pages/index/*` + `GET /api/dish/list` |
| 首页双列瀑布流布局 | ✅ 已完成 | `utils/waterfall.js` + `pages/index/index.wxml` |
| 浏览记录页 | ✅ 已完成 | `pages/browse-history/*` + `/api/user/browse/*` |
| 消息中心（列表、未读角标、跳转） | ✅ 已完成 | `pages/messages/*` + `/api/user/message/*` |
| 评价草稿本地保存与恢复 | ✅ 已完成 | `pages/review/review.js` + `pages/my-reviews` 草稿 Tab |
| 被拒评价修改重提 | ✅ 已完成 | `pages/my-reviews` + `pages/review`（`resubmit` 参数） |
| 论坛分区 Tab 筛选 | ✅ 已完成 | `pages/forum/forum.js`（全部/推荐/避雷/综合） |
| 发帖带图（最多 3 张） | ✅ 已完成 | `pages/forum/forum.js` + `utils/upload.js` |
| 论坛「我的帖子」Tab + 被拒重提 | ✅ 已完成 | `pages/forum/forum.js`（`tab=my`、`resubmitPostId`） |
| 举报提交页及入口 | ✅ 已完成 | `pages/report/*`；详情页/帖子详情「举报」按钮 |
| 个人中心菜单整合 | ✅ 已完成 | `pages/profile/profile.wxml` |
| 开发者工具「无依赖文件过滤」兼容 | ✅ 已完成 | `project.config.json`、`project.private.config.json` |
| 用户协议 / 隐私政策首次弹窗 | ❌ 未实现 | 见 §九 |
| 演示占位图资源 | ⚠️ 部分 | 引用 `/assets/placeholder.png` 但文件未提交 |

**成员 B 分工表内核心任务：11/12 已完成（用户协议弹窗未做）。**

---

## 二、各任务实现说明

### 2.1 首页高级筛选 + 瀑布流 ✅

- **页面**：`miniprogram/pages/index/index.js`、`index.wxml`、`index.wxss`
- **工具**：`miniprogram/utils/waterfall.js`（按估算高度分配左右列）
- **功能**：
  - 搜索关键词、食堂 Picker 筛选
  - 筛选面板：菜系、价格区间、距离、排序方式
  - 已选条件标签展示；重置 / 应用
  - 双列瀑布流卡片（封面 `widthFix` 自适应高度）
  - 定位失败时使用默认坐标（北京天安门附近）以保证距离排序可用
- **接口**：`GET /api/shop/list`、`GET /api/dish/list`（参数：`keyword`、`shopId`、`category`、`minPrice`、`maxPrice`、`sortBy`、`lat`、`lng`）

### 2.2 浏览记录 ✅

- **页面**：`miniprogram/pages/browse-history/*`
- **入口**：个人中心 →「浏览记录」
- **功能**：列表展示封面、名称、价格、浏览时间；点击进入菜品详情；下拉刷新；一键清空
- **自动记录**：`pages/detail/detail.js` 的 `onLoad` 调用 `POST /api/user/browse?dishId=`
- **接口**：
  - `POST /api/user/browse` — 写入浏览行为
  - `GET /api/user/browse/history` — 查询历史
  - `DELETE /api/user/browse/clear` — 清空

### 2.3 消息中心 ✅

- **页面**：`miniprogram/pages/messages/*`
- **入口**：个人中心 →「消息中心」（红色未读角标）
- **功能**：
  - 加载中 / 错误 / 空态 / 列表四态展示
  - 消息类型标签（评价通过/未通过、帖子审核、反馈回复等）
  - 进入页面后自动将全部消息标记已读；返回个人中心刷新角标
  - 单条删除、全部清空；下拉刷新
  - 点击消息按 `relatedType` + `type` 跳转对应页面
- **接口**：
  - `GET /api/user/message/unread/count` — 未读数（个人中心角标）
  - `GET /api/user/message/list` — 消息列表
  - `PUT /api/user/message/read` — 全部标记已读
  - `DELETE /api/user/message/{id}` — 删除单条
  - `DELETE /api/user/message/clear` — 清空全部

**消息点击跳转规则：**

| relatedType | type | 跳转目标 |
|-------------|------|----------|
| `post` | `post_approve` | 帖子详情 `/pages/forum-detail/forum-detail?id=` |
| `post` | `post_reject` | 论坛我的帖子并重开编辑 `/pages/forum/forum?tab=my&resubmitPostId=` |
| `post` | 其他 | 论坛我的帖子 Tab |
| `review` | `review_reject` | 我的评价并定位被拒项 `/pages/my-reviews/my-reviews?reviewId=` |
| `review` | 其他（有 dishId） | 菜品详情 |
| `review` | 其他 | 我的评价 |
| `feedback` | 任意 | 意见反馈页 |

> **数据库**：消息跳转字段需执行 `sql/migration_message_related.sql`（`related_type`、`related_id`、`dish_id`）。  
> 迁移前产生的旧消息 `related_*` 为空，仍可展示但提示「暂不支持跳转」。

### 2.4 评价草稿 + 被拒重提 ✅

- **写评价页**：`pages/review/review.js`、`review.wxml`
  - 「保存草稿」写入本地 Storage（键名 `review_draft_{dishId}`）
  - 再次进入自动恢复；提交成功后清除草稿
  - `resubmit=1` 时展示「修改被拒评价」提示
- **我的评价页**：`pages/my-reviews/my-reviews.js`、`my-reviews.wxml`
  - Tab：「我的评价」/「草稿」
  - 被拒评价「修改重提」跳转写评价页并预填内容
  - 支持从消息中心带 `reviewId` 参数直达被拒项
- **接口**：`GET /api/user/review/my`；提交时 `POST /api/review/submit`（`resubmit: true`）

### 2.5 论坛增强（分区 + 发帖带图 + 我的帖子） ✅

- **页面**：`pages/forum/forum.js`、`forum.wxml`、`forum.wxss`
- **功能**：
  - 主 Tab：「论坛」/「我的帖子」
  - 分区 Tab：全部、推荐、避雷、综合（参数 `zone`）
  - 发帖弹窗：标题、内容、分区、最多 3 张图片上传
  - 被拒帖子「修改后重新提交」：`POST /api/post/resubmit`
  - 消息中心拒绝帖可带 `resubmitPostId` 自动打开编辑弹窗
- **接口**：
  - `GET /api/post/list?zone=`
  - `GET /api/post/my`
  - `POST /api/post/publish`
  - `POST /api/post/resubmit`

### 2.6 举报功能（小程序端） ✅

- **页面**：`pages/report/report.js`、`report.wxml`
- **入口**：
  - `pages/detail/detail.js` — 评价旁「举报」
  - `pages/forum-detail/forum-detail.js` — 帖子「举报」
- **功能**：选择原因、填写说明（≥5 字）、提交
- **接口**：`POST /api/report/submit`（管理端处理见成员 C 的 `ReportManage.vue`）

### 2.7 个人中心整合 ✅

- **页面**：`pages/profile/profile.js`、`profile.wxml`、`profile.wxss`
- **菜单**：我的收藏、浏览记录、消息中心（未读角标）、我的评价、意见反馈、餐饮论坛、退出登录

### 2.8 工程配置修复 ✅

微信开发者工具开启「过滤无依赖文件」时，仅通过 `navigateTo` 字符串引用的页面（如 `messages`）可能无法注册，表现为白屏、`Page has not been registered yet`。

**已修改：**

| 文件 | 修改 |
|------|------|
| `project.config.json` | `ignoreDevUnusedFiles: false`、`ignoreUploadUnusedFiles: false` |
| `project.private.config.json` | `ignoreDevUnusedFiles: false` |
| `app.json` | 移除未使用的重定向页 `my-posts`、`review-drafts`（避免启动时报依赖分析错误） |
| `utils/request.js` | 响应格式校验、401 处理，避免 Promise 悬挂导致页面一直 loading |

---

## 三、修改 / 新增文件清单

### 3.1 小程序页面（本轮重点）

| 文件 | 说明 |
|------|------|
| `pages/index/index.js` | 高级筛选、瀑布流数据加载 |
| `pages/index/index.wxml` | 筛选面板、双列瀑布流 UI |
| `pages/index/index.wxss` | 首页样式 |
| `pages/browse-history/*` | 浏览记录页（新增） |
| `pages/messages/*` | 消息中心页（新增） |
| `pages/my-reviews/my-reviews.js` | 草稿 Tab、被拒重提、消息跳转 |
| `pages/my-reviews/my-reviews.wxml` | 双 Tab UI |
| `pages/review/review.js` | 草稿保存/恢复、重提模式 |
| `pages/review/review.wxml` | 草稿按钮、重提提示 |
| `pages/forum/forum.js` | 分区 Tab、发帖带图、我的帖子、重提 |
| `pages/forum/forum.wxml` | 论坛 UI 增强 |
| `pages/detail/detail.js` | 浏览记录上报、评价举报入口 |
| `pages/forum-detail/forum-detail.js` | 帖子举报入口 |
| `pages/report/*` | 举报提交页 |
| `pages/profile/profile.js` | 未读消息角标、浏览记录/消息中心入口 |
| `pages/profile/profile.wxml` | 个人中心菜单 |
| `app.json` | 注册新页面、清理无用页面 |

### 3.2 工具与配置

| 文件 | 说明 |
|------|------|
| `utils/waterfall.js` | 双列瀑布流分配算法（新增） |
| `utils/request.js` | 请求封装增强 |
| `utils/upload.js` | 图片上传（论坛/评价复用） |
| `utils/config.js` | API 基址 `http://localhost:8080` |
| `project.config.json` | 关闭无依赖文件过滤 |
| `project.private.config.json` | 同上 |

### 3.3 后端（组员 D 提供，B 联调依赖）

| 文件 | 说明 |
|------|------|
| `service/BrowseService.java` | 浏览记录读写 |
| `service/MessageService.java` | 消息发送、列表、已读 |
| `mapper/MessageMapper.java` | 消息 SQL（含 related 字段降级） |
| `entity/Message.java` | 消息实体 |
| `vo/MessageVO.java` | 消息返回对象 |
| `controller/UserController.java` | browse / message 相关路由 |
| `service/AuditService.java` | 评价审核后推送消息 |
| `service/PostService.java` | 帖子审核/提交后推送消息 |
| `service/FeedbackService.java` | 反馈回复后推送消息 |

### 3.4 SQL

| 文件 | 说明 |
|------|------|
| `sql/schema.sql` | `message` 表（含 `related_type` 等字段） |
| `sql/migration_message_related.sql` | 已有库补字段（**必执行一次**） |

---

## 四、数据库补充（已有库必做）

```sql
-- 消息跳转字段（未执行则新消息可能缺少关联信息，旧库 insert 会降级）
-- 见 sql/migration_message_related.sql
USE `catering`;

ALTER TABLE `message`
  ADD COLUMN `related_type` VARCHAR(20) DEFAULT NULL COMMENT 'review/post/feedback' AFTER `type`,
  ADD COLUMN `related_id` BIGINT DEFAULT NULL AFTER `related_type`,
  ADD COLUMN `dish_id` BIGINT DEFAULT NULL COMMENT '评价关联菜品' AFTER `related_id`;
```

若报 `1060 Duplicate column name`，说明已执行过，可忽略。

---

## 五、小程序页面一览

| 页面 | 路径 | 功能 | 成员 B 本轮 |
|------|------|------|-------------|
| 首页 | `pages/index` | 搜索、筛选、瀑布流 | ✅ 增强 |
| 推荐 | `pages/recommend` | 猜你喜欢 | — |
| 红黑榜 | `pages/redblack` | 红/黑榜 | — |
| 我的 | `pages/profile` | 个人中心入口 | ✅ 增强 |
| 菜品详情 | `pages/detail` | 详情、收藏、评价、举报 | ✅ 增强 |
| 写评价 | `pages/review` | 星级、文字、图片、草稿 | ✅ 增强 |
| 我的收藏 | `pages/favorites` | 收藏列表 | — |
| 我的评价 | `pages/my-reviews` | 审核状态、草稿、重提 | ✅ 增强 |
| 浏览记录 | `pages/browse-history` | 历史浏览 | ✅ 新增 |
| 消息中心 | `pages/messages` | 系统通知 | ✅ 新增 |
| 意见反馈 | `pages/feedback` | 提交反馈 | — |
| 论坛 | `pages/forum` | 分区、发帖、我的帖子 | ✅ 增强 |
| 帖子详情 | `pages/forum-detail` | 评论、点赞、举报 | ✅ 增强 |
| 举报 | `pages/report` | 提交举报 | ✅ 新增 |

---

## 六、API 速查（小程序端常用）

### 浏览记录

| API | 方法 | 用途 |
|-----|------|------|
| `/api/user/browse?dishId=` | POST | 记录浏览（详情页自动调用） |
| `/api/user/browse/history` | GET | 浏览历史列表 |
| `/api/user/browse/clear` | DELETE | 清空浏览记录 |

### 消息中心

| API | 方法 | 用途 |
|-----|------|------|
| `/api/user/message/unread/count` | GET | 未读数量 |
| `/api/user/message/list` | GET | 消息列表 |
| `/api/user/message/read` | PUT | 全部标记已读 |
| `/api/user/message/{id}` | DELETE | 删除单条 |
| `/api/user/message/clear` | DELETE | 清空全部 |

### 首页 / 菜品

| API | 方法 | 用途 |
|-----|------|------|
| `/api/shop/list` | GET | 食堂列表 |
| `/api/dish/list` | GET | 菜品列表（支持筛选参数） |
| `/api/dish/{id}` | GET | 菜品详情 |

### 评价 / 论坛 / 举报

| API | 方法 | 用途 |
|-----|------|------|
| `/api/user/review/my` | GET | 我的评价 |
| `/api/review/submit` | POST | 提交评价（含 `resubmit`） |
| `/api/post/list` | GET | 论坛列表（`zone` 可选） |
| `/api/post/my` | GET | 我的帖子 |
| `/api/post/publish` | POST | 发帖 |
| `/api/post/resubmit` | POST | 被拒帖子重提 |
| `/api/report/submit` | POST | 提交举报 |
| `/api/file/upload` | POST | 图片上传 |

> 统一响应：`{ status, message, success, data }`；需登录接口携带 `Authorization: Bearer <token>`。

---

## 七、启动与验收

### 7.1 启动

```bash
# 1. 后端（组员 D）
cd backend
# 确保 MySQL、Redis 已启动
# 已有库执行 sql/migration_message_related.sql
mvn spring-boot:run

# 2. 微信开发者工具
# 导入目录：miniprogram/
# 详情 → 本地设置 → 勾选「不校验合法域名…」
# config.js：baseUrl: 'http://localhost:8080'
# 修改配置后建议：清缓存 → 全部清除 → 重新编译
```

### 7.2 建议验收步骤

| 步骤 | 操作 | 预期 |
|------|------|------|
| 1 | 首页 → 筛选（价格/菜系/排序）→ 应用 | 列表按条件变化；双列瀑布流展示 |
| 2 | 进入任意菜品详情 | 后台写入浏览记录 |
| 3 | 个人中心 → 浏览记录 | 可见刚浏览的菜品；可清空 |
| 4 | 提交评价 → 管理端拒绝 | 个人中心消息角标 +1 |
| 5 | 个人中心 → 消息中心 | 可见拒绝通知；点击跳转我的评价/重提 |
| 6 | 论坛 → 分区 Tab 切换 | 列表按 zone 过滤 |
| 7 | 论坛 → 发帖带 1～3 张图 | 提交成功；消息中心收到「帖子已提交审核」 |
| 8 | 写评价 → 保存草稿 → 退出再进 | 内容自动恢复；草稿 Tab 可见 |
| 9 | 帖子/评价详情 → 举报 | 提交成功 |
| 10 | 退出消息中心返回个人中心 | 未读角标清零 |

### 7.3 触发消息的业务场景

| 场景 | 消息类型 | 触发位置（后端） |
|------|----------|------------------|
| 评价审核通过/拒绝 | `review_approve` / `review_reject` | `AuditService` |
| 帖子提交/通过/拒绝 | `post_submit` / `post_approve` / `post_reject` | `PostService` |
| 反馈回复/办结 | `feedback_reply` / `feedback_resolved` | `FeedbackService` |

---

## 八、已知问题与注意事项

1. **消息页白屏**：若控制台出现 `Page "pages/messages/messages" has not been registered yet`，请确认 `ignoreDevUnusedFiles` 为 `false` 并**重新编译**（见 §2.8）。
2. **数据库迁移**：未执行 `migration_message_related.sql` 时，消息仍可展示，但跳转关联字段为空，点击提示「暂不支持跳转」。
3. **旧消息数据**：迁移前产生的评价审核消息无 `related_type`，列表正常显示，仅无法一键跳转。
4. **占位图 500**：多处引用 `/assets/placeholder.png`，仓库内暂无该文件，不影响主流程，仅缺图时显示异常。
5. **HTTP 图片警告**：开发环境部分 seed 图片为 `http://`，小程序会提示需 HTTPS；本地演示可忽略，上线需改 HTTPS 或 OSS。
6. **真机调试**：`config.js` 的 `baseUrl` 不能写 `localhost`，需改为电脑局域网 IP。
7. **进入消息中心即已读**：打开列表后会调用「全部已读」，角标立即清零（产品设计行为，非 Bug）。
8. **微信登录**：仍为 mock 登录（`openId = "mock_" + code`），由组员 D 负责，非 B 范围。

---

## 九、仍属需求但未纳入成员 B 本轮范围

以下在 §9.4 / 总体需求中提及，**非本次 B 分工表任务**：

| 项 | 说明 |
|----|------|
| 用户协议 / 隐私政策弹窗 | 首次启动勾选同意 |
| 微信真实登录 | `jscode2session`，属 D + 配置项 |
| 微信订阅消息推送 | 需模板 ID 与后端 `PushService` |
| 演示图片资源补全 | P0 #1，需 `backend/uploads/demo/` 或改 seed |
| 手机号验证码登录 | 需求规格中的扩展登录方式 |

---

## 十、联系人

| 角色 | 负责 |
|------|------|
| 成员 B | 微信小程序页面、交互与联调 |
| 成员 C | Vue 管理端页面 |
| 成员 D | Spring Boot 后端 API、数据库、消息/浏览等业务逻辑 |

---

**文档版本**：v1.0  
**更新日期**：2026-05-23  
**项目**：高校餐饮服务质量感知与推荐系统 — 微信小程序（师生端）
