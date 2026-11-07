# ===== 构建阶段：Node 22 打包前端 =====
FROM node:22-alpine AS build
WORKDIR /app

# 强制使用官方 npm 源（避免 lock 文件里的 npmmirror 地址导致超时）
RUN npm config set registry https://registry.npmjs.org/

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ===== 运行阶段：Nginx 托管静态文件 =====
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
