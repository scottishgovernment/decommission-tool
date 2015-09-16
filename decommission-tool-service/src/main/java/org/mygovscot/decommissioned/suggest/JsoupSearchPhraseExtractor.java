package org.mygovscot.decommissioned.suggest;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.SuggestionsSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JsoupSearchPhraseExtractor implements SearchPhraseExtractor {

    @Autowired
    private DocumentSource documentSource;

    // selectors in order of precedence
    private SuggestionsSelector [] defaultSelectors = {selector("h1"), selector("title")};


    public String extract(Page page) throws IOException {

        List<SuggestionsSelector> selectors = page.getSite().getSuggestionsSelectors();

        // use the default selectors if none have been added
        if (selectors.isEmpty()) {
            selectors = Arrays.asList(defaultSelectors);
        }

        // fetch the page
        String firstHost = page.getSite().getHost().split(" ")[0];
        String url = "http://"+firstHost+page.getSrcUrl();
        Document doc = documentSource.getDocument(url);

        // iterate over the selectors until we get one with a non empty value
        for (SuggestionsSelector selector : selectors) {
            Elements matchingElements = doc.select(selector.getSelector());
            if (!matchingElements.isEmpty()) {
                String txt = getText(matchingElements, selector);
                if (!StringUtils.isEmpty(txt)) {
                    return txt;
                }
            }
        }

        // we didn't find anything
        return null;
    }

    private SuggestionsSelector selector(String elementSelector) {
        SuggestionsSelector s = new SuggestionsSelector();
        s.setSelector(elementSelector);
        return s;
    }

    private String getText(Elements elements, SuggestionsSelector selector) {
        Element el = elements.get(0);
        if (selector.getAttribSelector() == null) {
            return el.text();
        } else {
            return el.attr(selector.getAttribSelector());
        }
    }
}
