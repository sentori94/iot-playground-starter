package com.sentori.iot.controller;

import com.sentori.iot.repository.RunRepository;
import com.sentori.iot.service.RunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final RunService runService;
    private final RunRepository runRepo;

    public HomeController(RunService runService, RunRepository runRepo) {
        this.runService = runService;
        this.runRepo = runRepo;
    }

    @GetMapping("/")
    public String index(Model model) {
        boolean running = runRepo.existsRunning();
        model.addAttribute("running", running);
        model.addAttribute("runs", runRepo.findTop10ByOrderByStartedAtDesc());
        return "index";
    }

    @PostMapping("/simulate")
    public String simulate(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "sensorId", required = false) String sensorId, // (conserve si tu veux aussi un seul id)
            @RequestParam(name = "sensorsCount", required = false, defaultValue = "1") int sensorsCount,
            @RequestParam(name = "count", defaultValue = "10") int count,
            @RequestParam(name = "intervalMs", defaultValue = "200") long intervalMs,
            Model model
    ) {
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,20}")) {
            model.addAttribute("toast", "Username invalide (3–20, lettres/chiffres . _ -)");
            return index(model);
        }
        // clamp 1..20
        sensorsCount = Math.max(1, Math.min(20, sensorsCount));

        // Génère les IDs (SENSOR-001 .. SENSOR-00N) si pas de sensorId explicite
        List<String> sensorIds;
        if (sensorId != null && !sensorId.isBlank()) {
            sensorIds = List.of(sensorId.trim());
        } else {
            sensorIds = java.util.stream.IntStream.rangeClosed(1, sensorsCount)
                    .mapToObj(i -> String.format("SENSOR-%03d", i))
                    .toList();
        }

        log.info("Simulation requested sensorIds={} count={} intervalMs={}", sensorIds, count, intervalMs);

        var run = runService.tryStartMany(username, sensorIds, count, intervalMs); // <-- nouvelle méthode
        if (run == null) {
            log.warn("Simulation request rejected (already RUNNING)");
            model.addAttribute("toast", run == null
                    ? "Un lancement est déjà en cours. Réessaie dans un instant."
                    : "Simulation lancée (run=" + run.getId() + ", user=" + username + ")");
        } else {
            log.info("Simulation started runId={} sensors={} count={} intervalMs={}", run.getId(), sensorIds.size(), count, intervalMs);
            model.addAttribute("toast", "Simulation lancée (run=" + run.getId() + ", capteurs=" + sensorIds.size() + ")");
            model.addAttribute("runId", run.getId().toString());
        }
        return index(model);
    }
}
