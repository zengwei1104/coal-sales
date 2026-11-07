# ===== 构建阶段：Node 22 打包前端 =====
FROM node:22-alpine AS build
WORKDIR /app

# 强制使用官方 npm 源
RUN npm config set registry https://registry.npmjs.org/

# 只复制 package.json，不带 lock 文件
# 原因：package-lock.json 在 Windows 上生成，只记录了 Windows 平台的 rolldown 原生绑定，
# 容器内（Linux/musl）按 lock 安装会缺 @rolldown/binding-linux-x64-musl，
# 导致 vite build 报 "Cannot find native binding"。让 npm 在容器内重新解析可解决。
COPY package.json ./
RUN npm install

COPY . .
RUN npm run build

# ===== 运行阶段：Nginx 托管静态文件 =====
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
