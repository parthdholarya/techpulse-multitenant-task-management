# TechPulse – Multi-Tenant Task Management System

A secure, role-based task management REST API built using Java, Spring Boot, Spring Security, JWT, and H2 Database.

## 1. Project Overview

The **TechPulse Multi-Tenant Task Management System** is a backend application that allows multiple organizations to manage their users and tasks through a single application and database.

Each organization (tenant) has its own users and tasks. The system ensures that users can access only the data belonging to their organization.

The application implements JWT-based authentication, role-based authorization, and tenant-level data isolation.

## 2. Key Features

- Multi-tenant architecture with a shared database.
- JWT-based authentication.
- Role-based access control.
- Secure password hashing using BCrypt.
- Tenant-level data isolation.
- User and task management.
- Task assignment within the same tenant.
- Task status management.
- Input validation.
- Centralized exception handling.
- Automated unit and integration tests.
- Code quality analysis using SonarQube.

## 3. Technology Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Programming language |
| Spring Boot 4.0.0 | Backend framework |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| H2 Database | Database |
| Maven | Build and dependency management |
| JUnit 5 | Testing |
| Mockito | Mocking dependencies |
| JaCoCo | Test coverage |
| SonarQube | Code quality analysis |
| Postman | API testing |

## 4. Architecture

The application follows a layered architecture.

- Controller Layer – Handles HTTP requests and responses.
- Service Layer – Contains business logic.
- Repository Layer – Handles database operations.
- Entity Layer – Defines database entities.
- Security Layer – Handles JWT authentication and authorization.
- DTO Layer – Handles request and response data.

### Multi-Tenant Architecture

The application uses a shared database and a shared application codebase.

Each tenant has its own users and tasks. Tenant isolation is maintained using `tenant_id`.

### Database Entities

**Tenant**
- id
- name
- created_at

**User**
- id
- email
- password
- role
- tenant_id

**Task**
- id
- title
- description
- status
- assigned_to
- tenant_id

### Tenant Isolation Strategy

1. A tenant registers with its first ADMIN account.
2. The ADMIN logs in using tenant name, email, and password.
3. The server generates a JWT containing the tenant ID and role.
4. The JWT filter validates the token for protected requests.
5. The authenticated tenant information is stored in Spring Security's SecurityContext.
6. Services retrieve the tenant ID from the authenticated principal.
7. Repository queries filter records using the authenticated tenant ID.
8. Before updating a task, the service verifies that the task belongs to the current tenant.

**Important:** The application never trusts a tenant ID supplied by the client. Tenant identity comes from the verified JWT.

## 5. Authentication and Authorization

### JWT Authentication

The application uses JSON Web Tokens for stateless authentication.

The JWT contains:
- Subject: User email
- tenantId: Organization ID
- role: User role
- Issued-at timestamp
- Expiration timestamp

### Authentication Flow

1. User sends login credentials.
2. The application validates the tenant and user.
3. The password is verified using BCrypt.
4. A JWT is generated.
5. The client sends the token in the Authorization header.

Example:

`Authorization: Bearer <JWT_TOKEN>`

### JWT Security Components

- `JwtService` – Generates and validates JWT tokens.
- `JwtAuthenticationFilter` – Validates incoming JWT tokens.
- `TenantPrincipal` – Stores authenticated user information.
- `TenantContext` – Provides access to the current user's tenant ID, email, and role.
- `SecurityConfig` – Configures Spring Security.

### Role-Based Authorization

| Operation | ADMIN | MANAGER | USER |
|---|---|---|---|
| Create users | Yes | No | No |
| Create tasks | No | Yes | No |
| Assign tasks | No | Yes | No |
| View tasks according to role | Yes | Yes | Yes |
| Update assigned task status | No | No | Yes |

### Role Responsibilities

**ADMIN**
- Creates users within the tenant.
- Views all tasks belonging to the tenant.

**MANAGER**
- Creates tasks.
- Assigns tasks to users within the same tenant.
- Views all tasks belonging to the tenant.

**USER**
- Views only tasks assigned to them.
- Updates the status of their assigned tasks.

## 6. REST API Endpoints

Base URL:

`http://localhost:8081`

### Tenant Registration

**POST** `/tenant/register`

Creates a new tenant and its first ADMIN user.

Example request:

```json
{
  "tenantName": "TechPulse",
  "adminEmail": "admin@techpulse.com",
  "adminPassword": "Admin@123"
}
```

### User Login

**POST** `/auth/login`

