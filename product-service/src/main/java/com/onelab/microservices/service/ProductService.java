package com.onelab.microservices.service;

import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.ProductFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.ProductRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductFeignInterface productFeignInterface;
    private final UserFeignInterface userFeignInterface;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, String authHeader) {
        checkAdminAccess(authHeader);

        try {
            validateProduct(productDTO);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Optional<Product> existingProduct = productRepository.findByName(productDTO.getProductName());
        if (existingProduct.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Продукт с таким именем уже существует");
        }

        Product product = new Product();
        product.setName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());

        Product savedProduct = productRepository.save(product);

        ProductDTO responseDTO = new ProductDTO(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getQuantity()
        );

        InventoryItemDTO inventoryItemDTO = new InventoryItemDTO(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getQuantity()
        );

        try {
            productFeignInterface.addProduct(inventoryItemDTO);
        } catch (Exception e) {
            productRepository.delete(savedProduct);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка при добавлении товара в inventory-service");
        }

        kafkaProducerService.sendMessage("product-events", "CREATED", inventoryItemDTO);
        return responseDTO;
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));

        int stock = productFeignInterface.getStock(id).getBody();
        if (product.getQuantity() != stock) {
            product.setQuantity(stock);
            productRepository.save(product);
        }
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getPrice(), stock);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        Map<Long, Integer> stockMap = productFeignInterface.getAllStock().getBody();

        return productRepository.findAll().stream()
                .map(product -> {
                    int stock = stockMap.getOrDefault(product.getId(), 0);
                    return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getPrice(), stock);
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, String authHeader) {
        checkAdminAccess(authHeader);

        try {
            validateProduct(productDTO);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));

        int oldQuantity = product.getQuantity();

        product.setName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());

        Product updatedProduct = productRepository.save(product);

        InventoryItemDTO inventoryItemDTO = new InventoryItemDTO(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getQuantity()
        );

        try {
            productFeignInterface.restockProduct(inventoryItemDTO);
        } catch (Exception e) {
            product.setQuantity(oldQuantity);
            productRepository.save(product);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка при обновлении товара в inventory-service");
        }

        kafkaProducerService.sendMessage("product-events-update", "UPDATE", inventoryItemDTO);

        return new ProductDTO(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getQuantity());
    }

    @Transactional
    public void deleteProduct(Long id, String authHeader) {
        checkAdminAccess(authHeader);

        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден");
        }

        try {
            productFeignInterface.deleteProduct(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка при удалении товара из inventory-service");
        }
        kafkaProducerService.sendMessage("product-events", "DELETE", "Product ID: " + id);

        productRepository.deleteById(id);
    }

    private void validateProduct(ProductDTO productDTO) {
        if (productDTO.getProductName() == null || productDTO.getProductName().isBlank()) {
            throw new IllegalArgumentException("Имя продукта не может быть пустым");
        }
        if (productDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше 0");
        }
        if (productDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
    }

    private void checkAdminAccess(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }

        try {
            Boolean isAdmin = userFeignInterface.validateUserRole(authHeader, "ADMIN");
            if (Boolean.FALSE.equals(isAdmin)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
            }
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }
    }
}

