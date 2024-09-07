# Playwright-Server

This project demonstrates how to integrate Playwright with TioBoot to create a web scraping service that reduces overhead by managing Playwright instances efficiently. The solution initializes the Playwright browser instance at service startup and closes it properly upon service shutdown, ensuring optimal performance for high concurrency and low-latency scenarios.

## Features

- **Efficient Resource Management**: Playwright instance and browser are initialized once on service startup and reused for multiple requests.
- **Web Scraping**: Provides API endpoints for retrieving webpage content using Playwright.
- **HTML to Markdown Conversion**: Converts HTML content to Markdown format using `com.vladsch.flexmark`.
- **Dockerized Deployment**: The project is containerized for easy deployment.

## Prerequisites

- Java 1.8
- Maven 3.x
- Docker
- Playwright dependencies (e.g., Chromium)

## API Endpoints

### Get Web Page Content

**Endpoint**: `/playwright`

**Parameters**: `url` - The URL of the web page to retrieve.

**Example**:
```bash
curl "http://localhost/playwright?url=https://www.sjsu.edu/registrar/calendar/fall-2024.php"
```

### Convert HTML to Markdown

**Endpoint**: `/markdown`

**Parameters**: `url` - The URL of the web page to retrieve and convert.

**Example**:
```bash
curl "http://localhost/markdown?url=https://www.sjsu.edu/registrar/calendar/fall-2024.php"
```

## Build and Run with Docker

### Dockerfile

```Dockerfile
# First stage: Build
FROM litongjava/maven:3.8.8-jdk8u391 AS builder
WORKDIR /src
COPY pom.xml /src/
COPY src /src/src
RUN mvn package -DskipTests -Pproduction

# Second stage: Run
FROM litongjava/jdk:8u391-stable-slim
WORKDIR /app
COPY --from=builder /src/target/playwright-server-1.0.0.jar /app/

RUN apt update && apt install chromium -y && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
RUN java -jar /app/playwright-server-1.0.0.jar --download

CMD ["java", "-Xmx900m", "-Xms512m", "-jar", "playwright-server-1.0.0.jar"]
```

### Build and Run

Build the Docker image:

```bash
docker build -t litongjava/playwright-server:1.0.0 .
```

Run the Docker container:

```bash
docker run -p 8080:8080 litongjava/playwright-server:1.0.0
```

## Conclusion

This project integrates Playwright with TioBoot to provide a high-performance web scraping solution. By initializing the Playwright instance during service startup and releasing resources on shutdown, we can efficiently handle multiple requests without incurring the overhead of repeatedly starting the browser. The service is also containerized with Docker for easy deployment in any environment.