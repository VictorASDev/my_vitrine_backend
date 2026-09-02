ALTER TABLE users
    ALTER COLUMN profile_type DROP NOT NULL;

ALTER TABLE users
    ADD COLUMN registration_status VARCHAR(20) NOT NULL DEFAULT 'INCOMPLETE';

UPDATE users
SET registration_status = 'COMPLETE'
WHERE profile_type IS NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT ck_users_registration_status
        CHECK (registration_status IN ('INCOMPLETE', 'COMPLETE'));
