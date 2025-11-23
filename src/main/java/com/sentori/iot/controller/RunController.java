package com.sentori.iot.controller;

import com.sentori.iot.model.RunStartRequest;
import com.sentori.iot.model.RunStartResponse;
import com.sentori.iot.model.run.RunEntity;
import com.sentori.iot.repository.RunRepository;
import com.sentori.iot.util.GrafanaUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private static final Logger logger = LoggerFactory.getLogger(RunController.class);
    private static final DateTimeFormatter RUN_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final RunRepository runRepository;
    private final GrafanaUrlBuilder grafanaUrlBuilder;

    public RunController(RunRepository runRepository, GrafanaUrlBuilder grafanaUrlBuilder) {
        this.runRepository = runRepository;
        this.grafanaUrlBuilder = grafanaUrlBuilder;
    }

    /** GET /api/runs?sort=startedAt,desc&page=0&size=20 */
    @GetMapping
    public Page<RunEntity> list(Pageable pageable) {
        return runRepository.findAll(pageable);
    }

    /** GET /api/runs/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<RunEntity> get(@PathVariable("id") UUID id) {
        return runRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public List<RunEntity> all() {
        return runRepository.findAll(Sort.by(Sort.Direction.DESC, "startedAt"));
    }

    /**
     * POST /api/runs/start - Initialise une nouvelle simulation (run)
     * Génère un runId unique, crée l'entité en base et retourne l'URL Grafana
     */
    @PostMapping("/start")
    public RunStartResponse startRun(
            @RequestBody RunStartRequest request,
            @RequestHeader(value = "X-User", required = false, defaultValue = "anonymous") String user) {

        // Génération d'un runId unique basé sur timestamp + user
        String runId = generateRunId(user);

        logger.info("Démarrage de simulation (run) - runId: {}, user: {}, sensors: {}, duration: {}s, interval: {}s",
                runId, user, request.getSensorIds(), request.getDuration(), request.getInterval());

        // Construction de l'URL Grafana avec filtres pré-appliqués
        String grafanaUrl = grafanaUrlBuilder.buildUrl(runId, user);

        // Création de l'entité Run en base de données
        RunEntity runEntity = new RunEntity();
        runEntity.setUsername(user);
        runEntity.setStatus("RUNNING");
        runEntity.setStartedAt(OffsetDateTime.now());
        runEntity.setGrafanaUrl(grafanaUrl);

        // Stockage des paramètres de simulation dans params (JSONB)
        Map<String, Object> params = new HashMap<>();
        params.put("runId", runId);
        params.put("sensorIds", request.getSensorIds());
        params.put("duration", request.getDuration());
        params.put("interval", request.getInterval());
        runEntity.setParams(params);

        runRepository.save(runEntity);

        logger.info("Run créé avec UUID: {} et runId: {}", runEntity.getId(), runId);

        return new RunStartResponse(runId, grafanaUrl);
    }

    /**
     * Génère un runId unique au format: run-{timestamp}-{user}
     */
    private String generateRunId(String user) {
        String timestamp = LocalDateTime.now().format(RUN_ID_FORMATTER);
        String sanitizedUser = user.replaceAll("[^a-zA-Z0-9-]", "").toLowerCase();
        return String.format("run-%s-%s", timestamp, sanitizedUser);
    }

    /**
     * POST /api/runs/interrupt-all - Interrompt tous les runs en statut RUNNING
     * Utile pour nettoyer les simulations qui n'ont pas été terminées correctement
     */
    @PostMapping("/interrupt-all")
    public Map<String, Object> interruptAllRunningRuns() {
        logger.info("Interruption de tous les runs en cours...");

        // Récupérer tous les runs avec le statut RUNNING
        List<RunEntity> runningRuns = runRepository.findAll().stream()
                .filter(run -> "RUNNING".equals(run.getStatus()))
                .toList();

        int count = runningRuns.size();
        logger.info("Nombre de runs en RUNNING trouvés: {}", count);

        // Mettre à jour chaque run
        OffsetDateTime now = OffsetDateTime.now();
        for (RunEntity run : runningRuns) {
            run.setStatus("CANCELED");
            run.setFinishedAt(now);
            run.setErrorMessage("Simulation annulée manuellement");
            runRepository.save(run);
            logger.debug("Run {} annulé (UUID: {})", run.getParams().get("runId"), run.getId());
        }

        logger.info("✅ {} runs annulés avec succès", count);

        // Retourner un résumé
        Map<String, Object> response = new HashMap<>();
        response.put("interrupted", count);
        response.put("timestamp", now.toString());
        response.put("message", count > 0
                ? count + " simulation(s) annulée(s) avec succès"
                : "Aucune simulation en cours");

        return response;
    }

    /**
     * POST /api/runs/{runId}/finish - Termine un run spécifique
     * Body: { "status": "SUCCESS" | "FAILED", "errorMessage": "..." (optionnel si FAILED) }
     */
    @PostMapping("/{runId}/finish")
    public ResponseEntity<RunEntity> finishRun(
            @PathVariable("runId") String runId,
            @RequestBody Map<String, String> finishRequest) {

        logger.info("Demande de finalisation du run: {}", runId);

        // Chercher le run par runId dans les params
        List<RunEntity> matchingRuns = runRepository.findAll().stream()
                .filter(run -> {
                    if (run.getParams() == null) return false;
                    Object paramRunId = run.getParams().get("runId");
                    return runId.equals(paramRunId);
                })
                .toList();

        if (matchingRuns.isEmpty()) {
            logger.warn("Run {} introuvable", runId);
            return ResponseEntity.notFound().build();
        }

        RunEntity run = matchingRuns.get(0);

        // Vérifier que le run est bien en RUNNING
        if (!"RUNNING".equals(run.getStatus())) {
            logger.warn("Le run {} n'est pas en RUNNING (statut actuel: {})", runId, run.getStatus());
            return ResponseEntity.badRequest().build();
        }

        // Récupérer le statut final demandé
        String finalStatus = finishRequest.getOrDefault("status", "SUCCESS");
        if (!finalStatus.equals("SUCCESS") && !finalStatus.equals("FAILED")) {
            logger.warn("Statut invalide: {}. Doit être SUCCESS ou FAILED", finalStatus);
            return ResponseEntity.badRequest().build();
        }

        // Mettre à jour le run
        run.setStatus(finalStatus);
        run.setFinishedAt(OffsetDateTime.now());

        // Si FAILED, ajouter le message d'erreur
        if ("FAILED".equals(finalStatus)) {
            String errorMessage = finishRequest.getOrDefault("errorMessage", "Simulation échouée");
            run.setErrorMessage(errorMessage);
            logger.info("Run {} terminé avec statut FAILED: {}", runId, errorMessage);
        } else {
            logger.info("Run {} terminé avec statut SUCCESS", runId);
        }

        RunEntity savedRun = runRepository.save(run);

        return ResponseEntity.ok(savedRun);
    }
}
