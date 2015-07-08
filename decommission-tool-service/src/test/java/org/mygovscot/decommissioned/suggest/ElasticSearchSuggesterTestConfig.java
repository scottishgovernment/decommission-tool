package org.mygovscot.decommissioned.suggest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElasticSearchSuggesterTestConfig {

    @Bean
    public RestTemplate getRestTemplate() throws Exception {
        RestTemplate rt =  Mockito.mock(RestTemplate.class);

        Mockito.when(rt.getForObject("greenpath", JsonNode.class)).thenReturn(jsonNodeWithHits("/hitone", "/hittwo"));
        Mockito.when(rt.getForObject("empty", JsonNode.class)).thenReturn(jsonNode("{}"));
        Mockito.when(rt.getForObject("wrongJson", JsonNode.class)).thenReturn(jsonNode(hitsString("/hitinwrongplace")));

        return rt;
    }

    @Bean
    public ElasticSearchSuggester getElasticSearchSuggester() {
        return new ElasticSearchSuggester();
    }

    @Bean
    public SuggestServiceProperties getConfig() {
        SuggestServiceProperties p = new SuggestServiceProperties();
        p.setSearchUrl("");
        return p;
    }

    private JsonNode jsonNodeWithHits(String ...hits) throws Exception {
        ObjectMapper m = new ObjectMapper();

        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append("   \"hits\": {");
        b.append("      \"hits\": [");
        b.append(hitsString(hits));
        b.append("      ]");
        b.append("   }");
        b.append("}");

        System.out.println(b.toString());
        return m.readTree(b.toString());
    }

    private String hitsString(String ...hits) {
        return Stream.of(hits)
                .map(t -> "{\"_source\": { \"url\" : \"" + t + "\"} }")
                .collect(Collectors.joining(","));
    }

    private JsonNode jsonNode(String src) throws Exception {
        ObjectMapper m = new ObjectMapper();
        return m.readTree(src);
    }
}
