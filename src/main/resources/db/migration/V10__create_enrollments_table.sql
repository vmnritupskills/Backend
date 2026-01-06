CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,

    student_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(100) NOT NULL,

    enrolled_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_enrolled BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id)
        REFERENCES students(reg_no)
        ON DELETE CASCADE,

    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_code)
        REFERENCES courses(course_code)
        ON DELETE CASCADE,

    CONSTRAINT uq_student_course UNIQUE (student_id, course_code)
);

CREATE INDEX idx_enrollment_student
    ON enrollments(student_id);

CREATE INDEX idx_enrollment_course
    ON enrollments(course_code);
