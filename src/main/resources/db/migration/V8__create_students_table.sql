CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,

    reg_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,

    graduation_year INT NOT NULL,
    department VARCHAR(255) NOT NULL,

    user_id BIGINT NOT NULL UNIQUE,
    institution_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_student_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_student_institution_id ON students(institution_id);
CREATE INDEX idx_student_user_id ON students(user_id);
