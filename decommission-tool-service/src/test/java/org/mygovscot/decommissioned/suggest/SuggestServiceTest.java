package org.mygovscot.decommissioned.suggest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.PageSuggestionRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@ContextConfiguration(classes=SuggestServiceTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SuggestServiceTest {

    @Autowired
    SuggestService sut;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    PageSuggestionRepository pageSuggestionRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    SearchPhraseExtractor searchPhraseExtractor;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    Suggester suggester;

    SuggesterListener listener = Mockito.mock(SuggesterListener.class);

    @Test(expected=IllegalArgumentException.class)
    public void noSuchSite() throws IOException {

        // ARRANGE

        // ACT
        SuggestResults results = sut.updateSuggestions("noSuchSite", listener);

        // ASSERT - see expected exception
    }


    @Test
    public void greenPath() throws IOException {

        // ARRANGE

        // ACT
        SuggestResults results = sut.updateSuggestions("greenPath", listener);

        // ASSERT
        Assert.assertTrue(results.getSuggestionsCount() == 2);
    }

    @Test(expected=IOException.class)
    public void extractorThrowsUp() throws IOException {

        // ARRANGE

        // ACT
        SuggestResults results = sut.updateSuggestions("upchuckingExtractor", listener);

        // ASSERT -- see expected exception
    }

    @Test(expected=IllegalArgumentException.class)
    public void suggesterThrowsUp() throws IOException {

        // ARRANGE

        // ACT
        SuggestResults results = sut.updateSuggestions("upchuckingSuggester", listener);

        // ASSERT -- see expected exception
    }



}
