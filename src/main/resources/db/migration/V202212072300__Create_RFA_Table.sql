INSERT INTO activity_type(type)
VALUES ('PENDING_APPLICANT_ACTION'),
       ('RFA_SUBMITTED');

CREATE TABLE rfa
(
    id                  BIGSERIAL           PRIMARY KEY,
    application_id      BIGSERIAL           NOT NULL,
    revision_id_index   BIGINT              NOT NULL,
    status              VARCHAR(255)        NOT NULL,
    clarification_fields JSONB              NOT NULL,
    applicant_remarks   VARCHAR             NULL,
    response_date       TIMESTAMP           NULL,
    created_by          VARCHAR(255)        NOT NULL,
    created_at          TIMESTAMP           NOT NULL,
    updated_by          VARCHAR(255)        NOT NULL,
    updated_at          TIMESTAMP           NOT NULL,
    FOREIGN KEY (application_id) REFERENCES application(id)
	);
