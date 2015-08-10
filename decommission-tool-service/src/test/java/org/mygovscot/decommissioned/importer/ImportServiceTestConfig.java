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
import java.util.Collections;
import java.util.List;

@Configuration
public class ImportServiceTestConfig {

    @Bean
    public SiteRepository getSiteRepository() {
        List<Site> sites = new ArrayList<>();
        Collections.addAll(sites,
                site("invalidURI", "www.invalidurl.com"),
                site("emptyTarget", "www.emptyTarget.com"),
                site("greenPath", "www.greenpath.com"),
                site("greenPathWithHost", "www.greenpath.com"),
                site("wrongHost", "www.wronghost.com"),
                site("prePopulated", "www.prepop.com"),
                site("morethanonehost", "www.multi.com multi.com"));

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
    public WhitelistedHostRepository getWhitlistedHostsRepository() {
        WhitelistedHostRepository whitelistedHostRepository = Mockito.mock(WhitelistedHostRepository.class);
        return whitelistedHostRepository;
    }

    @Bean
    public ImportService getImportService() {
        return new ImportService();
    }


    static Page page(Site site, String srcUrl, String targetUrl) {
        Page p = new Page();
        p.setSite(site);
        p.setSrcUrl(srcUrl);
        p.setTargetUrl(targetUrl);
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
