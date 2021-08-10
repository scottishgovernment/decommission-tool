package org.mygovscot.decommissioned;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

public class Dump {

    private static Logger log = LoggerFactory.getLogger(Dump.class);

    public static void main() {
        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext();
        Initialiser config = new Initialiser();
        config.initialize(ctx);
        environment(ctx.getEnvironment());
    }

    public static void environment(ConfigurableEnvironment env) {
        StringBuilder builder = new StringBuilder();
        MutablePropertySources sources = env.getPropertySources();
        for (PropertySource src : sources) {
            if ("application".equals(src.getName()) && src instanceof MapPropertySource) {
                String[] names = ((MapPropertySource) src).getPropertyNames();
                for (String key : names) {
                    builder.append(String.format("  %s_%s=%s\n",
                            env.getProperty("application.name"),
                            StringUtils.replace(key, ".","_"),
                            env.getProperty(key)));
                }
            }
        }
        log.info("Configuration:\n{}", builder);
    }

}
