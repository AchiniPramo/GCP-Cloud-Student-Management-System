# Cloud Relational Databases in Action

A full-stack **Student Management System** built with Spring Boot REST API, responsive web UI, and Google Cloud Platform (GCP) integration. This application demonstrates cloud-native development with PostgreSQL, Google Cloud Storage, and modern web technologies.

## About

This project is part of the **Enterprise Cloud Application (ECA)** module in the **Higher Diploma in Software Engineering (HDSE)** program at the **Institute of Software Engineering (IJSE)**. It showcases real-world cloud application development patterns and best practices.

## Objective

Successfully migrate and build a **cloud-enabled student management system** that:
- ✅ Stores student data in **GCP Cloud SQL (PostgreSQL)**
- ✅ Uploads and manages student photos in **Google Cloud Storage**
- ✅ Provides a responsive web interface for CRUD operations
- ✅ Implements REST API with proper validation and error handling
- ✅ Demonstrates cloud infrastructure best practices

## Features

✨ **Core Functionality:**
- Create, Read, Update, Delete (CRUD) students
- Upload student photos to GCP Cloud Storage
- Real-time image display in student table
- Search students by name or contact number
- Duplicate contact number validation
- Responsive mobile-friendly UI

📊 **Technical Highlights:**
- REST API with FormData support for file uploads
- Cloud SQL database with auto-migration
- Public image serving from GCS bucket
- CORS-enabled for frontend-backend communication
- Comprehensive error handling and logging

## Tech Stack

### Backend
- **Java 25** - Latest Java features with preview support
- **Spring Boot 4.0.2** - Modern Spring Boot framework
- **Spring Data JPA** - ORM with Hibernate 7.2.1
- **Google Cloud Storage** - Image file storage
- **Google Auth Libraries** - GCP authentication
- **Lombok** - Annotation processor for boilerplate reduction
- **Maven** - Dependency management and build automation

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Responsive grid layout with gradient design
- **Vanilla JavaScript** - Async/await API calls, FormData handling

### Cloud Infrastructure
- **Google Cloud SQL** - PostgreSQL 18.2 at `34.93.174.151:5432`
- **Google Cloud Storage** - Bucket: `silent-bird-489817-g0-bucket`
- **GCP Project** - `silent-bird-489817-g0`

### DevOps & Containerization
- **Docker** - Container orchestration and deployment
- **Docker Compose** - Multi-container application setup
- **PostgreSQL Container** - Dockerized database service

## Docker & Containers Overview

### What is Docker?
**Docker** is a containerization platform that packages your entire application (code, dependencies, runtime) into a lightweight, portable **container** that can run consistently across different environments (laptop, server, cloud).

### What are Docker Containers?
A **container** is:
- 🐳 A lightweight, standalone, executable package
- 📦 Contains everything needed to run the application
- 🔒 Isolated from the host system and other containers
- ⚡ Starts in milliseconds (vs. seconds for VMs)
- 🌍 Works the same on Windows, Mac, Linux, and Cloud

### Why Docker for this Project?
- **Consistency**: Application runs identically everywhere
- **Portability**: Easy deployment to cloud, servers, or local machines
- **PostgreSQL Integration**: Run database inside a container
- **Scalability**: Easy to scale horizontally with container orchestration
- **Development**: Local environment mirrors production setup

## Docker Setup Instructions

### Prerequisites
- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop)
- **Docker Compose** - Included with Docker Desktop
- Verify installation: `docker --version` and `docker-compose --version`

### PostgreSQL in Docker Container

#### Option 1: Run PostgreSQL Container Directly

```bash
# Pull PostgreSQL image
docker pull postgres:18

# Run PostgreSQL container
docker run --name student-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=0786994314dS \
  -e POSTGRES_DB=eca \
  -p 5432:5432 \
  -v pgdata:/var/lib/postgresql/data \
  -d postgres:18
```

