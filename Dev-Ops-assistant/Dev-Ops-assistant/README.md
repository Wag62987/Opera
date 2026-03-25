# 🚀 DevOps Assistant

> **Mega Project | Final Presentation**  
> A Spring Boot–based intelligent backend system that automates end-to-end DevOps workflows using GitHub integration, JWT security, and dynamic CI/CD pipeline generation.

---

## 📌 Project Summary

**DevOps Assistant** eliminates the manual effort of setting up and monitoring CI/CD pipelines. Developers connect their GitHub repositories, choose their tech stack, and the system automatically generates, commits, and monitors GitHub Actions workflows — all secured with OAuth2 + JWT authentication.

| Attribute | Details |
|-----------|---------|
| **Type** | Backend Web Application |
| **Domain** | DevOps Automation |
| **Architecture** | Layered REST API (Spring Boot) |
| **Security** | OAuth2 GitHub Login + JWT |
| **Integration** | GitHub REST API v3 |
| **Build Tool** | Maven |
| **Language** | Java 17 |

---

## 🎯 Problem Statement

Setting up CI/CD pipelines requires deep DevOps knowledge. Developers waste hours configuring YAML files, debugging workflow errors, and monitoring build statuses across repositories.

**DevOps Assistant solves this by:**
- Automating GitHub Actions workflow generation for any project type
- Committing pipelines directly to repositories via the GitHub API
- Providing real-time CI/CD status monitoring and failure diagnostics
- Abstracting all DevOps complexity behind a clean REST API

---

## ✅ Features

### 🔐 Authentication & Security
- GitHub OAuth2 login flow
- JWT token generation and validation
- Stateless session management with `JwtFilter`
- Cookie and Authorization header token support
- Custom success handler for OAuth redirect

### 🐙 GitHub Integration
- Fetch all repositories for an authenticated user
- Import repositories into the system for tracking
- Commit auto-generated CI/CD workflow files to `.github/workflows/ci.yml`
- Update existing workflows (SHA-aware file update via GitHub Contents API)

### ⚙️ CI/CD Pipeline Generation
Supports automatic workflow generation for:

| Project Type | Build Tools Supported |
|-------------|----------------------|
| Spring Boot | Maven, Gradle |
| Node.js | npm, Yarn |
| React | npm, Yarn |
| Python | pip + pytest |

Additional options:
- **Docker** — auto-adds Docker image build step
- **CD (Continuous Deployment)** — appends deploy hook trigger via `curl`

### 📊 CI Status Monitoring
- Fetches latest GitHub Actions run status per repository
- Maps statuses: `SUCCESS`, `FAILED`, `RUNNING`, `NO_RUNS`
- Identifies the exact **failed step** when a pipeline breaks
- Returns logs URL for direct debugging

### 🔍 Workflow Run History
- Lists all past workflow runs per repository
- Shows commit SHA, branch, run status, and timestamp

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Client (Frontend)                 │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP Requests (JWT)
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot REST API                    │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │ Auth     │  │ GitHub    │  │ Deploy / CI      │  │
│  │Controller│  │Controller │  │ Controller       │  │
│  └────┬─────┘  └─────┬─────┘  └────────┬─────────┘  │
│       │              │                  │             │
│  ┌────▼──────────────▼──────────────────▼──────────┐ │
│  │              Service Layer                       │ │
│  │  AppUserService │ GithubService                 │ │
│  │  GithubWorkflowService │ GithubCommitService    │ │
│  │  GitHubActionsStatusService │ MonitoringService  │ │
│  └────────────────────────┬─────────────────────────┘│
│                           │                          │
│  ┌────────────────────────▼─────────────────────────┐│
│  │           Repository Layer (JPA)                 ││
│  │  AppUserRepository │ GitRepoRepository           ││
│  │  CICDConfigRepository                            ││
│  └────────────────────────┬─────────────────────────┘│
└───────────────────────────┼─────────────────────────┘
                            │
            ┌───────────────▼───────────────┐
            │            MySql DB           │
            └───────────────────────────────┘
                            │
            ┌───────────────▼───────────────┐
            │       GitHub REST API v3       │
            └───────────────────────────────┘
