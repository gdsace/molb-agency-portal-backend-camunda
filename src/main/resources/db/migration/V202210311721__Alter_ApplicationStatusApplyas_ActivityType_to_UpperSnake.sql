UPDATE application
SET status =
    CASE
        WHEN status = 'Approved' THEN 'APPROVED'
        WHEN status = 'Overdue' THEN 'OVERDUE'
        WHEN status = 'PartiallyApproved' THEN 'PARTIALLY_APPROVED'
        WHEN status = 'PendingApplicantAction' THEN 'PENDING_APPLICANT_ACTION'
        WHEN status = 'PendingOnlinePayment' THEN 'PENDING_ONLINE_PAYMENT'
        WHEN status = 'PendingOfflinePayment' THEN 'PENDING_OFFLINE_PAYMENT'
        WHEN status = 'PendingWithdrawal' THEN 'PENDING_WITHDRAWAL'
        WHEN status = 'Processing' THEN 'PROCESSING'
        WHEN status = 'Rejected' THEN 'REJECTED'
        WHEN status = 'RFASubmitted' THEN 'RFA_SUBMITTED'
        WHEN status = 'Stage2Submitted' THEN 'STAGE2_SUBMITTED'
        WHEN status = 'Submitted' THEN 'SUBMITTED'
        WHEN status = 'Withdrawn' THEN 'WITHDRAWN'
        ELSE status
    END
WHERE status IS NOT NULL;

UPDATE application
SET apply_as =
    CASE
        WHEN apply_as = 'As an applicant' THEN 'APPLICANT'
        WHEN apply_as = 'On behalf of applicant' THEN 'ON_BEHALF'
        ELSE apply_as
    END
WHERE apply_as IS NOT NULL;

UPDATE activity_type
SET type =
    CASE
        WHEN type = 'CreateApplication' THEN 'CREATE_APPLICATION'
        WHEN type = 'ClaimApplication' THEN 'CLAIM_APPLICATION'
        WHEN type = 'PartiallyApproveApplication' THEN 'PARTIALLY_APPROVE_APPLICATION'
        WHEN type = 'ApproveApplication' THEN 'APPROVE_APPLICATION'
        WHEN type = 'AssignApplication' THEN 'ASSIGN_APPLICATION'
        WHEN type = 'RejectApplication' THEN 'REJECT_APPLICATION'
        WHEN type = 'ReassignApplication' THEN 'REASSIGN_APPLICATION'
        WHEN type = 'ApproveApplicationWithdrawal' THEN 'APPROVE_APPLICATION_WITHDRAWAL'
        WHEN type = 'RejectApplicationWithdrawal' THEN 'REJECT_APPLICATION_WITHDRAWAL'
        WHEN type = 'WithdrawApplication' THEN 'WITHDRAWAL_APPLICATION'
        WHEN type = 'ApplicationOverdue' THEN 'APPLICATION_OVERDUE'
        ELSE type
    END
WHERE type IS NOT NULL;