**Parameters Explained:**
- `--name student-db` - Container name
- `-e POSTGRES_USER` - Database user
- `-e POSTGRES_PASSWORD` - Database password
- `-e POSTGRES_DB` - Default database to create
- `-p 5432:5432` - Port mapping (host:container)
- `-v pgdata:/var/lib/postgresql/data` - Data persistence volume
- `-d` - Run in detached mode

#### Verify PostgreSQL is Running
```bash
# Check container status
docker ps -a

# View logs
docker logs student-db

# Connect to database
docker exec -it student-db psql -U postgres -d eca
```

### Docker Compose (Recommended for Local Development)

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:18
    container_name: student-db
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 0786994314dS
      POSTGRES_DB: eca
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - student-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: student-management-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/eca
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: 0786994314dS
      SPRING_PROFILES_ACTIVE: docker
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - student-network

volumes:
  pgdata:
    driver: local

networks:
  student-network:
    driver: bridge
```

#### Start Services with Docker Compose
```bash
# Start all services (PostgreSQL + Spring Boot app)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Remove volumes (clean database)
docker-compose down -v
```

### Dockerfile for Spring Boot Application

Create `Dockerfile` in project root:

```dockerfile
# Build stage
FROM maven:3.8.1-openjdk-25 as builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:25-slim
WORKDIR /app
COPY --from=builder /app/target/Cloud-Relational-Databases-in-Action-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/employees || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and Run Docker Image

```bash
# Build custom Docker image
docker build -t student-management-app:latest .

# Run container with local PostgreSQL
docker run --name student-app \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eca \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=0786994314dS \
  -p 8080:8080 \
  --network host \
  student-management-app:latest
```

### Docker Commands Reference

```bash
# Image Operations
docker images                          # List all images
docker build -t name:tag .            # Build image
docker pull image:tag                 # Download image
docker rmi image:tag                  # Delete image

# Container Operations
docker ps                             # List running containers
docker ps -a                          # List all containers
docker run [options] image            # Create and start container
docker start container_name           # Start stopped container
docker stop container_name            # Stop running container
docker rm container_name              # Delete container
docker logs container_name            # View container logs
docker exec -it container_name bash   # Execute command in container

# Volume Operations
docker volume ls                      # List volumes
docker volume create vol_name         # Create volume
docker volume rm vol_name             # Delete volume
docker volume inspect vol_name        # View volume details

# Network Operations
docker network ls                     # List networks
docker network create net_name        # Create network
docker network inspect net_name       # View network details
```

### Production Deployment with Docker

#### Push to Docker Hub
```bash
# Login to Docker Hub
docker login

# Tag image with Docker Hub username
docker tag student-management-app:latest username/student-management-app:latest

# Push image
docker push username/student-management-app:latest
```

#### Deploy on Cloud (GCP Cloud Run, AWS ECS, etc.)
```bash
# GCP Cloud Run example
gcloud run deploy student-app \
  --image gcr.io/project-id/student-management-app \
  --platform managed \
  --memory 512Mi \
  --set-env-vars SPRING_PROFILES_ACTIVE=gcp \
  --region us-central1
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Web Browser                           │
│        (HTML5/CSS3/JavaScript Frontend)                 │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/REST
                     ↓
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Application                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Controllers  │  │  Services    │  │  Repositories│  │
│  │  (REST API)  │  │  (Business)  │  │   (Data)     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────┬──────────────────────────────┬───────────────┘
           │                              │
           ↓                              ↓
    ┌──────────────┐            ┌──────────────────┐
    │  GCP Cloud   │            │ Google Cloud     │
    │  SQL         │            │ Storage (Images) │
    │  PostgreSQL  │            │  Bucket          │
    └──────────────┘            └──────────────────┘
```

## Database Schema

