package com.sentori.iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class SimulatorService {
    private static final Logger log = LoggerFactory.getLogger(SimulatorService.class);
    private final Random rnd = new Random();

    @Async
    public CompletableFuture<Void> run(String sensorId, int count, long intervalMs) {
        String sid = (sensorId == null || sensorId.isBlank())
                ? UUID.randomUUID().toString().substring(0,8)
                : sensorId;

        log.info("Simulation started sensorId={} count={} intervalMs={}", sid, count, intervalMs);

        for (int i = 1; i <= count; i++) {
            try {
                double value = 10 + rnd.nextDouble() * 90; // 10..100
                long ts = System.currentTimeMillis();

                // exemples de logs variés
                log.debug("sensor={} step={} value={} ts={}", sid, i, String.format("%.2f", value), ts);
                if (value > 95) {
                    log.warn("sensor={} high_value={} step={}", sid, String.format("%.2f", value), i);
                }
                // simuler une erreur rare
                if (rnd.nextDouble() < 0.02) {
                    throw new RuntimeException("Random failure at step " + i);
                }

                Thread.sleep(intervalMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Simulation interrupted sensor={} step={}", sid, i);
                break;
            } catch (Exception ex) {
                log.error("Simulation error sensor={} step={} cause={}", sid, i, ex.toString());
                // on continue la boucle malgré l’erreur
            }
        }

        log.info("Simulation finished sensorId={}", sid);
        return CompletableFuture.completedFuture(null);
    }
}
