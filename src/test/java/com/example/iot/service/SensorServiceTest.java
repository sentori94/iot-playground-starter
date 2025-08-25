package com.example.iot.service;

import com.example.iot.model.SensorData;
import com.example.iot.repository.SensorDataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class SensorServiceTest {

    @Mock
    private SensorDataRepository repository;

    @InjectMocks
    private SensorService service;

    public SensorServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_shouldCallRepositorySave() {
        SensorData data = new SensorData("sensor-1", "temperature", 23.5, LocalDateTime.now());

        service.save(data);

        verify(repository, times(1)).save(data);
    }
}
