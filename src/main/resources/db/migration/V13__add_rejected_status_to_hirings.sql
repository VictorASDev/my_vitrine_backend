ALTER TABLE hirings
    DROP CONSTRAINT ck_hirings_status;

ALTER TABLE hirings
    ADD CONSTRAINT ck_hirings_status
        CHECK (status IN ('REQUESTED', 'REJECTED', 'ACCEPTED', 'IN_PRODUCTION', 'DELIVERED', 'APPROVED'));
