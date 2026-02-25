CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE movies (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    duration_mins INT NOT NULL,
    release_date  DATE NOT NULL,
    genre         VARCHAR(50),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()

);

CREATE TABLE theaters (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    address    VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    CONSTRAINT unique_theater_name_city UNIQUE (name, city)
);

CREATE TABLE screens (
     id         BIGSERIAL PRIMARY KEY,
     theater_id BIGINT NOT NULL REFERENCES theaters(id),
     name       VARCHAR(50) NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
     updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE showtimes (
    id         BIGSERIAL PRIMARY KEY,
    movie_id   BIGINT NOT NULL REFERENCES movies(id),
    screen_id  BIGINT NOT NULL REFERENCES screens(id),
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE seats (
    id          BIGSERIAL PRIMARY KEY,
    showtime_id BIGINT NOT NULL REFERENCES showtimes(id),
    row         CHAR(1) NOT NULL,
    number      INT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version     INT NOT NULL DEFAULT 0,
    UNIQUE (showtime_id, row, number),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);


CREATE TABLE bookings (
    id              BIGSERIAL PRIMARY KEY,
    confirmation_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    showtime_id     BIGINT NOT NULL REFERENCES showtimes(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    version         INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE booking_seats (
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    seat_id    BIGINT NOT NULL REFERENCES seats(id),
    PRIMARY KEY (booking_id, seat_id)
);

-- Indexes

-- for fast cancellation lookups
CREATE INDEX idx_bookings_confirmation_id ON bookings(confirmation_id);
CREATE INDEX idx_seats_showtime_id ON seats(showtime_id);
CREATE INDEX idx_showtimes_movie_id ON showtimes(movie_id);
CREATE INDEX idx_showtimes_screen_id ON showtimes(screen_id);
CREATE INDEX idx_screens_theater_id ON screens(theater_id);

-- - **Movie:** `id`, `title`, `description`, `duration_mins`, `release_date`
-- - **Theater:** `id`, `name`, `city`, `address`
-- - **Screen (Auditorium):** `id`, `theater_id`, `name` (e.g., "Screen 1")
-- - **Seat:** `id`, `screen_id`, `row_letter` (A-Z), `seat_number` (0-20). *(Note: Since layouts are identical, this table can be pre-populated programmatically when a Screen is created).*
-- - **Showtime:** `id`, `movie_id`, `screen_id`, `start_time`, `end_time`
-- - **Booking:** `id`, `user_id`, `showtime_id`, `confirmation_id` (UUID - indexed for fast cancellation lookups), `status` (PENDING, CONFIRMED, CANCELLED)