# enterprise-pto-management-portal

**enterprise-pto-management-portal** is a full-stack leave management application for tracking employee vacation requests, team balance, and managerial approval workflows.

## Project Description

This repository contains two main parts:

- `backend/` — Spring Boot application with REST APIs, JWT authentication, MySQL persistence, request approval workflow, holiday scheduling, and employee management.
- `frontend/my-vite-app/` — React + Vite single-page application with authentication, dashboard views, vacation request forms, admin review pages, and PWA support.

The app is designed for internal business use so employees can request time off and managers/admins can review, approve, reject, or cancel requests.

## Key Features

- User authentication and role-based access
- Vacation request creation and tracking
- Request approval, rejection, and cancellation workflows
- Employee profile and leave balance management
- Holiday calendar and date validation
- Dashboard for admins with summaries and pending reviews
- React UI with Vite, React Router, and service-based API integration

## Architecture

### Backend

- Java 21
- Spring Boot 4
- Spring Data JPA
- Spring Security
- Spring Mail
- JWT authentication
- MySQL connector
- Lombok for model boilerplate reduction

### Frontend

- React 19
- Vite 8
- Axios for API calls
- React Router Dom 7
- React Hook Form
- PWA support via `vite-plugin-pwa`

## Folder Structure

- `backend/`
  - `pom.xml` — Maven configuration
  - `src/main/java` — application, controllers, services, DTOs, entities, repositories, security
  - `src/main/resources/static` — built frontend assets for static serving
  - `src/main/resources/application.properties` — backend settings
- `frontend/my-vite-app/`
  - `package.json` — frontend dependencies and scripts
  - `src/` — React pages, components, service modules, routing, authentication context
  - `vite.config.js` — Vite configuration and API proxy setup

## API Overview

### Authentication

- `POST /api/v1/auth/login` — authenticate and receive JWT token

### Employees

- `GET /api/v1/employees/me` — current user profile
- `GET /api/v1/employees/all` — all employees
- `GET /api/v1/employees/search` — search employees
- `GET /api/v1/employees/department` — employees by department
- `GET /api/v1/employees/list` — employee listing
- `POST /api/v1/employees` — add employee
- `PUT /api/v1/employees/update` — update employee
- `DELETE /api/v1/employees/{id}` — remove employee

### Departments

- `GET /api/v1/departments` — department list

### Holidays

- `GET /api/v1/holiday/all` — all holidays
- `GET /api/v1/holiday/find-between-dates` — holiday range search
- `GET /api/v1/holiday/holiday-by-year` — annual holidays
- `POST /api/v1/holiday` — add holiday
- `PUT /api/v1/holiday` — update holiday

### Vacation Requests

- `POST /api/v1/request/{employeeId}` — create a new request
- `GET /api/v1/request/all` — all requests
- `GET /api/v1/request/status` — filter by status
- `GET /api/v1/request/employee/{id}` — requests by employee
- `GET /api/v1/request/pendings` — pending requests
- `PUT /api/v1/request/approved/{id}` — approve request
- `PUT /api/v1/request/rejected/{id}` — reject request
- `PUT /api/v1/request/cancelPendingVacationRequest/{id}` — cancel pending request
- `PUT /api/v1/request/approvedByResponsible/{id}` — manager approval
- `PUT /api/v1/request/rejectByResponsible/{id}` — manager rejection

## Getting Started

### Prerequisites

- Java 21 SDK
- Maven
- Node.js and npm
- MySQL database (or configured datasource)

### Run Backend

1. Open a terminal and go to `backend/`
2. Configure database connection in `backend/src/main/resources/application.properties`
3. Run:

```bash
cd backend
./mvnw spring-boot:run
```

or on Windows:

```powershell
cd backend
.
\mvnw.cmd spring-boot:run
```

### Run Frontend

1. Open a terminal and go to `frontend/my-vite-app/`
2. Install dependencies:

```bash
npm install
```

3. Start the dev server:

```bash
npm run dev
```

The frontend is configured to proxy API requests under `/api` to `http://localhost:8080`.

## Build

### Backend Build

```bash
cd backend
./mvnw package
```

### Frontend Build

```bash
cd frontend/my-vite-app
npm run build
```

## Notes

- The frontend stores JWT tokens in `localStorage` and attaches them to API requests with Axios.
- Admin and employee routes are separated using protected React routes.
- The backend Spring Boot application can serve the frontend static files when built and placed under `backend/src/main/resources/static`.

## Helpful Files

- `backend/HELP.md` — backend reference and Spring Boot notes
- `frontend/my-vite-app/vite.config.js` — PWA and proxy configuration
- `frontend/my-vite-app/package.json` — frontend dependency list
- `backend/pom.xml` — backend dependency list

## Contact / Next Steps

For local development, run the backend first, then start the frontend. If you want to integrate the frontend build into the backend automatically, build the React app and copy its `dist/` output into `backend/src/main/resources/static`.
