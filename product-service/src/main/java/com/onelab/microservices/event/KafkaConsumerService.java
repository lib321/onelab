package com.onelab.microservices.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.dto.KafkaMessageDTO;
import com.onelab.dto.UpdateQuantityDTO;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ProductRepository productRepository;

    @KafkaListener(topics = "product-quantity-update", groupId = "inventory-group")
    public void consumeProductEventUpdate(KafkaMessageDTO message) {
        log.info("Получено Kafka сообщение: action='{}', data={}", message.getAction(), message.getData());
        UpdateQuantityDTO quantityDTO = convertFromInventoryToProduct(message.getData());
        Optional<Product> product = productRepository.findById(quantityDTO.getProductId());
        product.ifPresent(value -> value.setQuantity(quantityDTO.getQuantity()));
        productRepository.save(product.get());
    }

    private UpdateQuantityDTO convertFromInventoryToProduct(Object data) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(data, UpdateQuantityDTO.class);
    }
}
