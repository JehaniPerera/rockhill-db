-- Function 4 (Payments & Billing) tables.
-- Spring runs this file on every start (spring.sql.init.mode=always), so every
-- statement uses IF NOT EXISTS. It runs AFTER Hibernate has created the Booking
-- table (spring.jpa.defer-datasource-initialization=true) and BEFORE data.sql.
-- There are deliberately no foreign keys: Function 4 reads Booking and Customer
-- with plain SQL, and a foreign key would block deleting a booking.

CREATE TABLE IF NOT EXISTS Payment (
    payment_id     INT           NOT NULL AUTO_INCREMENT,
    booking_id     BIGINT        NOT NULL,
    amount         DECIMAL(12,2) NOT NULL,
    payment_type   VARCHAR(20)   NOT NULL,                 -- 'Deposit' or 'Balance'
    payment_date   DATE          NOT NULL,
    payment_status VARCHAR(30)   NOT NULL,                 -- 'Partially Paid' or 'Fully Paid'
    refund_status  VARCHAR(20)   NOT NULL DEFAULT 'N/A',   -- 'N/A', 'Refunded' or 'Forfeited'
    refund_date    DATE          NULL,
    PRIMARY KEY (payment_id),
    KEY idx_payment_booking (booking_id)
);

CREATE TABLE IF NOT EXISTS Invoice (
    invoice_id   INT           NOT NULL AUTO_INCREMENT,
    booking_id   BIGINT        NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    issue_date   DATE          NOT NULL,
    PRIMARY KEY (invoice_id),
    KEY idx_invoice_booking (booking_id)
);

CREATE TABLE IF NOT EXISTS PaymentProof (
    proof_id         INT           NOT NULL AUTO_INCREMENT,
    booking_id       BIGINT        NOT NULL,
    payment_type     VARCHAR(20)   NOT NULL,               -- 'Deposit' or 'Balance'
    amount           DECIMAL(12,2) NOT NULL,
    bank_reference   VARCHAR(100)  NULL,
    file_name        VARCHAR(255)  NOT NULL,
    content_type     VARCHAR(100)  NULL,
    status           VARCHAR(30)   NOT NULL DEFAULT 'Pending Approval',  -- 'Pending Approval', 'Approved', 'Rejected'
    rejection_reason VARCHAR(500)  NULL,
    submitted_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at      DATETIME      NULL,
    PRIMARY KEY (proof_id),
    KEY idx_proof_booking (booking_id)
);
