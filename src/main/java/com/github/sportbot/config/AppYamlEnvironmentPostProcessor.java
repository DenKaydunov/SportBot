package com.github.sportbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads environment variables from app.yaml for local development.
 * In production (GCP), these are automatically provided by App Engine.
 */
public class AppYamlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AppYamlEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Only load app.yaml in local development (not in GCP)
        if (isRunningInGCP()) {
            return;
        }

        Path appYamlPath = Paths.get("app.yaml");
        if (!Files.exists(appYamlPath)) {
            log.debug("app.yaml not found, skipping environment variable loading");
            return;
        }

        try (InputStream input = new FileInputStream(appYamlPath.toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> appYaml = yaml.load(input);

            if (appYaml.containsKey("env_variables")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> envVars = (Map<String, Object>) appYaml.get("env_variables");

                Map<String, Object> propertySource = new HashMap<>();
                envVars.forEach((key, value) -> {
                    String envValue = String.valueOf(value);
                    propertySource.put(key, envValue);
                    // Also set as system property for compatibility
                    System.setProperty(key, envValue);
                });

                environment.getPropertySources()
                    .addFirst(new MapPropertySource("appYamlProperties", propertySource));

                log.info("Loaded {} environment variables from app.yaml", envVars.size());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load app.yaml", e);
        }
    }

    private boolean isRunningInGCP() {
        // GCP App Engine sets GAE_ENV environment variable
        return System.getenv("GAE_ENV") != null;
    }
}
