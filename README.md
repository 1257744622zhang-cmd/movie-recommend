# 🎬 电影推荐系统 (Movie Recommendation System)

前后端分离的电影推荐系统，综合 **协同过滤 + 内容画像 + 行为挖掘** 多种算法，实现个性化推荐，并配套完整的电影浏览、评分、收藏、影评与管理后台。

## ✨ 功能特性

### 用户与认证
- 注册 / 登录，JWT 无状态认证
- 个人资料维护、修改密码
- 兴趣标签与偏好设置

### 电影
- 电影浏览、分类筛选、关键词搜索
- 电影详情、评分（五星）
- 观影状态管理（想看 / 在看 / 看过）

### 收藏与内容
- 收藏 / 取消收藏
- 自定义收藏夹（folder）
- 影评 / 笔记（note）
- 标签体系（tag）

### 推荐引擎
- **基于用户的协同过滤**（UserCF）
- **基于内容 / 用户画像**的推荐
- **SVR 矩阵填充**（评分矩阵补全）
- **AutoEncoder** 深度推荐
- 推荐反馈闭环（喜欢 / 不喜欢，实时调整）
- 推荐结果可视化分析（ECharts）

### 管理后台
- 用户管理、电影管理
- 用户行为日志审计

## 🛠 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 2.7.18、Spring Data JPA、Spring Security、JJWT、MySQL |
| 前端 | Vue 2、Vite、Element UI、ECharts、Vue Router、Axios |
| 推荐算法 | 协同过滤、SVR、AutoEncoder、用户画像 |
| 构建 | Maven（多模块）、npm |

## 📁 目录结构

```
backend/                 # Spring Boot 多模块后端
├── api/                 # 入口模块：REST 控制器 + 主类 MovieRecommendApplication
├── core/                # 公共模块：安全、JWT、异常处理
├── user/                # 用户模块
├── movie/               # 电影模块
├── collection/          # 收藏 / 影评 / 标签 / 观影状态模块
├── recommend/           # 推荐算法模块（协同过滤 / SVR / AutoEncoder）
└── movie_data.sql       # 建库建表 + 100 条示例电影数据
frontend/                # Vue2 前端
```

## 🔧 环境要求

- JDK 8 / 11 / 17（建议 8 或 11）
- Maven 3.6+
- Node.js 16+
- MySQL 5.7+（默认 3306，账号 `root`，密码通过环境变量 `DB_PASSWORD` 注入）

## 🚀 快速开始

### 1. 准备数据库

启动 MySQL，任选其一：

- 执行 `backend/movie_data.sql`（自动建库建表 + 导入示例数据），或
- 手动执行 `CREATE DATABASE movie_recommend;`

### 2. 启动后端（端口 8080）

先设置数据库密码环境变量（仓库不保存真实密码）：

- **Git Bash / Linux**：`export DB_PASSWORD=你的密码`
- **PowerShell**：`$env:DB_PASSWORD="你的密码"`
- **Windows 永久**：`setx DB_PASSWORD "你的密码"`（设置后重开终端）
- **IDEA**：Run Configurations → `MovieRecommendApplication` → Environment variables → 添加 `DB_PASSWORD=你的密码`

```bash
cd backend
mvn clean install -DskipTests
cd api
mvn spring-boot:run
```

看到 `Tomcat started on port(s): 8080` 即成功。

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173 。前端已配置代理，`/api` 请求自动转发到 `http://localhost:8080`。

## 👤 演示账号

系统启动时会自动创建演示账号（密码已加密存储）：

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `123456` | 管理员 |
| `user` | `123456` | 普通用户 |

## 📸 界面截图

> 待补充：运行后截图放入 `docs/screenshots/` 目录并在此引用。

## 📄 许可证

仅供学习交流使用。
