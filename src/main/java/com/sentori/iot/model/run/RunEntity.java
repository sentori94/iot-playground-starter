package com.sentori.iot.model.run;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="runs")
public class RunEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "username", length = 20, nullable = false)
    private String username;

    @Column(nullable=false)
    private String status; // RUNNING,SUCCESS,FAILED,CANCELED

    @Column(nullable=false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    private OffsetDateTime finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)          // <— clé !
    @Column(columnDefinition = "jsonb")
    private Map<String,Object> params;

    @Column(columnDefinition="text")
    private String errorMessage;

    @Column(name = "grafana_url")
    private String grafanaUrl;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getGrafanaUrl() {
        return grafanaUrl;
    }

    public void setGrafanaUrl(String grafanaUrl) {
        this.grafanaUrl = grafanaUrl;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
