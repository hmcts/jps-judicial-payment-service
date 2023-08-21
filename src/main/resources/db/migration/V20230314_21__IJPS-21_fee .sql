create table public.fee (
                             fee_record_id bigserial constraint pk_fee_record primary key,
                             hmcts_service_id varchar(60) not null,
                             judge_role_type_id varchar(60) not null,
                             standard_fee numeric not null,
                             higher_threshold_fee numeric,
                             london_weighted_fee numeric,
                             effective_from timestamp without time zone not null,
                             fee_created_date timestamp without time zone not null
);
