package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Page;

public interface SuggesterListener {

    void start();

    void processingPage(Page page);

    void end();
}
