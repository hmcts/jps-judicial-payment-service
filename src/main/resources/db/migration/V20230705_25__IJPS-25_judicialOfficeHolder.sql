CREATE TABLE judicial_office_holder (
    local_joh_record_id bigserial constraint pk_judicial_office_holder primary key,
    personal_code varchar(255) NOT NULL,
    UNIQUE (personal_code)
);

ALTER TABLE ONLY public.sitting_record
ADD CONSTRAINT uc_personal_code UNIQUE (personal_code);

ALTER TABLE ONLY public.judicial_office_holder
ADD CONSTRAINT fk_joh_sitting_record FOREIGN KEY (personal_code)
  REFERENCES public.sitting_record(personal_code);


