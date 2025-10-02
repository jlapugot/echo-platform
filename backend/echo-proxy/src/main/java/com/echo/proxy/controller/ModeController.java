package com.echo.proxy.controller;

import com.echo.proxy.config.EchoMode;
import com.echo.proxy.config.ProxyConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing Echo Proxy mode (RECORD/REPLAY)
 */
@Slf4j
@RestController
@RequestMapping("/api/mode")
@RequiredArgsConstructor
public class ModeController {

    private final ProxyConfiguration proxyConfiguration;

    /**
     * Get current proxy mode and configuration
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getMode() {
        Map<String, String> response = new HashMap<>();
        response.put("mode", proxyConfiguration.getMode().name());
        response.put("sessionId", proxyConfiguration.getSessionId());
        response.put("targetUrl", proxyConfiguration.getTargetUrl());
        return ResponseEntity.ok(response);
    }

    /**
     * Switch proxy mode
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> switchMode(@RequestBody Map<String, String> request) {
        String newMode = request.get("mode");

        if (newMode == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mode is required"));
        }

        try {
            EchoMode mode = EchoMode.valueOf(newMode.toUpperCase());
            proxyConfiguration.setMode(mode);

            log.info("Switched mode to: {}", mode);

            Map<String, String> response = new HashMap<>();
            response.put("mode", mode.name());
            response.put("message", "Mode switched successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid mode. Must be RECORD or REPLAY"));
        }
    }

    /**
     * Get current target URL
     */
    @GetMapping("/target")
    public ResponseEntity<Map<String, String>> getTargetUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("targetUrl", proxyConfiguration.getTargetUrl());
        return ResponseEntity.ok(response);
    }

    /**
     * Update target URL at runtime
     */
    @PostMapping("/target")
    public ResponseEntity<Map<String, String>> updateTargetUrl(@RequestBody Map<String, String> request) {
        String newTargetUrl = request.get("targetUrl");

        if (newTargetUrl == null || newTargetUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Target URL is required"));
        }

        // Basic URL validation
        if (!newTargetUrl.startsWith("http://") && !newTargetUrl.startsWith("https://")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Target URL must start with http:// or https://"));
        }

        proxyConfiguration.setTargetUrl(newTargetUrl);
        log.info("Updated target URL to: {}", newTargetUrl);

        Map<String, String> response = new HashMap<>();
        response.put("targetUrl", newTargetUrl);
        response.put("message", "Target URL updated successfully");
        return ResponseEntity.ok(response);
    }
}
