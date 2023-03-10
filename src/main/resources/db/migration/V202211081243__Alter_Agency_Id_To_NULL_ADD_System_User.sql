ALTER TABLE USERS ALTER COLUMN agency_id DROP NOT NULL;

INSERT INTO public.users
(name, email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES( 'SYSTEM', 'system_user@system.com', 'ACTIVE', false, NULL, ' ', NOW(), ' ', NOW());

INSERT INTO public.user_role
        (users_id, role_id)
    VALUES
        ((SELECT id FROM users WHERE email = 'system_user@system.com'), (SELECT id FROM role WHERE code = 'system_admin'));


