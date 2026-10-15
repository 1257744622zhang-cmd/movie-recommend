# 电影推荐系统 (Movie Recommendation System)

前后端分离的电影推荐系统：基于内容的推荐 + 用户行为（评分/收藏/观看）驱动的个性化推荐。

## 技术栈

- **后端**：Spring Boot 2.7.18（多模块 Maven 工程）、Spring Data JPA、MySQL、JWT
- **前端**：Vue 2 + Vite + Element UI + ECharts + Vue Router
- **构建**：Maven、npm

## 目录结构

```
backend/          # Spring Boot 后端（多模块）
  api/            # 入口模块，REST API 控制器，主类 MovieRecommendApplication
  core/           # 公共/基础模块
  user/           # 用户模块
  movie/          # 电影模块
  collection/     # 收藏模块
  recommend/      # 推荐算法模块
  movie_data.sql  # 建库建表 + 100 条示例电影数据
frontend/         # Vue2 前端
```

## 环境要求

- JDK 8 / 11 / 17（建议 8 或 11）
- Maven 3.6+
- Node.js 16+
- MySQL 5.7+（默认 3306，账号 `root`，密码通过环境变量 `DB_PASSWORD` 注入）

## 启动步骤

### 1. 准备数据库

先启动 MySQL，然后任选其一：

- 执行 `backend/movie_data.sql`（自动建库建表 + 导入示例数据），或
- 手动执行 `CREATE DATABASE movie_recommend;`

数据库连接配置见 `backend/api/src/main/resources/application.yml`（账号默认 `root`，密码通过环境变量 `DB_PASSWORD` 注入，仓库不保存真实密码）。

### 2. 启动后端（端口 8080）

先设置数据库密码环境变量，任选其一：

- **Git Bash / Linux**：`export DB_PASSWORD=你的密码`
- **PowerShell**：`$env:DB_PASSWORD="你的密码"`
- **Windows 永久**：`setx DB_PASSWORD "你的密码"`（设置后需重开终端）
- **IDEA**：Run Configurations → `MovieRecommendApplication` → Environment variables → 添加 `DB_PASSWORD=你的密码`

```bash
cd backend
mvn clean install -DskipTests
cd api
mvn spring-boot:run
```

或在 IDEA 中直接运行 `MovieRecommendApplication`。看到 `Tomcat started on port(s): 8080` 即成功。

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173 。前端已配置代理，`/api` 请求会自动转发到 `http://localhost:8080`。
