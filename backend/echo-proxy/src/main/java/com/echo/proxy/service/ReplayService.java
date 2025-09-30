package com.echo.proxy.service;

import com.echo.proxy.config.ProxyConfiguration;
import com.echo.proxy.model.TrafficRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for retrieving recorded responses from Echo API during REPLAY mode.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplayService {

    private final ProxyConfiguration proxyConfiguration;
    private final WebClient.Builder webClientBuilder;

    /**
     * Fetches a matching recorded response from Echo API.
     *
     * @param sessionId Session ID
     * @param method HTTP method
     * @param path Request path
     * @param queryParams Query parameters
     * @return Mono containing the matched TrafficRecord, or empty if no match found
     */
    public Mono<TrafficRecord> findMatchingResponse(String sessionId, String method, String path, String queryParams) {
        String echoApiUrl = proxyConfiguration.getEchoApiUrl();

        return webClientBuilder.build()
                .get()
                .uri(echoApiUrl + "/api/v1/internal/match" +
                        "?sessionId={sessionId}&method={method}&path={path}&queryParams={queryParams}",
                        sessionId, method, path, queryParams != null ? queryParams : "")
                .retrieve()
                .bodyToMono(TrafficRecord.class)
                .doOnSuccess(record -> log.info("Found matching response for {} {}", method, path))
                .doOnError(error -> log.warn("No matching response found for {} {}: {}", method, path, error.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}