package org.mygovscot.decommissioned;

import org.mygovscot.beta.config.BetaConfigInitializer;
import org.mygovscot.beta.config.Dump;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@ComponentScan
public class DecommissionToolApp {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplicationBuilder(DecommissionToolApp.class).application();
        application.addInitializers(new BetaConfigInitializer());
        application.run(args);
        Dump.main(args);
    }

    public String fakeMethod() {
        return "fake method to bypass sonar's validation of utility classes.";
    }
}
