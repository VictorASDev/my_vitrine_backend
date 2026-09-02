CREATE TABLE store_profiles (
    user_id         UUID PRIMARY KEY,
    store_name      VARCHAR(150) NOT NULL,
    description     VARCHAR(1000),
    niche           VARCHAR(150),
    cnpj            VARCHAR(18),
    CONSTRAINT fk_store_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);
