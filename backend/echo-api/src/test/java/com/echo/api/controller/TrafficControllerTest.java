package com.echo.api.controller;

import com.echo.api.dto.TrafficRecordDto;
import com.echo.api.service.TrafficQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TrafficController.
 */
@WebMvcTest(TrafficController.class)
class TrafficControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrafficQueryService trafficQueryService;

    @Test
    void getTrafficBySession_shouldReturnTrafficList() throws Exception {
        // Given
        TrafficRecordDto dto = TrafficRecordDto.builder()
                .id(1L)
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .statusCode(200)
                .timestamp(Instant.now())
                .build();

        when(trafficQueryService.getTrafficBySession("test-session"))
                .thenReturn(List.of(dto));

        // When & Then
        mockMvc.perform(get("/api/v1/sessions/test-session/traffic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sessionId").value("test-session"))
                .andExpect(jsonPath("$[0].method").value("GET"));
    }

    @Test
    void findMatchingTraffic_shouldReturnMatch() throws Exception {
        // Given
        TrafficRecordDto dto = TrafficRecordDto.builder()
                .id(1L)
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .statusCode(200)
                .responseBody("{\"result\": \"success\"}")
                .build();

        when(trafficQueryService.findMatchingTraffic(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(dto));

        // When & Then
        mockMvc.perform(get("/api/v1/internal/match")
                        .param("sessionId", "test-session")
                        .param("method", "GET")
                        .param("path", "/api/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void findMatchingTraffic_shouldReturn404WhenNoMatch() throws Exception {
        // Given
        when(trafficQueryService.findMatchingTraffic(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/internal/match")
                        .param("sessionId", "test-session")
                        .param("method", "GET")
                        .param("path", "/api/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}