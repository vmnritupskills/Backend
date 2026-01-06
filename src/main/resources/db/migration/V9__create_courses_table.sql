CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    course_code VARCHAR(100) NOT NULL UNIQUE,
    duration VARCHAR(100) NOT NULL,
    semester INTEGER NOT NULL,

    syllabus_url TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
ALTER TABLE enrollments ADD COLUMN course_id BIGINT;

UPDATE enrollments e
SET course_id = c.id
FROM courses c
WHERE e.course_code = c.course_code;

ALTER TABLE enrollments ALTER COLUMN course_id SET NOT NULL;

ALTER TABLE enrollments DROP CONSTRAINT IF EXISTS fk_enrollment_course;
ALTER TABLE enrollments DROP COLUMN course_code;

ALTER TABLE enrollments
ADD CONSTRAINT fk_enrollment_course
FOREIGN KEY (course_id) REFERENCES courses(id);


CREATE UNIQUE INDEX idx_courses_course_code
ON courses(id);
CREATE INDEX idx_courses_deleted_at
ON courses(deleted_at);
CREATE INDEX idx_courses_semester
ON courses(semester);
CREATE INDEX idx_courses_active
ON courses(semester)
WHERE deleted_at IS NULL;
