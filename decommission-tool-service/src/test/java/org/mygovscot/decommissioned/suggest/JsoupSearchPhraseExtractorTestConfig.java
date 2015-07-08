package org.mygovscot.decommissioned.suggest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

public class JsoupSearchPhraseExtractorTestConfig {

    @Bean
    public JsoupSearchPhraseExtractor getPhraseExtractor() throws IOException {
        return new JsoupSearchPhraseExtractor();
    }

    @Bean
    public DocumentSource getDocumentSource() throws IOException{
        DocumentSource ds = Mockito.mock(DocumentSource.class);
        Mockito.when(ds.getDocument("http://www.titleandh1.com/greenpath")).thenReturn(documentWithTitleAndH1("title", "h1"));
        Mockito.when(ds.getDocument("http://www.notitle.com/greenpath")).thenReturn(documentWithTitleAndH1(null, "h1"));
        Mockito.when(ds.getDocument("http://www.emptytitle.com/greenpath")).thenReturn(documentWithTitleAndH1("", "h1"));
        Mockito.when(ds.getDocument("http://www.noh1.com/greenpath")).thenReturn(documentWithTitleAndH1("title", null));
        Mockito.when(ds.getDocument("http://www.emptyh1.com/greenpath")).thenReturn(documentWithTitleAndH1("title", ""));
        Mockito.when(ds.getDocument("http://www.notitleorh1.com/greenpath")).thenReturn(documentWithTitleAndH1(null, null));
        return ds;
    }

    private final Document documentWithTitleAndH1(String title, String h1) {

        StringBuilder s = new StringBuilder();
        s.append("<html>");
        s.append("<head>");
        if (title != null) {
            s.append("<title>").append(title).append("</title>");
        }
        s.append("</head>");
        s.append("<body>");
        if (h1 != null) {
            s.append("<h1>").append(h1).append("</h1>");
        }
        s.append("</body>");
        return Jsoup.parse(s.toString(), "");
    }
}