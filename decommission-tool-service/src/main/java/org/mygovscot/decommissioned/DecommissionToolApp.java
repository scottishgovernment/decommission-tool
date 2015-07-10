package org.mygovscot.decommissioned;

import org.mygovscot.beta.config.BetaConfigInitializer;
import org.mygovscot.beta.config.Dump;
import org.mygovscot.util.error.toplevelhandlers.SLF4JStrictTopLevelErrorHandler;
import org.mygovscot.util.error.toplevelhandlers.TopLevelErrorHandler;
import org.mygovscot.util.servlet.filter.ErrorHandlerFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.servlet.Filter;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@ComponentScan
public class DecommissionToolApp {
    /** For logging all unhandled exceptions. */
    public static final TopLevelErrorHandler TOP_LEVEL_ERROR_HANDLER = new SLF4JStrictTopLevelErrorHandler();

    DecommissionToolApp() {
        // Spring requires that constructor is not private.
    }

    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplicationBuilder(DecommissionToolApp.class).application();
            application.addInitializers(new BetaConfigInitializer());
            application.run(args);
            Dump.main(args);
        } catch (Throwable t) {
            TOP_LEVEL_ERROR_HANDLER.handleThrowable(t);
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

    @Bean
    public Filter errorHandlerFilter() {
        return new ErrorHandlerFilter();
    }
}
