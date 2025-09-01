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
            @RequestParam(name = "sensorId", required = false) String sensorId,
            @RequestParam(name = "count", defaultValue = "10") int count,
            @RequestParam(name = "intervalMs", defaultValue = "200") long intervalMs,
            Model model
    ) {
        log.info("Simulation requested sensorId={} count={} intervalMs={}", sensorId, count, intervalMs);

        var run = runService.tryStart(sensorId, count, intervalMs); // <-- on passe les inputs
        if (run == null) {
            log.warn("Simulation request rejected (already RUNNING)");
            model.addAttribute("toast", "Un lancement est déjà en cours. Réessaie dans un instant.");
        } else {
            log.info("Simulation started runId={} sensorId={} count={} intervalMs={}",
                    run.getId(), sensorId, count, intervalMs);
            model.addAttribute("toast", "Simulation lancée (run=" + run.getId() + ")");
        }
        return index(model);
    }


}
