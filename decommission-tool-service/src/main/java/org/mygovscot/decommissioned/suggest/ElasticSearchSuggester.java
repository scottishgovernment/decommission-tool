package org.mygovscot.decommissioned.suggest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class ElasticSearchSuggester implements Suggester {

    @Autowired
    private SuggestServiceProperties config;

    @Autowired
    private RestTemplate restTemplate;

    public List<String> suggestions(String searchPhrase) {
        List<String> suggestions = new ArrayList<>();

        // search for this title
        String url = config.getSearchUrl() + searchPhrase;
        JsonNode node = restTemplate.getForObject(url, JsonNode.class);
        ArrayNode hits = getHits(node);

        if (hits != null && hits.size() > 0) {
            for (JsonNode hit : hits) {
                suggestions.add(hit.get("_source").get("url").asText());
            }
        }

        return suggestions;
    }

    // get the hits array from this node ensuring there is no NPE
    private ArrayNode getHits(JsonNode node) {

        String [] path = {"hits", "hits"};
        JsonNode curr = node;
        for (String pathElement : path) {
            curr = curr.get(pathElement);
            if (curr == null) {
                return null;
            }
        }

        if (curr instanceof ArrayNode) {
            return (ArrayNode) curr;
        }
        return null;
    }

}
