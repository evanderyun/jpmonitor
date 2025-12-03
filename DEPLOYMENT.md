# Deployment Guide - JPM ERP System

This guide outlines the steps to deploy the JPM ERP System (Spring Boot Backend + React Frontend) to a production environment.

## Prerequisites

- **Java 21 LTS** (OpenJDK)
- **PostgreSQL 15+**
- **Node.js 18+** (for building frontend)
- **Maven 3.9+** (for building backend)

---

## 1. Database Setup

Ensure PostgreSQL is running and create the database:

```sql
CREATE DATABASE jpm_db;
CREATE USER jpm_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE jpm_db TO jpm_user;
```

*Note: In production, use a strong password and update `application.yml` or environment variables accordingly.*

---

## 2. Backend Deployment (Spring Boot)

### Build the JAR
Navigate to the `backend-java` directory and run:

```bash
export JAVA_HOME="/path/to/java-21"
mvn clean package -DskipTests
```

This will generate an executable JAR file in `api/target/api-0.0.1-SNAPSHOT.jar`.

### Run the Application
You can run the application using `java -jar`:

```bash
java -jar api/target/api-0.0.1-SNAPSHOT.jar
```

### Environment Variables
For production, override default settings using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/jpm_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
java -jar api/target/api-0.0.1-SNAPSHOT.jar
```

---

## 3. Frontend Deployment (React/Vite)

### Build for Production
Navigate to the root directory and run:

```bash
npm install
npm run build
```

Set backend API base URL using environment variable before build:

```bash
export VITE_API_BASE_URL="https://your-backend-domain/api"
npm run build
```

This creates a `dist/` folder containing optimized static files (HTML, CSS, JS).

### Serving the Frontend
You can serve the `dist/` folder using any web server (Nginx, Apache, or a simple Node server).

**Example Nginx Configuration:**

```nginx
server {
    listen 80;
    server_name erp.jpm.com;

    root /var/www/jpm-erp/dist;
    index index.html;

    # Handle React Router (SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API requests to Backend
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 4. Docker Deployment (Optional)

You can containerize the application for easier deployment.

### Backend Dockerfile
Create `backend-java/Dockerfile`:
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY api/target/api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Frontend Dockerfile
Create `Dockerfile` in root:
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 5. Verification

1. Access the frontend URL (e.g., `http://localhost`).
2. Login and verify dashboard data loads.
3. Check backend logs for any errors.
