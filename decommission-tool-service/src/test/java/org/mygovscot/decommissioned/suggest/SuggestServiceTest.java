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

    @Test(expected=IllegalArgumentException.class)
    public void noSuchPage() throws IOException {

        // ARRANGE

        // ACT
        sut.updateSuggestions("noSuchPage");

        // ASSERT - see expected exception
    }

    @Test
    public void greenPath() throws IOException {

        // ARRANGE

        // ACT
        sut.updateSuggestions("greenPath");

        // ASSERT
        // assert expected call made to add page suggestions
    }

    @Test(expected=IOException.class)
    public void extractorThrowsUp() throws IOException {

        // ARRANGE

        // ACT
        sut.updateSuggestions("/upchuckingExtractor");

        // ASSERT -- see expected exception
    }

    @Test(expected=IllegalArgumentException.class)
    public void suggesterThrowsUp() throws IOException {

        // ARRANGE

        // ACT
        sut.updateSuggestions("/upchuckingSuggester");

        // ASSERT -- see expected exception
    }



}
