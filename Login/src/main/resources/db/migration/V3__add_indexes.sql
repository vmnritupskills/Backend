-- V2__add_indexes.sql
-- INDEXES FOR PERFORMANCE (Long ID Schema)

-- ===============================
-- USERS
-- ===============================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_institution_id ON users(institution_id);

-- ===============================
-- SESSIONS
-- ===============================
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_active ON sessions(is_active);

-- ===============================
-- REFRESH TOKENS
-- ===============================
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);

-- ===============================
-- STUDENTS
-- ===============================
CREATE INDEX IF NOT EXISTS idx_students_email ON students(email);
CREATE INDEX IF NOT EXISTS idx_students_batch_id ON students(batch_id);

-- ===============================
-- BATCH YEARS
-- ===============================
CREATE INDEX IF NOT EXISTS idx_batch_years_institution_id ON batch_years(institution_id);

-- ===============================
-- BATCHES
-- ===============================
CREATE INDEX IF NOT EXISTS idx_batches_batch_year_id ON batches(batch_year_id);
CREATE INDEX IF NOT EXISTS idx_batches_institution_id ON batches(institution_id);

-- ===============================
-- CONTENT MANAGERS
-- ===============================
CREATE INDEX IF NOT EXISTS idx_cm_user_id ON content_managers(user_id);
CREATE INDEX IF NOT EXISTS idx_cm_institution_id ON content_managers(institution_id);

-- ===============================
-- CONTENT MANAGER BATCH MAPPING
-- ===============================
CREATE INDEX IF NOT EXISTS idx_cmb_cm_id ON content_manager_batches(content_manager_id);
CREATE INDEX IF NOT EXISTS idx_cmb_batch_id ON content_manager_batches(batch_id);

-- ===============================
-- AUDIT LOGS
-- ===============================
-- Column name is 'action' (NOT event_key)
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