```

---

## 📦 Package Structure

```
src/main/java/com/Innocent/DevOpsAsistant/Devops/Assistant/
│
├── Config/                    # Security, WebClient, OAuth2 config
│   └── Jwt/                   # JWT filter, decoder, utility
│
├── Controller/
│   ├── Authentication/        # Auth endpoints
│   ├── AppUser/               # User info endpoint
│   └── Github/                # Repo, deploy, CI status endpoints
│
├── Service/                   # All business logic
├── Repository/                # JPA data access
├── Models/                    # JPA entity classes
├── DTOs/                      # API request/response objects
├── Exception/                 # Global exception handling
├── Interfaces/                # CrudService abstraction
└── Utility/                   # AuthenticatedUser helper
```

---

## 🔄 Key Workflows

### 1. Authentication Flow
```
User → GitHub OAuth2 Login
     → GitHub redirects with code
     → CustomSuccessHandler exchanges code for token
     → JWT generated and set as HttpOnly cookie
     → All subsequent API calls validated via JwtFilter
```

### 2. Deploy Flow
```
POST /deploy/{repoId}
     → Validate JWT → Fetch repo from DB
     → GithubWorkflowService generates YAML based on config
     → GithubCommitService commits YAML to GitHub via API
     → GitHubActionsStatusService polls latest run status
     → Returns pipeline status + logs URL
```

### 3. CI Status Check Flow
```
GET /ci-status/{repoId}
     → Fetch repo → Call GitHub Actions API
     → Parse latest run: conclusion + status
     → If FAILED → fetch job steps → identify failed step
     → Return structured CIStatusResponse
```

---

## 🧪 Testing

Comprehensive unit test coverage across all layers:

| Test Class | Layer | Tests |
|-----------|-------|-------|
| `AppUserServiceTest` | Service | 6 tests — save, find, DTO mapping |
| `GithubWorkflowServiceTest` | Service | 10 tests — all project types, Docker, CD |
| `GithubServiceTest` | Service | 9 tests — repos, import, duplicate check |
| `GitHubMonitoringServiceTest` | Service | 6 tests — status mapping, edge cases |
| `ControllerTest` | Controller | 12 tests — all endpoints, JWT scenarios |
| `RepositoryTest` | Repository | 7 tests — `@DataJpaTest` slice |

**Run all tests:**
```bash
mvn test
```

**Run a specific test class:**
```bash
mvn test -Dtest=GithubWorkflowServiceTest
```

---

## 🛠️ Technology Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + OAuth2 + JWT |
| HTTP Client | Spring WebClient (reactive) |
| Data Access | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Build | Maven |
| Testing | JUnit 5 + Mockito + AssertJ |
| External API | GitHub REST API v3 |
| Version Control | Git + GitHub |

---

## ⚡ Setup and Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL
- GitHub OAuth App (Client ID + Secret)

### Clone & Build
```bash
git clone https://github.com/Wag62987/Dev-Ops-assistant.git
cd devops-assistant
mvn clean install
```

### Configure `application-dev.yaml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:5432/devops_db
    username: your_db_user
    password: your_db_password
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: YOUR_GITHUB_CLIENT_ID
            client-secret: YOUR_GITHUB_CLIENT_SECRET

jwt:
  secret: YOUR_JWT_SECRET
```

### Run
```bash
mvn spring-boot:run
```
Application starts at: `http://localhost:8080`

---

## 🔌 API Reference

| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/user/Info` | Get authenticated user details |
| `GET` | `/github/userRepos` | List GitHub repositories |
| `GET` | `/repos/imported` | List imported repositories |
| `POST` | `/repos/import` | Import a repository |
| `POST` | `/deploy/{repoId}` | Generate & deploy CI/CD pipeline |
| `GET` | `/ci-status/{repoId}` | Get latest CI run status |
| `GET` | `/ci-monitor/{repoId}` | Get full workflow run history |

---

## 🚀 Future Enhancements

| Enhancement | Description |
|------------|-------------|
| 🐳 Docker Support | Containerize the application with Dockerfile |
| ☸️ Kubernetes | Helm chart for K8s deployment |
| 🤖 AI Insights | LLM-powered failure diagnosis and fix suggestions |
| 🔔 Notifications | Slack/email alerts on pipeline failure |
| 👥 Role-Based Access | Admin, Developer, Viewer permission levels |
| 📈 Dashboard | Real-time pipeline metrics and analytics UI |
| 🔗 Multi-Platform | GitLab, Bitbucket CI/CD integration |
| 🔒 Secret Scanning | Detect exposed credentials in repositories |



> *DevOps Assistant — Automate the pipeline. Focus on the code.*
