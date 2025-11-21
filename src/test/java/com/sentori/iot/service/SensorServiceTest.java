package com.sentori.iot.service;

import com.sentori.iot.model.SensorData;
import com.sentori.iot.repository.SensorDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private SensorDataRepository repository;

    @InjectMocks
    private SensorService service;

    @Test
    void save_shouldCallRepositorySave() {
        // Given
        SensorData data = new SensorData("sensor-1", "temperature", 23.5, LocalDateTime.now());

        // When
        service.save(data);

        // Then
        verify(repository, times(1)).save(data);
    }

    @Test
    void save_shouldReturnSavedData() {
        // Given
        SensorData data = new SensorData("sensor-1", "temperature", 23.5, LocalDateTime.now());
        when(repository.save(any(SensorData.class))).thenReturn(data);

        // When
        SensorData result = service.save(data);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSensorId()).isEqualTo("sensor-1");
        assertThat(result.getType()).isEqualTo("temperature");
        assertThat(result.getReading()).isEqualTo(23.5);
    }

    @Test
    void save_shouldHandleMultipleSensors() {
        // Given
        SensorData data1 = new SensorData("sensor-1", "temperature", 23.5, LocalDateTime.now());
        SensorData data2 = new SensorData("sensor-2", "humidity", 65.0, LocalDateTime.now());

        // When
        service.save(data1);
        service.save(data2);

        // Then
        verify(repository, times(2)).save(any(SensorData.class));
    }
}
