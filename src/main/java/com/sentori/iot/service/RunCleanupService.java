package com.sentori.iot.service;

import com.sentori.iot.model.run.RunEntity;
import com.sentori.iot.repository.RunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service de nettoyage automatique des runs orphelins
 * Vérifie régulièrement les runs en RUNNING qui ont dépassé leur durée prévue et les marque comme TIMEOUT
 */
@Service
public class RunCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(RunCleanupService.class);

    private final RunRepository runRepository;

    @Value("${app.run.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.run.cleanup.grace-period-minutes:2}")
    private int gracePeriodMinutes;

    public RunCleanupService(RunRepository runRepository) {
        this.runRepository = runRepository;
    }

    /**
     * Job planifié qui s'exécute périodiquement (configurable via app.run.cleanup.check-interval-ms)
     * Cherche les runs en RUNNING qui ont dépassé leur durée prévue + marge de sécurité
     */
    @Scheduled(fixedDelayString = "${app.run.cleanup.check-interval-ms:300000}")
    public void checkAndCleanOrphanedRuns() {
        if (!cleanupEnabled) {
            logger.trace("Nettoyage des runs désactivé");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        logger.debug("Recherche de runs orphelins (dépassement de durée prévue + {} min de marge)...", gracePeriodMinutes);

        // Récupérer tous les runs en RUNNING
        List<RunEntity> orphanedRuns = runRepository.findAll().stream()
                .filter(run -> "RUNNING".equals(run.getStatus()))
                .filter(run -> isRunTimedOut(run, now))
                .toList();

        if (orphanedRuns.isEmpty()) {
            logger.trace("Aucun run orphelin trouvé");
            return;
        }

        logger.warn("⚠️ {} run(s) orphelin(s) détecté(s), marquage comme TIMEOUT", orphanedRuns.size());

        // Marquer chaque run comme TIMEOUT
        for (RunEntity run : orphanedRuns) {
            run.setStatus("TIMEOUT");
            run.setFinishedAt(now);

            int expectedDuration = getExpectedDurationSeconds(run);
            String errorMessage = String.format(
                "Simulation timeout - durée prévue dépassée (attendu: %d secondes + %d min de marge)",
                expectedDuration, gracePeriodMinutes
            );
            run.setErrorMessage(errorMessage);
            runRepository.save(run);

            String runId = run.getParams() != null ? (String) run.getParams().get("runId") : run.getId().toString();
            logger.warn("Run {} marqué comme TIMEOUT (démarré à: {}, durée prévue: {}s)",
                runId, run.getStartedAt(), expectedDuration);
        }

        logger.info("✅ {} run(s) orphelin(s) nettoyé(s)", orphanedRuns.size());
    }

    /**
     * Vérifie si un run a dépassé sa durée prévue + marge de sécurité
     */
    private boolean isRunTimedOut(RunEntity run, OffsetDateTime now) {
        if (run.getStartedAt() == null || run.getParams() == null) {
            return false;
        }

        // Récupérer la durée prévue depuis les params (en secondes)
        int expectedDurationSeconds = getExpectedDurationSeconds(run);
        if (expectedDurationSeconds <= 0) {
            // Si pas de durée dans params, fallback: timeout après 10 minutes
            return run.getStartedAt().plusMinutes(10).isBefore(now);
        }

        // Calculer la date de fin prévue = startedAt + duration + grace period
        OffsetDateTime expectedEndTime = run.getStartedAt()
                .plusSeconds(expectedDurationSeconds)
                .plusMinutes(gracePeriodMinutes);

        return now.isAfter(expectedEndTime);
    }

    /**
     * Extrait la durée prévue (duration) depuis les params du run
     * @return durée en secondes, ou -1 si non trouvé
     */
    private int getExpectedDurationSeconds(RunEntity run) {
        if (run.getParams() == null || !run.getParams().containsKey("duration")) {
            return -1;
        }

        Object durationObj = run.getParams().get("duration");
        if (durationObj instanceof Integer) {
            return (Integer) durationObj;
        } else if (durationObj instanceof Number) {
            return ((Number) durationObj).intValue();
        }

        return -1;
    }
}
