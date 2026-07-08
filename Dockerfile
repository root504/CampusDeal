# 阶段1：构建前端
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY campusdeal-client/package*.json ./
RUN npm ci
COPY campusdeal-client/ ./
RUN npm run build

# 阶段2：构建后端
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY campusdeal-server/pom.xml ./
RUN mvn dependency:go-offline -B
COPY campusdeal-server/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -B

# 阶段3：运行
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx256m -Xms128m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
