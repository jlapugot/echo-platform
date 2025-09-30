package com.echo.ingestor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing recorded HTTP traffic.
 * This entity stores the complete request/response pair for later retrieval.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recorded_traffic", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_session_method_path", columnList = "session_id,method,path")
})
public class RecordedTraffic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Session identifier for grouping related traffic
     */
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    /**
     * HTTP method (GET, POST, etc.)
     */
    @Column(name = "method", nullable = false, length = 10)
    private String method;

    /**
     * Request path
     */
    @Column(name = "path", nullable = false, length = 2048)
    private String path;

    /**
     * Request query parameters
     */
    @Column(name = "query_params", length = 2048)
    private String queryParams;

    /**
     * Request headers as JSON string
     */
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * Request body
     */
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    /**
     * Response HTTP status code
     */
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    /**
     * Response headers as JSON string
     */
    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    /**
     * Response body
     */
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    /**
     * Timestamp when the traffic was recorded
     */
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /**
     * Timestamp when the record was created in the database
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}