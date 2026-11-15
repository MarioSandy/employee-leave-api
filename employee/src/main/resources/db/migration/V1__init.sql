CREATE TYPE employment_status AS ENUM(
    'ACTIVE',
    'INACTIVE'
);

CREATE TYPE user_role AS ENUM(
    'EMPLOYEE',
    'MANAGER',
    'ADMIN'
);

CREATE TABLE employees (
   id UUID PRIMARY KEY,
   employee_code VARCHAR(30) NOT NULL UNIQUE,
   full_name VARCHAR(150) NOT NULL,
   email VARCHAR(150) NOT NULL UNIQUE,
   department VARCHAR(100) NOT NULL,
   employment_status employment_status NOT NULL DEFAULT 'ACTIVE',
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
   deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

CREATE INDEX idx_employees_department
    ON employees(department);

CREATE INDEX idx_employees_status
    ON employees(employment_status);

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    employee_id UUID NOT NULL,
    role user_role NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE user_session (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID,
    client_session_id UUID UNIQUE,
    client_name VARCHAR(50) NOT NULL,
    os_name VARCHAR(50) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    token TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);