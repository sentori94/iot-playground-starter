package com.sentori.iot.controller;

import com.sentori.iot.service.SimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final SimulatorService simulator;

    @GetMapping("/")
    public String index() {
        log.debug("UI loaded: index");
        return "index";
    }

    public HomeController(SimulatorService simulator) {
        this.simulator = simulator;
    }

    @PostMapping("/simulate")
    public String simulate(
            @RequestParam(name = "sensorId", required = false) String sensorId,
            @RequestParam(name = "count", defaultValue = "10") int count,
            @RequestParam(name = "intervalMs", defaultValue = "200") long intervalMs,
            Model model) {
        simulator.run(sensorId, count, intervalMs); // asynchrone
        model.addAttribute("toast", "Simulation lancée" + (sensorId != null && !sensorId.isBlank() ? " pour " + sensorId : "") + " !");
        return "index";
    }
}
