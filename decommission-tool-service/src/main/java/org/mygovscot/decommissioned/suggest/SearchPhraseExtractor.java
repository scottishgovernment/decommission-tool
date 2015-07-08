package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Page;

import java.io.IOException;

public interface SearchPhraseExtractor {

    String extract(Page page) throws IOException;
}
