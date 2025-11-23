package com.sentori.iot.service;

import com.sentori.iot.model.run.RunEntity;
import com.sentori.iot.repository.RunRepository;
import com.sentori.iot.util.GrafanaUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RunService {
    private static final Logger log = LoggerFactory.getLogger(RunService.class);
    private final RunRepository repo;
    private final SimulatorService simulator;
    private final GrafanaUrlBuilder grafanaUrlBuilder;
    private final String baseUrl = "http://localhost:8080"; // ou @Value si tu veux

    public RunService(RunRepository repo, SimulatorService simulator, GrafanaUrlBuilder grafanaUrlBuilder) {
        this.repo = repo;
        this.simulator = simulator;
        this.grafanaUrlBuilder = grafanaUrlBuilder;
    }

    @Transactional
    public RunEntity tryStartMany(String username, List<String> sensorIds, int count, long intervalMs) {
        Map<String,Object> paramsJson = new HashMap<>();
        paramsJson.put("source", "ui");
        paramsJson.put("sensorIds", sensorIds);
        paramsJson.put("count", count);
        paramsJson.put("intervalMs", intervalMs);

        log.info("Simulation requested params={}", paramsJson);

        RunEntity r = new RunEntity();
        r.setStatus("RUNNING");
        r.setParams(paramsJson);
        r.setStartedAt(OffsetDateTime.now());
        r.setUsername(username);

        try {
            RunEntity saved = repo.saveAndFlush(r);
            log.info("Run created id={} status=RUNNING ({} sensors)", saved.getId(), sensorIds.size());

            simulator.runManyRoundRobin(sensorIds, count, intervalMs, r) // <-- voir ci-dessous
                    .whenComplete((v, ex) -> finishAsync(saved.getId(), ex));

            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("Run rejected: another RUNNING exists (DB lock).");
            return null;
        }
    }

    private void finishAsync(UUID id, Throwable ex) {
        repo.findById(id).ifPresent(run -> {
            run.setFinishedAt(OffsetDateTime.now());
            if (ex == null) {
                run.setStatus("SUCCESS");
                log.info("Run {} finished SUCCESS", id);
            } else {
                run.setStatus("FAILED");
                run.setErrorMessage(ex.toString());
                log.error("Run {} FAILED: {}", id, ex.toString());
            }

            // Utilisation de GrafanaUrlBuilder
            String grafanaUrl = grafanaUrlBuilder.buildUrl(id.toString(), run.getUsername());
            run.setGrafanaUrl(grafanaUrl);
            log.debug("Grafana URL: " + grafanaUrl);

            repo.save(run);
        });
    }
}
