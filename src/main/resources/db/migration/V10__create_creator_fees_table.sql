CREATE TABLE creator_fees (
    id                          UUID PRIMARY KEY,
    hiring_id                   UUID NOT NULL,
    amount                      NUMERIC(12, 2) NOT NULL,
    platform_retention_amount   NUMERIC(12, 2) NOT NULL,
    status                      VARCHAR(20) NOT NULL,
    CONSTRAINT uq_creator_fees_hiring UNIQUE (hiring_id),
    CONSTRAINT fk_creator_fees_hiring FOREIGN KEY (hiring_id) REFERENCES hirings (id),
    CONSTRAINT ck_creator_fees_status CHECK (status IN ('PENDING', 'CONFIRMED'))
);
