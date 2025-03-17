package com.onelab.microservices.service;

import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaLoggingService kafkaLoggingService;

    public ProductService(ProductRepository productRepository, KafkaTemplate<String, Object> kafkaTemplate, KafkaLoggingService kafkaLoggingService) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaLoggingService = kafkaLoggingService;
    }

    @KafkaListener(topics = "product-topic", groupId = "product-group")
    public void consume(ProductDTO productDTO) {
        kafkaLoggingService.log("INFO", "Получен продукт: " + productDTO);

        if (!validateProduct(productDTO)) {
            kafkaLoggingService.log("ERROR", "Валидация не пройдена. Продукт не будет обработан.");
            return;
        }

        try {
            Optional<Product> existingProduct = productRepository.findByName(productDTO.getName());
            if (existingProduct.isPresent()) {
                kafkaLoggingService.log("WARN", "Продукт с таким именем уже существует: " + productDTO.getName());
                return;
            }

            Product product = new Product();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());

            Product savedProduct = productRepository.save(product);
            kafkaLoggingService.log("INFO", "Продукт сохранен: " + savedProduct);

            ProductDTO responseDTO = new ProductDTO(
                    savedProduct.getId(),
                    savedProduct.getName(),
                    savedProduct.getDescription(),
                    savedProduct.getPrice(),
                    productDTO.getQuantity()
            );

            InventoryItemDTO inventoryItemDTO = new InventoryItemDTO(
                    savedProduct.getId(),
                    savedProduct.getName(),
                    productDTO.getQuantity()
            );

            kafkaTemplate.send("product-response-topic", responseDTO);
            kafkaTemplate.send("inventory-topic", inventoryItemDTO);
            kafkaLoggingService.log("INFO", "Сообщения отправлены в Kafka.");

        } catch (Exception e) {
            kafkaLoggingService.log("ERROR", "Ошибка при обработке продукта: " + e.getMessage());
        }
    }

    private boolean validateProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().isBlank()) return false;
        if (productDTO.getPrice() <= 0) return false;
        return productDTO.getQuantity() >= 0;
    }
}

