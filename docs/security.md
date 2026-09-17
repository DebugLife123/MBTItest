# 安全说明

本文档描述当前代码实际实现的安全边界，不把计划或部署层能力写成已实现能力。

## 认证与授权

- Spring Security 无状态 JWT 会话，`SessionCreationPolicy.STATELESS`
- 访问令牌 HMAC-SHA256 签名，默认 24 小时
- 刷新令牌默认 7 天，可配置
- BCrypt 存储用户密码，管理员种子密码同样为 BCrypt
- `USER` 与 `ADMIN` 角色由 Spring Security 方法级与路由级授权控制
- 公开接口：注册、登录、刷新、健康检查、Bootstrap、OpenAPI
- 认证失败返回 HTTP 401 与 RFC 7807 `BAD_CREDENTIALS`
- 未认证访问返回 HTTP 401，无权限访问返回 HTTP 403

## 前端令牌处理

- 前端将访问令牌与刷新令牌保存在 `localStorage`
- Axios 请求自动附带 `Authorization: Bearer <token>`
- 401 时只允许一次自动刷新，并发 401 共用同一刷新请求
- 刷新失败会清理 `token`、`refreshToken` 与 `user`
- `localStorage` 方案需配合严格的 CSP 与 XSS 防护；生产部署建议评估 HttpOnly Cookie + SameSite 方案

## CSRF 与 CORS

- 因使用无状态 Bearer Token，后端显式关闭 CSRF
- 不存在基于 Cookie 的会话，因此当前请求不依赖 CSRF Token
- CORS 允许来源由 `CORS_ALLOWED_ORIGINS` 配置，默认仅本地开发前端地址
- 生产环境必须限制为实际前端域名，禁止使用通配符

## 输入与数据库安全

- Spring Data JPA 参数化查询，禁止字符串拼接 SQL
- Bean Validation 校验请求体、路径和查询参数
- Flyway 管理数据库结构，生产环境不使用 `ddl-auto: update`
- JPA 使用 `ddl-auto: validate` 校验实体与迁移结构一致性
- 敏感字段不返回响应，异常响应不暴露堆栈细节

## XSS 与响应头

- Vue 模板默认转义，未使用不可信 `v-html`
- 后端设置 `X-Content-Type-Options: nosniff`
- 后端设置 `X-Frame-Options: DENY`
- Nginx 设置 `X-Frame-Options`、`X-Content-Type-Options`、`Referrer-Policy`
- 当前尚未配置完整 CSP，生产环境应在 Nginx 或网关层增加 `Content-Security-Policy`

## 速率限制

- 已实现基于客户端 IP 的进程内限流过滤器
- 默认容量 120 次/分钟，可用 `RATE_LIMIT_ENABLED` 控制
- 业务接口受保护
- 当前未对登录接口单独做暴力破解限流；生产环境应增加账户级锁定与登录专用限流

## 日志与监控

- Logback 输出控制台日志与文件日志
- 请求使用 `TraceIdFilter` 生成/透传 Trace ID
- Actuator 暴露健康、信息、指标和 Prometheus 端点
- 健康详情仅授权用户可见
- 不在日志中输出密码、Token 原文或完整敏感请求体

## 依赖与镜像

- 后端依赖由 Maven 管理，前端依赖由 `package-lock.json` 固定
- Docker 镜像使用多阶段构建、非 root 用户运行
- 前端 Nginx 不加载开发服务器
- CI 执行类型检查、测试、覆盖率与构建检查

## 生产加固清单

1. 使用 HTTPS/TLS 1.2+，启用 HSTS
2. 配置完整 CSP，限制脚本、连接与图片源
3. 将 JWT 密钥迁移至平台密钥管理服务，不使用默认值
4. 将 CORS 限定为生产域名
5. 为登录接口增加账户级限流与失败锁定
6. 将进程内限流替换为 Redis 分布式限流
7. 配置数据库备份、最小权限账号与审计
8. 开启依赖漏洞扫描与定期升级
