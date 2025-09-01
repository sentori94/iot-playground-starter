package com.sentori.iot.service;

import com.sentori.iot.model.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulatorService {

    private static final Logger log = LoggerFactory.getLogger(SimulatorService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String ingestPath; // ex: /api/sensors/ingest

    public SimulatorService(
            RestTemplate restTemplate,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.sensor.ingest-path:/sensors/data}") String ingestPath
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.ingestPath = ingestPath;
    }

    /** Lance l'envoi de 'count' mesures, espacées de intervalMs, pour sensorId. */
    public CompletableFuture<Void> run(String sensorId, int count, long intervalMs) {
        return CompletableFuture.runAsync(() -> {
            log.info("Simulator start sensorId={} count={} intervalMs={}", sensorId, count, intervalMs);

            for (int i = 0; i < count; i++) {
                // ⚠ Adapte ces champs à ta structure réelle de SensorEntity et à ce que ton Controller attend.
                SensorData payload = new SensorData();
                payload.setSensorId(sensorId);                 // si ton entity a ce champ
                payload.setType("temperature");
                payload.setReading(randomDouble(0, 35, 2));               // idem
                payload.setTimestamp(LocalDateTime.now());    // ou Instant / LocalDateTime selon ton modèle

                restTemplate.postForEntity(baseUrl + ingestPath, payload, Void.class);
                log.debug("Posted {}/{} to {}{}", i + 1, count, baseUrl, ingestPath);

                if (intervalMs > 0) {
                    try {
                        Thread.sleep(intervalMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Simulation interrupted", e);
                    }
                }
            }

            log.info("Simulator finished sensorId={} ({} events)", sensorId, count);
        });
    }

    private double randomDouble(double min, double max, int decimals) {
        double raw = ThreadLocalRandom.current().nextDouble(min, max);
        double scale = Math.pow(10, decimals);
        return Math.round(raw * scale) / scale;
    }
}

