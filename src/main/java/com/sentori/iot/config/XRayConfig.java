package com.sentori.iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.strategy.sampling.NoSamplingStrategy;

import jakarta.servlet.Filter;

@Configuration
public class XRayConfig {

    @Bean
    public FilterRegistrationBean<Filter> xrayFilter() {
        // Initialiser le recorder X-Ray avec une stratégie simple
        AWSXRay.setGlobalRecorder(
            AWSXRayRecorderBuilder.standard()
                .withSamplingStrategy(new NoSamplingStrategy()) // Tracer toutes les requêtes
                .build()
        );

        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XRayJakartaFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
