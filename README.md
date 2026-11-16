# Employee Leave Management API

A backend project that demonstrates a simple HR leave-management system built with Spring Boot microservices. It separates employee and authentication management from leave-request processing, with each service owning its own PostgreSQL database.

## Overview

The system contains two services:

| Service | Port | Responsibility |
|---|---:|---|
| Employee Service | `8081` | Departments, employee records, user accounts, authentication, and JWT issuance |
| Leave Service | `8082` | Leave requests, employee leave history, approvals, rejections, and reporting |

The Leave Service communicates with the Employee Service when it needs employee-related information. Both services validate JWTs and use role-based access control.

## Main features

- Employee and department management
- User registration and login
- JWT access and refresh tokens
- Role-based access for `ADMIN`, `MANAGER`, and `EMPLOYEE`
- Submit and view personal leave requests
- Manager/admin leave review, approval, and rejection
- Leave reporting
- PostgreSQL persistence with Flyway database migrations
- Docker Compose setup for local development

## Technology stack

- Java 17
- Spring Boot 4
- Spring MVC and Spring Data JPA
- Spring Security OAuth2 Resource Server
- PostgreSQL 16
- Flyway
- Maven
- Docker and Docker Compose

## Architecture

```text
                  Client / Postman
                         |
             +-----------+-----------+
             |                       |
       Employee Service         Leave Service
          :8081                     :8082
             |                       |
       employee_db                leave_db
             |                       |
             +<---- REST call -------+
                  Employee Service
```

Each service has its own database. The services do not share tables or a database connection.

## Prerequisites

For the Docker Compose setup, install:

- Git
- Docker Desktop (or Docker Engine) with the Docker Compose plugin

For running the services directly on your machine, you will also need:

- JDK 17
- Maven 3.9+
- PostgreSQL 16

## Run locally with Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/MarioSandy/employee-leave-api.git
cd employee-leave-api
```

### 2. Configure environment variables

Copy the example environment file:

**macOS / Linux**
```bash
cp .env.example .env
```

**Windows PowerShell**
```powershell
Copy-Item .env.example .env
```

Set the required values in `.env`:

```dotenv
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-this-password
POSTGRES_PORT=5432

JWT_SECRET=replace-with-a-random-secret-at-least-32-characters
```

Use a long, random JWT secret. Do not commit your real `.env` file.

`POSTGRES_PORT` is optional; Docker Compose uses port `5432` when it is not set. The Compose file configures the internal Employee Service URL for the Leave Service automatically.

### 3. Build and start the services

```bash
docker compose up --build -d
```

Docker Compose starts PostgreSQL first, waits for its health check, and then starts the Employee and Leave services.

### 4. Check service logs

```bash
docker compose logs -f employee-service
docker compose logs -f leave-service
```

The services listen on:

- Employee Service: http://localhost:8081
- Leave Service: http://localhost:8082

### 5. Stop the services

```bash
docker compose down
```

To also delete the PostgreSQL data volume and start with empty databases next time:

```bash
docker compose down -v
```

**Warning:** `docker compose down -v` permanently deletes the local database data.

## Run services directly (without Docker Compose)

If you prefer to run the applications from your IDE or terminal:

1. Start PostgreSQL.
2. Create two databases: `employee_db` and `leave_db`.
3. Configure the following environment variables for the Employee Service:
   - `DATABASE_URL=jdbc:postgresql://localhost:5432/employee_db`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET`
4. Configure the following environment variables for the Leave Service:
   - `DATABASE_URL=jdbc:postgresql://localhost:5432/leave_db`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET`
   - `EMPLOYEE_SERVICE_URL=http://localhost:8081`
5. Start the Employee Service, then start the Leave Service.

From each service directory, run:

```bash
./mvnw spring-boot:run
```

If the repository does not include the Maven Wrapper, use:

```bash
mvn spring-boot:run
```

Run the Employee Service on port `8081` and the Leave Service on port `8082`.

## API overview

### Employee Service — `http://localhost:8081`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a user for an existing employee |
| POST | `/auth/login` | Authenticate and receive access/refresh tokens |
| POST | `/auth/refresh` | Refresh an access token |
| POST | `/employees` | Create an employee (Admin) |
| GET | `/employees` | List employees (Manager/Admin) |
| GET | `/employees/{id}` | Get an employee |
| PUT | `/employees/{id}` | Update an employee (Admin) |
| DELETE | `/employees/{id}` | Soft-delete an employee (Admin) |
| GET | `/employees/summary/by-department` | Employee counts by department (Manager/Admin) |

### Leave Service — `http://localhost:8082`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/leave-requests` | Submit a leave request |
| GET | `/leave-requests/me` | Get the authenticated employee's leave requests |
| GET | `/leave-requests` | List leave requests (Manager/Admin) |
| GET | `/leave-requests/report` | Get leave report (Manager/Admin) |
| PATCH | `/leave-requests/{id}/approve` | Approve a leave request (Manager/Admin) |
| PATCH | `/leave-requests/{id}/reject` | Reject a leave request (Manager/Admin) |

Protected endpoints require the access token returned by `POST /auth/login`:

```http
Authorization: Bearer <access_token>
```

Request and response fields are defined by the DTOs in each service.

## Demo data

The startup seeder creates default departments and sample employee/user accounts.

| Username | Role | Department |
|---|---|---|
| `alice.johnson` | MANAGER | HR |
| `robert.chen` | EMPLOYEE | Finance |
| `emily.davis` | EMPLOYEE | Marketing |
| `daniel.wilson` | MANAGER | Operations |
| `olivia.martinez` | EMPLOYEE | Sales |

The sample accounts use the demo password `Demo123!`. The admin account is seeded separately and uses the configured `app.admin.password` value.

These credentials are for local demonstration only. Change or remove demo accounts before deploying the application anywhere public.

## Current setup notes

Before expecting a clean first startup, check these repository setup details:

- The service build files are currently named `employee/dockerfile` and `leave/dockerfile` (lowercase). Docker Compose's default build filename is `Dockerfile`; rename them to `Dockerfile` or configure the Compose build entries to use the lowercase filenames.
- The current admin seeder injects `app.admin.password`, but that property is not currently declared in the checked-in application YAML or passed into the Employee Service by Compose. The value must be supplied before the Employee Service can start.
- The initial-admin Flyway seed migration is being removed in a separate change. For a fresh database, make sure the obsolete V5 SQL seed migration is no longer present before running Flyway.

These are setup prerequisites, not steps performed automatically by this README.

## License

No license has been specified for this project yet.
