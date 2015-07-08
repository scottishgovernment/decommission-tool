package org.mygovscot.decommissioned.suggest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@ContextConfiguration(classes=JsoupSearchPhraseExtractorTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JsoupSearchPhraseExtractorTest {

    @Autowired
    JsoupSearchPhraseExtractor sut;

    @Autowired
    DocumentSource documentSource;

    @Test
    public void greenpathTitleAndH1Present() throws IOException {
        Site site = new Site();
        site.setHost("www.titleandh1.com");
        Page page = new Page();
        page.setSrcUrl("/greenpath");
        page.setSite(site);
        String phrase = sut.extract(page);

        Assert.assertEquals("h1", phrase);
    }

    @Test
    public void greenpathNoTitle() throws IOException {
        Site site = new Site();
        site.setHost("www.notitle.com");
        Page page = new Page();
        page.setSrcUrl("/greenpath");
        page.setSite(site);
        String phrase = sut.extract(page);
        Assert.assertEquals("h1", phrase);
    }

    @Test
    public void greenpathEmptyTitle() throws IOException {
        Site site = new Site();
        site.setHost("www.emptytitle.com");
        Page page = new Page();
        page.setSrcUrl("/greenpath");
        page.setSite(site);
        String phrase = sut.extract(page);
        Assert.assertEquals("h1", phrase);
    }

    @Test
         public void greenpathNoH1() throws IOException {
        Site site = new Site();
        site.setHost("www.noh1.com");
        Page page = new Page();
        page.setSrcUrl("/greenpath");
        page.setSite(site);
        String phrase = sut.extract(page);
        Assert.assertEquals("title", phrase);
    }

    @Test
    public void greenpathEmptyH1() throws IOException {
        Site site = new Site();
        site.setHost("www.noh1.com");
        Page page = new Page();
        page.setSrcUrl("/greenpath");
        page.setSite(site);
        String phrase = sut.extract(page);
        Assert.assertEquals("title", phrase);
    }

    @Test
    public void greenpathNoH1OrTitle() throws IOException {
        Site site = new Site();
        site.setHost("www.notitleorh1.com");
        Page page = new Page();
        page.setSrcUrl("/greenpath");
        page.setSite(site);
        String phrase = sut.extract(page);
        Assert.assertEquals(null, phrase);
    }

}
