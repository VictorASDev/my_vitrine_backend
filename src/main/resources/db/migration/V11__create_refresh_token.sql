CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    token_id    UUID NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL,
    CONSTRAINT uq_refresh_tokens_token_id UNIQUE (token_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Acelera a checagem de tokens ativos de um usuario (usada na deteccao de reuso e no revoke-all).
CREATE INDEX idx_refresh_tokens_user_id_revoked ON refresh_tokens (user_id, revoked);
