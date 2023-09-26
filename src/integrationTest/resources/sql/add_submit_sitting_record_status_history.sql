
INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (nextval('sitting_record_sitting_record_id_seq'), '2023-05-11', 'RECORDED', '6', '852649', 'BBA3', '4918178', 2, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (nextval('sitting_record_sitting_record_id_seq'), '2023-05-11', 'RECORDED', '7', '852649', 'BBA3', '5918178', 1, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (nextval('sitting_record_sitting_record_id_seq'), '2023-05-11', 'RECORDED', '8', '852649', 'BBA3', '6918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (nextval('sitting_record_sitting_record_id_seq'), '2023-05-11', 'RECORDED', '9', '852649', 'BBA3', '7918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (nextval('sitting_record_sitting_record_id_seq'), '2023-05-11', 'RECORDED', '10', '852649', 'BBA3', '8918178', 6, 'HealthWorker', false, true);
INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm)
VALUES (nextval('sitting_record_sitting_record_id_seq'), '2023-05-11', 'RECORDED', '11', '852649', 'BBA3', '9918178', 6, 'HealthWorker', false, true);

INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 1, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 2, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 3, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 4, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 5, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, changed_date_time, changed_by_user_id, changed_by_name)
VALUES (nextval('status_history_status_history_id_seq'), 6, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');


INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'4918178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'5918178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'6918178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'7918178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'8918178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'9918178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'9928178');
INSERT INTO public.judicial_office_holder (local_joh_record_id, personal_code)
VALUES (nextval('judicial_office_holder_local_joh_record_id_seq'),'9938178');

INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (1,3, '2023-04-27', false, true);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (2,4, '2023-04-27', true, true);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (3,6, CURRENT_DATE + 2, true, true);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (4,7, CURRENT_DATE + 2, true, true);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (5,7, CURRENT_DATE, false, true);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (6,8, '2023-08-03', true, true);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag, london_flag)
VALUES (7,8, '2023-09-04', false, true);


