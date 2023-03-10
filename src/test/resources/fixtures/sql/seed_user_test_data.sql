TRUNCATE application RESTART IDENTITY CASCADE;
TRUNCATE licence_type RESTART IDENTITY CASCADE;
TRUNCATE user_role RESTART IDENTITY CASCADE;
TRUNCATE users RESTART IDENTITY CASCADE;
TRUNCATE agency RESTART IDENTITY CASCADE;
TRUNCATE role_authority RESTART IDENTITY CASCADE;

INSERT INTO agency (id,code, name) VALUES (1,'spf', 'Singapore Police Force');
INSERT INTO agency (id,code, name) VALUES (2,'scdf', 'Singapore Civil Defence Force');

INSERT INTO role_authority (role_id, authority_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (3, 1),
       (3, 2),
       (3, 3),
       (4, 1),
       (4, 2),
       (5, 1);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES (nextval('users_id_seq'),'test0', 'test0@test.com', 'ACTIVE', false, 1, 'test', '2022-08-02 19:47:50.905977', 'test', '2022-08-02 19:47:50.905977');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'test0@test.com'), 1);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES (nextval('users_id_seq'),'test1', 'test1@test.com', 'ACTIVE', false, 1, 'test1', '2022-08-02 19:51:39.391812', 'test1', '2022-08-02 19:51:39.391812');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'test1@test.com'), 2);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES  (nextval('users_id_seq'),'test2', 'supervisor@tech.gov.sg', 'INACTIVE', false, 1, 'test2', '2022-08-02 19:54:34.972713', 'test2', '2022-08-02 19:54:34.972713');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'supervisor@tech.gov.sg'), 5);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES (nextval('users_id_seq'),'test3', 'test3@test.com', 'ACTIVE', false, 1, 'test3', '2022-08-02 19:54:34.97949', 'test3', '2022-08-02 19:54:34.97949');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'test3@test.com'), 3);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES (nextval('users_id_seq'),'test4', 'test4@test.com', 'ACTIVE', false, 1, 'test4', '2022-08-02 19:54:34.983549', 'test4', '2022-08-02 19:54:34.983549');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'test4@test.com'), 4);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES (nextval('users_id_seq'),'test5', 'test5@test.com', 'ACTIVE', false, 2, 'test5', '2022-08-02 19:54:34.983549', 'test5', '2022-08-02 19:54:34.983549');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'test5@test.com'), 4);

INSERT INTO users ("id","name", email, status, is_deleted, agency_id, created_by, created_at, updated_by, updated_at)
VALUES (nextval('users_id_seq'),'deleted user', 'deleted_user@test.com', 'ACTIVE', true, 1, 'deleted_user', '2022-08-02 19:54:34.983549', 'deleted_user', '2022-08-02 19:54:34.983549');

INSERT INTO user_role (users_id, role_id)
VALUES ((select "id" from users where email = 'deleted_user@test.com'), 4);
