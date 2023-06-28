INSERT INTO public.sitting_record (sitting_record_id, sitting_date, status_id, region_id, epimms_id, hmcts_service_id, personal_code, contract_type_id, judge_role_type_id, am, pm, created_date_time, created_by_user_id, change_date_time, change_by_user_id) VALUES (1, '2023-05-11', 'RECORDED', '3', '852649', 'BBA3', '4918178', 1, 'HealthWorker', false, true, null, null, null, null);


INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, change_date_time, change_by_user_id, change_by_name) VALUES (1, 1, 'RECORDED', '2023-06-27 11:40:30.430090', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, change_date_time, change_by_user_id, change_by_name) VALUES (2, 1, 'SUBMITTED', '2023-06-28 11:40:30.490419', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
INSERT INTO public.status_history (status_history_id, sitting_record_id, status_id, change_date_time, change_by_user_id, change_by_name) VALUES (3, 1, 'PUBLISHED', '2023-06-29 11:40:30.494324', 'd139a314-eb40-45f4-9e7a-9e13f143cc3a', 'Recorder');
