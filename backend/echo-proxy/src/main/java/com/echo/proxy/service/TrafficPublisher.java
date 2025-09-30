package com.echo.proxy.service;

import com.echo.proxy.config.RabbitMQConfiguration;
import com.echo.proxy.model.TrafficRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing recorded traffic to RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a traffic record to the RabbitMQ queue for asynchronous persistence.
     *
     * @param trafficRecord The traffic record to publish
     */
    public void publishTraffic(TrafficRecord trafficRecord) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.TRAFFIC_QUEUE, trafficRecord);
            log.debug("Published traffic record for session: {} path: {}",
                    trafficRecord.getSessionId(), trafficRecord.getPath());
        } catch (Exception e) {
            log.error("Failed to publish traffic record: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish traffic record", e);
        }
    }
}