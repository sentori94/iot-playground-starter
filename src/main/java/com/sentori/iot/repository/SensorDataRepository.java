package com.sentori.iot.repository;

import com.sentori.iot.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

}
