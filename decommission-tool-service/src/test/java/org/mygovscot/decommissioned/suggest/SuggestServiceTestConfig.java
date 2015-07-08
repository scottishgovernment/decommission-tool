package org.mygovscot.decommissioned.suggest;

import org.mockito.Mockito;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.PageSuggestionRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SuggestServiceTestConfig {

    @Bean
    public PageRepository getPageRepository() {
        PageRepository pageRepository = Mockito.mock(PageRepository.class);
        return pageRepository;
    }

    @Bean
    public PageSuggestionRepository getPageSuggestionRepository() {
        return Mockito.mock(PageSuggestionRepository.class);
    }

    @Bean
    public SuggestService getSuggestService() {
        return new SuggestService();
    }

    @Bean
    public SearchPhraseExtractor searchPhraseExtractor() throws IOException {
        SearchPhraseExtractor mock = Mockito.mock(SearchPhraseExtractor.class);
        Page greenPathPage = new Page();
        Mockito.when(mock.extract(Mockito.eq(page("/page1")))).thenReturn("searchPhrase");
        Mockito.when(mock.extract(Mockito.eq(page("/page2")))).thenReturn("searchPhrase");
        Mockito.when(mock.extract(Mockito.eq(page("/upchuckingSuggester")))).thenReturn("upchuckingSuggester");
        Mockito.when(mock.extract(Mockito.eq(page("/upchuckingExtractor")))).thenThrow(new IOException("dummy"));
        return mock;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate mock = Mockito.mock(RestTemplate.class);
        return mock;
    }

    @Bean
    public Suggester getSuggester() {
        Suggester s = Mockito.mock(Suggester.class);
        Mockito.when(s.suggestions("searchPhrase"))
                .thenReturn(Arrays.asList("/page1Suggestion", "/page2Suggestion"));
        Mockito.when(s.suggestions("upchuckingSuggester"))
                .thenThrow(new IllegalArgumentException("dummy"));

        return s;
    }

    @Bean
    public SiteRepository getSiteRepository() {
        SiteRepository siteRepository = Mockito.mock(SiteRepository.class);
        Mockito.when(siteRepository.findOne(Mockito.any(String.class)))
                .thenReturn(site("greenPath", "/page1", "/page2"));
        Mockito.when(siteRepository.findOne("upchuckingExtractor"))
                .thenReturn(site("upchuckingExtractor", "/upchuckingExtractor"));
        Mockito.when(siteRepository.findOne("upchuckingSuggester"))
                .thenReturn(site("upchuckingSuggester", "/upchuckingSuggester"));
        Mockito.when(siteRepository.findOne("noSuchSite"))
                .thenReturn(null);
        return siteRepository;
    }

    private Site site(String id, String ... srcUrls) {
        Site site = new Site();
        site.setPages(Stream.of(srcUrls).map(t -> page(t)).collect(Collectors.toList()));
        site.setId(id);
        return site;
    }

    private Page page(String srcUrl) {
        Page page = new Page();
        page.setSrcUrl(srcUrl);
        page.setTargetUrl("/");
        return page;
    }
}
