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
