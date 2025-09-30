package com.echo.ingestor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Data transfer object for traffic records received from RabbitMQ.
 * Mirrors the TrafficRecord from echo-proxy service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficRecord {

    private String sessionId;
    private String method;
    private String path;
    private Map<String, String> requestHeaders;
    private String requestBody;
    private Integer statusCode;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private Instant timestamp;
    private String queryParams;
}