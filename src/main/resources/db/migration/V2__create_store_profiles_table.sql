CREATE TABLE store_profiles (
    user_id         UUID PRIMARY KEY,
    store_name      VARCHAR(150) NOT NULL,
    description     VARCHAR(1000),
    CONSTRAINT fk_store_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);
