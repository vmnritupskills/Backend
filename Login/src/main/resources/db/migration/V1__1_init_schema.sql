-- ===========================================
-- V1__init_schema.sql
-- LMS Schema using BIGSERIAL (Long IDs)
-- ===========================================

-- ===========================================
-- ROLES
-- ===========================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ===========================================
-- INSTITUTIONS
-- ===========================================
CREATE TABLE IF NOT EXISTS institutions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT now(),
    created_by BIGINT
);

-- ===========================================
-- USERS
-- ===========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    role_id BIGINT REFERENCES roles(id),
    institution_id BIGINT REFERENCES institutions(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP
);

-- ===========================================
-- AUDIT LOGS
-- ===========================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    performed_by VARCHAR(255),
    metadata TEXT,
    timestamp TIMESTAMP DEFAULT now()
);

-- ===========================================
-- SESSIONS
-- ===========================================
CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512),
    device_info TEXT,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT now()
);

-- ===========================================
-- REFRESH TOKENS
-- ===========================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_id BIGINT REFERENCES sessions(id) ON DELETE CASCADE,
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now()
);

-- ===========================================
-- BATCH YEARS
-- ===========================================
CREATE TABLE IF NOT EXISTS batch_years (
    id BIGSERIAL PRIMARY KEY,
    institution_id BIGINT NOT NULL REFERENCES institutions(id),
    name VARCHAR(255) NOT NULL,
    start_year TIMESTAMP,
    end_year TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now()
);

-- ===========================================
-- BATCHES
-- ===========================================
CREATE TABLE IF NOT EXISTS batches (
    id BIGSERIAL PRIMARY KEY,
    batch_year_id BIGINT NOT NULL REFERENCES batch_years(id),
    institution_id BIGINT NOT NULL REFERENCES institutions(id),
    name VARCHAR(255) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now()
);

-- ===========================================
-- CONTENT MANAGERS
-- ===========================================
CREATE TABLE IF NOT EXISTS content_managers (
    id BIGSERIAL PRIMARY KEY,
    institution_id BIGINT NOT NULL REFERENCES institutions(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    email VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT now()
);

-- ===========================================
-- CONTENT MANAGER → BATCH MAPPING
-- ===========================================
CREATE TABLE IF NOT EXISTS content_manager_batches (
    id BIGSERIAL PRIMARY KEY,
    content_manager_id BIGINT NOT NULL REFERENCES content_managers(id) ON DELETE CASCADE,
    batch_id BIGINT NOT NULL REFERENCES batches(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT now()
);

-- ===========================================
-- STUDENTS
-- ===========================================
CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    roll_number VARCHAR(100),
    branch VARCHAR(255),
    date_of_admission VARCHAR(100),
    batch_id BIGINT REFERENCES batches(id),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
