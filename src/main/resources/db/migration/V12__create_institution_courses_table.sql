CREATE TABLE institution_courses (
    id BIGSERIAL PRIMARY KEY,

    institution_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ic_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ic_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_institution_course
        UNIQUE (institution_id, course_id)
);

CREATE INDEX idx_ic_institution
    ON institution_courses(institution_id);

CREATE INDEX idx_ic_course
    ON institution_courses(course_id);

ALTER TABLE institution_courses
ADD COLUMN deleted_at TIMESTAMP;
