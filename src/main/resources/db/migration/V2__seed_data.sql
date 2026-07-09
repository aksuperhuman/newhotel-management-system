-- ============================================================
-- V2: Sample data. Passwords are BCrypt hashes of 'password123'.
-- ============================================================

INSERT INTO users (email, password, full_name, role) VALUES
  ('admin@hotel.com',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOa2j3k5x1sQx0bqk2n0qXwYb3dGkQK2u', 'System Admin',   'ADMIN'),
  ('manager@hotel.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa2j3k5x1sQx0bqk2n0qXwYb3dGkQK2u', 'Hotel Manager',  'HOTEL_MANAGER'),
  ('customer@hotel.com','$2a$10$7EqJtq98hPqEX7fNZaFWoOa2j3k5x1sQx0bqk2n0qXwYb3dGkQK2u', 'Jane Customer',  'CUSTOMER');

INSERT INTO amenities (name) VALUES
  ('WIFI'), ('POOL'), ('GYM'), ('SPA'), ('PARKING'), ('BREAKFAST'), ('AIRPORT_SHUTTLE');

INSERT INTO hotels (name, description, city, address, star_rating, manager_id) VALUES
  ('Grand Central Hotel', 'Luxury stay in the heart of downtown', 'Bengaluru', '1 MG Road', 5, 2),
  ('Seaside Retreat',     'Beachfront rooms with ocean views',    'Goa',       'Baga Beach Rd', 4, 2);

INSERT INTO hotel_amenities (hotel_id, amenity_id) VALUES
  (1,1),(1,2),(1,3),(1,6),
  (2,1),(2,2),(2,5),(2,7);

INSERT INTO rooms (hotel_id, room_number, room_type, capacity, price, status, features) VALUES
  (1,'101','STANDARD',2, 3500.00,'AVAILABLE','City view, King bed'),
  (1,'102','DELUXE',  2, 5500.00,'AVAILABLE','Balcony, Mini bar'),
  (1,'201','PREMIUM', 3, 8500.00,'AVAILABLE','Lounge access'),
  (1,'301','SUITE',   4,15000.00,'AVAILABLE','Private jacuzzi'),
  (2,'11', 'STANDARD',2, 3000.00,'AVAILABLE','Garden view'),
  (2,'12', 'DELUXE',  3, 6000.00,'AVAILABLE','Ocean view, Balcony');
