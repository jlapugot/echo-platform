package com.echo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Echo API Service.
 * This service provides REST APIs for querying and managing recorded traffic.
 *
 * @author Echo Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
public class EchoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EchoApiApplication.class, args);
    }
}