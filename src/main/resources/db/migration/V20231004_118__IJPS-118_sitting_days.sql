CREATE TABLE public.sitting_days (
   sitting_days_id bigserial constraint pk_sitting_days primary key,
   personal_code varchar(255) not null,
   judge_role_type_id varchar(255) not null,
   financial_year varchar(255) not null,
   sitting_count bigint not null,
   UNIQUE (personal_code, financial_year)
);
