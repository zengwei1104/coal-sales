# ===== 构建阶段：Maven + JDK 21 打包 =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ===== 运行阶段：只保留 JRE + jar =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/coal-sales-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
