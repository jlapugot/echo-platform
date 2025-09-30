package com.echo.proxy.service;

import com.echo.proxy.config.RabbitMQConfiguration;
import com.echo.proxy.model.TrafficRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrafficPublisher service.
 */
@ExtendWith(MockitoExtension.class)
class TrafficPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private TrafficPublisher trafficPublisher;

    @BeforeEach
    void setUp() {
        trafficPublisher = new TrafficPublisher(rabbitTemplate);
    }

    @Test
    void publishTraffic_shouldPublishToQueue() {
        // Given
        TrafficRecord trafficRecord = TrafficRecord.builder()
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .requestHeaders(Map.of("Content-Type", "application/json"))
                .requestBody("")
                .statusCode(200)
                .responseHeaders(Map.of("Content-Type", "application/json"))
                .responseBody("{\"status\": \"ok\"}")
                .timestamp(Instant.now())
                .build();

        // When
        trafficPublisher.publishTraffic(trafficRecord);

        // Then
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(RabbitMQConfiguration.TRAFFIC_QUEUE), eq(trafficRecord));
    }

    @Test
    void publishTraffic_shouldThrowException_whenPublishFails() {
        // Given
        TrafficRecord trafficRecord = TrafficRecord.builder()
                .sessionId("test-session")
                .method("GET")
                .path("/api/test")
                .build();

        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate)
                .convertAndSend(eq(RabbitMQConfiguration.TRAFFIC_QUEUE), eq(trafficRecord));

        // When & Then
        assertThrows(RuntimeException.class, () -> trafficPublisher.publishTraffic(trafficRecord));
    }
}