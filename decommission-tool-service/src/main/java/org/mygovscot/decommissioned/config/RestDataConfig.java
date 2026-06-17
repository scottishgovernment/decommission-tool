package org.mygovscot.decommissioned.config;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.model.WhitelistedHost;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
@Import(RepositoryRestMvcConfiguration.class)
public class RestDataConfig implements RepositoryRestConfigurer {

    @Inject
    private Validator validator;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(Page.class);
        config.exposeIdsFor(Site.class);
        config.exposeIdsFor(WhitelistedHost.class);
        config.setBasePath("redirects");
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        SpringValidatorAdapter adapter = new SpringValidatorAdapter(validator);
        validatingListener.addValidator("beforeCreate", adapter);
        validatingListener.addValidator("beforeSave", adapter);
    }

}
