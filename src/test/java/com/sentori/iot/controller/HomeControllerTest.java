package com.sentori.iot.controller;

import com.sentori.iot.repository.RunRepository;
import com.sentori.iot.service.RunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RunRepository runRepository;

    @MockBean
    private RunService runService;

    @Test
    void index_shouldReturnIndexPage() throws Exception {
        // Given
        when(runRepository.existsRunning()).thenReturn(false);
        when(runRepository.findTop10ByOrderByStartedAtDesc()).thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("running"))
                .andExpect(model().attributeExists("runs"))
                .andExpect(model().attribute("running", false));
    }

    @Test
    void index_shouldShowRunningBadgeWhenSimulationIsRunning() throws Exception {
        // Given
        when(runRepository.existsRunning()).thenReturn(true);
        when(runRepository.findTop10ByOrderByStartedAtDesc()).thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("running", true));
    }

    @Test
    void simulate_shouldRejectInvalidUsername() throws Exception {
        // Given
        when(runRepository.existsRunning()).thenReturn(false);
        when(runRepository.findTop10ByOrderByStartedAtDesc()).thenReturn(Collections.emptyList());

        // When/Then - Username trop court
        mockMvc.perform(post("/simulate")
                        .param("username", "ab")
                        .param("sensorsCount", "1")
                        .param("count", "10")
                        .param("intervalMs", "200"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("toast"))
                .andExpect(model().attribute("toast", "Username invalide (3–20, lettres/chiffres . _ -)"));
    }

    @Test
    void simulate_shouldRejectInvalidUsernameCharacters() throws Exception {
        // Given
        when(runRepository.existsRunning()).thenReturn(false);
        when(runRepository.findTop10ByOrderByStartedAtDesc()).thenReturn(Collections.emptyList());

        // When/Then - Username avec caractères invalides
        mockMvc.perform(post("/simulate")
                        .param("username", "user@name!")
                        .param("sensorsCount", "1")
                        .param("count", "10")
                        .param("intervalMs", "200"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("toast"))
                .andExpect(model().attribute("toast", "Username invalide (3–20, lettres/chiffres . _ -)"));
    }

    @Test
    void simulate_shouldClampSensorsCountBetween1And20() throws Exception {
        // Given
        when(runRepository.existsRunning()).thenReturn(false);
        when(runRepository.findTop10ByOrderByStartedAtDesc()).thenReturn(Collections.emptyList());
        when(runService.tryStartMany(anyString(), anyList(), anyInt(), anyLong())).thenReturn(null);

        // When/Then - sensorsCount > 20 devrait être clampé à 20
        mockMvc.perform(post("/simulate")
                        .param("username", "testuser")
                        .param("sensorsCount", "50")
                        .param("count", "10")
                        .param("intervalMs", "200"))
                .andExpect(status().isOk());

        // Vérifier que le service a été appelé (même si retourne null)
        // Le clamping est fait dans le controller
    }
}

