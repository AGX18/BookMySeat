# BookMySeat 🎬

A RESTful cinema booking system built with Spring Boot 4. Allows users to browse movies, view showtimes, and book seats. Admins can manage movies, theaters, screens, and showtimes.

---

## Tech Stack

- **Java 25** with Spring Boot 4.0.3
- **Spring Data JPA** + Hibernate 7
- **Spring Security 7** with JWT authentication
- **PostgreSQL 18** (via Docker Compose)
- **Flyway** for database migrations
- **Lombok** for boilerplate reduction
- **SpringDoc OpenAPI** (Swagger UI)
- **Testcontainers** for repository testing

---

## Getting Started

### Prerequisites

- Java 25+
- Docker + Docker Compose
- Maven 3.9+

### Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/agx/bookmyseat.git
   cd bookmyseat
   ```

2. Start the database:
   ```bash
   docker compose up -d
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8080`. Flyway will automatically apply all migrations on startup.

---

## Configuration

Set the following in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your_base64_encoded_secret
jwt.expiration=86400000
```

---

## API Overview

### Authentication

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login and receive JWT token |

### Users

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/users/{id}` | Authenticated | Get user by ID |
| GET | `/api/users?email=` | Authenticated | Get user by email |

### Movies

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/movies` | Public | List movies (filter by genre, title, date) |
| GET | `/api/movies/{id}` | Public | Get movie by ID |
| POST | `/api/movies` | Admin | Create a movie |
| PUT | `/api/movies/{id}` | Admin | Update a movie |
| DELETE | `/api/movies/{id}` | Admin | Delete a movie |

### Theaters & Screens

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/theaters` | Public | List theaters (filter by city, name) |
| GET | `/api/theaters/{id}` | Public | Get theater by ID |
| POST | `/api/theaters` | Admin | Create a theater |
| PUT | `/api/theaters/{id}` | Admin | Update a theater |
| DELETE | `/api/theaters/{id}` | Admin | Delete a theater |
| GET | `/api/theaters/{id}/screens` | Public | List screens in a theater |
| GET | `/api/theaters/{theaterId}/screens/{screenId}` | Public | Get screen by ID |
| POST | `/api/theaters/{id}/screens` | Admin | Add a screen to a theater |
| PUT | `/api/theaters/{theaterId}/screens/{screenId}` | Admin | Update a screen |
| DELETE | `/api/theaters/{theaterId}/screens/{screenId}` | Admin | Delete a screen |

### Showtimes & Seats

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/showtimes` | Public | List showtimes (filter by movieId, screenId, theaterId, date) |
| GET | `/api/showtimes/{id}` | Public | Get showtime by ID |
| POST | `/api/showtimes` | Admin | Create a showtime |
| DELETE | `/api/showtimes/{id}` | Admin | Delete a showtime |
| GET | `/api/showtimes/{id}/seats` | Public | Get all seats for a showtime |
| GET | `/api/showtimes/{id}/seats/available` | Public | Get available seats for a showtime |

### Bookings

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/bookings` | Authenticated | Create a booking |
| GET | `/api/bookings/{confirmationId}` | Authenticated | Get booking by confirmation ID |
| PATCH | `/api/bookings/{confirmationId}/cancel` | Authenticated | Cancel a booking |
| GET | `/api/bookings/me` | Authenticated | Get all bookings for the current user |

---

## Authentication

The API uses JWT Bearer token authentication. After registering or logging in, include the token in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <your_token>
```

### Roles

- `USER` — assigned by default on registration. Can browse, book, and manage their own bookings.
- `ADMIN` — can create and manage movies, theaters, screens, and showtimes.

---

## Domain Model

```
User ──── Booking ──── Showtime ──── Movie
                   └── Seat      └── Screen ──── Theater
```

- A **Theater** has multiple **Screens**
- A **Screen** hosts multiple **Showtimes**
- A **Showtime** belongs to a **Movie** and automatically generates **Seats** on creation
- A **Booking** links a **User** to a **Showtime** and a set of **Seats**
- Each booking has a unique **confirmationId** (UUID) used for lookup and cancellation

---

## Error Responses

All errors follow a consistent format:

```json
{
  "timestamp": "2026-03-04T23:55:15",
  "status": 404,
  "error": "Movie not found with id: 99"
}
```

| Status | Meaning |
|--------|---------|
| 400 | Validation failed |
| 401 | Missing or invalid token |
| 403 | Insufficient role |
| 404 | Resource not found |
| 409 | Conflict (duplicate email, overlapping showtime, seats unavailable) |

---

## Running Tests

```bash
./mvnw test
```

Repository tests use **Testcontainers** and spin up a real PostgreSQL instance automatically. Controller tests use `@WebMvcTest` with Mockito.

---

## Project Structure

```
src/main/java/io/agx/bookmyseat
├── config/          # SecurityConfig, PasswordEncoderConfig
├── controller/      # REST controllers
├── dto/
│   ├── request/     # CreateUserRequest, LoginRequest, etc.
│   └── response/    # UserResponse, BookingResponse, etc.
├── entity/          # JPA entities
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── repository/      # Spring Data JPA repositories
├── security/        # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
├── service/         # Business logic
└── specification/   # JpaSpecificationExecutor specifications
```

---

## Swagger UI

Once the application is running, visit:

```
http://localhost:8080/swagger-ui.html
```