CREATE TABLE creator_profiles (
    user_id         UUID PRIMARY KEY,
    bio             VARCHAR(1000),
    portfolio_url   VARCHAR(500),
    CONSTRAINT fk_creator_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);
