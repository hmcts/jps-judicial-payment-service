create table public.sitting_record (
   sitting_record_id bigserial constraint pk_sitting_record primary key,
   sitting_date date not null,
   status_id varchar(255) not null,
   region_id varchar(2) not null,
   epims_id varchar(255) not null,
   hmcts_service_id varchar(255) not null,
   personal_code varchar(255) not null,
   contract_type_id bigint not null,
   judge_role_type_id varchar(255) not null,
   am boolean,
   pm boolean,
   created_date_time timestamp not null,
   created_by_user_id varchar(255) not null,
   change_date_time timestamp,
   change_by_user_id  varchar(255)
);
