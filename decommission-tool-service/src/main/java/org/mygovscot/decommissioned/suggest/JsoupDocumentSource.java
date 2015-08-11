package org.mygovscot.decommissioned.suggest;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsoupDocumentSource implements DocumentSource {

    private static final Logger LOG = LoggerFactory.getLogger(JsoupDocumentSource.class);

    public Document getDocument(String url) throws IOException {
        LOG.info("fetching {}", url);
        try {
            return Jsoup.connect(url).timeout(10000).get();
        } catch (HttpStatusException e) {
            LOG.error("Failed to fetch url: ", e);
            throw e;
        }
    }

}
