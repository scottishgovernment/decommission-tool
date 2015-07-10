package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.PageSuggestion;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.PageSuggestionRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.List;

@Component
public class SuggestService {

    private static final Logger LOG = LoggerFactory.getLogger(SuggestService.class);

    @Autowired
    private PageSuggestionRepository pageSuggestionRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SearchPhraseExtractor searchPhraseExtractor;

    @Autowired
    private Suggester suggester;

    //@Transactional
    public SuggestResults updateSuggestions(String siteId, SuggesterListener listener) throws IOException {

        Site site = siteRepository.findOne(siteId);
        if (site == null) {
            throw new IllegalArgumentException("No such site:"+siteId);
        }

        LOG.debug("updateSuggestions {} {}", siteId, site.getHost());
        int replacementCount = 0;

        for (Page page : site.getPages()) {
            int rank = 0;

            listener.processingPage(page);

            String searchPhrase = searchPhraseExtractor.extract(page);
            List<String> suggestedPages = suggester.suggestions(searchPhrase);
            LOG.debug("\tpage={} searchPhrase={} suggested pages={}", page.getSrcUrl(), searchPhrase, suggestedPages);

            // clear the existing suggestions for this page
            List<PageSuggestion> suggestionsForPage = pageSuggestionRepository.findByPageId(page.getId());
            pageSuggestionRepository.delete(suggestionsForPage);

            // now save all of the new suggestions that we generated
            for (String suggestedPage : suggestedPages) {
                PageSuggestion suggestion = new PageSuggestion();
                suggestion.setPage(page);
                suggestion.setRank(rank++);
                suggestion.setUrl(suggestedPage);
                pageSuggestionRepository.save(suggestion);

                // if this page currently has / as its target and this is the first result then update ...
                if ("/".equals(page.getTargetUrl())) {
                    LOG.debug("\tsetting target url to {}", suggestedPage);
                    replacementCount++;
                    page.setTargetUrl(suggestedPage);
                    pageRepository.save(page);
                }
            }
        }
        LOG.debug("done");
        listener.end();

        return new SuggestResults(replacementCount);
    }


}