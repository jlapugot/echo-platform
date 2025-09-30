package com.echo.api.repository;

import com.echo.api.entity.RecordedTraffic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RecordedTraffic entity.
 */
@Repository
public interface RecordedTrafficRepository extends JpaRepository<RecordedTraffic, Long> {

    /**
     * Finds all recorded traffic for a given session.
     *
     * @param sessionId Session identifier
     * @return List of recorded traffic
     */
    List<RecordedTraffic> findBySessionIdOrderByTimestampDesc(String sessionId);

    /**
     * Finds a matching recorded traffic entry for replay.
     * Matches based on session, method, path, and query parameters.
     *
     * @param sessionId Session identifier
     * @param method HTTP method
     * @param path Request path
     * @param queryParams Query parameters (can be null or empty)
     * @return Optional containing the first matching record
     */
    @Query("SELECT rt FROM RecordedTraffic rt WHERE rt.sessionId = :sessionId " +
            "AND rt.method = :method " +
            "AND rt.path = :path " +
            "AND (rt.queryParams = :queryParams OR (:queryParams IS NULL AND rt.queryParams IS NULL) OR (:queryParams = '' AND rt.queryParams IS NULL)) " +
            "ORDER BY rt.timestamp DESC")
    Optional<RecordedTraffic> findMatchingTraffic(
            @Param("sessionId") String sessionId,
            @Param("method") String method,
            @Param("path") String path,
            @Param("queryParams") String queryParams
    );

    /**
     * Counts traffic records for a given session.
     *
     * @param sessionId Session identifier
     * @return Count of records
     */
    long countBySessionId(String sessionId);

    /**
     * Finds all unique session IDs.
     *
     * @return List of session IDs
     */
    @Query("SELECT DISTINCT rt.sessionId FROM RecordedTraffic rt ORDER BY rt.sessionId")
    List<String> findAllSessionIds();
}