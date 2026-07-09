-- =====================================================================
-- V1: Core schema for Hotel Reservation Engine
-- Note the `version` columns: they back JPA @Version optimistic locking.
-- =====================================================================

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token         VARCHAR(512) NOT NULL UNIQUE,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE hotels (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    city          VARCHAR(128) NOT NULL,
    address       VARCHAR(512),
    star_rating   INT          NOT NULL DEFAULT 0,
    manager_id    BIGINT       REFERENCES users(id),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_hotels_city ON hotels(city);
CREATE INDEX idx_hotels_star_rating ON hotels(star_rating);

CREATE TABLE hotel_amenities (
    hotel_id      BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    amenity       VARCHAR(64) NOT NULL,
    PRIMARY KEY (hotel_id, amenity)
);

CREATE TABLE hotel_images (
    id            BIGSERIAL PRIMARY KEY,
    hotel_id      BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    url           VARCHAR(1024) NOT NULL
);

CREATE TABLE rooms (
    id            BIGSERIAL PRIMARY KEY,
    hotel_id      BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_number   VARCHAR(32) NOT NULL,
    room_type     VARCHAR(32) NOT NULL,   -- STANDARD | DELUXE | PREMIUM | SUITE
    capacity      INT         NOT NULL,
    price         NUMERIC(12,2) NOT NULL,
    status        VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    version       BIGINT      NOT NULL DEFAULT 0,   -- optimistic lock
    UNIQUE (hotel_id, room_number)
);
CREATE INDEX idx_rooms_hotel ON rooms(hotel_id);
CREATE INDEX idx_rooms_type ON rooms(room_type);

CREATE TABLE room_features (
    room_id       BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    feature       VARCHAR(64) NOT NULL,
    PRIMARY KEY (room_id, feature)
);

-- Per-day inventory rows. A unique constraint on (room_id, date) is the
-- final database-level guarantee against double booking.
CREATE TABLE room_inventory (
    id            BIGSERIAL PRIMARY KEY,
    room_id       BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    stay_date     DATE   NOT NULL,
    reserved      BOOLEAN NOT NULL DEFAULT FALSE,
    reservation_id BIGINT,
    version       BIGINT NOT NULL DEFAULT 0,   -- optimistic lock
    UNIQUE (room_id, stay_date)
);
CREATE INDEX idx_inventory_room_date ON room_inventory(room_id, stay_date);

CREATE TABLE reservations (
    id            BIGSERIAL PRIMARY KEY,
    reference     VARCHAR(36) NOT NULL UNIQUE,
    user_id       BIGINT NOT NULL REFERENCES users(id),
    room_id       BIGINT NOT NULL REFERENCES rooms(id),
    check_in      DATE   NOT NULL,
    check_out     DATE   NOT NULL,
    guests        INT    NOT NULL,
    total_amount  NUMERIC(12,2) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    expires_at    TIMESTAMPTZ,
    version       BIGINT NOT NULL DEFAULT 0,   -- optimistic lock
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_expires ON reservations(expires_at);

CREATE TABLE payments (
    id            BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    provider      VARCHAR(32) NOT NULL,   -- RAZORPAY | STRIPE | PAYPAL
    provider_ref  VARCHAR(128),
    amount        NUMERIC(12,2) NOT NULL,
    status        VARCHAR(32) NOT NULL,   -- SUCCESS | FAILED | REFUNDED | PENDING
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_reservation ON payments(reservation_id);
