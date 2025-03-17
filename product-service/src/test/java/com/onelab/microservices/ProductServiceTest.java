package com.onelab.microservices;

import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.ProductFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.ProductRepository;
import com.onelab.microservices.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductFeignInterface productFeignInterface;
    @Mock private UserFeignInterface userFeignInterface;
    @Mock private KafkaProducerService kafkaProducerService;

    @InjectMocks private ProductService productService;

    private ProductDTO validProductDTO;
    private Product existingProduct;
    private final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        validProductDTO = new ProductDTO(1L, "ProductA", "Description", 100.0, 10);
        existingProduct = new Product(1L, "ProductA", "Description", 100.0, 10);
    }

    @Test
    void createProduct_whenValid_shouldSaveAndSendKafka() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.findByName(validProductDTO.getProductName())).thenReturn(Optional.empty());
        when(productRepository.save(any())).thenReturn(existingProduct);
        when(productFeignInterface.addProduct(any())).thenReturn(ResponseEntity.ok().build());

        ProductDTO result = productService.createProduct(validProductDTO, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(validProductDTO.getProductName(), result.getProductName());
        verify(productRepository).save(any());
        verify(kafkaProducerService).sendMessage(eq("product-events"), eq("CREATED"), any(InventoryItemDTO.class));
    }

    @Test
    void createProduct_whenAlreadyExists_shouldThrowConflict() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.findByName(validProductDTO.getProductName())).thenReturn(Optional.of(existingProduct));

        assertThrows(ResponseStatusException.class, () -> productService.createProduct(validProductDTO, AUTH_HEADER));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_whenExists_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productFeignInterface.getStock(1L)).thenReturn(ResponseEntity.ok(10));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(10, result.getQuantity());
        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_whenValid_shouldUpdateAndSendKafka() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any())).thenReturn(existingProduct);
        when(productFeignInterface.restockProduct(any())).thenReturn(ResponseEntity.ok().build());

        ProductDTO updated = new ProductDTO(1L, "UpdatedName", "UpdatedDescription", 200.0, 15);
        ProductDTO result = productService.updateProduct(1L, updated, AUTH_HEADER);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getProductName());
        verify(kafkaProducerService).sendMessage(eq("product-events-update"), eq("UPDATE"), any(InventoryItemDTO.class));
    }

    @Test
    void deleteProduct_whenExists_shouldDeleteAndSendKafka() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productFeignInterface.deleteProduct(1L)).thenReturn(ResponseEntity.ok().build());

        productService.deleteProduct(1L, AUTH_HEADER);

        verify(productRepository).deleteById(1L);
        verify(kafkaProducerService).sendMessage(eq("product-events"), eq("DELETE"), anyString());
    }

    @Test
    void deleteProduct_whenNotExists_shouldThrowNotFound() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> productService.deleteProduct(1L, AUTH_HEADER));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void getAllProducts_whenValid_shouldReturnProductList() {
        Map<Long, Integer> stockMap = Map.of(1L, 5);
        when(productFeignInterface.getAllStock()).thenReturn(ResponseEntity.ok(stockMap));
        when(productRepository.findAll()).thenReturn(List.of(existingProduct));

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getQuantity());
    }

    @Test
    void createProduct_whenNameIsNull_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(null, null, "Desc", 10.0, 5);

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Имя продукта не может быть пустым",
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
    }

    @Test
    void validateProduct_whenPriceIsZero_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(null, "Product", "Desc", 0.0, 5);

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Цена должна быть больше 0",
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
    }

    @Test
    void validateProduct_whenQuantityIsNegative_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(null, "Product", "Desc", 10.0, -1);

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Количество не может быть отрицательным",
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
    }

    @Test
    void createProduct_whenNoAuthHeader_shouldThrowUnauthorized() {
        ProductDTO validProductDTO = new ProductDTO(null, "Product", "Desc", 10.0, 5);

        assertThrowsResponseStatus(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь",
                () -> productService.createProduct(validProductDTO, null));
    }

    @Test
    void checkAdminAccess_whenUserNotAdmin_shouldThrowForbidden() {
        mockAdminAccess(false);
        ProductDTO validProductDTO = new ProductDTO(null, "Product", "Desc", 10.0, 5);

        assertThrowsResponseStatus(HttpStatus.FORBIDDEN, "Доступ запрещен",
                () -> productService.createProduct(validProductDTO, AUTH_HEADER));
    }

    @Test
    void updateProduct_whenNameIsNull_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(1L, null, "Desc", 10.0, 5);

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Имя продукта не может быть пустым",
                () -> productService.updateProduct(1L, invalidDTO, AUTH_HEADER));
    }

    private void mockAdminAccess(boolean isAdmin) {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(isAdmin);
    }

    private void assertThrowsResponseStatus(HttpStatus status, String message, Executable executable) {
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, executable);
        assertEquals(status, thrown.getStatusCode());
        assertEquals(message, thrown.getReason());
    }
}
