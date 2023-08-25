create table public.court_venue (
   local_court_venue_record_id bigserial constraint pk_court_venue primary key,
   epimms_id varchar(255) not null,
   hmcts_service_id varchar(255) not null,
   cost_center_code varchar(255) not null
);
