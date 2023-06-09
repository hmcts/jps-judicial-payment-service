create table public.fee (
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

ALTER TABLE ONLY public.fee
    ADD CONSTRAINT payment_service_pkey PRIMARY KEY (fee_record_id);

ALTER TABLE ONLY public.fee
ADD CONSTRAINT uc_fee_id UNIQUE (fee_id);

CREATE SEQUENCE public.fee_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.fee_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY public.fee
ALTER COLUMN request_id SET DEFAULT nextval('public.judicial_id_seq');

ALTER SEQUENCE public.fee_id_seq OWNED BY public.fee.fee_id;
