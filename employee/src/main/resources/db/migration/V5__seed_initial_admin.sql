-- Bootstrap the minimum data required to access the system for the first time.
-- The password must be supplied as a DelegatingPasswordEncoder-compatible Argon2 hash.
INSERT INTO departments (id, code, name)
VALUES (gen_random_uuid(), 'IT', 'Information Technology');

INSERT INTO employees (
    id,
    employee_code,
    full_name,
    email,
    department_id,
    employment_status
)
VALUES (
    gen_random_uuid(),
    'EMP-0001',
    'System Administrator',
    'admin@example.com',
    'IT',
    'ACTIVE'
);

INSERT INTO users (
    id,
    name,
    username,
    password,
    employee_id,
    role
)
SELECT
    gen_random_uuid(),
    'System Administrator',
    'admin',
    '${admin-password-hash}',
    e.id,
    'ADMIN'
FROM employees e
WHERE e.employee_code = 'EMP-0001';
