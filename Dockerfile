# 第一阶段：构建阶段
FROM litongjava/maven:3.8.8-jdk8u391 AS builder

# 设置工作目录
WORKDIR /src

# 复制pom.xml并下载依赖
COPY pom.xml /src/
COPY src /src/src

# 运行maven打包命令
RUN mvn package -DskipTests -Pproduction

# 第二阶段：运行阶段
FROM litongjava/jdk:8u391-stable-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制生成的jar文件到运行阶段
COPY --from=builder /src/target/playwright-server-1.0.0.jar /app/

# 安装Chromium浏览器
RUN apt update && apt install chromium -y && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

# 下载Playwright依赖
RUN java -jar /app/playwright-server-1.0.0.jar --download

# 运行jar文件
CMD ["java", "-Xmx900m","-Xms512m","-jar", "playwright-server-1.0.0.jar"]
