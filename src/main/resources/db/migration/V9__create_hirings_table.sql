CREATE TABLE hirings (
    id              UUID PRIMARY KEY,
    store_id        UUID NOT NULL,
    creator_id      UUID NOT NULL,
    product_id      UUID NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    CONSTRAINT fk_hirings_store FOREIGN KEY (store_id) REFERENCES store_profiles (user_id),
    CONSTRAINT fk_hirings_creator FOREIGN KEY (creator_id) REFERENCES creator_profiles (user_id),
    CONSTRAINT fk_hirings_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_hirings_status CHECK (status IN ('REQUESTED', 'ACCEPTED', 'IN_PRODUCTION', 'DELIVERED', 'APPROVED'))
);
