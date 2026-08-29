CREATE TABLE sales (
    id                  UUID PRIMARY KEY,
    affiliate_link_id   UUID NOT NULL,
    amount              NUMERIC(12, 2) NOT NULL,
    sale_date           TIMESTAMP NOT NULL,
    CONSTRAINT fk_sales_affiliate_link FOREIGN KEY (affiliate_link_id) REFERENCES affiliate_links (id)
);
