CREATE TYPE leave_type AS ENUM(
    'SICK_LEAVE',
    'DAY_OFF'
);

CREATE TYPE leave_request_status AS ENUM(
    'PENDING',
    'APPROVED',
    'REJECTED'
);

CREATE TABLE leave_requests (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL,
    leave_type leave_type NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500),
    attachment_url VARCHAR(500),
    status leave_request_status NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    rejection_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_leave_dates
        CHECK (end_date >= start_date),
        CONSTRAINT chk_review
        CHECK (
            (status = 'PENDING' AND reviewed_by IS NULL)
            OR
            (status <> 'PENDING' AND reviewed_by IS NOT NULL)
        )
);

CREATE INDEX idx_leave_employee
    ON leave_requests(employee_id);

CREATE INDEX idx_leave_status
    ON leave_requests(status);

CREATE INDEX idx_leave_created_at
    ON leave_requests(created_at);