package com.sentori.iot.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SensorMetrics {

    private final MeterRegistry registry;
    private final ConcurrentMap<String, Holder> perSensor = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Holder> perSeries = new ConcurrentHashMap<>();

    public SensorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /** Maj de la température en taguant sensor_id + user + run */
    public void setTemperature(String sensorId, double value, String user, String runId) {
        String u = (user == null || user.isBlank()) ? "anonymous" : user;
        String r = (runId == null || runId.isBlank()) ? "n/a" : runId;
        String key = sensorId + "|" + u + "|" + r; // <- clé par combinaison

        perSeries.compute(key, (k, holder) -> {
            if (holder == null) {
                Holder h = new Holder(value);
                Gauge.builder("sensor_data_value", h, Holder::get)
                        .description("Dernière valeur de température par capteur")
                        .tags(
                                "sensor_id", sensorId,
                                "type",      "temperature",
                                "user",      u,
                                "run",       r
                        )
                        .register(registry);
                return h;
            } else {
                holder.set(value);
                return holder;
            }
        });
    }

    /** Petit holder thread‑safe (lecture/écriture volatiles) */
    private static final class Holder {
        private volatile double v;
        Holder(double initial) { this.v = initial; }
        double get() { return v; }
        void set(double nv) { this.v = nv; }
    }
}
