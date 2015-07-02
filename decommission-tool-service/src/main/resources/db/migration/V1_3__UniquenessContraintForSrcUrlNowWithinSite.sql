-- create a copy of the page table but withoput the unioque constraint on src_url
CREATE TABLE page_copy (
  id varchar not null primary key,
  site_id varchar,
  src_url varchar,
  target_url varchar,
  match_level varchar
);
insert into page_copy (select * from page);

-- drop the original table and then rename the copy
drop table page;
alter table page_copy rename to page;

ALTER TABLE page ADD CONSTRAINT site_and_src_url_unique UNIQUE (site_id, src_url);