# 部署文档

## 部署拓扑

```text
浏览器
  │
  ▼
Nginx :5173（容器内 :80）
  ├── Vue 3 静态资源
  └── /api/ → Spring Boot :8080
                 ├── MySQL 8.4 :3306（宿主机 :13306）
                 └── Redis 7.4 :6379（宿主机 :16379）
```

## 环境要求

- Docker Engine 24+ 或 Docker Desktop
- Docker Compose v2
- 2 核 CPU、4 GB 内存、10 GB 可用磁盘
- PowerShell 7（仅运行 E2E 脚本时需要）

## 一键启动

```bash
git clone https://github.com/DebugLife123/MBTItest.git
cd MBTItest
cp .env.example .env
docker compose up -d --build
docker compose ps
```

默认端口：

| 服务 | 地址 |
| --- | --- |
| 前端 | <http://localhost:5173> |
| 后端 API | <http://localhost:8080> |
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| MySQL | `localhost:13306` |
| Redis | `localhost:16379` |

默认管理员：`admin / admin123`。首次登录后应立即修改或通过初始化数据替换。

## 环境变量

`.env.example` 提供可覆盖项：

```properties
MYSQL_PORT=13306
REDIS_PORT=16379
BACKEND_PORT=8080
FRONTEND_PORT=5173
MYSQL_DATABASE=mbti_ai
MYSQL_USER=mbti
MYSQL_PASSWORD=mbti_dev_password
MYSQL_ROOT_PASSWORD=root_dev_password
REDIS_PASSWORD=redis_dev_password
JWT_SECRET=replace-with-at-least-256-bit-secret
```

生产环境必须替换全部默认密码与 `JWT_SECRET`。

## 健康检查

```bash
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/health
curl -I http://localhost:5173/
```

预期：

- MySQL、Redis、Backend 为 `healthy`
- Actuator 返回 `{"status":"UP"}`
- 业务健康接口返回 `code: 0`，数据库与 Redis 均为 `UP`
- 前端返回 HTTP 200

## Flyway 迁移

容器后端启动时自动执行：

```text
backend/src/main/resources/db/migration/
├── V1__init_schema.sql
└── V2__seed_data.sql
```

V1 建立用户、维度、题目、测评尝试、答案、结果与人格解析表及索引；V2 写入管理员、8 个维度、36 道题和 16 种人格类型。

如需从空库重新迁移：

```bash
docker compose down -v
docker compose up -d --build
```

生产环境不要使用 `docker compose down -v`，该命令会删除数据卷。

## 日志

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
docker compose logs -f redis
```

后端同时输出控制台日志和文件日志，容器内日志目录为 `/app/logs`。

## 停止与更新

```bash
docker compose down
docker compose pull
docker compose up -d --build
```

## 生产部署建议

1. 在反向代理或负载均衡器终止 TLS，并启用 HSTS
2. 使用实际域名限制 CORS
3. 使用密钥管理服务注入 `JWT_SECRET` 与数据库密码
4. 将 MySQL 数据卷迁移到持久化存储并配置备份
5. 使用 Redis 分布式限流替换进程内限流
6. 将 Actuator 管理端口与业务端口隔离
7. 在 Nginx 添加完整 CSP 与更严格的响应头
8. 使用 CI 产出的不可变镜像标签，而非仅依赖 `latest`
