CREATE TABLE institution_course_managers (
    id BIGSERIAL PRIMARY KEY,

    institution_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    content_manager_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_icm_institution FOREIGN KEY (institution_id) REFERENCES institutions(id),
    CONSTRAINT fk_icm_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_icm_cm FOREIGN KEY (content_manager_id) REFERENCES content_managers(id),

    CONSTRAINT uq_inst_course_cm UNIQUE (institution_id, course_id, content_manager_id)
);
