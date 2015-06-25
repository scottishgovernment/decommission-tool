-- we do not use the following fields for anything: site_match_msg, category_match_msg varchar, page_match_msg
ALTER TABLE site DROP COLUMN site_match_msg;
ALTER TABLE site DROP COLUMN category_match_msg;
ALTER TABLE site DROP COLUMN page_match_msg;