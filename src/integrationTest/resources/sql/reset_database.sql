delete from status_history;
delete from sitting_record;
delete from judicial_office_holder;
delete from joh_attributes;
delete from joh_payroll;
alter sequence sitting_record_sitting_record_id_seq restart;
alter sequence status_history_status_history_id_seq restart;
alter sequence judicial_office_holder_local_joh_record_id_seq restart;
alter sequence joh_attributes_joh_attributes_id_seq restart;
alter sequence joh_payroll_joh_payroll_id_seq restart;
