CREATE TABLE departments (
     id UUID PRIMARY KEY,
     name VARCHAR(100) NOT NULL UNIQUE,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP WITH TIME ZONE
);

ALTER TABLE employees
    DROP COLUMN department;

ALTER TABLE employees
    ADD COLUMN department_id UUID NOT NULL;

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(id);