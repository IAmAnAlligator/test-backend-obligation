CREATE TABLE obligations (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    category VARCHAR(50) NOT NULL,
    recurrence VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    next_payment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);