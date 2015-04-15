CREATE TABLE site (
  id varchar not null primary key,
  name varchar not null,
  description varchar,
  host varchar not null unique,
  site_match_msg varchar,
  category_match_msg varchar,
  page_match_msg varchar
);

CREATE TABLE page (
  id varchar not null primary key,
  site_id varchar,
  src_url varchar unique,
  target_url varchar,
  match_level varchar
);

ALTER TABLE page ADD CONSTRAINT page_fk_site FOREIGN KEY (site_id) REFERENCES site (id);