INSERT INTO authority(code, name)
VALUES ('process_application','Process Application'),
       ('reassign_self','Reassign Owned Applications'),
       ('reassign_all','Reassign all Applications');
INSERT INTO role_authority(role_id, authority_id)
VALUES (4, 4),
       (3, 4),
       (3, 6),
       (4, 5),
       (1, 3),
       (1, 2),
       (1, 1),
       (3, 1),
       (3, 2),
       (3, 3);