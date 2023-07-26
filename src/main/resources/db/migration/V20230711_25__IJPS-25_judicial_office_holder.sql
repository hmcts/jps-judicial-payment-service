ALTER TABLE IF EXISTS judicial_office_holder
  DROP CONSTRAINT IF EXISTS pk_judicial_office_holder;
ALTER TABLE IF EXISTS judicial_office_holder
  DROP CONSTRAINT IF EXISTS fk_joh_sitting_record;
DROP TABLE IF EXISTS judicial_office_holder;

CREATE TABLE judicial_office_holder (
    local_joh_record_id bigserial constraint pk_judicial_office_holder primary key,
    sitting_record_id bigint NOT NULL
);

ALTER TABLE ONLY public.judicial_office_holder
    ADD CONSTRAINT fk_joh_sitting_record FOREIGN KEY (sitting_record_id)
        REFERENCES public.sitting_record(sitting_record_id);

