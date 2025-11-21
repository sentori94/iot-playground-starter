package com.sentori.iot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentori.iot.model.SensorData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ingest_shouldReturn200() throws Exception {
        // Given
        SensorData data = new SensorData("sensor-1", "temperature", 25.0, LocalDateTime.now());

        // When/Then
        mockMvc.perform(post("/sensors/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data))
                        .header("X-User", "testuser")
                        .header("X-Run-Id", "test-run-123"))
                .andExpect(status().isOk());
    }

    @Test
    void list_shouldReturnArray() throws Exception {
        mockMvc.perform(get("/sensors/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
