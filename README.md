# Code Review Platform

Real-time collaborative code review platform with AI-powered analysis and microservices architecture.

## Description

A production-grade distributed system for code review, combining automated AI analysis with real-time human collaboration. Think GitHub Pull Requests + Google Docs + ChatGPT for code review.

### Key Features
- JWT-based authentication with role-based access control
- Stateless security with BCrypt password hashing
- Centralized exception handling with consistent error responses
- Fully containerized with Docker (multi-stage builds)
- Environment-based configuration with secret management

### Architecture

Currently a monolithic Spring Boot application, evolving toward microservices architecture with event-driven communication and AI-powered code analysis.

```
┌─────────────────────────────────────────────┐
│ Docker (crp-network)                        │
│                                             │
│ ┌──────────────┐     ┌──────────────┐       │
│ │  crp-app     │────►│ crp-postgres │       │
│ │  Spring Boot │     │ PostgreSQL 15│       │
│ │  Port 8080   │     │ Port 5432    │       │
│ └──────────────┘     └──────────────┘       │
│                                             │
└─────────────────────────────────────────────┘
```

## Tech Stack

| Category       | Technology                          |
|----------------|-------------------------------------|
| Backend        | Spring Boot 4.0.2, Spring Security  |
| Database       | PostgreSQL 15                       |
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
JWT_SECRET=your-256-bit-secret-key-here
```

3. **Run with Docker (recommended)**
```bash
docker-compose up -d --build
```
This starts both the application and PostgreSQL. Access the API at `http://localhost:8080`.

4. **Run locally (for development)**
```bash
docker-compose up -d postgres
./mvnw spring-boot:run
```

## API Endpoints

| Method | Endpoint             | Access  | Description              |
|--------|----------------------|---------|--------------------------|
| POST   | `/api/auth/register` | Public  | Register a new user      |
| POST   | `/api/auth/login`    | Public  | Login and receive JWT    |
| GET    | `/api/auth/me`       | Private | Get current user profile |
| GET    | `/api/auth/health`   | Public  | Health check             |

> Detailed API documentation with request/response examples will be available via Swagger/OpenAPI.

## Database Schema

```
┌─────────────────────────────┐
│          users               │
├─────────────────────────────┤
│ id          BIGSERIAL (PK)  │
│ username    VARCHAR UNIQUE   │
│ email       VARCHAR UNIQUE   │
│ password_hash VARCHAR        │
│ full_name   VARCHAR          │
│ role        VARCHAR          │
│ is_active   BOOLEAN          │
│ created_at  TIMESTAMP        │
│ updated_at  TIMESTAMP        │
└─────────────────────────────┘
```

## Author

Malini Janaki Sankaran