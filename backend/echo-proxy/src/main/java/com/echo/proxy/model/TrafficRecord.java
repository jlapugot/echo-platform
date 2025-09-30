package com.echo.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Data model representing a recorded HTTP request/response pair.
 * This object is published to RabbitMQ for asynchronous persistence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrafficRecord {

    /**
     * Session identifier for grouping related traffic
     */
    private String sessionId;

    /**
     * HTTP method (GET, POST, etc.)
     */
    private String method;

    /**
     * Request path
     */
    private String path;

    /**
     * Request headers
     */
    private Map<String, String> requestHeaders;

    /**
     * Request body as string
     */
    private String requestBody;

    /**
     * Response HTTP status code
     */
    private Integer statusCode;

    /**
     * Response headers
     */
    private Map<String, String> responseHeaders;

    /**
     * Response body as string
     */
    private String responseBody;

    /**
     * Timestamp when the traffic was recorded
     */
    private Instant timestamp;

    /**
     * Request query parameters
     */
    private String queryParams;
}