-- a generated suggestion
CREATE TABLE suggestions_selector (
  id varchar not null primary key,
  site_id varchar not null,
  selector varchar
);

ALTER TABLE suggestions_selector ADD CONSTRAINT suggestions_selector_fk_site FOREIGN KEY (site_id) REFERENCES site (id) ON DELETE  CASCADE;

ALTER TABLE suggestions_selector
  ADD CONSTRAINT site_and_selector_unique UNIQUE(site_id, selector);