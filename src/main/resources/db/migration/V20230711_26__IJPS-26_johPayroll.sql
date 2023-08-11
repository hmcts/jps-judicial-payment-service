ALTER TABLE IF EXISTS joh_payroll
  DROP CONSTRAINT IF EXISTS pk_joh_payroll;
ALTER TABLE IF EXISTS joh_payroll
  DROP CONSTRAINT IF EXISTS fk_johp_judicial_office_holder;
DROP TABLE IF EXISTS joh_payroll;

CREATE TABLE joh_payroll (
    joh_payroll_id bigserial constraint pk_joh_payroll primary key,
    local_joh_record_id bigint NOT NULL,
    effective_start_date date not null,
    judge_role_type_id varchar(255) NOT NULL,
    payroll_id varchar(255) NOT NULL
);

ALTER TABLE ONLY public.joh_payroll
ADD CONSTRAINT fk_johp_judicial_office_holder FOREIGN KEY (local_joh_record_id)
  REFERENCES public.judicial_office_holder(local_joh_record_id);


