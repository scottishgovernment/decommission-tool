package org.mygovscot.decommissioned.suggest;

import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;

public class JsoupDocumentSourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionForInvalidUrl() throws IOException {
        new JsoupDocumentSource().getDocument("");
    }

    @Test(expected = IOException.class)
    public void throwsExceptionForNotRespondingUrl() throws IOException {
        Document d = new JsoupDocumentSource().getDocument("http://qqq.mnb.com");
    }

}
