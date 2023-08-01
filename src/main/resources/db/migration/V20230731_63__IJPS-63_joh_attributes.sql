create table public.joh_attributes (
   joh_attributes_id bigserial constraint pk_joh_attributes primary key,
   local_joh_record_id bigint not null,
   effective_start_date date not null,
   crown_servant_flag boolean,
   london_flag boolean
);
