# CI/CD 说明

## 当前工作流

仓库使用 `.github/workflows/pr-check.yml`，在 Pull Request 和 `main` 分支推送时执行。

### 后端检查

1. 使用 Java 17（Temurin）
2. Maven 依赖缓存
3. `mvn clean verify`
4. 运行全部后端测试与 JaCoCo 覆盖率门禁
5. 上传 JaCoCo 报告

后端测试使用 H2 内存数据库，不依赖 MySQL 或 Redis service container。

### 前端检查

1. 使用 Node.js 22
2. `npm ci`
3. `npm run type-check`
4. `npm test -- --run`
5. `npm run test:coverage -- --run`
6. `npm run build`
7. 上传覆盖率到 Codecov

### Docker 检查

后端与前端测试通过后：

1. 使用 `docker/setup-buildx-action@v3`
2. 使用 `docker/build-push-action@v6`
3. 分别构建后端与前端镜像
4. 验证多阶段 Dockerfile 可构建

## 本地复现

```bash
cd backend
mvn clean verify

cd ../frontend
npm ci
npm run type-check
npm test -- --run
npm run test:coverage -- --run
npm run build

cd ..
docker compose config --quiet
docker compose build
```

## 覆盖率

- 后端 JaCoCo 行覆盖率门禁：70%
- 前端 Vitest 生成覆盖率报告
- Codecov 使用 `codecov/codecov-action@v5`

## 分支与提交规范

- 主分支：`main`
- 功能分支默认使用 `codex/` 前缀
- 提交信息采用 Conventional Commits：
  - `feat:` 功能
  - `fix:` 修复
  - `test:` 测试
  - `docs:` 文档
  - `chore:` 工程杂项

## 发布建议

当前工作流只做测试与镜像构建验证，不自动推送生产镜像。正式发布时建议增加：

1. GitHub Container Registry 或 Docker Hub 登录
2. 基于 Git SHA 的不可变镜像标签
3. 镜像漏洞扫描（Trivy 或 Grype）
4. 部署到 staging 后执行 `scripts/e2e-verify.ps1`
5. 人工审批后发布生产
6. 记录镜像 digest 与回滚版本
