INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-05-11', 'SUBMITTED', '3', '852649', 'BBA3', '4918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-09-10', 'RECORDED', '3', '852649', 'BBA3', '4918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ('2022-05-11', 'SUBMITTED', '4', '852649', 'BBA3', '4918178', 1, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ('2023-04-05', 'SUBMITTED', '4', '852649', 'BBA3', '4918178', 1, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ('2022-04-05', 'SUBMITTED', '4', '852649', 'BBA3', '4918178', 1, 'HealthWorker', false, true);

insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2023-24', 'judge', '4918178', 300);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2022-23', 'judge', '4918178', 3);
