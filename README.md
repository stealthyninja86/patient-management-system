# Patient Management System

A Spring Boot microservice application for managing patients with authentication and Kafka integration.

## Architecture

- **auth-service**: Authentication service with JWT (port 4005)
- **patient-service**: Patient management service (port 4006)
- **Kafka**: Message broker for inter-service communication
- **PostgreSQL**: Database for each service

## Tech Stack

- Spring Boot 4.0.5
- Java 21
- PostgreSQL
- Kafka
- Docker & Docker Compose

## Services

### Auth Service (port 4005)
- JWT-based authentication
- User management
- `/login` endpoint - POST with `{"email": "...", "password": "..."}`
- Test user: `test@test.com` / `password123`

### Patient Service (port 4006)
- Patient CRUD operations
- Kafka producer/consumer integration

## Running the Application

```bash
docker-compose up -d
```

## API Endpoints

- `POST http://localhost:4005/login` - Authenticate user
- `GET http://localhost:4006/patients` - List patients (requires JWT)

## Build

```bash
# Build all services
docker-compose build

# Or build individually
docker build -t auth-service ./auth-service
docker build -t patient-service ./patient-service
```
