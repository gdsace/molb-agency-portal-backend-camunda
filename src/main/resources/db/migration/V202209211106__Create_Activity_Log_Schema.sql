CREATE TABLE activity_type
(
    id   BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL
);

INSERT INTO activity_type(type)
VALUES ('CreateApplication'),
       ('ClaimApplication'),
       ('PartiallyApproveApplication'),
       ('ApproveApplication'),
       ('AssignApplication'),
       ('RejectApplication'),
       ('ReassignApplication'),
       ('ApproveApplicationWithdrawal'),
       ('RejectApplicationWithdrawal'),
       ('WithdrawApplication'),
       ('ApplicationOverdue')
