INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-05-11', 'SUBMITTED', '3', '852649', 'BBA3', '4918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-05-11', 'SUBMITTED', '3', '952649', 'BBA3', '5918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-05-11', 'SUBMITTED', '3', '952650', 'BBA3', '6018178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-05-11', 'SUBMITTED', '3', '952651', 'BBA3', '6118178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record ( sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES ( '2023-05-11', 'SUBMITTED', '3', '952652', 'BBA3', '6118179', 6, '58', false, true);


insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2023-24', 'judge', '4918178', 300);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2022-23', 'judge', '4918178', 3);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2023-24', 'judge', '5918178', 30);
insert into public.sitting_days (financial_year, judge_role_type_id, personal_code, sitting_count)
values ('2022-23', 'judge', '5918178', 3);

INSERT INTO public.court_venue (epimms_id, hmcts_service_id, cost_center_code)
VALUES ('952649', 'BBA3', '12');
INSERT INTO public.court_venue (epimms_id, hmcts_service_id, cost_center_code)
VALUES ('952650', 'BBA3', '12');
INSERT INTO public.court_venue (epimms_id, hmcts_service_id, cost_center_code)
VALUES ('952651', 'BBA3', '12');
INSERT INTO public.court_venue (epimms_id, hmcts_service_id, cost_center_code)
VALUES ('952652', 'BBA3', '12');
INSERT INTO public.court_venue (epimms_id, hmcts_service_id, cost_center_code)
VALUES ('952653', 'AAA4', '12');

INSERT INTO public.judicial_office_holder ( personal_code)
VALUES ('6018178');
INSERT INTO public.judicial_office_holder ( personal_code)
VALUES ( '5123421');
INSERT INTO public.judicial_office_holder ( personal_code)
VALUES ( '6118178');
INSERT INTO public.judicial_office_holder ( personal_code)
VALUES ( '6118179');

INSERT INTO public.joh_attributes ( local_joh_record_id, effective_start_date, crown_servant_flag,
                                    london_flag)
VALUES ( 1, '2023-05-11', true, false);
INSERT INTO public.joh_attributes ( local_joh_record_id, effective_start_date, crown_servant_flag,
                                   london_flag)
VALUES ( 2, '2023-09-14', false, true);
INSERT INTO public.joh_attributes ( local_joh_record_id, effective_start_date, crown_servant_flag,
                                    london_flag)
VALUES ( 3, '2023-05-11', true, false);
INSERT INTO public.joh_attributes ( local_joh_record_id, effective_start_date, crown_servant_flag,
                                    london_flag)
VALUES ( 4, '2023-05-11', true, false);


INSERT INTO public.joh_payroll (joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id,
                                payroll_id)
VALUES (2, 2, '2023-09-14', 'Health', '345');
INSERT INTO public.joh_payroll (joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id,
                                payroll_id)
VALUES (3, 3, '2023-05-11', 'Health', '345');
INSERT INTO public.joh_payroll (joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id,
                                payroll_id)
VALUES (4, 4, '2023-05-11', 'Health', '345');

INSERT INTO public.fee ( hmcts_service_id, judge_role_type_id, standard_fee, higher_threshold_fee,
                         london_weighted_fee, effective_start_date, fee_created_date)
VALUES ( 'BBA3', '58', 10, 100, 150, '2023-02-14', '2023-09-12');
INSERT INTO public.fee ( hmcts_service_id, judge_role_type_id, standard_fee, higher_threshold_fee,
                         london_weighted_fee, effective_start_date, fee_created_date)
VALUES ( 'AAA4', '58', 10, 100, 150, '2023-02-14', '2023-09-12');

insert into public.service
(hmcts_service_id, service_name, account_center_code, onboarding_start_date, retention_time_in_months, close_recorded_record_after_time_in_months)
values('AAA4', 'Real Service', 'TSTACTR1', '2023-02-22', 6, 3);




