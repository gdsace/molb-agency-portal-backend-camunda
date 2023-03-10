CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR      NOT NULL,
    email      VARCHAR      NOT NULL,
    status     VARCHAR      NOT NULL,
    is_deleted BOOLEAN,
    agency_id  BIGINT       NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);
CREATE INDEX users_email_idx ON users (email);
ALTER TABLE users
    ADD FOREIGN KEY (agency_id) REFERENCES agency (id);

CREATE TABLE role
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR UNIQUE NOT NULL,
    name VARCHAR        NOT NULL
);

CREATE TABLE authority
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR UNIQUE NOT NULL,
    name VARCHAR        NOT NULL
);

CREATE TABLE user_role
(
    users_id BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    CONSTRAINT user_role_un UNIQUE (users_id, role_id)
);

ALTER TABLE user_role
    ADD FOREIGN KEY (users_id) REFERENCES users (id),
    ADD FOREIGN KEY (role_id) REFERENCES role (id);

CREATE TABLE role_authority
(
    role_id      BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    CONSTRAINT role_authority_un UNIQUE (role_id, authority_id)
);

ALTER TABLE role_authority
    ADD FOREIGN KEY (role_id) REFERENCES role (id),
    ADD FOREIGN KEY (authority_id) REFERENCES authority (id);

INSERT INTO authority(code, name)
VALUES ('add_user', 'Add User'), ('edit_user', 'Edit User'), ('delete_user', 'Delete User');

INSERT INTO role(code, name)
VALUES ('system_admin', 'System Admin'),
       ('helpdesk', 'Helpdesk (L2)'),
       ('agency_supervisor', 'Agency Supervisor'),
       ('agency_officer', 'Agency Officer'),
       ('agency_officer_ro', 'Agency Officer (Read Only)');