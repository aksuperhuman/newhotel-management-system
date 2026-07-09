# High-Concurrency Hotel Reservation Engine

An enterprise-grade, event-driven hotel booking backend in the spirit of Booking.com / Airbnb / Expedia. Built to demonstrate advanced **Java 21 + Spring Boot 3**, distributed concurrency control, Redis, Kafka, Docker, and clean system design.

> The centerpiece is a **concurrency-safe reservation engine** that prevents double booking under thousands of simultaneous requests using four layered strategies (Redis distributed lock -> pessimistic DB lock -> optimistic versioning + retry -> DB unique constraint).

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language / Framework | Java 21, Spring Boot 3.3 |
| Security | Spring Security, JWT (access + refresh), BCrypt, method-level `@PreAuthorize` |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL, Flyway |
| Caching / Locking | Redis (cache, session, distributed lock, rate limiting) |
| Messaging | Apache Kafka (event-driven) |
| Mapping / Boilerplate | MapStruct, Lombok |
| Docs | springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, Testcontainers |
| Observability | Actuator, Micrometer, Prometheus, Grafana |
| Build / Run | Maven, Docker, Docker Compose |

---

## Architecture (Layered + Clean)

```
controller  ->  service  ->  repository  ->  PostgreSQL
                   |
                   +--> lock (Redis distributed lock)
                   +--> payment (Strategy + Factory)
                   +--> event  (Kafka producer)  --> consumers (analytics, notifications)
                   +--> notification (Observer: Email / SMS)
```

### Design patterns used
- **DTO + Mapper (MapStruct)** — no entity leaks across the API boundary
- **Repository / Service layer** — clean separation of concerns
- **Builder** — entity + DTO construction (Lombok `@Builder`)
- **Factory** — `PaymentGatewayFactory` resolves a gateway strategy
- **Strategy** — `PaymentGatewayStrategy` (Razorpay / Stripe / PayPal)
- **Observer** — `NotificationService` fans out to Email/SMS channels
- **SOLID** throughout (single-responsibility services, DI, interface segregation)

---

## The Concurrency Engine (core)

Double booking is prevented with **defense in depth**. Each layer catches races the previous one can miss:

1. **Redis distributed lock** (`DistributedLockService`) — cluster-wide mutex per room, acquired *before* the DB transaction. `SET NX PX` for atomic acquire-with-TTL; Lua compare-and-delete for safe release. Sheds contention early across all app instances.
2. **Pessimistic DB lock** (`SELECT ... FOR UPDATE` on the exact `room_inventory` rows) — hard serialization at the source of truth for the contended (room, date) rows.
3. **Optimistic locking** (`@Version` on inventory + reservation) **+ bounded retry** — for lower-contention updates without holding row locks.
4. **Unique constraint** `(room_id, stay_date)` — the final DB backstop. Correctness never depends on app timing alone.

Transaction isolation is **READ_COMMITTED**, with strong locking scoped only to contended rows (not full SERIALIZABLE) to preserve throughput. See `ReservationService` for heavily-commented rationale on each strategy.

### Reservation lifecycle

```
PENDING -> LOCK_ROOM -> PAYMENT_PENDING -> CONFIRMED -> CHECKED_IN -> CHECKED_OUT -> COMPLETED
                              |
                              +--(payment fails / expires)--> CANCELLED  (inventory auto-released)
```

A scheduler (`ReservationScheduler`) reclaims inventory from `PAYMENT_PENDING` reservations that blow past their payment window, and emits daily occupancy/revenue reports.

---

## Kafka events

| Event | Topic | Consumed by |
|-------|-------|-------------|
| ReservationCreated | `reservation.created` | notifications |
| ReservationConfirmed | `reservation.confirmed` | analytics |
| ReservationCancelled | `reservation.cancelled` | analytics, notifications |
| PaymentCompleted | `payment.completed` | notifications |
| PaymentFailed | `payment.failed` | (extendable) |
| InventoryUpdated | `inventory.updated` | analytics |

---

## Getting started

### Run everything with Docker Compose

```bash
docker compose up --build
```

This starts Postgres, Redis, Zookeeper, Kafka, Prometheus, Grafana, and the app.

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator health | http://localhost:8080/actuator/health |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |

### Run locally (infra in Docker, app in IDE)

```bash
docker compose up postgres redis kafka zookeeper
mvn spring-boot:run
```

### Seeded accounts (password: `password123`)

| Role | Email |
|------|-------|
| ADMIN | admin@hotel.com |
| HOTEL_MANAGER | manager@hotel.com |
| CUSTOMER | customer@hotel.com |

---

## API quick tour

```bash
# 1. Login
curl -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"customer@hotel.com","password":"password123"}'

# 2. Book a room (use the returned accessToken)
curl -X POST localhost:8080/api/v1/reservations \
  -H 'Authorization: Bearer <TOKEN>' -H 'Content-Type: application/json' \
  -d '{"roomId":1,"checkIn":"2026-08-01","checkOut":"2026-08-03","guests":2,"gateway":"STRIPE"}'
```

A Postman collection lives in [`docs/postman_collection.json`](docs/postman_collection.json).

---

## Testing

```bash
mvn test
```

- **Unit**: `JwtServiceTest`, `PaymentGatewayFactoryTest`
- **Integration**: `AbstractIntegrationTest` (Testcontainers Postgres)
- **Concurrency**: `ReservationConcurrencyTest` fires 20 simultaneous bookings at one room/date range and asserts at most one wins
- **Load**: k6 script in [`docs/loadtest.k6.js`](docs/loadtest.k6.js)

---

## Project structure

```
src/main/java/com/hotelreservation
├── HotelReservationApplication.java
├── config          # Security, Redis, Kafka topics, OpenAPI
├── controller      # REST endpoints
├── domain          # JPA entities + enums
├── dto             # request/response records
├── event           # Kafka events, producer, consumers
├── exception       # global handler + custom errors
├── lock            # Redis distributed lock
├── mapper          # MapStruct mappers
├── notification    # Observer channels (Email/SMS)
├── payment         # Strategy + Factory (Razorpay/Stripe/PayPal)
├── repository      # Spring Data JPA repos
├── scheduler       # background jobs
├── security        # JWT filter/service, user details
└── service         # business logic incl. reservation engine
src/main/resources/db/migration   # Flyway V1 schema, V2 seed
docs/                              # diagrams, Postman, load test
```

See [`docs/DIAGRAMS.md`](docs/DIAGRAMS.md) for the ERD and booking sequence diagram.
