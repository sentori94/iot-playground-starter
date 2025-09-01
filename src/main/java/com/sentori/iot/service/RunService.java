package com.sentori.iot.service;

import com.sentori.iot.model.run.RunEntity;
import com.sentori.iot.repository.RunRepository;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RunService {
    private static final Logger log = LoggerFactory.getLogger(RunService.class);
    private final RunRepository repo;
    private final SimulatorService simulator;
    private final RestTemplate restTemplate; // conservé si utilisé ailleurs
    private final String baseUrl = "http://localhost:8080"; // ou @Value si tu veux

    public RunService(RunRepository repo, SimulatorService simulator, RestTemplate restTemplate) {
        this.repo = repo;
        this.simulator = simulator;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public RunEntity tryStart(String sensorId, int count, long intervalMs) {
        Map<String,Object> paramsJson = new HashMap<>();
        paramsJson.put("source", "ui");
        paramsJson.put("sensorId", sensorId);
        paramsJson.put("count", count);
        paramsJson.put("intervalMs", intervalMs);

        log.info("Simulation requested params={}", paramsJson);

        RunEntity r = new RunEntity();
        r.setStatus("RUNNING");
        r.setParams(paramsJson);

        try {
            // ⚠ si ton lock DB s'appuie sur une contrainte unique (ex: une seule ligne RUNNING),
            // c'est ici que le saveAndFlush peut lever DataIntegrityViolationException.
            RunEntity saved = repo.saveAndFlush(r);

            log.info("Run created id={} status=RUNNING", saved.getId());

            simulator.run(sensorId, count, intervalMs)
                    .whenComplete((v, ex) -> finishAsync(saved.getId(), ex));

            return saved;

        } catch (DataIntegrityViolationException e) {
            log.warn("Simulation request rejected (lock active): already RUNNING");
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
            repo.save(run);
        });
    }
}
