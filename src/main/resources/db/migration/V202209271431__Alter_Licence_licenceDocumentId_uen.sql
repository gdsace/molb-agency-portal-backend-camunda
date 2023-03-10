ALTER TABLE licence
    ALTER COLUMN licence_document_id TYPE jsonb USING licence_document_id::jsonb;
ALTER TABLE licence
    ALTER Column uen DROP NOT NULL;
Alter TABLE licence
    add constraint fk_application_licence foreign key (application_id) references application (id);
ALTER TABLE licence
    rename column licence_document_id to licence_documents;
ALTER TABLE licence
    ALTER Column licence_documents DROP NOT NULL;
ALTER TABLE licence
    ADD licence_issuance_type varchar NULL;
UPDATE
    licence
SET licence_issuance_type = 'NO_LICENCE'
WHERE licence.licence_issuance_type IS NULL;
ALTER TABLE licence ALTER COLUMN licence_issuance_type SET NOT NULL;
