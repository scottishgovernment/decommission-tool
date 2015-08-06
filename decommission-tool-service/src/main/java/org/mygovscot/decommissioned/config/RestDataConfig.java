package org.mygovscot.decommissioned.config;

import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.PageSuggestion;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.model.WhitelistedHost;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@Import(RepositoryRestMvcConfiguration.class)
public class RestDataConfig extends RepositoryRestMvcConfiguration {

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        super.configureRepositoryRestConfiguration(config);
        config.exposeIdsFor(Page.class);
        config.exposeIdsFor(Site.class);
        config.exposeIdsFor(WhitelistedHost.class);
        config.exposeIdsFor(PageSuggestion.class);
        config.setBaseUri("redirects");
    }
}