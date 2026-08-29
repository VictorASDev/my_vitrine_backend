CREATE TABLE products (
    id                      UUID PRIMARY KEY,
    store_id                UUID NOT NULL,
    name                    VARCHAR(200) NOT NULL,
    price                   NUMERIC(12, 2) NOT NULL,
    commission_percentage   NUMERIC(5, 2) NOT NULL,
    image_url               VARCHAR(500),
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL,
    CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES store_profiles (user_id)
);
