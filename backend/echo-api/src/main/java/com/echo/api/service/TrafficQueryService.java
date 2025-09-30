package com.echo.api.service;

import com.echo.api.dto.SessionSummaryDto;
import com.echo.api.dto.TrafficRecordDto;
import com.echo.api.entity.RecordedTraffic;
import com.echo.api.repository.RecordedTrafficRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for querying recorded traffic data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficQueryService {

    private final RecordedTrafficRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Retrieves all recorded traffic for a session.
     *
     * @param sessionId Session identifier
     * @return List of traffic records
     */
    public List<TrafficRecordDto> getTrafficBySession(String sessionId) {
        log.info("Retrieving traffic for session: {}", sessionId);
        List<RecordedTraffic> entities = repository.findBySessionIdOrderByTimestampDesc(sessionId);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a matching traffic record for replay mode.
     *
     * @param sessionId Session identifier
     * @param method HTTP method
     * @param path Request path
     * @param queryParams Query parameters
     * @return Optional containing matched traffic record
     */
    public Optional<TrafficRecordDto> findMatchingTraffic(String sessionId, String method, String path, String queryParams) {
        log.debug("Searching for match: session={}, method={}, path={}, queryParams={}",
                sessionId, method, path, queryParams);

        return repository.findMatchingTraffic(sessionId, method, path, queryParams)
                .map(this::convertToDto);
    }

    /**
     * Gets summary information for all sessions.
     *
     * @return List of session summaries
     */
    public List<SessionSummaryDto> getSessionSummaries() {
        List<String> sessionIds = repository.findAllSessionIds();
        return sessionIds.stream()
                .map(sessionId -> SessionSummaryDto.builder()
                        .sessionId(sessionId)
                        .recordCount(repository.countBySessionId(sessionId))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Converts entity to DTO.
     *
     * @param entity RecordedTraffic entity
     * @return TrafficRecordDto
     */
    private TrafficRecordDto convertToDto(RecordedTraffic entity) {
        return TrafficRecordDto.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .method(entity.getMethod())
                .path(entity.getPath())
                .queryParams(entity.getQueryParams())
                .requestHeaders(parseJsonToMap(entity.getRequestHeaders()))
                .requestBody(entity.getRequestBody())
                .statusCode(entity.getStatusCode())
                .responseHeaders(parseJsonToMap(entity.getResponseHeaders()))
                .responseBody(entity.getResponseBody())
                .timestamp(entity.getTimestamp())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Parses JSON string to Map.
     *
     * @param json JSON string
     * @return Map representation
     */
    private Map<String, String> parseJsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            return null;
        }
    }
}