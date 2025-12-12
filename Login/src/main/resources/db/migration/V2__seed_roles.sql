INSERT INTO roles (id, name, description) VALUES
    (1, 'ADMIN', 'System super admin'),
    (2, 'STUDENT', 'Student user'),
    (3, 'INSTITUTION_ADMIN', 'Admin user of a specific institution'),
    (4, 'CONTENT_MANAGER', 'Institution content manager')
ON CONFLICT (id) DO NOTHING;
