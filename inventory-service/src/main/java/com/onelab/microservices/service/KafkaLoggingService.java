package com.onelab.microservices.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaLoggingService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void log(String level, String message) {
        String logMessage = level + ": " + message;
        kafkaTemplate.send("log-topic", logMessage);
    }
}
