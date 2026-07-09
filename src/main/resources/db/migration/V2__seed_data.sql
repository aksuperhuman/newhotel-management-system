-- =====================================================================
-- V2: Sample data. Passwords are BCrypt hashes of 'Password123!'
-- =====================================================================

INSERT INTO users (email, password, full_name, role) VALUES
 ('admin@hotel.com',   '$2a$10$Dow1p3s8Q3sVn8u9m0KpieNQ7xY0kZ0m1Y6b7c8d9e0f1g2h3i4j', 'System Admin', 'ADMIN'),
 ('manager@hotel.com', '$2a$10$Dow1p3s8Q3sVn8u9m0KpieNQ7xY0kZ0m1Y6b7c8d9e0f1g2h3i4j', 'Hotel Manager', 'HOTEL_MANAGER'),
 ('customer@hotel.com','$2a$10$Dow1p3s8Q3sVn8u9m0KpieNQ7xY0kZ0m1Y6b7c8d9e0f1g2h3i4j', 'Jane Customer', 'CUSTOMER');

INSERT INTO hotels (name, description, city, address, star_rating, manager_id) VALUES
 ('The Grand Meridian', 'Luxury downtown hotel', 'Bengaluru', '1 MG Road', 5, 2),
 ('Seaside Comfort Inn', 'Budget-friendly by the coast', 'Goa', 'Beach Rd 22', 3, 2);

INSERT INTO hotel_amenities (hotel_id, amenity) VALUES
 (1,'WIFI'),(1,'POOL'),(1,'SPA'),(1,'GYM'),(2,'WIFI'),(2,'PARKING');

INSERT INTO rooms (hotel_id, room_number, room_type, capacity, price, status) VALUES
 (1,'101','STANDARD',2,3500.00,'AVAILABLE'),
 (1,'201','DELUXE',3,6500.00,'AVAILABLE'),
 (1,'301','SUITE',4,12000.00,'AVAILABLE'),
 (2,'101','STANDARD',2,1800.00,'AVAILABLE'),
 (2,'102','PREMIUM',3,3200.00,'AVAILABLE');
