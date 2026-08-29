CREATE TABLE commissions (
    id                          UUID PRIMARY KEY,
    sale_id                     UUID NOT NULL,
    commission_amount           NUMERIC(12, 2) NOT NULL,
    platform_retention_amount   NUMERIC(12, 2) NOT NULL,
    status                      VARCHAR(20) NOT NULL,
    CONSTRAINT uq_commissions_sale UNIQUE (sale_id),
    CONSTRAINT fk_commissions_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
    CONSTRAINT ck_commissions_status CHECK (status IN ('PENDING', 'CONFIRMED'))
);
