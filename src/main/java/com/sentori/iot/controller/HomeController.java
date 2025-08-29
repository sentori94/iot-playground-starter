package com.sentori.iot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String index() {
        log.debug("UI loaded: index");
        return "index";
    }

    @GetMapping("/simulate")
    public String simulate(Model model) {
        String sensorId = UUID.randomUUID().toString().substring(0,8);
        try {
            log.debug("Simulate called for sensorId={}", sensorId);
            // Ici tu mettras l’appel à ton service de simulation
            log.info("Sensor event ingested id={}", sensorId);
            model.addAttribute("toast", "Événement capteur simulé : " + sensorId);
        } catch (Exception ex) {
            log.error("Simulation failed sensorId={} cause={}", sensorId, ex.toString());
            model.addAttribute("toast", "Échec simulation : " + ex.getMessage());
        }
        return "index";
    }
}
