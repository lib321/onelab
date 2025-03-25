package com.onelab.microservices.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.dto.*;
import com.onelab.dto.KafkaMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "order-events", groupId = "order-group")
    public void consumeOrderEvent(KafkaMessageDTO message) {
        log.info("Получено Kafka сообщение: action='{}', data={}", message.getAction(), message.getData());
        switch (message.getAction()) {
            case "NOT_ENOUGH" -> log.info("Недостаточно товара: {}", convertToDTO(message.getData()));
            case "CONFIRMED" -> log.info("Заказ успешно создан: {}", convertToDTO(message.getData()));
            default -> log.warn("Неизвестное действие: {}", message.getAction());
        }
    }
    private InventoryRequestDTO convertToDTO(Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(data, InventoryRequestDTO.class);
    }
}
