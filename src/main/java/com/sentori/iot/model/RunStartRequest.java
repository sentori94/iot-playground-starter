package com.sentori.iot.model;

import java.util.List;

public class RunStartRequest {
    private List<String> sensorIds;
    private int duration; // en secondes
    private int interval; // en secondes

    // Constructeurs
    public RunStartRequest() {
    }

    public RunStartRequest(List<String> sensorIds, int duration, int interval) {
        this.sensorIds = sensorIds;
        this.duration = duration;
        this.interval = interval;
    }

    // Getters et Setters
    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
