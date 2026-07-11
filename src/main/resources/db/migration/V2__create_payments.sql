CREATE TABLE payments (
    id UUID PRIMARY KEY,
    obligation_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    paid_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_payment_obligation
        FOREIGN KEY (obligation_id)
        REFERENCES obligations(id)
        ON DELETE CASCADE
);