Authenticates a user and returns a JWT.

Example request:

```json
{
  "tenantName": "TechPulse",
  "email": "admin@techpulse.com",
  "password": "Admin@123"
}
```

### Create User

**POST** `/users`

Access: ADMIN

Example request:

```json
{
  "email": "manager@techpulse.com",
  "password": "Manager@123",
  "role": "MANAGER"
}
```

### Create Task

**POST** `/tasks`

Access: MANAGER

Example request:

```json
{
  "title": "Implement JWT Authentication",
  "description": "Implement secure JWT authentication."
}
```

### Assign Task

**PUT** `/tasks/{taskId}/assign`

Access: MANAGER

Example request:

```json
{
  "userId": 3
}
```

### Update Task Status

**PUT** `/tasks/{taskId}/status`

Access: USER

Example request:

```json
{
  "status": "IN_PROGRESS"
}
```

Supported task statuses:
- PENDING
- IN_PROGRESS
- COMPLETED

### Get Tasks

**GET** `/tasks`

Access: ADMIN, MANAGER, USER

- ADMIN and MANAGER receive all tasks belonging to their tenant.
- USER receives only tasks assigned to them.

All protected endpoints require a valid JWT in the Authorization header.

## 7. Project Setup

### Prerequisites

- Java 17 or later
- Maven
- IntelliJ IDEA or another Java IDE
- Postman

### Clone Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

### Navigate to Project

```bash
cd multitenant-task-management
```

### Configure JWT Secret

Set the `JWT_SECRET` environment variable before running the application.

PowerShell example:

```powershell
$env:JWT_SECRET = "<YOUR_BASE64_ENCODED_SECRET>"
```

Use a strong, securely generated secret. Do not commit real secrets to GitHub.

### Run Application

```bash
mvn spring-boot:run
```

The application runs at:

`http://localhost:8081`

### H2 Database Console

URL:

`http://localhost:8081/h2-console`

Use the JDBC URL configured in `application.yaml`:

`jdbc:h2:file:./data/taskmanagementdb`

Username: `sa`

Password: Leave blank unless you configure one.

## 8. Testing

The application includes automated tests using JUnit 5, Mockito, and Spring Boot Test.

Testing covers:
- Tenant registration.
- User creation.
- Authentication and JWT generation.
- JWT validation.
- Tenant context.
- Task creation and assignment.
- Task status updates.
- Role-based access.
- Tenant isolation.
- Controller behavior.
- Exception handling.

Run tests:

```bash
mvn test
```

Generate the JaCoCo coverage report:

```bash
mvn clean verify
```

Report location:

`target/site/jacoco/index.html`

## 9. Code Quality

SonarQube is used to analyze code quality, reliability, security findings, and test coverage.

Recorded project analysis results:

- Quality Gate: Passed
- Overall test coverage: 88.1%
- Code duplication: 0%
- Security rating: A
- Reliability rating: A
- Maintainability rating: A
- Open issues: 0

Security findings and hotspots should be reviewed in context; these results do not replace a complete security audit.

## 10. Exception Handling

The application uses centralized exception handling through `GlobalExceptionHandler`.

Handled scenarios include:

- Resource not found – HTTP 404
- Duplicate resource – HTTP 409
- Forbidden operation – HTTP 403
- Authentication failure – HTTP 401
- Invalid request data – HTTP 400
- Unexpected server error – HTTP 500

## 11. Security Considerations

- Passwords are hashed using BCrypt.
- JWT tokens are validated before accessing protected endpoints.
- Tenant ID is extracted from the verified JWT.
- Repository queries are tenant-scoped.
- Task ownership is validated before updates.
- Users cannot access another tenant's data.
- Protected APIs require authentication.
- Secrets are supplied through environment variables.
- CSRF protection is disabled because the application uses stateless JWT authentication through the Authorization header, not browser cookies.

## 12. Future Enhancements

- Redis caching.
- Refresh token implementation.
- Pagination and sorting.
- API documentation using Swagger/OpenAPI.
- Docker-based deployment.
- Cloud deployment.
- Audit logging.
- More advanced integration and security testing.

## 13. Project Information

**Project:** TechPulse Multi-Tenant Task Management System

**Type:** Backend REST API

**Architecture:** Multi-Tenant Architecture

**Database:** H2

**Build Tool:** Maven

**Developed By:** Parth Dholariya

---

Built with Java and Spring Boot, focusing on secure authentication, role-based authorization, and tenant-level data isolation.
