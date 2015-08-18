package org.mygovscot.decommissioned.suggest;

import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.PageSuggestion;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.PageSuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;

import java.util.List;

@Component
public class SuggestService {

    private static final Logger LOG = LoggerFactory.getLogger(SuggestService.class);

    @Autowired
    private PageSuggestionRepository pageSuggestionRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SearchPhraseExtractor searchPhraseExtractor;

    @Autowired
    private Suggester suggester;

    public int updateSuggestions(String pageId) throws IOException {
        Page page = pageRepository.findOne(pageId);

        if (page == null) {
            throw new IllegalArgumentException("Page not found: "+pageId);
        }
        String searchPhrase = searchPhraseExtractor.extract(page);
        LOG.debug("phrase: {}", searchPhrase);
        return updateSuggestionsForPage(page, searchPhrase);
    }

    private int updateSuggestionsForPage(Page page, String searchPhrase) throws IOException {

        if (page.isLocked()) {
            LOG.debug("\tpage={} is locked, skipping", page.getSrcUrl());
            return 0;
        }

        int rank = 0;
        List<String> suggestedPages = suggester.suggestions(searchPhrase);
        LOG.debug("\tpage={} searchPhrase={} suggested pages={}", page.getSrcUrl(), searchPhrase, suggestedPages);

        // clear the existing suggestions for this page
        pageSuggestionRepository.delete(pageSuggestionRepository.findByPageId(page.getId()));

        int replacements = 0;
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
                page.setTargetUrl(suggestedPage);
                pageRepository.save(page);
                replacements++;
            }
        }
        return replacements;
    }

}