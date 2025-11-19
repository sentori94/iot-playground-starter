package com.sentori.iot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Value("${app.reports.api-gateway-url}")
    private String apiGatewayUrl;

    @Value("${app.reports.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ReportController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadReports() {
        log.info("Download reports requested from API Gateway: {}", apiGatewayUrl);

        try {
            // Appel vers l'API Gateway avec l'API Key
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("x-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiGatewayUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Décoder le Base64 pour obtenir les bytes du fichier ZIP
                byte[] zipBytes = Base64.getDecoder().decode(response.getBody());
                log.info("Reports downloaded and decoded successfully, size: {} bytes", zipBytes.length);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(zipBytes);
                InputStreamResource resource = new InputStreamResource(inputStream);

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                responseHeaders.setContentDisposition(
                        ContentDisposition.builder("attachment")
                                .filename("simulation-reports.zip")
                                .build()
                );
                responseHeaders.setContentLength(zipBytes.length);

                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .body(resource);
            } else {
                log.error("Failed to download reports, status: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).build();
            }

        } catch (Exception e) {
            log.error("Error downloading reports from API Gateway", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
