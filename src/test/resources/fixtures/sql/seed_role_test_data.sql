TRUNCATE role RESTART IDENTITY CASCADE;

INSERT INTO role(code, name)
VALUES ('system_admin', 'System Admin'),
       ('helpdesk', 'Helpdesk (L2)'),
       ('agency_supervisor', 'Agency Supervisor'),
       ('agency_officer', 'Agency Officer'),
       ('agency_officer_ro', 'Agency Officer (Read Only)');
