# 系统架构

## 总体设计

系统采用模块化单体架构，而不是过早拆分微服务。测评、认证、用户中心、管理后台和数据分析共享一个 Spring Boot 进程与一套事务边界，前端为独立的 Vue 3 SPA。

```text
Vue 3 SPA
  │  REST /api/v1
  ▼
Spring Boot
  ├── auth          注册、登录、JWT、刷新令牌
  ├── assessment    题目、测评尝试、答题、计分、结果
  ├── user          成长轨迹、职业建议、兼容度、CSV 导出
  ├── admin         用户、题目、人格解析、统计与分析
  ├── ai            Provider 抽象、咨询会话、SSE 流式输出、团队分析
  └── common        统一响应、异常、安全、限流、追踪
  │
  ├── MySQL / Flyway
  └── Redis
```

## 模块职责

| 模块 | 职责 |
| --- | --- |
| `common` | 统一响应、ProblemDetail、JWT、限流、Trace ID、日志 |
| `auth` | 用户、BCrypt、认证、访问令牌与刷新令牌 |
| `assessment` | 测评尝试、36 题、答案、四维计分、人格结果 |
| `user` | 成长轨迹、职业建议、兼容度、CSV 导出 |
| `admin` | RBAC 管理接口、题目与人格解析 CRUD、统计与分析 |
| `ai` | 模型 Provider 抽象、多轮咨询、SSE 流式输出、会话持久化、团队画像分析 |

## API 约定

- 统一前缀：`/api/v1`
- 成功响应：`ApiResponse<T>`，`code = 0`
- 失败响应：RFC 7807 `ProblemDetail`
- 认证头：`Authorization: Bearer <token>`
- 时间：ISO-8601，业务时区 `Asia/Shanghai`
- 分页：`page`、`size`、`sort`

## 数据所有权

- Flyway 是数据库结构的唯一来源
- JPA `ddl-auto: validate`，不自动修改表结构
- MySQL 保存用户、题目、测评与结果
- Redis 用于运行时依赖与热点缓存
- 文件日志由 Logback 写入，不进入业务数据库

## AI 边界

AI 模块已经落地，设计上将确定性测评事实与生成式建议严格分层：

- `ai`：Provider 路由、提示词组装、多轮上下文、SSE 流式响应、会话与团队分析持久化
- Provider：`mock`、`deepseek`、`openai-compatible`；未配置密钥时自动回退 mock 并在状态接口显式暴露
- 模型调用期间不持有数据库事务，采用“准备会话、调用模型、保存结果”三段短事务
- 测评计分与人格结论仍由规则引擎和数据库负责，AI 不直接修改测评事实数据
- 当前可直接扩展 RAG、异步任务队列与 token/成本观测，不影响测评主链路
