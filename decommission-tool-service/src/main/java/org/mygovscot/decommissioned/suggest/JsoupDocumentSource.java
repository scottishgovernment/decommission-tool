package org.mygovscot.decommissioned.suggest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsoupDocumentSource implements DocumentSource {

    public Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).timeout(120000).get();
    }

}