**Employee Table:**
```sql
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact VARCHAR(20) NOT NULL UNIQUE,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## API Endpoints

### REST API

| Method   | Endpoint                      | Description                    |
|----------|-------------------------------|--------------------------------|
| `GET`    | `/api/v1/employees`           | Get all students               |
| `GET`    | `/api/v1/employees/{id}`      | Get student by ID              |
| `POST`   | `/api/v1/employees`           | Create new student with image  |
| `PUT`    | `/api/v1/employees/{id}`      | Update student and/or image    |
| `DELETE` | `/api/v1/employees/{id}`      | Delete student and image       |

### Request Format

**Create/Update Student (FormData):**
```
name: string (required)
address: string (required)
contact: string (required, unique, 10+ digits)
image: file (optional, max 50MB)
```

### Response Format

```json
{
  "id": 1,
  "name": "John Doe",
  "address": "123 Main Street, Colombo",
  "contact": "0771234567",
  "imageUrl": "https://storage.googleapis.com/silent-bird-489817-g0-bucket/uuid_filename.jpg"
}
```

## Screenshots

### Student Management UI
![Student List Interface](src/main/resources/screencapture-localhost-8080-2026-03-14-12_46_41.png)

### Student Form with Image Upload
![Student Form](src/main/resources/Screenshot%202026-03-14%20125124.png)

### GCP Cloud Storage Bucket
![GCS Bucket](src/main/resources/gcp-bucket%20for%20images%20store.png)

## Getting Started

### Prerequisites

⚠️ **IMPORTANT: PostgreSQL is REQUIRED for this application.**

**Option 1: Local PostgreSQL Installation**
- **PostgreSQL 18+** - [Download here](https://www.postgresql.org/download/)
- **pgAdmin** (optional) - Database management GUI

**Option 2: Docker Container (Recommended)**
- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop)
- **Docker Compose** - Included with Docker Desktop

**Common Requirements**
- Java 25+ - [Download here](https://openjdk.org/projects/jdk/25/)
- Maven 3.8+ - [Download here](https://maven.apache.org/download.cgi)
- Google Cloud Project with service account credentials (for image upload feature)

### Installation

#### Method 1: Using Docker Compose (Recommended - Automated Setup)

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Cloud-Relational-Databases-in-Action.git
   cd Cloud-Relational-Databases-in-Action
   ```

2. **Create docker-compose.yml**
   - See Docker section above for complete docker-compose file
   - File includes PostgreSQL and Spring Boot containers

3. **Start Services**
   ```bash
   docker-compose up -d
   ```
   This will:
   - Start PostgreSQL container automatically
   - Create database `eca` with user `postgres`
   - Build and start Spring Boot application
   - Everything runs on configured ports

4. **Access Application**
   ```
   http://localhost:8080
   ```

#### Method 2: Manual Setup (Local PostgreSQL)

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/Cloud-Relational-Databases-in-Action.git
   cd Cloud-Relational-Databases-in-Action
   ```

2. **Install PostgreSQL**
   - Download and install PostgreSQL 18+
   - Create database: `eca`
   - Create user: `postgres` with password `0786994314dS`
   - Grant privileges:
     ```sql
     CREATE DATABASE eca;
     CREATE USER postgres WITH PASSWORD '0786994314dS';
     GRANT ALL PRIVILEGES ON DATABASE eca TO postgres;
     ```

3. **Configure GCP Credentials** (Optional - Required for Image Upload)
   - Download service account JSON from GCP Console
   - Place it at: `src/main/resources/silent-bird-489817-g0-992c58ab9b27.json`

4. **Update Database Configuration**
   - Edit `src/main/resources/application-dev.yaml`:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/eca
         username: postgres
         password: 0786994314dS
     ```

5. **Build the Project**
   ```bash
   ./mvnw clean package -DskipTests
   ```

6. **Run Application (Dev Profile)**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

7. **Access Application**
   ```
   http://localhost:8080
   ```

#### Method 3: Using Docker Directly (PostgreSQL Container Only)

