package com.echo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Data transfer object for traffic records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficRecordDto {

    private Long id;
    private String sessionId;
    private String method;
    private String path;
    private String queryParams;
    private Map<String, String> requestHeaders;
    private String requestBody;
    private Integer statusCode;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private Instant timestamp;
    private Instant createdAt;
}