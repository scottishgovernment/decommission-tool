package org.mygovscot.decommissioned;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan
public class DecommissionToolApp {

    private static final Logger log =
            LoggerFactory.getLogger(DecommissionToolApp.class);

    DecommissionToolApp() {
        // Spring requires that constructor is not private.
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplicationBuilder(DecommissionToolApp.class).application();
            application.addInitializers(new Initialiser());
            application.run(args);
            Dump.main();
        } catch (Throwable t) {
            log.error("Application failed", t);
        }
    }

    @Bean
    ThreadPoolTaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(20);
        taskExecutor.setCorePoolSize(5);
        taskExecutor.initialize();
        return taskExecutor;
    }

}
