package com.echo.proxy.filter;

import com.echo.proxy.config.EchoMode;
import com.echo.proxy.config.ProxyConfiguration;
import com.echo.proxy.model.TrafficRecord;
import com.echo.proxy.service.TrafficPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global filter for recording HTTP traffic in RECORD mode.
 * Captures request/response pairs and publishes them to RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordModeFilter implements GlobalFilter, Ordered {

    private final ProxyConfiguration proxyConfiguration;
    private final TrafficPublisher trafficPublisher;
    private final List<HttpMessageReader<?>> messageReaders;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (proxyConfiguration.getMode() != EchoMode.RECORD) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String queryParams = request.getURI().getQuery();

        // Capture request headers
        Map<String, String> requestHeaders = convertHeaders(request.getHeaders());

        // Read request body
        return ServerRequest.create(exchange, messageReaders)
                .bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(requestBody -> {
                    // Capture response
                    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
                        @Override
                        public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
                            Flux<DataBuffer> flux = Flux.from(body);
                            return super.writeWith(flux.collectList().flatMapMany(dataBuffers -> {
                                // Capture response body
                                StringBuilder responseBody = new StringBuilder();
                                dataBuffers.forEach(dataBuffer -> {
                                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    dataBuffer.readPosition(0); // Reset read position
                                    responseBody.append(new String(bytes, StandardCharsets.UTF_8));
                                });

                                // Build traffic record
                                TrafficRecord trafficRecord = TrafficRecord.builder()
                                        .sessionId(proxyConfiguration.getSessionId())
                                        .method(method)
                                        .path(path)
                                        .queryParams(queryParams)
                                        .requestHeaders(requestHeaders)
                                        .requestBody(requestBody)
                                        .statusCode(getDelegate().getStatusCode().value())
                                        .responseHeaders(convertHeaders(getDelegate().getHeaders()))
                                        .responseBody(responseBody.toString())
                                        .timestamp(Instant.now())
                                        .build();

                                // Publish to RabbitMQ asynchronously
                                try {
                                    trafficPublisher.publishTraffic(trafficRecord);
                                } catch (Exception e) {
                                    log.error("Failed to publish traffic: {}", e.getMessage());
                                }

                                return Flux.fromIterable(dataBuffers);
                            }));
                        }
                    };

                    // Continue with decorated response
                    return chain.filter(exchange.mutate().response(decoratedResponse).build());
                });
    }

    /**
     * Converts HttpHeaders to a simple Map for serialization.
     *
     * @param headers HttpHeaders object
     * @return Map of header names to values (first value only)
     */
    private Map<String, String> convertHeaders(HttpHeaders headers) {
        Map<String, String> map = new HashMap<>();
        headers.forEach((key, values) -> {
            if (!values.isEmpty()) {
                map.put(key, values.get(0));
            }
        });
        return map;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}