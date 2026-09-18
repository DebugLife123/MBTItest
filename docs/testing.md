# 测试说明

## 测试分层

### 后端

- JUnit 5 + Mockito：服务层与安全组件单元测试
- MockMvc：认证、测评、管理接口契约测试
- `@SpringBootTest`：上下文与关键集成路径
- H2 内存数据库：CI 不依赖 MySQL
- Redis 健康检查在测试中关闭，避免外部依赖
- JaCoCo：行覆盖率门禁 70%

运行：

```bash
cd backend
mvn clean verify
```

当前本地结果：后端测试全部通过，JaCoCo 门禁通过。

### 前端

- Vitest：工具、Store 与 API 行为测试
- Vue Test Utils：组件渲染与交互测试
- TypeScript：`vue-tsc --noEmit`
- Axios Adapter 模拟：测试 401 刷新、重试与并发合并

运行：

```bash
cd frontend
npm ci
npm run type-check
npm test -- --run
npm run test:coverage -- --run
npm run build
```

当前本地结果：4 个测试文件、16 项测试通过，覆盖率命令退出码 0，生产构建通过。

## 容器端到端测试

先启动完整环境：

```bash
docker compose down -v
docker compose up -d --build
docker compose ps
```

健康标准：

- `mbti-mysql`：healthy
- `mbti-redis`：healthy
- `mbti-backend`：healthy
- `mbti-frontend`：可访问

运行：

```powershell
pwsh -NoProfile -File ./scripts/e2e-verify.ps1
```

脚本覆盖：

1. 注册用户 A/B
2. 登录、当前用户、刷新令牌
3. 两个用户各自开始测评、获取 36 题、提交答案、完成测评
4. 结果历史、成长轨迹、职业建议、兼容度
5. CSV 导出与 UTF-8 BOM 原始字节验证
6. 管理员登录与 ADMIN 角色
7. 统计、用户列表、人格分布、完成率
8. 题目新增、编辑、删除
9. 人格解析读取与更新
10. 公共健康接口

真实 Docker 环境以脚本运行时输出的实际断言总数为准；断言集覆盖基础业务与 AI 全链路。

## CI 对应关系

- `.github/workflows/pr-check.yml` 执行后端与前端检查
- 后端使用 H2，不依赖 MySQL/Redis service container
- Docker Job 构建后端与前端镜像
- Codecov 上传覆盖率
- Node 版本为 22，Java 版本为 17

## 回归注意事项

- 修改 Flyway 已发布迁移后，本地必须执行 `docker compose down -v` 重新迁移
- 管理员默认账号为 `admin / admin123`，种子散列变更需重跑 E2E
- 修改认证响应结构时同步更新 `frontend/src/__tests__/api/request-auth.spec.ts`
- 修改统一响应或错误结构时同步更新后端控制器测试与 E2E 脚本
