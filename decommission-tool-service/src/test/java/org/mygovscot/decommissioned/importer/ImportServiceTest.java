package org.mygovscot.decommissioned.importer;


import junit.framework.Assert;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void paramsInSrcUrlAreStripped() {

        // ARRANGE
        String csv = "/withparams?p1=one&p2=two";

        // ACT
        sut.importRedirects("withparams", csv);

        // ASSERT
        Site site = siteRepository.findOne("withparams");
        Page page1 = page(site, "/withparams", "/");
        Mockito.verify(pageRepository).save(eq(page1));
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

    @Test
    public void invalidSrcURI() {

        // ARRANGE
        String csv = "hhh:// /o    INVALID ne, /one-redirect";
        ImportResult expected = new ImportResult(
                Collections.singletonList(new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid srcUrl", 1)));

        // ACT
        ImportResult actual = sut.importRedirects("invalidURI", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void targetURLNotWhitelisted() {

        // ARRANGE
        String csv = "/one,http://black.com/";
        ImportResult expected = new ImportResult(
                Collections.singletonList(new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid targetUrl", 1)));

        // ACT
        ImportResult actual = sut.importRedirects("invalidURI", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void wrongSiteHost() {
        // ARRANGE
        String csv = "http://www.wronghost-different.com/one, /one-redirect";
        ImportResult expected = new ImportResult(
                Collections.singletonList(new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid srcUrl", 1)));

        // ACT
        ImportResult actual = sut.importRedirects("wrongHost", csv);

        // ASSERT
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void tooManyRecords() {
        // ARRANGE
        String csv = "/one, /one-redirect,what am I?";
        ImportResult expected = new ImportResult(
                Collections.singletonList(new ImportRecordResult(ImportRecordResult.Type.ERROR, "Wrong Number of Fields", 1)));


        // ACT
        ImportResult actual = sut.importRedirects("greenPath", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
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

    @Test
    public void importTestResult() {
        ImportTypeResult one = new ImportTypeResult(ImportRecordResult.Type.ERROR, "msg", Collections.singletonList(new Long(1)));
        ImportTypeResult two = new ImportTypeResult(ImportRecordResult.Type.ERROR, "msg", Collections.singletonList(new Long(1)));
        Set<ImportTypeResult> set = new HashSet<>();
        set.add(one);

        Assert.assertTrue(set.contains(two));
        Assert.assertTrue(one.equals(one));
        Assert.assertTrue(one.equals(two));
        Assert.assertFalse(one.equals(one.toString()));
        ;
    }

    @Test
    public void importRecordResult() {
        ImportRecordResult one = new ImportRecordResult(ImportRecordResult.Type.ERROR, "msg", new Long(1));
        ImportRecordResult two = new ImportRecordResult(ImportRecordResult.Type.ERROR, "msg", new Long(1));
        Set<ImportRecordResult> set = new HashSet<>();
        set.add(one);

        Assert.assertTrue(set.contains(two));
        Assert.assertTrue(one.equals(one));
        Assert.assertTrue(one.equals(two));
        Assert.assertFalse(one.equals(one.toString()));
    }

    @Test
    public void importResult() {
        ImportRecordResult recRes = new ImportRecordResult(ImportRecordResult.Type.ERROR, "msg", new Long(1));
        ImportResult one = new ImportResult(Collections.singletonList(recRes));
        ImportResult two = new ImportResult(Collections.singletonList(recRes));
        Set<ImportResult> set = new HashSet<>();
        set.add(one);

        Assert.assertTrue(set.contains(two));
        Assert.assertTrue(one.equals(one));
        Assert.assertTrue(one.equals(two));
        Assert.assertFalse(one.equals(one.toString()));
    }
}
