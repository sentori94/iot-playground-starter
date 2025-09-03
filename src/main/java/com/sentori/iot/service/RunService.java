package com.sentori.iot.service;

import com.sentori.iot.model.run.RunEntity;
import com.sentori.iot.repository.RunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final String baseUrl = "http://localhost:8080"; // ou @Value si tu veux


    private @Value("${app.grafana.base-url}")        String gfBase;
    private @Value("${app.grafana.dashboard-path}")  String gfPath;
    private @Value("${app.grafana.org-id}")          String gfOrgId;
    private @Value("${app.grafana.ds-prom-uid}")     String gfDsPromUid;
    private @Value("${app.grafana.timezone}")        String gfTimezone;
    private @Value("${app.grafana.default-from}")    String gfDefaultFrom;   // ex: now-15m
    private @Value("${app.grafana.default-to}")      String gfDefaultTo;     // ex: now
    private @Value("${app.grafana.default-refresh}") String gfRefresh;       // ex: 5s

    public RunService(RunRepository repo, SimulatorService simulator) {
        this.repo = repo;
        this.simulator = simulator;
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
            run.setGrafanaUrl(buildGrafanaUrlRelative(run));
            log.debug("Grafana URL: " + run.getGrafanaUrl());
            repo.save(run);
        });
    }

    private String buildGrafanaUrlRelative(RunEntity run) {
        // username prioritaire : colonne, sinon params.user
        String user = run.getUsername();
        if ((user == null || user.isBlank()) && run.getParams() != null) {
            Object u = run.getParams().get("user");
            if (u != null) user = String.valueOf(u);
        }
        if (user == null || user.isBlank()) user = "anonymous";

        return UriComponentsBuilder.fromHttpUrl(gfBase)
                .path(gfPath)
                .queryParam("orgId", gfOrgId)
                .queryParam("from", gfDefaultFrom)
                .queryParam("to", gfDefaultTo)
                .queryParam("timezone", gfTimezone)
                .queryParam("var-DS_PROM", gfDsPromUid)
                .queryParam("var-user", user)
                .queryParam("var-run", run.getId().toString())
                .queryParam("var-sensor", "$__all")   // identique à ton lien
                .queryParam("refresh", gfRefresh)
                .build(true) // garde les $ et autres intactes
                .toUriString();
    }
}
