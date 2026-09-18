# 性能说明

## 当前已实现

### 数据库

- Flyway V1/V3 为用户、测评尝试、答案、结果与 AI 会话建立必要索引
- 测评答案使用 JPA 批量保存，`hibernate.jdbc.batch_size=20`
- 结果查询按用户分页，避免一次加载全部历史
- `open-in-view: false`，数据库会话不跨越视图渲染
- HikariCP 连接池可配置，默认最大 20、最小空闲 5

### Redis 与限流

- Redis 作为运行时依赖与热点数据缓存支撑
- 限流过滤器按客户端 IP 限制请求速率，默认 120 次/分钟
- 健康检查暴露数据库与 Redis 状态

### 后端

- Spring Boot 多阶段镜像只交付 JRE 与可执行 JAR
- JVM 默认 `-Xms256m -Xmx512m`、G1 GC
- 响应压缩开启，覆盖 JSON、XML 与文本
- Actuator 暴露指标与 Prometheus 端点

### 前端

- Vite 生产构建、路由懒加载
- Pinia 状态管理，避免重复请求
- Axios 401 刷新合并，防止并发刷新风暴
- Nginx 静态资源长缓存，HTML 不缓存
- Gzip 开启

## 已验证结果

- 后端 `mvn clean verify` 通过，JaCoCo 行覆盖率门禁 70%
- 前端类型检查、16 项测试、覆盖率命令与生产构建通过
- Docker Compose 四个服务真实启动并健康
- 真实 E2E 断言全部通过（数量以脚本输出为准）

## 已知待优化项

1. 前端 ECharts 与主包 chunk 较大，可按页面进一步拆分
2. `vite.config.ts` 使用 `__dirname`，在非 Node 环境有兼容性告警
3. 后端限流当前为进程内实现，多实例部署需迁移至 Redis
4. 管理端统计为实时聚合，数据量增大后可增加物化统计表
5. 测评题目与人格解析可增加缓存 TTL 与主动失效策略
6. E2E 脚本目前依赖 PowerShell 7，若需跨平台可改为 Node 或 Java 测试
