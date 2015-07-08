package org.mygovscot.decommissioned.suggest;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@ContextConfiguration(classes=ElasticSearchSuggesterTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ElasticSearchSuggesterTest {

    @Autowired
    private SuggestServiceProperties config;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ElasticSearchSuggester sut;

    @Test
    public void greenpath() {
        List<String> expected = Arrays.asList("/hitone", "/hittwo");
        List<String> actual = sut.suggestions("greenpath");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void wrongJson() {
        List<String> expected = Collections.emptyList();
        List<String> actual = sut.suggestions("wrongJson");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void empty() {
        List<String> expected = Collections.emptyList();
        List<String> actual = sut.suggestions("empty");
        Assert.assertEquals(expected, actual);
    }
}
