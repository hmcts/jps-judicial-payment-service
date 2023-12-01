CREATE TABLE public.exported_file_data_header (
   exported_group_id bigserial constraint pk_exported_file_data_header primary key,
   exported_date_time timestamp not null,
   group_name varchar(255) not null,
   exported_by varchar(255) not null,
   status varchar(255) not null,
   hmcts_service_id varchar(255) not null
);
