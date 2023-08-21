alter table public.status_history
rename column change_date_time to changed_date_time;
alter table public.status_history
rename column change_by_user_id to changed_by_user_id;
alter table public.status_history
rename column change_by_name to changed_by_name;
