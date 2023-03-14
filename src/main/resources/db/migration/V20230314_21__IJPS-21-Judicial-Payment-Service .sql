CREATE TABLE public.judicial-payment-service (
                             fee_record_id bigint not null,
                             hmcts_service_id varchar(60) not null,
                             fee_id varchar(60) not null,
                             judge_role_type_id varchar(60) not null,
                             standard_fee integer not null,
                             london_weighted_fee integer,
                             fee_description varchar(120) not null,
                             effective_from timestamp without time zone not null,
                             pensionable_code integer,
);
