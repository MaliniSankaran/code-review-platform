# Code Review Platform

Real-time collaborative code review platform with AI-powered analysis and microservices architecture.

## TL;DR

- Distributed code review platform inspired by GitHub PRs + Google Docs
- Built using Spring Boot microservices with API Gateway + Kafka event-driven architecture
- Supports real-time collaboration with JWT auth, RBAC, and role-based access control
- Includes file management (MinIO), caching (Redis), and centralized shared library for consistency
- Fully containerized with Docker Compose across 10+ services

## Description

A production-grade distributed system for code review, combining automated AI analysis with real-time human collaboration, built as a microservices architecture. Think GitHub Pull Requests + Google Docs + ChatGPT for code review.

### Key Features
- API Gateway as single entry point with centralized JWT validation, CORS, and request logging
- JWT-based authentication with role-based access control
- Repository and code file management with S3-compatible object storage
- Pull request workflow with status tracking (OPEN, CLOSED, MERGED)
- Three-level comment system (PR-level, file-level, line-level)
- Ownership-based authorization across all resources
- Shared common-lib module — centralized entities, exceptions, and DTOs
- Centralized exception handling with consistent error responses
- Event-driven architecture with Apache Kafka — PR and comment activity published as events
- Dedicated notification-service as a pure Kafka consumer — no database, no REST endpoints
- Fully containerized with Docker (multi-stage builds, monorepo build context)
- Environment-based configuration with secret management
- Auto-generated API documentation via Swagger/OpenAPI on all services

### Architecture

Five independently deployable Spring Boot services sharing a common library, all running on a Docker bridge network. All client traffic flows through the API Gateway. PR and comment events flow asynchronously through Kafka to the notification service.

```
                        Client (browser / Postman)
                                   │
                                   ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ Docker (crp-network)                                                      │
│                                                                           │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                 gateway-service  port 8888                      │      │
│  │         JWT filter · CORS · logging · routing                   │      │
│  └──────────┬──────────────────┬──────────────────┬────────────────┘      │
│             │                  │                  │                       │
│  ┌──────────▼───┐   ┌──────────▼───┐   ┌──────────▼───┐                   │
│  │ auth-service │   │ file-service │   │review-service│                   │
│  │  port 8081   │   │  port 8082   │   │  port 8080   │                   │
│  │  JWT · auth  │   │ repos · files│   │  analysis    │                   │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘                   │
│         │                  │                  │                           │
│  ┌──────▼──────────────────▼──────────────────▼────────┐                  │
│  │                     common-lib                      │                  │
│  │         User · Role · exceptions · shared DTOs      │                  │
│  └─────────────────────────────────────────────────────┘                  │
│                            │                                              │
│                   Kafka events (file-service publishes)                   │
│                            ▼                                              │
│                    ┌───────────────┐    ┌──────────────────────────┐      │
│                    │  crp-kafka    │───▶│  notification-service    │      │
│                    │  port 9092    │    │  port 8089               │      │
│                    └───────┬───────┘    │  Kafka consumer only     │      │
│                            │            └──────────────────────────┘      │
│                    ┌───────▼───────┐                                      │
│                    │ crp-zookeeper │                                      │
│                    └───────────────┘                                      │
│                                                                           │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐                   │
│  │ crp-postgres │   │  crp-redis   │   │  crp-minio   │                   │
│  │  port 5432   │   │  port 6379   │   │  port 9000   │                   │
│  └──────────────┘   └──────────────┘   └──────────────┘                   │
└───────────────────────────────────────────────────────────────────────────┘
```

### System Design Decisions (Current)
- API Gateway pattern for centralized authentication, routing, and request control
- Microservices separation by domain (auth, file, review, notifications)
- Kafka-based event-driven communication to decouple PR lifecycle events from notifications
- Shared common-lib module for consistent domain models across services
- MinIO object storage for scalable file handling
  
### Service Responsibilities

| Service | Port | Owns |
|---------|------|------|
| gateway-service | 8888 | Routing, JWT validation, CORS, request logging |
| auth-service | 8081 | User registration, login, JWT issuance |
| file-service | 8082 | Repositories, code files, pull requests, comments, MinIO, Kafka producer |
| review-service | 8080 | Code analysis, AI review (Week 7) |
| notification-service | 8089 | Kafka consumer — listens for PR/comment events |
| common-lib | — | User, Role entities, shared exceptions, shared DTOs |

### Kafka Topics

| Topic | Producer | Consumer | Trigger |
|-------|----------|----------|---------|
| `pr-created` | file-service | notification-service | Pull request created |
| `comment-added` | file-service | notification-service | Comment added to PR |
| `pr-updated` | file-service | notification-service | PR status changed |

## Tech Stack

