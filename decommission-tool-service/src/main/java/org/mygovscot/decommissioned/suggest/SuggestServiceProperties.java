package org.mygovscot.decommissioned.suggest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;


@Service
@ConfigurationProperties(prefix = "suggester")
public class SuggestServiceProperties {

    private String searchUrl;

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

}
