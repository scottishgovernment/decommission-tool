package org.mygovscot.decommissioned;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Initialiser implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String CONFIG_FILE = "application.yml";
    public static final String PROP_APP_NAME = "application.name";

    private Environment internalEnv;
    private String prefix;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        List<PropertySource<?>> applicationSource = getApplicationPropertySource();
        prefix = String.format("%s_", (String) applicationSource.get(0).getProperty(PROP_APP_NAME));
        PropertySource environmentSource = getEnvironmentPropertySource();
        ConfigurableEnvironment environment;
        if (applicationContext != null) {
            environment = applicationContext.getEnvironment();
        } else {
            environment = new StandardEnvironment();
        }
        environment.getPropertySources().addFirst(environmentSource);
        for (PropertySource<?> src : applicationSource) {
            environment.getPropertySources().addLast(src);
        }
        internalEnv = environment;
    }

    public  Environment getEnvironment() {
        return internalEnv;
    }

    private String getConfigFile() {
        return  System.getProperty("betaconfig.file", CONFIG_FILE);
    }

    protected final PropertySource<Map<String, Object>> getEnvironmentPropertySource() {
        Map<String, Object> envMap = new HashMap<>();
        for (Map.Entry<String, String> envEntry : System.getenv().entrySet()) {
            String key = envEntry.getKey();
            if (key.startsWith(prefix)) {
                String refinedKey = key.replaceFirst(prefix, "");
                envMap.put(refinedKey, envEntry.getValue());
            }
        }
        return new SystemEnvironmentPropertySource("environment", envMap);
    }

    protected List<PropertySource<?>> getApplicationPropertySource() {
        try {
            Resource resource = new ClassPathResource(getConfigFile());
            // We don't have concept of profiles yet, that is why profile passed as null
            List<PropertySource<?>> source = new YamlPropertySourceLoader().load("application", resource);
            if (null == source) {
                throw new RuntimeException(
                        String.format("Failed to load %s for default profile", getConfigFile()));
            }
            return source;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to load application configuration, " +
                    "make sure %s exists in classpath root", getConfigFile()), e);
        }
    }

}
