CREATE TABLE users (
    id              UUID PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    profile_type    VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_profile_type CHECK (profile_type IN ('STORE', 'AFFILIATE', 'CREATOR'))
);

CREATE TABLE social_networks (
    id       UUID PRIMARY KEY,
    user_id  UUID NOT NULL,
    name     VARCHAR(100) NOT NULL,
    url      VARCHAR(500) NOT NULL,
    CONSTRAINT fk_social_networks_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_social_networks_user_id ON social_networks (user_id);
