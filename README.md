# MBTI AI Assessment Platform

一个基于现代全栈技术重构的 MBTI 职业性格测评系统。原 Servlet/JSP 项目保持不动，本目录是独立的新工程。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、ECharts |
| 后端 | Java 17、Spring Boot 3.5、Spring Security、Spring Data JPA、Bean Validation |
| 数据 | MySQL 8.4、Redis 7.4、Flyway |
| API | REST `/api/v1`、统一响应体、RFC 7807 ProblemDetail、OpenAPI |
| 测试 | JUnit 5、Mockito、MockMvc、Vitest、Vue Test Utils |
| 交付 | Docker Compose、多阶段镜像、Nginx、GitHub Actions |

## 已实现能力

- 用户注册、登录、JWT 访问令牌与刷新令牌
- 36 题测评流程、答题校验、四维计分与 16 型人格解析
- 测评历史、成长轨迹、职业建议、双人性格兼容度
- 用户 CSV 导出（UTF-8 BOM）
- 管理后台：统计、用户管理、题目 CRUD、人格类型 CRUD、分析图表
- Redis 限流、统一异常、结构化日志、Actuator 健康检查
- Flyway V1/V2 初始化结构与迁移数据

## 一键启动

前置条件：Docker Desktop 已启动。

```bash
cp .env.example .env
docker compose up --build -d
```

默认地址：

- 前端：<http://localhost:5173>
- 后端 API：<http://localhost:8080>
- 健康检查：<http://localhost:8080/api/v1/health>
- Swagger UI：<http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON：<http://localhost:8080/v3/api-docs>

默认管理员：`admin / admin123`。生产环境请通过 `.env` 覆盖数据库、Redis 和 JWT 配置。

停止服务：

```bash
docker compose down
```

清空本地数据卷并重新迁移：

```bash
docker compose down -v
docker compose up --build -d
```

## 本地开发

```bash
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

前端开发服务器默认代理 `/api` 到 `http://localhost:8080`。

## 测试与真实端到端验证

```bash
cd backend && mvn clean verify
cd frontend && npm ci && npm run type-check && npm test -- --run && npm run build
```

容器启动完成且健康后，可用 PowerShell 7 执行真实 API 闭环验证：

```powershell
pwsh -NoProfile -File ./scripts/e2e-verify.ps1
```

脚本覆盖注册、登录、刷新、双用户 36 题测评、结果、成长、职业建议、兼容度、CSV BOM、管理员统计与 CRUD。当前真实容器验证结果为 119 项断言全部通过。

## 数据库迁移

迁移文件位于 `backend/src/main/resources/db/migration/`：

- `V1__init_schema.sql`：完整表结构与索引
- `V2__seed_data.sql`：管理员、8 个维度、36 道题、16 种人格类型

Flyway 在后端启动时自动执行迁移，无需手工建表。

## 项目结构

```text
backend/   Spring Boot API 与 Flyway 迁移
frontend/  Vue 3 管理端与用户端
docs/      架构、部署、安全、测试、性能与 CI/CD 文档
scripts/   真实端到端验证脚本
```

更详细说明见 [docs/deployment.md](docs/deployment.md)、[docs/security.md](docs/security.md)、[docs/testing.md](docs/testing.md) 和 [docs/phase-4-5-plan.md](docs/phase-4-5-plan.md)。
