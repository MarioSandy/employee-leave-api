-- Seed the first department, employee, and administrator account.
-- The password is supplied through Flyway's admin-password-hash placeholder.
-- Expected format: {argon2}$argon2id$...

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
