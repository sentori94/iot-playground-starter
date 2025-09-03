package com.sentori.iot.controller;

import com.sentori.iot.metrics.SensorMetrics;
import com.sentori.iot.model.SensorData;
import com.sentori.iot.service.SensorService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService service;
    private final SensorMetrics metrics;
    private MeterRegistry registry;
    // une variable toute bête dont la valeur sera exposée en Gauge
    private volatile double lastValue = Double.NaN; // garde la dernière valeur reçue

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SensorController.class);

    /*
    private final Counter globalIngest = Counter.builder("sensor_data_ingested_total")
            .description("Nombre total de mesures ingérées")
            .register(registry);

     */

    public SensorController(SensorService service, SensorMetrics metrics, MeterRegistry registry) {
        this.service = service;
        this.metrics = metrics;
        this.registry = registry;

        //this.counter = registry.counter("sensor_data_ingested_total");
    }

    // POST : ingestion de données manuelles
    @PostMapping("/data")
    public void ingest(@RequestBody SensorData sensorData,
                       @RequestHeader(value = "X-User", required = false) String user,
                       @RequestHeader(value = "X-Run-Id", required = false) String runId) {
        sensorData.setTimestamp(LocalDateTime.now());
        service.save(sensorData);

        // Mise à jour de la métrique temps réel (courbe par capteur)
        metrics.setTemperature(sensorData.getSensorId(), sensorData.getReading(), user, runId);

        // 1) Compteur global (facultatif)
        //globalIngest.increment();

        // 2) Compteur tagué (pour Grafana: user/run/sensor)
        registry.counter(
                "sensor_data_ingested_total",
                "user", user,
                "run", runId,
                "sensor_id", sensorData.getSensorId()
        ).increment();
    }

    // GET : récupération de toutes les données capteurs
    @GetMapping("/data")
    public List<SensorData> list() {
        return service.findAll();
    }

    // Getter utilisé par le Gauge
    public double getLastValue() {
        return lastValue;
    }
}
