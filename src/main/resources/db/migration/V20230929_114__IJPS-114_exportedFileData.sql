CREATE TABLE public.exported_file_data (
   exported_file_data_id bigserial constraint pk_exported_file_data primary key,
   exported_file_id bigint not null,
   sitting_record_id bigint not null unique,
   record_type varchar(255) not null,
   transaction_id bigint not null,
   employee_number varchar(255) not null,
   transaction_date date not null,
   transaction_time time not null,
   pay_element_id bigint not null,
   pay_element_start_date date not null,
   fixed_or_temp_indicator varchar(255) not null,
   employees_value varchar(255) not null,
   post_id bigint not null,
   cost_center varchar(255) not null,

   CONSTRAINT fk_exported_file_id
    FOREIGN KEY(exported_file_id)
    REFERENCES public.exported_files(exported_file_id),
   CONSTRAINT fk_sitting_record_id
    FOREIGN KEY(sitting_record_id)
    REFERENCES public.sitting_record(sitting_record_id)
);
