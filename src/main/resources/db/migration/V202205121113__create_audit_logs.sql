DROP TABLE IF EXISTS audit_logs;

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    time TIMESTAMP NOT NULL,
    operation_type VARCHAR(10) NOT NULL,
    entity_identity VARCHAR(255),
    changes JSONB,
    snapshot_after_change JSONB
);
