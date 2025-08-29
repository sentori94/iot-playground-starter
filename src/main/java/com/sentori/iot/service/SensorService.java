package com.sentori.iot.service;

import com.sentori.iot.model.SensorData;
import com.sentori.iot.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorService {

    private final SensorDataRepository repository;

    public SensorService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public SensorData save(SensorData data) {
        return repository.save(data);
    }

    public List<SensorData> findAll() {
        return repository.findAll();
    }
}
