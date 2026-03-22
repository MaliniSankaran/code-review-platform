# Code Review Platform

Real-time collaborative code review platform with AI-powered analysis and microservices architecture.

## Description

A production-grade distributed system for code review, combining automated AI analysis with real-time human collaboration. Think GitHub Pull Requests + Google Docs + ChatGPT for code review.

### Key Features
- JWT-based authentication with role-based access control
- Repository and code file management with S3-compatible object storage
- Pull request workflow with status tracking (OPEN, CLOSED, MERGED)
- Three-level comment system (PR-level, file-level, line-level)
- Ownership-based authorization across all resources
- Centralized exception handling with consistent error responses
- Fully containerized with Docker (multi-stage builds)
- Environment-based configuration with secret management

### Architecture

Currently a monolithic Spring Boot application, evolving toward microservices architecture with event-driven communication and AI-powered code analysis.

```
┌───────────────────────────────────────────────────────┐
│ Docker (crp-network)                                  │
│                                                       │
│ ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│ │  crp-app     │  │ crp-postgres │  │  crp-minio   │ │
│ │  Spring Boot │─►│ PostgreSQL 15│  │  Object Store │ │
│ │  Port 8080   │  │ Port 5432    │  │  Port 9000   │ │
│ │              │─►│              │  │  Console 9001│ │
│ └──────────────┘  └──────────────┘  └──────────────┘ │
│                                                       │
└───────────────────────────────────────────────────────┘
```

## Tech Stack

| Category       | Technology                          |
|----------------|-------------------------------------|
| Backend        | Spring Boot 4.0.2, Spring Security  |
| Database       | PostgreSQL 15                       |
| Object Storage | MinIO (S3-compatible)               |
| Authentication | JWT, BCrypt                         |
| ORM            | Spring Data JPA, Hibernate          |
| DevOps         | Docker, Docker Compose              |
| Build          | Maven                               |

## Getting Started

### Prerequisites
- Docker and Docker Compose installed
- Java 17+ (for local development)
- Maven (for local development)

### Setup

1. **Clone the repository**
```bash
git clone <your-repo-url>
cd code-review-platform
```

2. **Create environment file**

Create a `.env` file in the project root:
```
POSTGRES_PASSWORD=your_password_here
MINIO_ROOT_PASSWORD=your_minio_password_here
JWT_SECRET=your-256-bit-secret-key-here
```

3. **Run with Docker (recommended)**
```bash
docker-compose up -d --build
```
This starts the application, PostgreSQL, and MinIO. Access the API at `http://localhost:8080` and MinIO console at `http://localhost:9001`.

4. **Run locally (for development)**
```bash
docker-compose up -d postgres minio
./mvnw spring-boot:run
```

## API Endpoints

### Authentication
| Method | Endpoint             | Access  | Description              |
|--------|----------------------|---------|--------------------------|
| POST   | `/api/auth/register` | Public  | Register a new user      |
| POST   | `/api/auth/login`    | Public  | Login and receive JWT    |
| GET    | `/api/auth/me`       | Private | Get current user profile |

### Repositories
| Method | Endpoint                   | Access  | Description              |
|--------|----------------------------|---------|--------------------------|
| POST   | `/api/repositories`        | Private | Create a repository      |
| GET    | `/api/repositories`        | Private | List my repositories     |
| GET    | `/api/repositories/{id}`   | Private | Get specific repository  |
| PUT    | `/api/repositories/{id}`   | Private | Update repository (owner)|
| DELETE | `/api/repositories/{id}`   | Private | Delete repository (owner)|

### Files
| Method | Endpoint                                          | Access  | Description              |
|--------|---------------------------------------------------|---------|--------------------------|
| POST   | `/api/repositories/{repoId}/files`                | Private | Upload file to repo      |
| GET    | `/api/repositories/{repoId}/files`                | Private | List files in repo       |
| GET    | `/api/repositories/{repoId}/files/{fileId}`       | Private | Get file metadata        |
| GET    | `/api/repositories/{repoId}/files/{fileId}/download` | Private | Download file content |
| DELETE | `/api/repositories/{repoId}/files/{fileId}`       | Private | Delete file (owner)      |

### Pull Requests
| Method | Endpoint                                             | Access  | Description              |
|--------|------------------------------------------------------|---------|--------------------------|
| POST   | `/api/repositories/{repoId}/pulls`                   | Private | Create a pull request    |
| GET    | `/api/repositories/{repoId}/pulls`                   | Private | List PRs in repo         |
| GET    | `/api/repositories/{repoId}/pulls/{prId}`            | Private | Get specific PR          |
| PATCH  | `/api/repositories/{repoId}/pulls/{prId}/status`     | Private | Update PR status (author)|
| DELETE | `/api/repositories/{repoId}/pulls/{prId}`            | Private | Delete PR (author)       |

### Comments
| Method | Endpoint                              | Access  | Description              |
|--------|---------------------------------------|---------|--------------------------|
| POST   | `/api/pulls/{prId}/comments`          | Private | Add comment to PR        |
| GET    | `/api/pulls/{prId}/comments`          | Private | List comments on PR      |
| DELETE | `/api/pulls/{prId}/comments/{commentId}` | Private | Delete comment (author)|

> Detailed API documentation with request/response examples will be available via Swagger/OpenAPI.

## Database Schema

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│     users        │     │  repositories    │     │   code_files     │
├──────────────────┤     ├──────────────────┤     ├──────────────────┤
│ id          (PK) │◄──┐ │ id          (PK) │◄──┐ │ id          (PK) │
│ username         │   │ │ name             │   │ │ filename         │
│ email            │   │ │ description      │   │ │ file_path        │
│ password_hash    │   ├─│ owner_id    (FK) │   │ │ content_type     │
│ full_name        │   │ │ language         │   │ │ size             │
│ role             │   │ │ is_public        │   ├─│ repository_id(FK)│
│ is_active        │   │ │ created_at       │   │ │ uploaded_by_id(FK)│──┐
│ created_at       │   │ │ updated_at       │   │ │ created_at       │  │
│ updated_at       │   │ └──────────────────┘   │ │ updated_at       │  │
└──────────────────┘   │                        │ └──────────────────┘  │
        ▲              │ ┌──────────────────┐   │                       │
        │              │ │  pull_requests   │   │ ┌──────────────────┐  │
        │              │ ├──────────────────┤   │ │    comments      │  │
        │              │ │ id          (PK) │◄──┤ ├──────────────────┤  │
        │              │ │ title            │   │ │ id          (PK) │  │
        │              │ │ description      │   │ │ content (TEXT)   │  │
        │              │ │ status           │   │ │ pull_request_id(FK)│ │
        │              ├─│ repository_id(FK)│   │ │ code_file_id(FK) │─┘
        │              │ │ author_id   (FK) │──┘  │ line_number      │
        └──────────────┤ │ created_at       │     │ author_id   (FK) │
                       │ │ updated_at       │     │ created_at       │
                       │ └──────────────────┘     │ updated_at       │
                       │                          └──────────────────┘
                       └──── FK to users
```

## Author

Malini Janaki Sankaran