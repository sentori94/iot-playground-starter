package com.sentori.iot.controller;

import com.sentori.iot.metrics.SensorMetrics;
import com.sentori.iot.model.SensorData;
import com.sentori.iot.service.SensorService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService service;
    private final SensorMetrics metrics;
    private final Counter counter;
    private final Random random = new Random();
    // une variable toute bête dont la valeur sera exposée en Gauge
    private volatile double lastValue = Double.NaN; // garde la dernière valeur reçue

    public SensorController(SensorService service, SensorMetrics metrics, MeterRegistry registry) {
        this.service = service;
        this.metrics = metrics;
        this.counter = registry.counter("sensor_data_ingested_total");

        // --- Simulateur optionnel (si tu veux garder la génération auto) ---
        Thread simulator = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    SensorData simulated = new SensorData(
                            "sensor-" + random.nextInt(3),
                            "temperature",
                            20 + random.nextDouble() * 10,
                            LocalDateTime.now()
                    );
                    service.save(simulated);
                    metrics.setTemperature(simulated.getSensorId(), simulated.getReading());
                    counter.increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        simulator.setDaemon(true);
        simulator.start();
    }

    // POST : ingestion de données manuelles
    @PostMapping("/data")
    public void ingest(@RequestBody SensorData sensorData) {
        sensorData.setTimestamp(LocalDateTime.now());
        service.save(sensorData);

        // Mise à jour de la métrique temps réel (courbe par capteur)
        metrics.setTemperature(sensorData.getSensorId(), sensorData.getReading());
        counter.increment();
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
