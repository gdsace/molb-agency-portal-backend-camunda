ALTER TABLE rfa
    ADD COLUMN revision_id_index_created BIGINT NULL;

UPDATE rfa
SET revision_id_index_created = revision_id_index;

ALTER TABLE rfa
    ALTER COLUMN revision_id_index_created SET NOT NULL;

ALTER TABLE rfa
    RENAME COLUMN revision_id_index TO revision_id_index_updated;

INSERT INTO activity_type(type)
VALUES ('CANCEL_RFA');
