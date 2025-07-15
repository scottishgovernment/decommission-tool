package org.mygovscot.decommissioned.importer;

import org.mockito.Mockito;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.mygovscot.decommissioned.repository.WhitelistedHostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

@Configuration
public class ImportServiceTestConfig {

    @Bean
    public SiteRepository getSiteRepository() {
        List<Site> sites = new ArrayList<>();
        Collections.addAll(sites,
                site("invalidURI", "www.invalidurl.com"),
                site("emptyTarget", "www.emptyTarget.com"),
                site("withparams", "www.withparams.com"),
                site("greenPath", "www.greenpath.com"),
                site("greenPathWithHost", "www.greenpath.com"),
                site("wrongHost", "www.wronghost.com"),
                site("prePopulated", "www.prepop.com"),
                site("morethanonehost", "www.multi.com multi.com"));

        SiteRepository siteRepository = mock(SiteRepository.class);
        for (Site s : sites) {
            String id = s.getId();
            Mockito.when(siteRepository.getById(id)).thenReturn(s);
        }
        return siteRepository;
    }

    @Bean
    public PageRepository getPageRepository() {
        PageRepository pageRepository = mock(PageRepository.class);
        Mockito.when(pageRepository.findOneBySiteIdAndSrcUrl("prePopulated", "/one")).thenReturn(page(null, "/one", "/one-redirect"));
        Mockito.when(pageRepository.findOneBySiteIdAndSrcUrl("prePopulated", "/two")).thenReturn(page(null, "/two", "/two-redirect"));
        Mockito.when(pageRepository.findBySiteId("prePopulated")).thenReturn(Arrays.asList(page(null, "/one", "/one-redirect"), page(null, "/two", "/two-redirect")));
        Mockito.when(pageRepository.findBySiteId("greenPath")).thenAnswer(x -> new ArrayList<>());
        Mockito.when(pageRepository.findBySiteId("wrongHost")).thenAnswer(x -> new ArrayList<>());
        return pageRepository;
    }

    @Bean
    public WhitelistedHostRepository getWhitlistedHostsRepository() {
        return mock(WhitelistedHostRepository.class);
    }

    @Bean
    public ImportService getImportService() {
        return new ImportService();
    }

    static Page page(Site site, String srcUrl, String targetUrl) {
        return page(site, srcUrl, targetUrl, Page.RedirectType.PERMANENT);
    }

    static Page page(Site site, String srcUrl, String targetUrl, Page.RedirectType redirectType) {
        Page p = new Page();
        p.setSite(site);
        p.setSrcUrl(srcUrl);
        p.setTargetUrl(targetUrl);
        p.setType(Page.MatchType.EXACT);
        p.setRedirectType(redirectType);
        return p;
    }

    static Site site(String id, String host) {
        Site s = new Site();
        s.setHost(host);
        s.setName(id + " name");
        s.setHttpsSupported(false);
        s.setId(id);
        return s;
    }
}
