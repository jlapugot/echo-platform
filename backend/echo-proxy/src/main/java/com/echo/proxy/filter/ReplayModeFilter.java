package com.echo.proxy.filter;

import com.echo.proxy.config.EchoMode;
import com.echo.proxy.config.ProxyConfiguration;
import com.echo.proxy.model.TrafficRecord;
import com.echo.proxy.service.ReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Global filter for replaying recorded HTTP traffic in REPLAY mode.
 * Instead of forwarding requests, it returns pre-recorded responses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplayModeFilter implements GlobalFilter, Ordered {

    private final ProxyConfiguration proxyConfiguration;
    private final ReplayService replayService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (proxyConfiguration.getMode() != EchoMode.REPLAY) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String queryParams = request.getURI().getQuery();
        String sessionId = proxyConfiguration.getSessionId();

        log.info("REPLAY mode: Looking for recorded response for {} {}", method, path);

        return replayService.findMatchingResponse(sessionId, method, path, queryParams)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(optionalRecord -> {
                    if (optionalRecord.isPresent()) {
                        log.info("Found matching response, returning recorded data");
                        return writeResponse(exchange, optionalRecord.get());
                    } else {
                        log.warn("No matching response found for {} {}", method, path);
                        return writeNotFoundResponse(exchange);
                    }
                });
    }

    /**
     * Writes the recorded response back to the client.
     *
     * @param exchange ServerWebExchange
     * @param trafficRecord Recorded traffic data
     * @return Mono<Void>
     */
    private Mono<Void> writeResponse(ServerWebExchange exchange, TrafficRecord trafficRecord) {
        ServerHttpResponse response = exchange.getResponse();

        // Set status code
        response.setStatusCode(HttpStatus.valueOf(trafficRecord.getStatusCode()));

        // Set response headers (excluding CORS, transfer, and encoding headers)
        if (trafficRecord.getResponseHeaders() != null) {
            trafficRecord.getResponseHeaders().forEach((key, value) -> {
                // Skip headers that shouldn't be copied from recorded response
                if (!key.equalsIgnoreCase("Transfer-Encoding")
                    && !key.equalsIgnoreCase("Content-Length")
                    && !key.equalsIgnoreCase("Content-Encoding")
                    && !key.equalsIgnoreCase("Access-Control-Allow-Origin")
                    && !key.equalsIgnoreCase("Access-Control-Allow-Credentials")
                    && !key.equalsIgnoreCase("Access-Control-Allow-Methods")
                    && !key.equalsIgnoreCase("Access-Control-Allow-Headers")
                    && !key.equalsIgnoreCase("Access-Control-Expose-Headers")) {
                    response.getHeaders().add(key, value);
                }
            });
        }

        // Write response body
        byte[] bytes = trafficRecord.getResponseBody() != null
                ? trafficRecord.getResponseBody().getBytes(StandardCharsets.UTF_8)
                : new byte[0];

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Writes a 404 Not Found response when no matching recording is found.
     *
     * @param exchange ServerWebExchange
     * @return Mono<Void>
     */
    private Mono<Void> writeNotFoundResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().add("Content-Type", "application/json");

        String errorMessage = "{\"error\": \"No recorded response found for this request\"}";
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}