1. **Start PostgreSQL Container**
   ```bash
   docker run --name student-db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=0786994314dS \
     -e POSTGRES_DB=eca \
     -p 5432:5432 \
     -v pgdata:/var/lib/postgresql/data \
     -d postgres:18
   ```

2. **Verify Connection**
   ```bash
   docker exec -it student-db psql -U postgres -d eca
   ```

3. **Run Spring Boot Application**
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eca
   export SPRING_DATASOURCE_USERNAME=postgres
   export SPRING_DATASOURCE_PASSWORD=0786994314dS
   ./mvnw spring-boot:run
   ```

### PostgreSQL Configuration Details

**Database Name:** `eca`  
**User:** `postgres`  
**Password:** `0786994314dS`  
**Port:** `5432`  
**Default Host:** `localhost` (local) or `postgres` (Docker container)

**Connection String Examples:**
```
# Local PostgreSQL
jdbc:postgresql://localhost:5432/eca

# Docker Container
jdbc:postgresql://postgres:5432/eca

# GCP Cloud SQL
jdbc:postgresql://34.93.174.151:5432/eca
```

3. **GCP Setup** (Optional - for image upload to cloud)
   - Configure GCP Credentials
   - Create GCS bucket
   - Update `src/main/resources/application-gcp.yaml` with GCP Cloud SQL credentials:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://YOUR_CLOUD_SQL_IP:5432/eca
         username: postgres
         password: YOUR_PASSWORD
     ```

4. **Create GCS Bucket**
   ```bash
   gsutil mb gs://silent-bird-489817-g0-bucket
   gsutil iam ch serviceAccount:allUsers:objectViewer gs://silent-bird-489817-g0-bucket
   ```

5. **Build the Project**
   ```bash
   ./mvnw clean package -DskipTests
   ```

6. **Run the Application**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=gcp"
   ```

7. **Access the Application**
   - Open browser: `http://localhost:8080`
   - Students table will load with all existing records
   - Add new student with image, edit, or delete students

## Configuration Files

### application.yaml
Default configuration with GCP profile activated.

### application-gcp.yaml
GCP-specific configuration:
- Cloud SQL PostgreSQL connection
- Multipart file upload limits (50MB)
- Hibernate auto-migration enabled

### application-dev.yaml
Local development configuration for testing.

## Project Structure

```
Cloud-Relational-Databases-in-Action/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── lk/ijse/eca/cloud_rdbms/
│   │   │       ├── CloudRelationalDatabasesInActionApplication.java
│   │   │       ├── controller/
│   │   │       │   └── EmployeeController.java
│   │   │       ├── entity/
│   │   │       │   └── Employee.java
│   │   │       ├── repository/
│   │   │       │   └── EmployeeRepository.java
│   │   │       ├── service/
│   │   │       │   └── GcsImageService.java
│   │   │       └── config/
│   │   │           └── GcsConfig.java
│   │   ├── resources/
│   │   │   ├── application.yaml
│   │   │   ├── application-gcp.yaml
│   │   │   ├── application-dev.yaml
│   │   │   ├── static/
│   │   │   │   ├── index.html
│   │   │   │   ├── styles.css
│   │   │   │   └── app.js
│   │   │   └── gcp-bucket-credentials.json
│   └── test/
└── pom.xml
```

## Key Components

### EmployeeController
REST API controller handling CRUD operations with FormData support for image uploads.

### GcsImageService
Service for uploading and deleting images from Google Cloud Storage bucket.

### GcsConfig
Spring configuration bean for initializing Google Cloud Storage client.

### Employee Entity
JPA entity with fields: id, name, address, contact, imageUrl.

### Frontend (app.js)
Client-side JavaScript managing API calls, form validation, and UI interactions.

## Validation Rules

- **Name**: Required, non-empty
- **Address**: Required, non-empty  
- **Contact**: Required, unique, 10+ digits (numeric only)
- **Image**: Optional, max 50MB, JPEG/PNG recommended

