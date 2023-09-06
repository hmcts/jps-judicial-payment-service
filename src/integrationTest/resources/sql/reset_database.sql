delete from joh_payroll;
delete from judicial_office_holder;
delete from status_history;
delete from sitting_record;
delete from service;
alter sequence sitting_record_sitting_record_id_seq restart;
alter sequence status_history_status_history_id_seq restart;
