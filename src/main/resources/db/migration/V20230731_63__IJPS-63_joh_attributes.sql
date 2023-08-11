create table public.joh_attributes (
   joh_attributes_id bigserial constraint pk_joh_attributes primary key,
   local_joh_record_id bigint not null,
   effective_start_date date not null,
   crown_servant_flag boolean,
   london_flag boolean
);

ALTER TABLE public.joh_attributes
ADD CONSTRAINT fk_joh_attributes_judicial_office_holder FOREIGN KEY (local_joh_record_id)
REFERENCES public.judicial_office_holder(local_joh_record_id);
