CREATE TABLE content_managers (
    id BIGSERIAL PRIMARY KEY,

    institution_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,

    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    department VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cm_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cm_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_cm_institution_id ON content_managers(institution_id);
CREATE INDEX idx_cm_user_id ON content_managers(user_id);
