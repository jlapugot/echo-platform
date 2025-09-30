package com.echo.ingestor.listener;

import com.echo.ingestor.model.TrafficRecord;
import com.echo.ingestor.service.TrafficIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener for consuming traffic records.
 * Listens to the traffic.recorded queue and persists messages to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficListener {

    private final TrafficIngestionService ingestionService;

    /**
     * Consumes traffic records from RabbitMQ and persists them.
     *
     * @param trafficRecord Traffic record message
     */
    @RabbitListener(queues = "${echo.rabbitmq.queue-name}")
    public void handleTrafficRecord(TrafficRecord trafficRecord) {
        log.debug("Received traffic record: session={}, method={}, path={}",
                trafficRecord.getSessionId(),
                trafficRecord.getMethod(),
                trafficRecord.getPath());

        try {
            ingestionService.ingestTraffic(trafficRecord);
        } catch (Exception e) {
            log.error("Failed to process traffic record: {}", e.getMessage(), e);
            // In production, consider dead letter queue or retry mechanism
            throw e;
        }
    }
}