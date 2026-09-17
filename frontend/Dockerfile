# 多阶段构建 - 构建阶段
FROM node:22-alpine AS builder

WORKDIR /build

# 复制 package.json 并安装依赖（利用 Docker 缓存）
COPY package*.json ./
RUN npm ci --prefer-offline --no-audit

# 复制源代码并构建
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:1.25-alpine

# 创建非 root 用户
RUN addgroup -S nginx-custom && adduser -S nginx-custom -G nginx-custom

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 设置权限
RUN chown -R nginx-custom:nginx-custom /usr/share/nginx/html && \
    chown -R nginx-custom:nginx-custom /var/cache/nginx && \
    chown -R nginx-custom:nginx-custom /var/log/nginx && \
    chown -R nginx-custom:nginx-custom /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx-custom:nginx-custom /var/run/nginx.pid

# 切换到非 root 用户
USER nginx-custom

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:80/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
