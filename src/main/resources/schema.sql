CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  user_name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL unique
);

create TABLE IF NOT EXISTS items (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  item_name VARCHAR(255) NOT NULL,
  item_description VARCHAR(2048),
  owner_id BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  available BOOLEAN
);

create TABLE IF NOT EXISTS bookings (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  item_id BIGINT,
  author_id BIGINT,
  start_booking TIMESTAMP WITHOUT TIME ZONE,
  finish_booking TIMESTAMP(0) WITHOUT TIME ZONE,
  booking_status VARCHAR(48) CHECK (booking_status IN ('WAITING', 'APPROVED', 'REJECTED', 'CANCELED', 'COMPLETED')),
  CONSTRAINT pk_booking PRIMARY KEY (id),
  FOREIGN KEY (item_id) REFERENCES items(id),
  FOREIGN KEY (author_id) REFERENCES users(id)
);

create TABLE IF NOT EXISTS comments (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  item_id BIGINT,
  author_id BIGINT,
  created TIMESTAMP WITHOUT TIME ZONE,
  text VARCHAR(5000),
  CONSTRAINT pk_comment PRIMARY KEY (id),
  FOREIGN KEY (item_id) REFERENCES items(id),
  FOREIGN KEY (author_id) REFERENCES users(id)
);


