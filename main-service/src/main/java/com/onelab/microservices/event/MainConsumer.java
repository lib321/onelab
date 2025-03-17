package com.onelab.microservices.event;

import com.onelab.microservices.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MainConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MainConsumer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "product-response-topic", groupId = "main-group")
    public void listenProductResponse(ProductDTO productDTO) {
        log.info("Ответ от product-service: {}", productDTO);
    }


    @KafkaListener(topics = "inventory-response-topic", groupId = "main-group")
    public void listenInventoryResponse(InventoryResponseDTO response) {
        if (response.isAvailable()) {
            System.out.println("Товар есть в наличии. Оформляем заказ...");

            OrderDTO orderDTO = new OrderDTO(response.getProductId(), response.getProductName(), response.getQuantity(), response.getCustomerName());
            kafkaTemplate.send("order-topic", orderDTO);
            System.out.println("Заказ отправлен на обработку.");
        } else {
            System.out.println("Недостаточно товара на складе!");
        }
    }

    @KafkaListener(topics = "restock-request-topic", groupId = "main-group")
    public void handleRestockRequest(InventoryCheckRequestDTO request) {
        log.info("Получен запрос на пополнение запасов для продукта ID: {}", request.getProductId());

        int restockAmount = 5;
        InventoryRestockRequestDTO restockRequest = new InventoryRestockRequestDTO(request.getProductId(), restockAmount);

        kafkaTemplate.send("inventory-restock-topic", String.valueOf(request.getProductId()), restockRequest);
        log.info("Отправлен запрос на пополнение запасов для продукта ID: {} на {} единиц", request.getProductId(), restockAmount);
    }

    @KafkaListener(topics = "log-topic", groupId = "log-consumer-group")
    public void consumeLog(String logMessage) {
        log.info("Получен лог: {}", logMessage);
    }
}
