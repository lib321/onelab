package com.onelab.microservices.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.InventoryUpdateDTO;
import com.onelab.microservices.dto.KafkaMessageDTO;
import com.onelab.microservices.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    private final InventoryService inventoryService;

    public KafkaConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "product-events", groupId = "inventory-group")
    public void consumeProductEvent(KafkaMessageDTO message) {
        log.info("Получено Kafka сообщение: action='{}', data={}", message.getAction(), message.getData());

        switch (message.getAction()) {
            case "CREATED" -> log.info("Добавлен продукт: {}", convertToDTO(message.getData()));
            case "DELETE" -> log.info("Удалён продукт: {}", message.getData());
            default -> log.warn("Неизвестное действие: {}", message.getAction());
        }
    }

    @KafkaListener(topics = "product-events-update", groupId = "inventory-group")
    public void consumeProductEventUpdate(KafkaMessageDTO message) {
        log.info("Получено Kafka сообщение: action='{}', data={}", message.getAction(), message.getData());
        inventoryService.restockProduct(convertToDTO(message.getData()));
    }

    @KafkaListener(topics = "order-events", groupId = "order-group")
    public void consumeOrderEvent(KafkaMessageDTO message) {
        log.info("Получено Kafka сообщение: action='{}', data={}", message.getAction(), message.getData());
        switch (message.getAction()) {
            case "CREATED" -> log.info("Запрос на создание заказа: {}", convertToDTO(message.getData()));
            case "DELETE" -> log.info("Удален заказ: {}", message.getData());
            default -> log.warn("Неизвестное действие: {}", message.getAction());
        }
    }

    @KafkaListener(topics = "order-events-update", groupId = "order-group")
    public void consumeOrderEventUpdate(KafkaMessageDTO message) {
        log.info("Получено Kafka сообщение: action='{}', data={}", message.getAction(), message.getData());

        InventoryUpdateDTO updateDTO = convertOrderDTOtoItemDTO(message.getData());
        inventoryService.updateInventory(updateDTO);
    }

    private InventoryUpdateDTO convertOrderDTOtoItemDTO(Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(data, InventoryUpdateDTO.class);
    }

    private InventoryItemDTO convertToDTO(Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(data, InventoryItemDTO.class);
    }
}
