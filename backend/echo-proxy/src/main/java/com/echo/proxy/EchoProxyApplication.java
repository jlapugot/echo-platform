package com.echo.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Echo Proxy Service.
 * This service acts as a smart proxy that can record and replay HTTP traffic.
 *
 * @author Echo Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
public class EchoProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EchoProxyApplication.class, args);
    }
}