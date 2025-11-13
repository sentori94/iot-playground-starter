package com.sentori.iot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.emitters.Emitter;
import com.amazonaws.xray.emitters.UDPEmitter;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;

import jakarta.servlet.Filter;

@Configuration
public class XRayConfig {

    // Récupère l’adresse depuis Spring (application.yml) ou ENV; valeur par défaut 127.0.0.1:2000
    @Value("${com.amazonaws.xray.emitters.daemon-address:${AWS_XRAY_DAEMON_ADDRESS:127.0.0.1:2000}}")
    private String xrayDaemonAddress;

    @Bean
    public FilterRegistrationBean<Filter> xrayFilter() {
        try {
            // Emitter explicite avec l’adresse choisie
            Emitter emitter = new UDPEmitter(xrayDaemonAddress);

            AWSXRay.setGlobalRecorder(
                AWSXRayRecorderBuilder.standard()
                    .withEmitter(emitter)
                    .withSamplingStrategy(new LocalizedSamplingStrategy())
                    .build()
            );

            System.out.println("INFO: AWS X-Ray daemon address set to: " + xrayDaemonAddress);
        } catch (Exception e) {
            System.err.println("WARN: X-Ray initialization failed (daemon=" + xrayDaemonAddress + "): " + e.getMessage());
        }

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XRayJakartaFilter());
        // Filtrer toutes les URLs (/*), exclusion de /actuator/* gérée par le filtre lui-même
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
