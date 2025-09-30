package com.echo.ingestor.service;

import com.echo.ingestor.entity.RecordedTraffic;
import com.echo.ingestor.model.TrafficRecord;
import com.echo.ingestor.repository.RecordedTrafficRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for persisting traffic records to the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficIngestionService {

    private final RecordedTrafficRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Persists a traffic record to the database.
     *
     * @param trafficRecord Traffic record to persist
     * @return Persisted entity
     */
    @Transactional
    public RecordedTraffic ingestTraffic(TrafficRecord trafficRecord) {
        try {
            RecordedTraffic entity = RecordedTraffic.builder()
                    .sessionId(trafficRecord.getSessionId())
                    .method(trafficRecord.getMethod())
                    .path(trafficRecord.getPath())
                    .queryParams(trafficRecord.getQueryParams())
                    .requestHeaders(convertMapToJson(trafficRecord.getRequestHeaders()))
                    .requestBody(trafficRecord.getRequestBody())
                    .statusCode(trafficRecord.getStatusCode())
                    .responseHeaders(convertMapToJson(trafficRecord.getResponseHeaders()))
                    .responseBody(trafficRecord.getResponseBody())
                    .timestamp(trafficRecord.getTimestamp())
                    .build();

            RecordedTraffic saved = repository.save(entity);
            log.info("Ingested traffic record: session={}, method={}, path={}, id={}",
                    saved.getSessionId(), saved.getMethod(), saved.getPath(), saved.getId());

            return saved;
        } catch (Exception e) {
            log.error("Failed to ingest traffic record: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to persist traffic record", e);
        }
    }

    /**
     * Converts a map to JSON string for storage.
     *
     * @param map Map to convert
     * @return JSON string representation
     */
    private String convertMapToJson(java.util.Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert map to JSON: {}", e.getMessage());
            return null;
        }
    }
}