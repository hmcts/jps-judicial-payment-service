CREATE TABLE public.exported_files (
  exported_file_id bigserial constraint pk_exported_files primary key,
  exported_group_id bigint not null,
  exported_date_time timestamp not null,
  file_name varchar(255) not null,
  file_record_count integer not null,
  CONSTRAINT fk_exported_files
    FOREIGN KEY(exported_group_id)
    REFERENCES public.exported_file_data_header(exported_group_id)
);



