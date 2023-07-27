create table public.fee (
                             fee_record_id bigint not null,
                             hmcts_service_id varchar(60) not null,
                             judge_role_type_id varchar(60) not null,
                             standard_fee integer not null,
                             higher_threshold_fee integer,
                             london_weighted_fee integer,
                             effective_from timestamp without time zone not null,
                             fee_created_date timestamp without time zone not null
);

ALTER TABLE ONLY public.fee
    ADD CONSTRAINT payment_service_pkey PRIMARY KEY (fee_record_id);

CREATE SEQUENCE public.fee_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
