package com.sentori.iot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.report.api-gateway-url=https://api.test.com/reports",
    "app.report.api-key=test-api-key"
})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void downloadReports_shouldReturnZipFileWhenSuccessful() throws Exception {
        // Given - Base64 encodé représentant un petit fichier ZIP fictif
        String base64Zip = "UEsDBAoAAAAAAKWCA1kAAAAAAAAAAAAAAAAJABwAaGVsbG8udHh0VVQJAAOqjzhnqo84Z3V4CwABBOgDAAAE6AMAAFBLAQIeAwoAAAAAAKWCA1kAAAAAAAAAAAAAAAAJABgAAAAAAAEAAACkgQAAAABoZWxsby50eHRVVAUAA6qPOGd1eAsAAQToAwAABOgDAABQSwUGAAAAAAEAAQBPAAAAQwAAAAAA";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(base64Zip, HttpStatus.OK));

        // When/Then
        mockMvc.perform(get("/api/reports/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream;charset=UTF-8"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"simulation-reports.zip\""));
    }

    @Test
    void downloadReports_shouldReturn500WhenApiGatewayFails() throws Exception {
        // Given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("API Gateway error"));

        // When/Then
        mockMvc.perform(get("/api/reports/download"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void downloadReports_shouldReturnErrorStatusWhenApiGatewayReturnsError() throws Exception {
        // Given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.FORBIDDEN));

        // When/Then
        mockMvc.perform(get("/api/reports/download"))
                .andExpect(status().isForbidden());
    }
}
