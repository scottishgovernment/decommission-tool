package org.mygovscot.decommissioned.suggest;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface DocumentSource {

    Document getDocument(String url) throws IOException;
}
