package org.mygovscot.decommissioned.importer;


import org.aspectj.lang.annotation.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mygovscot.beta.config.BetaConfigInitializer;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ContextConfiguration(classes=ImportServiceTest.class)
@Configuration
@RunWith(SpringJUnit4ClassRunner.class)
public class ImportServiceTest {

    @Autowired
    ImportService sut;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    SiteRepository siteRepository;

    @Test
    public void greenPath() {

        // ARRANGE
        String csv = "/one, /one-redirect\n" +
                "/two, /two-redirect";

        // ACT
        sut.importRedirects("greenPath", csv);

        // ASSERT
        Site site = siteRepository.findOne("greenPath");
        Page page1 = page(site, "/one", "/one-redirect");
        Page page2 = page(site, "/two", "/two-redirect");
        Mockito.verify(pageRepository).save(Mockito.eq(page1));
        Mockito.verify(pageRepository).save(Mockito.eq(page2));
    }

    @Test
    public void greenPathWithFullyQualifiedUrl() {

        // ARRANGE
        String csv = "http://www.greenpath.com/one, /one-redirect\n" +
                "http://www.greenpath.com/two, /two-redirect";

        // ACT
        sut.importRedirects("greenPathWithHost", csv);

        // ASSERT
        Site site = siteRepository.findOne("greenPathWithHost");
        Page page1 = page(site, "/one", "/one-redirect");
        Page page2 = page(site, "/two", "/two-redirect");
        Mockito.verify(pageRepository).save(Mockito.eq(page1));
        Mockito.verify(pageRepository).save(Mockito.eq(page2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSuchSite() {
        String csv = "/one, /one-redirect";
        sut.importRedirects("noSuchSite", csv);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidURI() {
        String csv = "hhh:// /o    INVALID ne, /one-redirect";
        sut.importRedirects("invalidURI", csv);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongSiteHost() {
        // ARRANGE
        String csv = "http://www.wronghost-different.com/one, /one-redirect\n" +
                "http://www.wronghost.com/two, /two-redirect";

        // ACT
        sut.importRedirects("wrongHost", csv);

        // ASSERT -- see expected exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooManyRecords() {
        // ARRANGE
        String csv = "/one, /one-redirect,what am I?\n" +
                "/two, /two-redirect";

        // ACT
        sut.importRedirects("greenPath", csv);

        // ASSERT -- see expected exception
    }

    @Test
    public void srcUrlASlreadyExistsNotOverwritten() {
        // ARRANGE

        String csv = "/one, /one-redirect\n" +
                "/two, /two-redirect";

        // ACT
        sut.importRedirects("prePopulated", csv);

        // ASSERT
        Mockito.verify(pageRepository, Mockito.never()).save(Mockito.eq(page(null, "/one", "/one-redirect")));
        Mockito.verify(pageRepository, Mockito.never()).save(Mockito.eq(page(null, "/two", "/two-redirect")));
    }

    @Bean
    public SiteRepository getSiteRepository() {
        List<Site> sites = new ArrayList<>();
        Collections.addAll(sites,
                site("invalidURI", "www.invalidurl.com"),
                site("greenPath", "www.greenpath.com"),
                site("greenPathWithHost", "www.greenpath.com"),
                site("wrongHost", "www.wronghost.com"),
                site("prePopulated", "www.prepop.com"));

        SiteRepository siteRepository = Mockito.mock(SiteRepository.class);
        for (Site s : sites) {
            Mockito.when(siteRepository.findOne(s.getId())).thenReturn(s);
        }
        return siteRepository;
    }

    @Bean
    public PageRepository getPageRepository() {
        PageRepository pageRepository = Mockito.mock(PageRepository.class);
        Mockito.when(pageRepository.findOneBySiteIdAndSrcUrl("prePopulated", "/one")).thenReturn(page(null, "/one", "/one-redirect"));
        Mockito.when(pageRepository.findOneBySiteIdAndSrcUrl("prePopulated", "/two")).thenReturn(page(null, "/two", "/two-redirect"));
        return pageRepository;
    }

    @Bean
    public ImportService getImportService() {
        return new ImportService();
    }


    private Page page(Site site, String srcUrl, String targetUrl) {
        Page p = new Page();
        p.setSite(site);
        p.setSrcUrl(srcUrl);
        p.setTargetUrl(targetUrl);
        return p;
    }

    private Site site(String id, String host) {
        Site s = new Site();
        s.setHost(host);
        s.setName(id + " name");
        s.setHttpsSupported(false);
        s.setId(id);
        return s;
    }
}
