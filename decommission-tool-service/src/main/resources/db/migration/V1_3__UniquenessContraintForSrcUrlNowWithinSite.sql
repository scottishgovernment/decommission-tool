ALTER TABLE page DROP CONSTRAINT page_src_url_key;
ALTER TABLE page ADD CONSTRAINT site_and_src_url_unique UNIQUE (site_id, src_url);