| Category | Technology |
|----------|------------|
| Backend | Spring Boot 4.0.2, Spring Security |
| API Gateway | Spring Cloud Gateway 2025.1.1 |
| API Docs | Springdoc OpenAPI 2.8.6 (Swagger UI) |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Object Storage | MinIO (S3-compatible) |
| Messaging | Apache Kafka (Confluent 7.4.0) |
| Authentication | JWT, BCrypt |
| ORM | Spring Data JPA, Hibernate |
| Shared Library | common-lib (Maven module) |
| DevOps | Docker, Docker Compose |
| Build | Maven (multi-stage Dockerfiles) |

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
docker-compose up --build
```
This starts all services plus PostgreSQL, Redis, MinIO, Zookeeper, and Kafka.

4. **Run locally (for development)**

Start infrastructure first:
```bash
docker-compose up postgres redis minio zookeeper kafka
```
Then run each service from IntelliJ in this order: auth-service, file-service, review-service, gateway-service, notification-service.

> Note: Stop IntelliJ services before running `docker-compose up` — running both simultaneously splits Kafka consumer group partitions between local and Docker instances.

## API Endpoints

All endpoints are accessible via the API Gateway at `http://localhost:8888`. Individual service ports are for local development only.

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| GET | `/api/auth/me` | Private | Get current user profile |

### Repositories
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/repositories` | Private | Create a repository |
| GET | `/api/repositories` | Private | List my repositories |
| GET | `/api/repositories/{id}` | Private | Get specific repository |
| PUT | `/api/repositories/{id}` | Private | Update repository (owner) |
| DELETE | `/api/repositories/{id}` | Private | Delete repository (owner) |

### Files
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/repositories/{repoId}/files` | Private | Upload file to repo |
| GET | `/api/repositories/{repoId}/files` | Private | List files in repo |
| GET | `/api/repositories/{repoId}/files/{fileId}` | Private | Get file metadata |
| GET | `/api/repositories/{repoId}/files/{fileId}/download` | Private | Download file content |
| DELETE | `/api/repositories/{repoId}/files/{fileId}` | Private | Delete file (owner) |

### Pull Requests
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/repositories/{repoId}/pulls` | Private | Create a pull request |
| GET | `/api/repositories/{repoId}/pulls` | Private | List PRs in repo |
| GET | `/api/repositories/{repoId}/pulls/{prId}` | Private | Get specific PR |
| PATCH | `/api/repositories/{repoId}/pulls/{prId}/status` | Private | Update PR status (author) |
| DELETE | `/api/repositories/{repoId}/pulls/{prId}` | Private | Delete PR (author) |

### Comments
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/pulls/{prId}/comments` | Private | Add comment to PR |
| GET | `/api/pulls/{prId}/comments` | Private | List comments on PR |
| DELETE | `/api/pulls/{prId}/comments/{commentId}` | Private | Delete comment (author) |

### Analysis
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/analysis?language={language}&analysisType={type}` | Private | Analyse code by language and type |

Available analysis types: `PERFORMANCE`, `SECURITY`, `STYLE`

> Full interactive API documentation available via Swagger UI:
> - Auth: `http://localhost:8081/swagger-ui/index.html`
> - File/Repo/PR/Comments: `http://localhost:8082/swagger-ui/index.html`
> - Analysis: `http://localhost:8080/swagger-ui/index.html`

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
│ is_active        │   │ │ created_at       │   │ │uploaded_by_id(FK)│──┐
│ created_at       │   │ │ updated_at       │   │ │ created_at       │  │
│ updated_at       │   │ └──────────────────┘   │ │ updated_at       │  │
└──────────────────┘   │                        │ └──────────────────┘  │
        ▲              │ ┌──────────────────┐   │                       │
        │              │ │  pull_requests   │   │ ┌──────────────────┐  │
        │              │ ├──────────────────┤   │ ├──────────────────┤  │
        │              │ │ id          (PK) │◄──┤ │    comments      │  │
        │              │ │ title            │   │ ├──────────────────┤  │
        │              │ │ description      │   │ │ id          (PK) │  │
        │              │ │ status           │   │ │ content (TEXT)   │  │
        │              ├─│ repository_id(FK)│   │ │pull_request_id(FK)│ │
        │              │ │ author_id   (FK) │───┘ │ code_file_id(FK) │─┘
        └──────────────┤ │ created_at       │     │ line_number      │
                       │ │ updated_at       │     │ author_id   (FK) │
                       │ └──────────────────┘     │ created_at       │
                       │                          │ updated_at       │
                       │                          └──────────────────┘
                       └──── FK to users
```

> User and Role entities live in common-lib and are shared across all services via @EntityScan.

## Design Patterns

| Pattern | Where |
|---------|-------|
| Repository | JpaRepository for all DB access |
| DTO | All API responses use DTOs, never entities |
| Builder | Lombok @Builder on all entities and DTOs |
| Factory | FileProcessorFactory — auto-discovers file processors by extension |
| Strategy | AnalysisContext — runtime swap between Security/Performance/Style analysis |
| Observer | Spring ApplicationEvents for in-process PR/comment notifications |
| Filter Chain | JwtAuthenticationFilter (services), JwtAuthFilter (gateway) |
| Adapter | UserPrincipal adapts User entity to Spring Security UserDetails |

## Project Structure

```
code-review-platform/
├── gateway-service/        ← Spring Cloud Gateway, port 8888
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── auth-service/           ← Spring Boot, port 8081
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── file-service/           ← Spring Boot, port 8082, Kafka producer
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── review-service/         ← Spring Boot, port 8080
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── notification-service/   ← Spring Boot, port 8089, Kafka consumer only
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── common-lib/             ← Shared Maven module
│   ├── src/
│   └── pom.xml
├── docker-compose.yml      ← All 10 containers on crp-network
└── .env                    ← POSTGRES_PASSWORD, JWT_SECRET, MINIO_ROOT_PASSWORD
```

## Author

Malini Janaki Sankaran
