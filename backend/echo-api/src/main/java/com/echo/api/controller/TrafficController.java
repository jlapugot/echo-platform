package com.echo.api.controller;

import com.echo.api.dto.SessionSummaryDto;
import com.echo.api.dto.TrafficRecordDto;
import com.echo.api.service.TrafficQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing and querying recorded traffic.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficQueryService trafficQueryService;

    /**
     * Retrieves all recorded traffic for a specific session.
     *
     * @param sessionId Session identifier
     * @return List of traffic records
     */
    @GetMapping("/sessions/{sessionId}/traffic")
    public ResponseEntity<List<TrafficRecordDto>> getTrafficBySession(@PathVariable String sessionId) {
        log.info("GET /api/v1/sessions/{}/traffic", sessionId);
        List<TrafficRecordDto> traffic = trafficQueryService.getTrafficBySession(sessionId);
        return ResponseEntity.ok(traffic);
    }

    /**
     * Retrieves summaries for all sessions.
     *
     * @return List of session summaries
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionSummaryDto>> getAllSessions() {
        log.info("GET /api/v1/sessions");
        List<SessionSummaryDto> sessions = trafficQueryService.getSessionSummaries();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Internal endpoint for finding matching traffic during replay mode.
     * Used by echo-proxy service.
     *
     * @param sessionId Session identifier
     * @param method HTTP method
     * @param path Request path
     * @param queryParams Query parameters (optional)
     * @return Matching traffic record or 404
     */
    @GetMapping("/internal/match")
    public ResponseEntity<TrafficRecordDto> findMatchingTraffic(
            @RequestParam String sessionId,
            @RequestParam String method,
            @RequestParam String path,
            @RequestParam(required = false) String queryParams) {
        log.debug("GET /api/v1/internal/match - session={}, method={}, path={}", sessionId, method, path);

        return trafficQueryService.findMatchingTraffic(sessionId, method, path, queryParams)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a specific traffic record by ID.
     *
     * @param id Traffic record ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/traffic/{id}")
    public ResponseEntity<Void> deleteTrafficRecord(@PathVariable Long id) {
        log.info("DELETE /api/v1/traffic/{}", id);
        trafficQueryService.deleteTrafficRecord(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all traffic records for a specific session.
     *
     * @param sessionId Session identifier
     * @return 204 No Content on success
     */
    @DeleteMapping("/sessions/{sessionId}/traffic")
    public ResponseEntity<Void> deleteSessionTraffic(@PathVariable String sessionId) {
        log.info("DELETE /api/v1/sessions/{}/traffic", sessionId);
        trafficQueryService.deleteSessionTraffic(sessionId);
        return ResponseEntity.noContent().build();
    }
}