INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (1, '2023-12-06', 'SUBMITTED', '3', '852649', 'BBA3', '4918178', 6, 'HealthWorker', false, true);

INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 1, 'RECORDED', '2023-11-20 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 1, 'SUBMITTED', '2023-11-21 11:40:30.490419', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');

INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (2, '2023-12-11', 'SUBMITTED', '3', '852649', 'BBA3', '4918178', 6, 'HealthWorker', false, true);

INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 2, 'RECORDED', '2023-11-20 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 2, 'SUBMITTED', '2023-11-21 11:40:30.490419', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');

insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2023-24', 'judge', '4918178', 300);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2022-23', 'judge', '4918178', 3);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2023-24', 'judge', '5918178', 30);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2022-23', 'judge', '5918178', 3);
