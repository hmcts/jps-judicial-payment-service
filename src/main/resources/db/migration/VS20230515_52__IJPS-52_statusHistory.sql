
CREATE TABLE public.status_history (
	status_history_id bigint not null,
  sitting_record_id bigint not null,
  status_id varchar(255) not null,
  change_date_time timestamp not null,
  change_by_user_id varchar(255) not null,
  CONSTRAINT pk_status_history PRIMARY KEY (status_history_id),
	CONSTRAINT fk_status_history FOREIGN KEY  (sitting_record_id) REFERENCES public.sitting_record(sitting_record_id);
);
