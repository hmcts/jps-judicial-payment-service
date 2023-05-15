
CREATE TABLE public.status_history (
	status_history_ID bigint not null,
  sitting_record_ID bigint not null,
  status_ID date not null,
  change_date_time timestamp not null,
  change_by_user_ID status_id varchar(255) not null,
  CONSTRAINT pk_status_history PRIMARY KEY (status_history_id),
	CONSTRAINT fk_status_history FOREIGN KEY (sitting_record_ID)
);
