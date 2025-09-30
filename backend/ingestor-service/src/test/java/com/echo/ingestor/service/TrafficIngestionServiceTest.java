package com.echo.ingestor.service;

import com.echo.ingestor.entity.RecordedTraffic;
import com.echo.ingestor.model.TrafficRecord;
import com.echo.ingestor.repository.RecordedTrafficRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrafficIngestionService.
 */
@ExtendWith(MockitoExtension.class)
class TrafficIngestionServiceTest {

    @Mock
    private RecordedTrafficRepository repository;

    private TrafficIngestionService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TrafficIngestionService(repository, objectMapper);
    }

    @Test
    void ingestTraffic_shouldPersistRecordSuccessfully() {
        // Given
        TrafficRecord trafficRecord = TrafficRecord.builder()
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .queryParams("param=value")
                .requestHeaders(Map.of("Content-Type", "application/json"))
                .requestBody("{\"test\": true}")
                .statusCode(200)
                .responseHeaders(Map.of("Content-Type", "application/json"))
                .responseBody("{\"result\": \"success\"}")
                .timestamp(Instant.now())
                .build();

        RecordedTraffic savedEntity = RecordedTraffic.builder()
                .id(1L)
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .statusCode(200)
                .build();

        when(repository.save(any(RecordedTraffic.class))).thenReturn(savedEntity);

        // When
        RecordedTraffic result = service.ingestTraffic(trafficRecord);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).save(any(RecordedTraffic.class));
    }

    @Test
    void ingestTraffic_shouldHandleNullHeaders() {
        // Given
        TrafficRecord trafficRecord = TrafficRecord.builder()
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .statusCode(200)
                .timestamp(Instant.now())
                .build();

        RecordedTraffic savedEntity = RecordedTraffic.builder()
                .id(1L)
                .sessionId("test-session")
                .build();

        when(repository.save(any(RecordedTraffic.class))).thenReturn(savedEntity);

        // When
        RecordedTraffic result = service.ingestTraffic(trafficRecord);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).save(any(RecordedTraffic.class));
    }
}