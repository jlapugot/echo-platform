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
     * Get current proxy mode
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getMode() {
        Map<String, String> response = new HashMap<>();
        response.put("mode", proxyConfiguration.getMode().name());
        response.put("sessionId", proxyConfiguration.getSessionId());
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
}
