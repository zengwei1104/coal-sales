# ===== 构建阶段：Maven + JDK 21 打包 =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 配置阿里云 Maven 镜像，大幅加速国内依赖下载
RUN mkdir -p /root/.m2 && \
    printf '%s\n' \
    '<settings>' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>aliyun</id>' \
    '      <name>Aliyun Maven</name>' \
    '      <url>https://maven.aliyun.com/repository/public</url>' \
    '      <mirrorOf>central</mirrorOf>' \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' \
    > /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== 运行阶段：只保留 JRE + jar =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/coal-sales-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
