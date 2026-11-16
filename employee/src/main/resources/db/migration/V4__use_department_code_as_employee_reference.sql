-- Keep UUID as the internal department primary key, but expose and reference
-- departments by a stable human-readable code.
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS fk_users_department;

ALTER TABLE users
    DROP COLUMN IF EXISTS department_id;

ALTER TABLE departments
    ADD COLUMN code VARCHAR(30);

WITH numbered_departments AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS row_num
    FROM departments
)
UPDATE departments d
SET code = 'DEPT-' || LPAD(n.row_num::text, 4, '0')
FROM numbered_departments n
WHERE d.id = n.id;

ALTER TABLE departments
    ALTER COLUMN code SET NOT NULL;

ALTER TABLE departments
    ADD CONSTRAINT uk_departments_code UNIQUE (code);

ALTER TABLE employees
    DROP CONSTRAINT IF EXISTS fk_employees_department;

ALTER TABLE employees
    ADD COLUMN department_code VARCHAR(30);

UPDATE employees e
SET department_code = d.code
FROM departments d
WHERE e.department_id = d.id;

ALTER TABLE employees
    ALTER COLUMN department_code SET NOT NULL;

ALTER TABLE employees
    DROP COLUMN department_id;

ALTER TABLE employees
    RENAME COLUMN department_code TO department_id;

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_department_code
        FOREIGN KEY (department_id) REFERENCES departments(code);

CREATE INDEX idx_employees_department
    ON employees(department_id);
