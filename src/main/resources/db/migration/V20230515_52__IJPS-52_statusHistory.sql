create table public.status_history (
    status_history_id bigserial constraint pk_status_history primary key,
    sitting_record_id bigint not null,
    status_id varchar(255) not null,
    change_date_time timestamp not null,
    change_by_user_id varchar(255) not null,
    change_by_name varchar(255) not null
);
