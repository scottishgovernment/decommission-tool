package org.mygovscot.decommissioned.importer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mygovscot.decommissioned.importer.ImportServiceTestConfig.page;

@ContextConfiguration(classes=ImportServiceTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ImportServiceTest {

    @Inject
    ImportService sut;

    @Inject
    PageRepository pageRepository;

    @Inject
    SiteRepository siteRepository;
    
    @Test
    public void greenPath() {

        // ARRANGE
        String csv = "/onegp, /one-redirect\n" +
                "/twogp, /two-redirect\n" +
                "/threegp, /three-redirect, TEMPORARY\n" +
                "/fourgp, /four-redirect, REDIRECT\n" +
                "/fivegp, /five-redirect, PERMANENT"
                ;

        // ACT
        sut.importRedirects("greenPath", csv);

        // ASSERT
        Site site = siteRepository.getById("greenPath");
        Page page1 = page(site, "/onegp", "/one-redirect", Page.RedirectType.PERMANENT);
        Page page2 = page(site, "/twogp", "/two-redirect", Page.RedirectType.PERMANENT);
        Page page3 = page(site, "/threegp", "/three-redirect", Page.RedirectType.REDIRECT);
        Page page4 = page(site, "/fourgp", "/four-redirect", Page.RedirectType.REDIRECT);
        Page page5 = page(site, "/fivegp", "/five-redirect", Page.RedirectType.PERMANENT);
        verify(pageRepository).save(eq(page1));
//        verify(pageRepository).save(eq(page2));
//        verify(pageRepository).save(eq(page3));
//        verify(pageRepository).save(eq(page4));
//        verify(pageRepository).save(eq(page5));
    }

    @Test
    public void greenPathMultipleHosts() {

        // ARRANGE
        String csv = "http://www.multi.com/onemulti, /one-redirect\n" +
                "http://multi.com/twomulti, /two-redirect";

        // ACT
        sut.importRedirects("morethanonehost", csv);

        // ASSERT
        Site site = siteRepository.getById("morethanonehost");
        Page page1 = page(site, "/onemulti", "/one-redirect");
        Page page2 = page(site, "/twomulti", "/two-redirect");
        verify(pageRepository).save(eq(page1));
        verify(pageRepository).save(eq(page2));
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
        Site site = siteRepository.getById("emptyTarget");
        Page page1 = page(site, "/oneemp", "/");
        Page page2 = page(site, "/twoemp", "/two-redirect");
        verify(pageRepository).save(eq(page1));
        verify(pageRepository).save(eq(page2));
    }

    @Test
    public void paramsInSrcUrlAreStripped() {

        // ARRANGE
        String csv = "/withparams?p1=one&p2=two";

        // ACT
        sut.importRedirects("withparams", csv);

        // ASSERT
        Site site = siteRepository.getById("withparams");
        Page page1 = page(site, "/withparams", "/");
        verify(pageRepository).save(eq(page1));
    }

    @Test
    public void noTargetColumnDefaultsToHomePage() {

        // ARRANGE
        String csv =
                "/one\n" + "/two, /two-redirect";

        // ACT
        sut.importRedirects("emptyTarget", csv);

        // ASSERT
        Site site = siteRepository.getById("emptyTarget");
        Page page1 = page(site, "/one", "/");
        Page page2 = page(site, "/two", "/two-redirect");
        verify(pageRepository).save(eq(page1));
        verify(pageRepository).save(eq(page2));
    }

    @Test
    public void greenPathWithFullyQualifiedUrl() {

        // ARRANGE
        String csv = "http://www.greenpath.com/one, /one-redirect\n" +
                "http://www.greenpath.com/two, /two-redirect";

        // ACT
        sut.importRedirects("greenPathWithHost", csv);

        // ASSERT
        Site site = siteRepository.getById("greenPathWithHost");
        Page page1 = page(site, "/one", "/one-redirect");
        Page page2 = page(site, "/two", "/two-redirect");
        verify(pageRepository).save(eq(page1));
        verify(pageRepository).save(eq(page2));
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
        String csv = "/one, /one-redirect,PERMANENT,EXACT,what am I?";
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
        verify(pageRepository, never()).save(eq(page(null, "/one", "/one-redirect")));
        verify(pageRepository, never()).save(eq(page(null, "/two", "/two-redirect")));
    }

    @Test
    public void srcUrlContainingEncodesSpaces() {
        // ARRANGE
        Site site = siteRepository.getById("prePopulated");
        String csv = "/one%20, /one-redirect\n" +
                "/two%20three, /two-redirect";

        // ACT
        sut.importRedirects("prePopulated", csv);

        // ASSERT
        verify(pageRepository).save(eq(page(site, "/one%20", "/one-redirect")));
        verify(pageRepository).save(eq(page(site, "/two%20three", "/two-redirect")));
    }

    @Test
    public void duplicateSrcURIRecorded() {

        // ARRANGE
        String csv = "/one,/to\n"
                + "/one,/to";
        List<ImportRecordResult> results = new ArrayList<>();
        results.add(new ImportRecordResult(ImportRecordResult.Type.DUPLICATE, "srcUrl appears more than once in this file", 2));
        results.add(new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "", 1));
        ImportResult expected = new ImportResult(results);

        // ACT
        ImportResult actual = sut.importRedirects("invalidURI", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void importTestResult() {
        ImportTypeResult one = new ImportTypeResult(ImportRecordResult.Type.ERROR, "msg", Collections.singletonList(new Long(1)));
        ImportTypeResult two = new ImportTypeResult(ImportRecordResult.Type.ERROR, "msg", Collections.singletonList(new Long(1)));
        Set<ImportTypeResult> set = new HashSet<>();
        set.add(one);

        assertTrue(set.contains(two));
        assertTrue(one.equals(one));
        assertTrue(one.equals(two));
        assertFalse(one.equals(one.toString()));
    }

    @Test
    public void importRecordResult() {
        ImportRecordResult one = new ImportRecordResult(ImportRecordResult.Type.ERROR, "msg", new Long(1));
        ImportRecordResult two = new ImportRecordResult(ImportRecordResult.Type.ERROR, "msg", new Long(1));
        Set<ImportRecordResult> set = new HashSet<>();
        set.add(one);

        assertTrue(set.contains(two));
        assertTrue(one.equals(one));
        assertTrue(one.equals(two));
        assertFalse(one.equals(one.toString()));
    }

    @Test
    public void importResult() {
        ImportRecordResult recRes = new ImportRecordResult(ImportRecordResult.Type.ERROR, "msg", new Long(1));
        ImportResult one = new ImportResult(Collections.singletonList(recRes));
        ImportResult two = new ImportResult(Collections.singletonList(recRes));
        Set<ImportResult> set = new HashSet<>();
        set.add(one);

        assertTrue(set.contains(two));
        assertTrue(one.equals(one));
        assertTrue(one.equals(two));
        assertFalse(one.equals(one.toString()));
    }
    
    @Test
    public void importNoChange() {
        //Attempting to import an unchanged record should fail
        Mockito.when(pageRepository.findOneBySiteIdAndSrcUrl("prePopulated", "/one")).thenReturn(page(null, "/one", "/one-redirect"));
        Mockito.when(pageRepository.findOneBySiteIdAndSrcUrl("prePopulated", "/two")).thenReturn(page(null, "/two", "/two-redirect"));
        Mockito.when(pageRepository.findBySiteId("prePopulated")).thenReturn(Arrays.asList(page(null, "/one", "/one-redirect"), page(null, "/two", "/two-redirect")));
        
        String csv = "/one,/one-redirect";
        List<ImportRecordResult> results = new ArrayList<>();
        results.add(new ImportRecordResult(ImportRecordResult.Type.NOCHANGE, "Unchanged", 1));
        ImportResult expected = new ImportResult(results);

        // ACT
        ImportResult actual = sut.importRedirects("prePopulated", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void importChangeToMatchType() {
        //Attempting to import an unchanged record should fail
        String csv = "/one,/one-redirect,,REGEXP";
        List<ImportRecordResult> results = new ArrayList<>();
        results.add(new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "", 1));
        ImportResult expected = new ImportResult(results);

        // ACT
        ImportResult actual = sut.importRedirects("prePopulated", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void importChangeToRedirectType() {
        //Attempting to import an unchanged record should fail
        String csv = "/one,/one-redirect,TEMPORARY";
        List<ImportRecordResult> results = new ArrayList<>();
        results.add(new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "", 1));
        ImportResult expected = new ImportResult(results);

        // ACT
        ImportResult actual = sut.importRedirects("prePopulated", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void importChangeToUrl() {
        //Attempting to import an unchanged record should fail
        String csv = "/one,/one-redirect-change";
        List<ImportRecordResult> results = new ArrayList<>();
        results.add(new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "", 1));
        ImportResult expected = new ImportResult(results);

        // ACT
        ImportResult actual = sut.importRedirects("prePopulated", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void importWithQuotesChangeToUrl() {
        //Attempting to import an unchanged record should fail
        String csv = "\"/one\",\"/one-redirect,change\"";
        List<ImportRecordResult> results = new ArrayList<>();
        results.add(new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "", 1));
        ImportResult expected = new ImportResult(results);

        // ACT
        ImportResult actual = sut.importRedirects("prePopulated", csv);

        // ASSERT
        Assert.assertEquals(expected, actual);
    }
}
