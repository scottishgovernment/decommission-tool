package org.mygovscot.decommissioned.importer;


import org.aspectj.lang.annotation.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import org.mygovscot.beta.config.BetaConfigInitializer;
import static org.mygovscot.decommissioned.importer.ImportServiceTestConfig.page;
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

@ContextConfiguration(classes=ImportServiceTestConfig.class)
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
        String csv = "/onegp, /one-redirect\n" +
                "/twogp, /two-redirect";

        // ACT
        sut.importRedirects("greenPath", csv);

        // ASSERT
        Site site = siteRepository.findOne("greenPath");
        Page page1 = page(site, "/onegp", "/one-redirect");
        Page page2 = page(site, "/twogp", "/two-redirect");
        Mockito.verify(pageRepository).save(eq(page1));
        Mockito.verify(pageRepository).save(eq(page2));
    }

    @Test
    public void greenPathMultipleHosts() {

        // ARRANGE
        String csv = "http://www.multi.com/onemulti, /one-redirect\n" +
                "http://multi.com/twomulti, /two-redirect";

        // ACT
        sut.importRedirects("morethanonehost", csv);

        // ASSERT
        Site site = siteRepository.findOne("morethanonehost");
        Page page1 = page(site, "/onemulti", "/one-redirect");
        Page page2 = page(site, "/twomulti", "/two-redirect");
        Mockito.verify(pageRepository).save(eq(page1));
        Mockito.verify(pageRepository).save(eq(page2));
    }

    @Test
    public void emptyTargetDefaultsToHomePage() {

        // ARRANGE
        String csv =
                "/oneemp,\n" +
                "/twoemp, /two-redirect";

        // ACT
        sut.importRedirects("emptyTarget", csv);

        // ASSERT
        Site site = siteRepository.findOne("emptyTarget");
        Page page1 = page(site, "/oneemp", "/");
        Page page2 = page(site, "/twoemp", "/two-redirect");
        Mockito.verify(pageRepository).save(eq(page1));
        Mockito.verify(pageRepository).save(eq(page2));
    }

    @Test
    public void noTargetColumnDefaultsToHomePage() {

        // ARRANGE
        String csv =
                "/one\n" + "/two, /two-redirect";

        // ACT
        sut.importRedirects("emptyTarget", csv);

        // ASSERT
        Site site = siteRepository.findOne("emptyTarget");
        Page page1 = page(site, "/one", "/");
        Page page2 = page(site, "/two", "/two-redirect");
        Mockito.verify(pageRepository).save(eq(page1));
        Mockito.verify(pageRepository).save(eq(page2));
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
        Mockito.verify(pageRepository).save(eq(page1));
        Mockito.verify(pageRepository).save(eq(page2));
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
        Mockito.verify(pageRepository, Mockito.never()).save(eq(page(null, "/one", "/one-redirect")));
        Mockito.verify(pageRepository, Mockito.never()).save(eq(page(null, "/two", "/two-redirect")));
    }

    @Test
    public void srcUrlContainingEncodesSpaces() {
        // ARRANGE
        Site site = siteRepository.findOne("prePopulated");
        String csv = "/one%20, /one-redirect\n" +
                "/two%20three, /two-redirect";

        // ACT
        sut.importRedirects("prePopulated", csv);

        // ASSERT
        Mockito.verify(pageRepository).save(eq(page(site, "/one%20", "/one-redirect")));
        Mockito.verify(pageRepository).save(eq(page(site, "/two%20three", "/two-redirect")));
    }
}
