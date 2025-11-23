package com.sentori.iot.model;

public class RunStartResponse {
    private String runId;
    private String grafanaUrl;

    // Constructeurs
    public RunStartResponse() {
    }

    public RunStartResponse(String runId, String grafanaUrl) {
        this.runId = runId;
        this.grafanaUrl = grafanaUrl;
    }

    // Getters et Setters
    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getGrafanaUrl() {
        return grafanaUrl;
    }

    public void setGrafanaUrl(String grafanaUrl) {
        this.grafanaUrl = grafanaUrl;
    }
}
