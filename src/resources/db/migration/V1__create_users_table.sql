-- ═══════════════════════════════════════════════════════════════
-- V1__CREATE_USERS_TABLE.SQL
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(254) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'GUEST',
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
                       failed_attempts INT DEFAULT 0,
                       lock_time TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- ═══════════════════════════════════════════════════════════════

CREATE TABLE hotels (
                        id UUID PRIMARY KEY,
                        name VARCHAR(200) NOT NULL,
                        description TEXT NOT NULL,
                        country VARCHAR(100) NOT NULL,
                        city VARCHAR(100) NOT NULL,
                        price_per_night DECIMAL(10, 2) NOT NULL,
                        stars DECIMAL(2, 1) NOT NULL,
                        region VARCHAR(30) NOT NULL,
                        cover_image_url VARCHAR(500),
                        is_featured BOOLEAN DEFAULT FALSE,
                        average_rating DECIMAL(3, 2),
                        total_reviews INT DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP
);

CREATE INDEX idx_hotel_country ON hotels(country);
CREATE INDEX idx_hotel_region ON hotels(region);
CREATE INDEX idx_hotel_price ON hotels(price_per_night);

-- ═══════════════════════════════════════════════════════════════

CREATE TABLE rooms (
                       id UUID PRIMARY KEY,
                       hotel_id UUID NOT NULL,
                       room_number VARCHAR(50) NOT NULL,
                       type VARCHAR(30) NOT NULL,
                       price DECIMAL(10, 2) NOT NULL,
                       capacity INT NOT NULL,
                       available BOOLEAN DEFAULT TRUE,
                       floor_number INT,
                       description TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
                       UNIQUE(hotel_id, room_number)
);

CREATE INDEX idx_room_hotel ON rooms(hotel_id);
CREATE INDEX idx_room_available ON rooms(hotel_id, available);

-- ═══════════════════════════════════════════════════════════════

CREATE TABLE reservations (
                              id UUID PRIMARY KEY,
                              user_id UUID NOT NULL,
                              hotel_id UUID NOT NULL,
                              room_id UUID NOT NULL,
                              check_in_date DATE NOT NULL,
                              check_out_date DATE NOT NULL,
                              total_price DECIMAL(12, 2) NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              special_requests TEXT,
                              confirmation_code VARCHAR(20) NOT NULL UNIQUE,
                              number_of_guests INT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,
                              CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_reservation_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
                              CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE INDEX idx_res_user ON reservations(user_id);
CREATE INDEX idx_res_hotel ON reservations(hotel_id);
CREATE INDEX idx_res_code ON reservations(confirmation_code);

-- ═══════════════════════════════════════════════════════════════

CREATE TABLE reviews (
                         id UUID PRIMARY KEY,
                         hotel_id UUID NOT NULL,
                         user_id UUID NOT NULL,
                         reservation_id UUID,
                         rating INT NOT NULL,
                         comment TEXT,
                         verified BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP,
                         CONSTRAINT fk_review_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
                         CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_review_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
                         UNIQUE(user_id, hotel_id)
);

CREATE INDEX idx_review_hotel ON reviews(hotel_id);
CREATE INDEX idx_review_user ON reviews(user_id);

-- ═══════════════════════════════════════════════════════════════

CREATE TABLE hotel_amenities (
                                 hotel_id UUID NOT NULL,
                                 amenity VARCHAR(100) NOT NULL,
                                 CONSTRAINT fk_hotel_amenities_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);

CREATE INDEX idx_hotel_amenities_hotel_id ON hotel_amenities(hotel_id);

-- ═══════════════════════════════════════════════════════════════

CREATE TABLE room_images (
                             room_id UUID NOT NULL,
                             image_url VARCHAR(500) NOT NULL,
                             CONSTRAINT fk_room_images_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE INDEX idx_room_images_room_id ON room_images(room_id);