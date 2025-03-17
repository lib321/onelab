package com.onelab.microservices.service;

import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.InventoryResponseDTO;
import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, InventoryResponseDTO> kafkaTemplate;
    private final KafkaLoggingService kafkaLoggingService;

    public void checkAndReserveInventory(InventoryRequestDTO request) {
        Optional<InventoryItem> itemOpt = inventoryRepository.findByProductName(request.getProductName());
        kafkaLoggingService.log("INFO", "Запрос на проверку наличия товара: " + request);

        if (itemOpt.isPresent()) {
            InventoryItem item = itemOpt.get();
            boolean isAvailable = item.getQuantity() >= request.getQuantity();

            InventoryResponseDTO response = new InventoryResponseDTO(
                    item.getProductId(),
                    item.getProductName(),
                    request.getQuantity(),
                    request.getCustomerName(),
                    isAvailable
            );

            kafkaTemplate.send("inventory-response-topic", response);

            if (isAvailable) {
                item.setQuantity(item.getQuantity() - request.getQuantity());
                inventoryRepository.save(item);
                kafkaLoggingService.log("INFO", "Резервирование товара: " + item);
            } else {
                kafkaLoggingService.log("WARN", "Недостаточно товара: " + item);
            }

        } else {
            InventoryResponseDTO response = new InventoryResponseDTO(
                    null, request.getProductName(), request.getQuantity(), request.getCustomerName(), false
            );
            kafkaTemplate.send("inventory-response-topic", response);
            kafkaLoggingService.log("ERROR", "Товар не найден: " + request.getProductName());
        }
    }

    public void add(InventoryItem item) {
        inventoryRepository.save(item);
        kafkaLoggingService.log("INFO", "Добавлен новый товар в инвентарь: " + item);
    }

    public int getStockByProductId(Long productId) {
        Optional<InventoryItem> item = inventoryRepository.findByProductId(productId);
        int stock = item.map(InventoryItem::getQuantity).orElse(0);
        kafkaLoggingService.log("INFO", "Запрос количества товара ID: " + productId + ", на складе: " + stock);
        return stock;
    }

    public void restockProduct(Long productId, int quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        item.setQuantity(item.getQuantity() + quantity);
        inventoryRepository.save(item);
        kafkaLoggingService.log("INFO", "Пополнены запасы для товара ID: " + productId + " на " + quantity + " единиц");
    }

}

