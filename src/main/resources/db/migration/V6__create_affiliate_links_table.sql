CREATE TABLE affiliate_links (
    id              UUID PRIMARY KEY,
    affiliate_id    UUID NOT NULL,
    product_id      UUID NOT NULL,
    code            VARCHAR(50) NOT NULL,
    type            VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    CONSTRAINT uq_affiliate_links_code UNIQUE (code),
    CONSTRAINT fk_affiliate_links_affiliate FOREIGN KEY (affiliate_id) REFERENCES affiliate_profiles (user_id),
    CONSTRAINT fk_affiliate_links_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_affiliate_links_type CHECK (type IN ('LINK', 'COUPON'))
);
