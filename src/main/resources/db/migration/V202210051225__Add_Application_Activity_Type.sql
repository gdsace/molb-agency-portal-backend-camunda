ALTER TABLE application
    ADD COLUMN activity_type_id INTEGER NULL,
    ADD FOREIGN KEY (activity_type_id) REFERENCES activity_type (id);

ALTER TABLE application_history
    ADD COLUMN activity_type_id INTEGER NOT NULL;

UPDATE application
SET activity_type_id = CASE
                           WHEN status = 'Submitted' THEN 1
                           WHEN status = 'Processing' THEN 2
                           WHEN status = 'PartiallyApproved' THEN 3
                           WHEN status = 'Approved' THEN 4
                           WHEN status = 'Rejected' THEN 6
                           WHEN status = 'PendingWithdrawal' AND case_status = 'Pending Assignment' THEN 10
                           WHEN status = 'PendingWithdrawal' AND case_status = 'Assigned' THEN 2
                           WHEN status = 'Overdue' THEN 11
    END
WHERE activity_type_id IS NULL;

ALTER TABLE application
    ALTER COLUMN activity_type_id SET NOT NULL;

