package com.sentori.iot.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Charge le fichier .env au démarrage de l'application UNIQUEMENT en profil 'local'
 * et injecte les variables dans l'environnement Spring
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // Charge le .env UNIQUEMENT si le profil 'local' est actif
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isLocalProfile = false;
        for (String profile : activeProfiles) {
            if ("local".equals(profile)) {
                isLocalProfile = true;
                break;
            }
        }

        if (!isLocalProfile) {
            System.out.println("Profile 'local' not active, skipping .env loading");
            return;
        }

        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // Ne plante pas si le .env n'existe pas
                    .load();

            Map<String, Object> dotenvProperties = new HashMap<>();

            dotenv.entries().forEach(entry -> {
                dotenvProperties.put(entry.getKey(), entry.getValue());
                System.out.println("Loaded from .env: " + entry.getKey());
            });

            environment.getPropertySources()
                    .addFirst(new MapPropertySource("dotenvProperties", dotenvProperties));

            System.out.println("✓ .env file loaded successfully for 'local' profile");

        } catch (Exception e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }
}
