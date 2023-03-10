update activity_type  set type = 'RFA_RESPONDED' where type ='RFA_SUBMITTED';
update activity_type set type = 'SEND_RFA' where type = 'PENDING_APPLICANT_ACTION';
update activity_type set type = 'WITHDRAW_APPLICATION' where type = 'WITHDRAWAL_APPLICATION';

update application set status = 'RFA_RESPONDED' where status = 'RFA_SUBMITTED';
update application_history set status = 'RFA_RESPONDED' where status = 'RFA_SUBMITTED';
update rfa set status = 'RFA_RESPONDED' where status = 'RFA_SUBMITTED';