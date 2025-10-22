package com.sentori.iot.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrafanaConfig {

    private static final Logger log = LoggerFactory.getLogger(GrafanaConfig.class);

    @Value("${app.grafana.base-url}")
    private String grafanaBaseUrl;

    @PostConstruct
    public void logGrafanaUrl() {
        log.info("✅ Grafana Base URL: {}", grafanaBaseUrl);
    }
}
