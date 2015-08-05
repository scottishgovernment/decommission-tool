-- delete suggestions upon delete
ALTER TABLE page_suggestion DROP CONSTRAINT page_suggesiton_fk_page;
ALTER TABLE page_suggestion ADD CONSTRAINT page_suggesiton_fk_page FOREIGN KEY (page_id) REFERENCES page (id) ON DELETE  CASCADE;