# Employee Leave Management API

A backend HR leave-management system built with Spring Boot microservices. Employee management and authentication are separated from leave-request processing, and each service owns an independent PostgreSQL database.

## Overview

| Service | Port | Responsibility |
|---|---:|---|
| Employee Service | 8081 | Departments, employee records, user accounts, authentication, and JWT issuance |
| Leave Service | 8082 | Leave requests, personal leave history, approvals, rejections, and reporting |

The Leave Service communicates with the Employee Service over HTTP when it needs employee information. Both services validate JWTs and enforce role-based access.

## Features

- Employee and department management
- User registration and login
- JWT access and refresh tokens
- Role-based access for `ADMIN`, `MANAGER`, and `EMPLOYEE`
- Submit and view personal leave requests
- Manager/admin leave review, approval, and rejection
- Leave reporting
- PostgreSQL persistence with Flyway migrations
- Docker Compose local development environment

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

Each service owns its own database; they do not share tables or database connections.

## Prerequisites

For Docker Compose:
- Git
- Docker Desktop, or Docker Engine with the Docker Compose plugin

For running the services directly:
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

Copy the example file.

**macOS / Linux**
```bash
cp .env.example .env
```

**Windows PowerShell**
```powershell
Copy-Item .env.example .env
```

Set the values in `.env`:

```dotenv
POSTGRES_USER=postgres
POSTGRES_PASSWORD=change-this-password
POSTGRES_PORT=5432

JWT_SECRET=replace-with-a-long-random-secret
```

Use a long, random JWT secret and do not commit your real `.env` file. `POSTGRES_PORT` is optional; Compose defaults to port 5432. The internal Employee Service URL is configured automatically for the Leave Service.

### 3. Build and start

```bash
docker compose up --build -d
```

Compose starts PostgreSQL, waits for its health check, and then starts both application services.

### 4. View logs

```bash
docker compose logs -f employee-service
docker compose logs -f leave-service
```

Service URLs:
- Employee Service: http://localhost:8081
- Leave Service: http://localhost:8082

### 5. Stop the application

```bash
docker compose down
```

To also remove the PostgreSQL data volume:

```bash
docker compose down -v
```

**Warning:** `docker compose down -v` permanently deletes the local database data.

## Run without Docker Compose

1. Start PostgreSQL.
2. Create two databases: `employee_db` and `leave_db`.
3. Set these environment variables for the Employee Service:
   - `DATABASE_URL=jdbc:postgresql://localhost:5432/employee_db`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET`
4. Set these environment variables for the Leave Service:
   - `DATABASE_URL=jdbc:postgresql://localhost:5432/leave_db`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET`
   - `EMPLOYEE_SERVICE_URL=http://localhost:8081`
5. Start Employee Service first, then Leave Service.

From each service directory, run:

```bash
mvn spring-boot:run
```

The Employee Service listens on port 8081 and the Leave Service on port 8082.

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
| DELETE | `/employees/{id}` | Delete an employee (Admin) |
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

## Seeded demo data

The Employee Service startup seeder creates the default departments and sample employee/user accounts.

| Username | Role | Department |
|---|---|---|
| `alice.johnson` | MANAGER | Human Resources |
| `robert.chen` | EMPLOYEE | Finance |
| `emily.davis` | EMPLOYEE | Marketing |
| `daniel.wilson` | MANAGER | Operations |
| `olivia.martinez` | EMPLOYEE | Sales |

All five demo accounts use the password `Demo123!`. The admin account is seeded separately and currently reads its password from the `app.admin.password` property.

These credentials are for local demonstration only. Do not use them in a public deployment.

## Repository setup notes

A couple of existing configuration details need attention before expecting a clean first startup:

- The service build files are named `employee/dockerfile` and `leave/dockerfile` (lowercase), while Docker Compose looks for `Dockerfile` by default. Rename those files to `Dockerfile`, or explicitly configure the Compose build entries to use the lowercase filenames.
- The admin seeder injects `app.admin.password`, but this property is not declared in the checked-in application YAML or passed to the Employee Service in the current Compose configuration. Supply that property before starting the Employee Service.

## License

No license has been specified for this project yet.
