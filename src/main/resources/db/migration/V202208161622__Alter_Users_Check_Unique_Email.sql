UPDATE users
SET is_deleted = FALSE
WHERE is_deleted IS NULL;

ALTER TABLE Users
    ALTER COLUMN is_deleted SET NOT NULL,
    ALTER COLUMN is_deleted SET DEFAULT FALSE;

DROP INDEX IF EXISTS uidx_uemail_not_deleted;
CREATE UNIQUE INDEX uidx_uemail_not_deleted
    ON users (email, is_deleted) WHERE is_deleted IS NOT TRUE;
