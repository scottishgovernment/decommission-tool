-- a generated suggestion
CREATE TABLE page_suggestion (
  id varchar not null primary key,
  page_id varchar,
  rank integer,
  url varchar
);

ALTER TABLE page_suggestion ADD CONSTRAINT page_suggesiton_fk_page FOREIGN KEY (page_id) REFERENCES page (id);