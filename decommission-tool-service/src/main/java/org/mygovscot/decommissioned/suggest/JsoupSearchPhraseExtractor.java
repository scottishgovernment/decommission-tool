package org.mygovscot.decommissioned.suggest;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.mygovscot.decommissioned.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsoupSearchPhraseExtractor implements SearchPhraseExtractor {

    @Autowired
    private DocumentSource documentSource;

    // selectors in order of precedence
    private String [] selectors = {"h1", "title"};

    public String extract(Page page) throws IOException {

        // fetch the page
        String firstHost = page.getSite().getHost().split(" ")[0];
        String url = "http://"+firstHost+page.getSrcUrl();
        Document doc = documentSource.getDocument(url);

        // iterate over the selectors until we get one with a non empty value
        for (String selector : selectors) {
            Elements matchingElements = doc.select(selector);
            if (!matchingElements.isEmpty()) {
                String txt = matchingElements.get(0).text();
                if (!StringUtils.isEmpty(txt)) {
                    return txt;
                }
            }
        }

        // we didnt find anything
        return null;
    }

}
