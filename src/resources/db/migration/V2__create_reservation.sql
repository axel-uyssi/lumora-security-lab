CREATE TABLE reservations (
                              id UUID PRIMARY KEY,

                              user_id UUID NOT NULL,

                              code VARCHAR(50) NOT NULL UNIQUE,

                              check_in DATE NOT NULL,
                              check_out DATE NOT NULL,

                              status VARCHAR(50) NOT NULL,

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP,

                              CONSTRAINT fk_reservation_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);