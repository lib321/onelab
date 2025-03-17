package com.onelab.microservices.event;

import com.onelab.microservices.dto.InventoryCheckRequestDTO;
import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.InventoryRestockRequestDTO;
import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-topic", groupId = "inventory-group")
    public void consumeInventoryMessage(InventoryItemDTO inventoryItemDTO) {
        log.info("------------- {} ", inventoryItemDTO);
        InventoryItem item = new InventoryItem();
        item.setProductId(inventoryItemDTO.getProductId());
        item.setProductName(inventoryItemDTO.getProductName());
        item.setQuantity(inventoryItemDTO.getQuantity());

        inventoryService.add(item);
    }

    @KafkaListener(topics = "inventory-check-topic", groupId = "inventory-group")
    public void consume(InventoryRequestDTO request) {
        inventoryService.checkAndReserveInventory(request);
    }

    @KafkaListener(topics = "inventory-check-stock-topic", groupId = "inventory-group")
    public void checkInventory(InventoryCheckRequestDTO request) {
        int stock = inventoryService.getStockByProductId(request.getProductId());

        if (stock < 2) {
            log.info("Низкий уровень запасов для продукта ID: {}. Запрос на пополнение.", request.getProductId());
            kafkaTemplate.send("restock-request-topic", new InventoryCheckRequestDTO(request.getProductId()));
        }
    }

    @KafkaListener(topics = "inventory-restock-topic", groupId = "inventory-group")
    public void restockInventory(InventoryRestockRequestDTO request) {
        inventoryService.restockProduct(request.getProductId(), request.getQuantity());
        log.info("Запасы пополнены для продукта ID: {} на {} единиц", request.getProductId(), request.getQuantity());
    }

}

