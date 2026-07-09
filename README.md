# High-Concurrency Hotel Reservation Engine

Enterprise-grade hotel booking backend built with **Java 21 + Spring Boot 3**, designed around the hard problem that platforms like Booking.com/Airbnb/Expedia solve: **thousands of concurrent booking requests must never double-book a room.**

## Why this project is interesting

The booking path defends against double-booking with **four layers**, cheapest to strongest:

| Layer | Mechanism | Purpose |
|-------|-----------|---------|
| 1 | **Redis distributed lock** (`SET NX PX` + Lua release) | Cross-instance gate. Repels competing requests before they hit the DB. Absorbs thundering herd. |
| 2 | **Pessimistic DB lock** (`SELECT ... FOR UPDATE`) | Authoritative serialization on the room + inventory rows within a transaction. |
| 3 | **Optimistic lock** (`@Version`) + **retry** | Detects races that slip past the lock (e.g. TTL expiry); caller retries with backoff. |
| 4 | **Unique constraint** `(room_id, stay_date)` | Final database-level backstop against duplicate holds. |

Transaction isolation is `REPEATABLE_READ` so availability can't shift underneath a booking mid-transaction. See [`docs/diagrams/booking-sequence.md`](docs/diagrams/booking-sequence.md).

## Tech Stack

Java 21 · Spring Boot 3.3 · Spring Security + JWT (access + refresh) · Spring Data JPA / Hibernate · PostgreSQL · Redis · Kafka · Flyway · MapStruct · Lombok · springdoc OpenAPI · Micrometer + Prometheus + Grafana · Docker Compose · JUnit 5 + Mockito + Testcontainers.

## Architecture

Layered / clean architecture:

```
controller  -> REST + validation + @PreAuthorize (RBAC)
service     -> business logic, transactions, the reservation engine
repository  -> Spring Data JPA + locking queries
domain      -> JPA entities + enums
dto/mapper  -> request/response records, MapStruct
concurrency -> Redis distributed lock
payment     -> Strategy (gateways) + Factory (resolver)
kafka/event -> publisher + consumers (Observer)
scheduler   -> expiry sweeps + reports
```

**Patterns used:** DTO, Repository, Service Layer, Builder (Lombok), Factory (`PaymentGatewayFactory`), Strategy (`PaymentGateway`), Observer (Kafka consumers). SOLID throughout: e.g. adding a new payment provider is a single new `@Component`.

## Domain / Roles

- **CUSTOMER** — search, book, cancel
- **HOTEL_MANAGER** — manage hotels, rooms, amenities, images
- **ADMIN** — everything + dashboard analytics

Reservation lifecycle: `PENDING -> LOCK_ROOM -> PAYMENT_PENDING -> CONFIRMED -> CHECKED_IN -> CHECKED_OUT -> COMPLETED`, with `CANCELLED` releasing inventory automatically (also swept by a scheduled job when a payment hold expires).

## Running locally

### Everything via Docker Compose
```bash
docker compose up --build
```
Brings up Postgres, Redis, Kafka + Zookeeper, Prometheus, Grafana, and the app.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator health: http://localhost:8080/actuator/health
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin / admin)

### Run the app from your IDE
Start just the infra, then run `HotelReservationApplication`:
```bash
docker compose up postgres redis kafka zookeeper
./mvnw spring-boot:run
```

## Sample credentials (seeded via Flyway V2)

| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@hotel.com | Password123! |
| HOTEL_MANAGER | manager@hotel.com | Password123! |
| CUSTOMER | customer@hotel.com | Password123! |

> The seeded BCrypt hashes are placeholders for demo shape. Register a fresh user via `/api/v1/auth/register` for guaranteed working login.

## API quickstart

```bash
# Register
curl -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"jane@t.com","password":"Password123!","fullName":"Jane","role":"CUSTOMER"}'

# Book (use the accessToken from the response above)
curl -X POST localhost:8080/api/v1/reservations -H 'Content-Type: application/json' \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"roomId":2,"checkIn":"2026-09-10","checkOut":"2026-09-12","guests":2,"paymentProvider":"STRIPE"}'
```

A Postman collection lives in [`docs/postman`](docs/postman).

## Testing

```bash
./mvnw test
```

- **Unit:** distributed lock behavior, payment factory/strategy.
- **Integration:** auth flow via MockMvc + Testcontainers Postgres.
- **Concurrency:** `ReservationConcurrencyIT` fires 20 simultaneous bookings at one room/date range and asserts **at most one** wins.
- **Load:** k6 script in [`docs/load-test`](docs/load-test).

## Monitoring

Actuator exposes `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`. Prometheus scrapes the app; Grafana visualizes. Micrometer tags every metric with the application name.

## Diagrams

- [Entity Relationship Diagram](docs/diagrams/erd.md)
- [Booking Sequence Diagram](docs/diagrams/booking-sequence.md)

## License

MIT
