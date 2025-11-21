package com.sentori.iot.service;

import com.sentori.iot.model.SensorData;
import com.sentori.iot.model.run.RunEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulatorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private SimulatorService simulatorService;

    private final String baseUrl = "http://localhost:8080";
    private final String ingestPath = "/sensors/data";

    @BeforeEach
    void setUp() {
        simulatorService = new SimulatorService(restTemplate, baseUrl, ingestPath);
    }

    @Test
    void runManyRoundRobin_shouldPostCorrectNumberOfEvents() {
        // Given
        List<String> sensorIds = List.of("SENSOR-001", "SENSOR-002");
        int count = 3;
        long intervalMs = 0; // Pas de délai pour accélérer le test

        RunEntity mockRun = new RunEntity();
        mockRun.setId(UUID.randomUUID());
        mockRun.setUsername("testuser");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        CompletableFuture<Void> future = simulatorService.runManyRoundRobin(sensorIds, count, intervalMs, mockRun);
        future.join(); // Attendre la fin

        // Then
        // Total events = sensorIds.size() * count = 2 * 3 = 6
        verify(restTemplate, times(6)).postForEntity(anyString(), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void runManyRoundRobin_shouldPostToCorrectUrl() {
        // Given
        List<String> sensorIds = List.of("SENSOR-001");
        int count = 1;
        long intervalMs = 0;

        RunEntity mockRun = new RunEntity();
        mockRun.setId(UUID.randomUUID());
        mockRun.setUsername("testuser");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        CompletableFuture<Void> future = simulatorService.runManyRoundRobin(sensorIds, count, intervalMs, mockRun);
        future.join();

        // Then
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(HttpEntity.class), eq(Void.class));
        assertThat(urlCaptor.getValue()).isEqualTo(baseUrl + ingestPath);
    }

    @Test
    void runManyRoundRobin_shouldIncludeCorrectHeadersInRequest() {
        // Given
        List<String> sensorIds = List.of("SENSOR-001");
        int count = 1;
        long intervalMs = 0;

        RunEntity mockRun = new RunEntity();
        UUID runId = UUID.randomUUID();
        mockRun.setId(runId);
        mockRun.setUsername("testuser");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        CompletableFuture<Void> future = simulatorService.runManyRoundRobin(sensorIds, count, intervalMs, mockRun);
        future.join();

        // Then
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(Void.class));
        HttpEntity capturedEntity = entityCaptor.getValue();

        assertThat(capturedEntity.getHeaders().get("X-User")).contains("testuser");
        assertThat(capturedEntity.getHeaders().get("X-Run-Id")).contains(runId.toString());
    }

    @Test
    void runManyRoundRobin_shouldGenerateSensorDataWithCorrectFields() {
        // Given
        List<String> sensorIds = List.of("SENSOR-001");
        int count = 1;
        long intervalMs = 0;

        RunEntity mockRun = new RunEntity();
        mockRun.setId(UUID.randomUUID());
        mockRun.setUsername("testuser");

        ArgumentCaptor<HttpEntity<SensorData>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        CompletableFuture<Void> future = simulatorService.runManyRoundRobin(sensorIds, count, intervalMs, mockRun);
        future.join();

        // Then
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(Void.class));
        SensorData payload = entityCaptor.getValue().getBody();

        assertThat(payload).isNotNull();
        assertThat(payload.getSensorId()).isEqualTo("SENSOR-001");
        assertThat(payload.getType()).isEqualTo("temperature");
        assertThat(payload.getReading()).isBetween(0.0, 35.0);
        assertThat(payload.getTimestamp()).isNotNull();
    }

    @Test
    void runManyRoundRobin_shouldCompleteSuccessfullyWithMultipleSensors() {
        // Given
        List<String> sensorIds = List.of("SENSOR-001", "SENSOR-002", "SENSOR-003");
        int count = 5;
        long intervalMs = 0;

        RunEntity mockRun = new RunEntity();
        mockRun.setId(UUID.randomUUID());
        mockRun.setUsername("testuser");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // When
        CompletableFuture<Void> future = simulatorService.runManyRoundRobin(sensorIds, count, intervalMs, mockRun);

        // Then
        assertThatCode(future::join).doesNotThrowAnyException();
        verify(restTemplate, times(15)).postForEntity(anyString(), any(HttpEntity.class), eq(Void.class));
    }
}

