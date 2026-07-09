-- ============================================================
-- V1: Core schema for the Hotel Reservation Engine
-- ============================================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token           VARCHAR(512) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    revoked         BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE hotels (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    city            VARCHAR(128) NOT NULL,
    address         VARCHAR(512),
    star_rating     INT          NOT NULL DEFAULT 0,
    manager_id      BIGINT       NOT NULL REFERENCES users(id),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_hotels_city ON hotels(city);
CREATE INDEX idx_hotels_star_rating ON hotels(star_rating);

CREATE TABLE amenities (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE hotel_amenities (
    hotel_id        BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    amenity_id      BIGINT NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (hotel_id, amenity_id)
);

CREATE TABLE hotel_images (
    id              BIGSERIAL PRIMARY KEY,
    hotel_id        BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    url             VARCHAR(1024) NOT NULL
);

CREATE TABLE rooms (
    id              BIGSERIAL PRIMARY KEY,
    hotel_id        BIGINT       NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_number     VARCHAR(32)  NOT NULL,
    room_type       VARCHAR(32)  NOT NULL,
    capacity        INT          NOT NULL,
    price           NUMERIC(12,2) NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'AVAILABLE',
    features        TEXT,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (hotel_id, room_number)
);
CREATE INDEX idx_rooms_hotel ON rooms(hotel_id);
CREATE INDEX idx_rooms_type ON rooms(room_type);

-- Per-room, per-date inventory row. Optimistic locking via `version`.
-- One row per (room, date) guarantees a unique lock target for concurrency control.
CREATE TABLE room_inventory (
    id              BIGSERIAL PRIMARY KEY,
    room_id         BIGINT       NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    stay_date       DATE         NOT NULL,
    booked          BOOLEAN      NOT NULL DEFAULT FALSE,
    reservation_id  BIGINT,
    version         BIGINT       NOT NULL DEFAULT 0,
    UNIQUE (room_id, stay_date)
);
CREATE INDEX idx_inventory_room_date ON room_inventory(room_id, stay_date);

CREATE TABLE reservations (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT       NOT NULL REFERENCES users(id),
    hotel_id        BIGINT       NOT NULL REFERENCES hotels(id),
    room_id         BIGINT       NOT NULL REFERENCES rooms(id),
    check_in        DATE         NOT NULL,
    check_out       DATE         NOT NULL,
    guests          INT          NOT NULL,
    total_amount    NUMERIC(12,2) NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    expires_at      TIMESTAMP,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_reservations_customer ON reservations(customer_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_expires ON reservations(expires_at);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    reservation_id  BIGINT       NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    gateway         VARCHAR(32)  NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    transaction_ref VARCHAR(128),
    attempts        INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_reservation ON payments(reservation_id);
