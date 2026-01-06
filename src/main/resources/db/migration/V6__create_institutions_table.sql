CREATE TABLE institutions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    aishe_code VARCHAR(100) UNIQUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_institution_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE institutions
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP;
