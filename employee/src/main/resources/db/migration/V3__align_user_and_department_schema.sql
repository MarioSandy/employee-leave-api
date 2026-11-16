ALTER TABLE "user" RENAME TO users;

ALTER TABLE users
    ADD COLUMN department_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments(id);

ALTER TABLE departments
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL;
