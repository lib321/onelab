package com.onelab.microservices.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.microservices.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class Config {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final InventoryRepository inventoryRepository;

    public Config(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Bean
    @ExternalTaskSubscription(
            topicName = "check-available-topic",
            processDefinitionKey = "Order-process",
            includeExtensionProperties = true
    )
    public ExternalTaskHandler externalTaskHandler() {
        return (externalTask, externalTaskService) -> {
            try {
                Map<String, Object> variables = externalTask.getAllVariables();
                log.info("Получены переменные: {}", variables);

                String itemsJson = (String) variables.get("items");
                List<Map<String, Object>> items = objectMapper.readValue(itemsJson, new TypeReference<>() {
                });

                boolean allAvailable = items.stream().allMatch(item -> {
                    String productName = (String) item.get("productName");
                    int quantity = (int) item.get("quantity");

                    return inventoryRepository.findByProductName(productName)
                            .map(inventoryItem -> inventoryItem.getQuantity() >= quantity)
                            .orElse(false);
                });

                if (allAvailable) {
                    externalTaskService.complete(externalTask);
                } else {
                    externalTaskService.handleBpmnError(externalTask, "OutOfStock", "Недостаточно товара на складе");
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage());
            }
        };
    }
}

