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

    public SensorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /** MAJ (ou création) du Gauge pour un capteur donné. */
    public void setTemperature(String sensorId, double value) {
        perSensor.compute(sensorId, (id, holder) -> {
            if (holder == null) {
                Holder h = new Holder(value);
                // Gauge par capteur, tagué pour des courbes séparées
                Gauge.builder("sensor_data_value", h, Holder::get)
                        .description("Dernière valeur de température par capteur")
                        .tags(Tags.of(
                                "sensor_id", id,
                                "type", "temperature"
                        ))
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
