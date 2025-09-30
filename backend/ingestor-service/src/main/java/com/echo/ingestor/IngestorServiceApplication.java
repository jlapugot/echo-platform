package com.echo.ingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Echo Ingestor Service.
 * This service consumes traffic records from RabbitMQ and persists them to PostgreSQL.
 *
 * @author Echo Platform Team
 * @version 1.0.0
 */
@SpringBootApplication
public class IngestorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IngestorServiceApplication.class, args);
    }
}