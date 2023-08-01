CREATE TABLE judicial_office_holder (
    local_joh_record_id bigserial constraint pk_judicial_office_holder primary key,
    personal_code varchar(255) NOT NULL,
    UNIQUE (personal_code)
);


