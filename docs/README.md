# todo — Kotlin + Spring Boot 4 Backend

A production-style backend for a todo application, built with Kotlin and Spring Boot 4 on PostgreSQL and Redis. It includes JWT-based authentication, role-based access control, task management APIs, rate limiting, OpenAPI documentation, and operational monitoring.

> Portfolio note: this project demonstrates a clean backend architecture for a real-world app: secure auth, user lifecycle flows, protected APIs, a task domain, and production-focused configuration and observability.

---

## ✨ What this project does

### ✅ Core domain
- ✅ Todo CRUD — create, list, view, update, and delete tasks
- ✅ Search + pagination — filter and paginate tasks with sorting support
- ✅ User ownership — tasks are scoped to the authenticated user
- ✅ Clean API responses — consistent success/error envelope across endpoints

### 🔐 Authentication & security
- ✅ JWT authentication — access tokens for protected endpoints
- ✅ Password-based user auth flow — register, login, token refresh, and logout
- ✅ Role-based access — admin operations are protected via method security
- ✅ BCrypt password hashing
- ✅ Stateless security model — stateless session management and custom API responses for unauthorized/forbidden access
- ✅ Rate limiting — request throttling per user/IP
- ✅ CORS configuration for browser clients

### 👤 Account flows
- ✅ User account lifecycle — profile/account management and account purge support
- ✅ Verification and lifecycle automation — account cleanup and scheduled maintenance
- ✅ Account-related services for profile data and user operations

### 🛡️ Platform & operations
- ✅ PostgreSQL + JPA persistence
- ✅ Redis-based infrastructure for caching / rate limiting / shared runtime support
- ✅ Actuator health and metrics
- ✅ OpenAPI/Swagger docs
- ✅ I18n message support
- ✅ Docker-ready setup

---

## 🧱 Stack

- Kotlin 2.3.21
- Spring Boot 4.1.0
- Java 21
- PostgreSQL with Spring Data JPA
- Redis with Spring Data Redis
- Spring Security + JJWT
- Spring Web + validation
- WebSocket support included in the project stack
- Bucket4j for rate limiting
- Springdoc OpenAPI for API docs
- Micrometer + Prometheus
- Twilio integration for SMS flows
- AWS S3 support for storage-related functionality
- Apache Tika + Thumbnailator for upload handling
- JUnit 5, MockK, Spring Boot Test

---

## 🌐 API surface

### Auth — `/api/auth`
| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Log in with credentials and receive tokens |
| `POST` | `/api/auth/refresh` | Refresh access token |
| `POST` | `/api/auth/logout` | Revoke/logout user session |
| `POST` | `/api/auth/password-reset/...` | Password reset flow |
| `POST` | `/api/auth/verify/...` | Verification-related account flows |

### User / Account — `/api/user` and `/api/account`
| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/user/me` | Current user profile |
| `PATCH` | `/api/user/me` | Update profile/account data |
| `POST` | `/api/user/change-password` | Change password |
| `POST` | `/api/user/change-phone/...` | Phone change flow |
| `GET` | `/api/account/...` | Account lifecycle endpoints |

### Todo — `/api/todo`
| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/todo` | List todos with search + pagination |
| `POST` | `/api/todo` | Create a todo |
| `GET` | `/api/todo/{id}` | Get todo by id |
| `PUT` | `/api/todo/{id}` | Update todo |
| `DELETE` | `/api/todo/{id}` | Delete todo |

### Admin — `/api/admin`
| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/admin/...` | Admin-only data / management endpoints |
| `POST` / `PATCH` | `/api/admin/...` | User/admin management actions |

### Public / operational
| Method | Path | Purpose |
|---|---|---|
| `GET` | `/actuator/health` | Health status |
| `GET` | `/actuator/prometheus` | Metrics export |
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/v3/api-docs` | OpenAPI JSON spec |

---

## ⚙️ Profiles

| File | When |
|---|---|
| `application.yaml` | shared base config |
| `application-dev.yaml` | local development settings |
| `application-prod.yaml` | production deployment configuration |

Run with:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
