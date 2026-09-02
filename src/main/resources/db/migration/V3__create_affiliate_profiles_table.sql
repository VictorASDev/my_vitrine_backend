CREATE TABLE affiliate_profiles (
    user_id     UUID PRIMARY KEY,
    bio         VARCHAR(1000),
    niche       VARCHAR(150),
    profile_photo_url VARCHAR(500),
    CONSTRAINT fk_affiliate_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);
