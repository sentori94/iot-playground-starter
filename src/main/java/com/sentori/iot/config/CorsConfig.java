package com.sentori.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser les credentials (cookies, headers d'authentification)
        config.setAllowCredentials(true);

        // Origines autorisées (Angular en local)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));

        // Headers autorisés
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-User",
                "X-Run-Id"
        ));

        // Méthodes HTTP autorisées
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // Headers exposés (visibles côté client)
        config.setExposedHeaders(Arrays.asList(
                "X-Run-Id",
                "Content-Disposition"
        ));

        // Durée du cache de la configuration CORS (1 heure)
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

