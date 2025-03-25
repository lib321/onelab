package com.onelab.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        @Bean
        @ExternalTaskSubscription(
                topicName = "validate-order-topic",
                processDefinitionKey = "Order-process",
                includeExtensionProperties = true
        )
        public ExternalTaskHandler externalTaskHandler() {
            return (externalTask, externalTaskService) -> {
                Map<String, Object> variables = externalTask.getAllVariables();
                log.info("Получены переменные: {}", variables);

                String customerName = (String) variables.get("customerName");
                String itemsJson = (String) variables.get("items");

                List<Map<String, Object>> items;
                try {
                    items = objectMapper.readValue(
                            itemsJson, new TypeReference<>() {
                            });
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e.getMessage());
                }

                boolean isValid = customerName != null && !customerName.isEmpty() &&
                        items != null && !items.isEmpty() &&
                        items.stream().allMatch(item ->
                                item.get("productName") != null &&
                                        !((String) item.get("productName")).isEmpty() &&
                                        (int) item.get("quantity") > 0);

                variables.put("firstItemName", items != null ? items.get(0).get("productName") : null);
                variables.put("firstItemQuantity", items != null ? items.get(0).get("quantity") : null);
                if (!isValid) {
                    externalTaskService.handleBpmnError(externalTask, "InvalidData", "Ошибка валидации заказа", variables);
                } else {
                    externalTaskService.complete(externalTask, variables);
                }
            };
        }
    }
