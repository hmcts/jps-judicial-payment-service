INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (1, '2023-05-11', 'RECORDED', '6', '852649', 'BBA3', '4918178', 2, 'HealthWorker', false, true);

INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (1,'4918178');

INSERT INTO public.joh_attributes
(local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES ( 1, '2023-05-14', false, false);

INSERT INTO public.joh_payroll
(joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id, payroll_id)
VALUES (1, 1, '2023-05-03', 'Judge', 2);

INSERT INTO public.joh_payroll
(joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id, payroll_id)
VALUES (2, 1, '2023-05-01', 'Health', 1);


INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (2, '2023-05-11', 'RECORDED', '6', '852649', 'BBA3', '4918180', 2, 'HealthWorker', false, true);

INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (2,'4918180');

INSERT INTO public.joh_attributes
(local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (2, '2023-05-11', false, false);

INSERT INTO public.joh_payroll
(joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id, payroll_id)
VALUES (3, 2, '2023-05-11', 'Judge', 2);


