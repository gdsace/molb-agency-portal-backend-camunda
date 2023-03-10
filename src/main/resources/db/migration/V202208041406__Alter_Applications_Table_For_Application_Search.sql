ALTER TABLE application
ADD COLUMN case_status VARCHAR(255) NULL,
ADD COLUMN officer_name VARCHAR(255) NULL,
ADD COLUMN applicant_name VARCHAR(255) NULL;

UPDATE application
SET case_status = CASE
                      WHEN status = 'Approved' THEN 'Closed'
                      WHEN status = 'Overdue' THEN 'Closed'
                      WHEN status = 'PartiallyApproved' THEN 'Closed'
                      WHEN status = 'PendingApplicantAction' THEN 'Pending Applicant Assigned'
                      WHEN status = 'PendingOnlinePayment' THEN 'Pending Applicant Assigned'
                      WHEN status = 'PendingOfflinePayment' THEN 'Pending Applicant Assigned'
                      WHEN status = 'PendingWithdrawal' THEN 'Pending Assignment Assigned'
                      WHEN status = 'Processing' THEN 'Assigned'
                      WHEN status = 'Rejected' THEN 'Closed'
                      WHEN status = 'RFASubmitted' THEN 'Pending Assignment Assigned'
                      WHEN status = 'Stage2Submitted' THEN 'Pending Assignment Assigned'
                      WHEN status = 'Submitted' THEN 'Pending Assignment'
                      WHEN status = 'Withdrawn' THEN 'Closed'
    END
WHERE case_status IS NULL;

UPDATE
    application
SET applicant_name = CASE
                         WHEN company ->>'uen' IS NULL THEN applicant->>'name'
                        ELSE company->>'companyName'
END
WHERE applicant_name IS NULL;

ALTER TABLE application ALTER COLUMN case_status SET NOT NULL;
ALTER TABLE application ALTER COLUMN applicant_name SET NOT NULL;