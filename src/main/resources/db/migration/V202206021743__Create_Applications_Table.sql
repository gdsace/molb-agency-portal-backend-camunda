CREATE TABLE agency
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255)        NOT NULL
);

CREATE TABLE licence_type
(
    id         BIGSERIAL PRIMARY KEY,
    licence_id BIGINT UNIQUE NOT NULL,
    name       VARCHAR(255)  NOT NULL,
    agency_id  BIGINT        NOT NULL
);

ALTER TABLE licence_type
    ADD FOREIGN KEY (agency_id) REFERENCES agency (id);

CREATE TABLE application
(
    id                  BIGSERIAL PRIMARY KEY,
    application_number  VARCHAR(255) UNIQUE NOT NULL,
    agency_id           BIGINT              NOT NULL,
    licence_type_id     BIGINT              NOT NULL,
    licence_name        VARCHAR(255)        NOT NULL,
    status              VARCHAR(255)        NOT NULL,
    submitted_date      TIMESTAMP           NOT NULL,
    transaction_type    VARCHAR(255)        NOT NULL,
    apply_as            VARCHAR(255)        NOT NULL,
    login_type          VARCHAR(255)        NOT NULL,
    applicant           JSONB               NULL,
    filer               JSONB               NULL,
    company             JSONB               NULL,
    licence_data_fields JSONB               NULL,
    form_meta_data      JSONB               NULL,
    reviewer_id         BIGINT              NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_by          VARCHAR(255)        NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

ALTER TABLE application
    ADD FOREIGN KEY (agency_id) REFERENCES agency (id),
    ADD FOREIGN KEY (licence_type_id) REFERENCES licence_type (id);

CREATE TABLE licence
(
    id                  BIGSERIAL PRIMARY KEY,
    application_id      BIGINT              NULL,
    licence_number      VARCHAR(255) UNIQUE NOT NULL,
    licence_name        VARCHAR(255)        NOT NULL,
    login_type          VARCHAR(255)        NOT NULL,
    uen                 VARCHAR(255)        NOT NULL,
    nric                VARCHAR(255)        NOT NULL,
    licence_type_id     BIGINT              NULL,
    status              VARCHAR(255)        NOT NULL,
    licence_document_id VARCHAR(255)        NOT NULL,
    issue_date          TIMESTAMP           NOT NULL,
    start_date          TIMESTAMP           NOT NULL,
    expiry_date         TIMESTAMP           NOT NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_by          VARCHAR(255)        NOT NULL,
    updated_at          TIMESTAMP           NOT NULL
);

ALTER TABLE licence
    ADD FOREIGN KEY (licence_type_id) REFERENCES licence_type (id);