package com.echo.proxy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Echo Proxy.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "echo.proxy")
public class ProxyConfiguration {

    /**
     * Operating mode: RECORD or REPLAY
     */
    private EchoMode mode = EchoMode.RECORD;

    /**
     * Session ID for grouping recorded traffic
     */
    private String sessionId = "default-session";

    /**
     * Target service URL for proxying in RECORD mode
     */
    private String targetUrl;

    /**
     * Echo API base URL for fetching recorded responses in REPLAY mode
     */
    private String echoApiUrl = "http://localhost:8082";
}