INSERT INTO users (
    email,
    password_hash,
    name,
    role_id,
    is_active
)
VALUES (
    'admin@lms.com',
    '$2a$12$LbJfi2VSpUoWI4LkbZMq7e0auJnFgyakLN8BYBT30H9ufRRXapl4K',
    'System Admin',
    1,
    TRUE
);