## Error Handling

- **Duplicate Contact**: Returns 400 with message "Contact number already exists"
- **Invalid Image**: Image fails silently, student still creates
- **Missing Fields**: Returns 400 with validation error messages
- **Missing Image on Load**: Shows 📷 placeholder emoji
- **GCS Unavailable**: Image upload skipped, student data still saved

## Deployment Notes

### File Size Configuration
Max file upload is set to 50MB. To change:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
```

### GCS Bucket Permissions
Ensure the bucket is public:
```bash
gsutil iam ch serviceAccount:allUsers:objectViewer gs://silent-bird-489817-g0-bucket
```

### Environment Variables (Alternative to File)
Instead of credentials file, set:
```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

## Testing

### Manual Testing via Web UI
1. Navigate to `http://localhost:8080`
2. Add student with photo - image uploads to GCS
3. View students in table with photos
4. Edit student or photo - old image deleted, new uploaded
5. Delete student - image removed from GCS

### API Testing with cURL
```bash
# Get all students
curl http://localhost:8080/api/v1/employees

# Create student with image
curl -X POST http://localhost:8080/api/v1/employees \
  -F "name=John Doe" \
  -F "address=Colombo" \
  -F "contact=0771234567" \
  -F "image=@/path/to/image.jpg"

# Update student
curl -X PUT http://localhost:8080/api/v1/employees/1 \
  -F "name=Jane Doe" \
  -F "image=@/path/to/new-image.jpg"

# Delete student
curl -X DELETE http://localhost:8080/api/v1/employees/1
```

## Troubleshooting

### Images Not Showing
- ✅ Check console logs (F12) for image URL
- ✅ Verify bucket is publicly accessible
- ✅ Check image URL format: `https://storage.googleapis.com/bucket-name/filename`

### GCS Connection Failed
- ✅ Verify credentials file path: `src/main/resources/silent-bird-489817-g0-992c58ab9b27.json`
- ✅ Check service account has Storage permissions
- ✅ Verify project ID in GcsConfig matches GCP project

### Upload Fails (413 Content Too Large)
- ✅ Increase multipart size limits in application.yaml
- ✅ Check image file size is under 50MB

### Database Connection Failed
- ✅ Verify Cloud SQL IP address and port
- ✅ Check database name (`eca`) exists
- ✅ Confirm username/password credentials
- ✅ Whitelist your IP in Cloud SQL

## Lessons Learned

1. **Cloud Database Integration** - Setting up external cloud SQL databases with proper authentication
2. **Image File Management** - Storing files in cloud storage and serving public URLs
3. **FormData Handling** - Processing multipart form data in Spring Boot REST API
4. **Service Account Authentication** - Using GCP service accounts for programmatic access
5. **Responsive Web Design** - Creating mobile-friendly frontends with HTML/CSS/JS
6. **API Validation** - Implementing business logic validation before persistence
7. **Error Resilience** - Gracefully handling optional features (images) failure

## Future Enhancements

- 📧 Email notifications on student creation/deletion
- 🔐 User authentication and authorization
- 📊 Student analytics dashboard
- 🖼️ Image compression and thumbnails
- 📱 Mobile app (React Native/Flutter)
- 🔄 Automated backups to Cloud Storage
- 🚀 CI/CD pipeline with Cloud Build

## License

This project is for educational purposes within the IJSE program.

## Need Help?

If you encounter issues:
1. Check the console logs (F12 in browser)
2. Review server logs for detailed error traces
3. Verify GCP credentials and permissions
4. Check database connectivity
5. Reach out via Slack workspace or create an issue

## Author

Developed as part of IJSE ECA Module - Cloud-Relational-Databases-in-Action

---

**Last Updated**: March 14, 2026  
**Status**: ✅ Fully Functional with Image Upload to GCS