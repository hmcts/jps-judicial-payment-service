create table public.service (
  local_service_record_id bigserial constraint pk_service primary key,
  hmcts_service_id varchar(255) not null,
  account_center_code varchar(255) not null,
  onboarding_start_date	date not null,
  retention_time_in_months integer
);
