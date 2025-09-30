package com.echo.proxy.config;

/**
 * Enum representing the operating modes of Echo Proxy.
 */
public enum EchoMode {
    /**
     * Record mode: Proxy passes requests to real services and records traffic
     */
    RECORD,

    /**
     * Replay mode: Proxy returns recorded responses instead of forwarding requests
     */
    REPLAY
}