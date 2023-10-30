INSERT INTO public.judicial_office_holder ( personal_code)
VALUES ('4923421');
INSERT INTO public.judicial_office_holder ( personal_code)
VALUES ( '5123421');

INSERT INTO public.joh_attributes ( local_joh_record_id, effective_start_date, crown_servant_flag,
                                   london_flag)
VALUES ( 1, '2023-09-14', true, false);
INSERT INTO public.joh_attributes (joh_attributes_id, local_joh_record_id, effective_start_date, crown_servant_flag,
                                   london_flag)
VALUES (2, 2, '2023-09-14', false, true);

INSERT INTO public.joh_payroll (joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id,
                                payroll_id)
VALUES (1, 1, '2023-09-14', 'Judge', '123');
INSERT INTO public.joh_payroll (joh_payroll_id, local_joh_record_id, effective_start_date, judge_role_type_id,
                                payroll_id)
VALUES (2, 2, '2023-09-14', 'Health', '345');

