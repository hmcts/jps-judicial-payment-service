alter table public.sitting_record
drop column if exists created_date_time cascade;
alter table public.sitting_record
drop column if exists created_by_user_id cascade;
alter table public.sitting_record
drop column if exists change_date_time cascade;
alter table public.sitting_record
drop column if exists change_by_user_id cascade;
