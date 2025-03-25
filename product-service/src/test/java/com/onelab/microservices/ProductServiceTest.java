package com.onelab.microservices;

import com.onelab.dto.InventoryItemDTO;
import com.onelab.microservices.dto.ProductByCategoryDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.ProductFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Category;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.ProductRepository;
import com.onelab.microservices.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDate;
import java.util.ArrayList;
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
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ProductService productService;

    private ProductDTO validProductDTO;
    private Product existingProduct;
    private Category category;
    private final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        category = new Category(1L, "Category1", new ArrayList<>());
        validProductDTO = new ProductDTO(
                1L, "ProductA", 1000, 10, 1L,
                LocalDate.of(2024, 9, 10));
        existingProduct = new Product(1L, "ProductA", 1000, 10, category,
                LocalDate.of(2024, 9, 10), null);
        category.getProducts().add(existingProduct);
    }

    @Test
    void createProduct_whenValid_shouldSaveAndSendKafka() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.findByProductName(validProductDTO.getProductName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenReturn(existingProduct);
        when(productFeignInterface.addProduct(any())).thenReturn(ResponseEntity.ok().build());

        ProductDTO result = productService.createProduct(validProductDTO, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(validProductDTO.getProductName(), result.getProductName());
        assertEquals(1L, result.getCategoryId());
        verify(productRepository).save(any());
        verify(kafkaProducerService).sendMessage(eq("product-events"), eq("CREATED"), any(InventoryItemDTO.class));
    }

    @Test
    void createCategory_shouldSaveCategory() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(categoryRepository.save(any())).thenReturn(category);

        Optional<Category> result = productService.createCategory(Optional.of(category), AUTH_HEADER);

        assertEquals("Category1", result.get().getCategoryName());
        verify(categoryRepository).save(any());
    }

    @Test
    void createProduct_whenAlreadyExists_shouldThrowConflict() {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(true);
        when(productRepository.findByProductName(validProductDTO.getProductName())).thenReturn(Optional.of(existingProduct));

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

        ProductDTO updated = new ProductDTO(1L, "UpdatedName", 2000, 15, 1L,
                LocalDate.of(2025, 10, 12));
        ProductDTO result = productService.updateProduct(1L, updated, AUTH_HEADER);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getProductName());
        assertNotEquals(validProductDTO.getLocalDate(), result.getLocalDate());
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
        ProductDTO invalidDTO = new ProductDTO(
                1L, null, 2000, 15, 1L,
                LocalDate.of(2025, 10, 12));

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Имя продукта не может быть пустым",
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
    }

    @Test
    void validateProduct_whenPriceIsZero_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(1L, "UpdatedName", 0, 15, 1L,
                LocalDate.of(2025, 10, 12));

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Цена должна быть больше 0",
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
    }

    @Test
    void validateProduct_whenQuantityIsNegative_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(1L, "UpdatedName", 1000, -1, 1L,
                LocalDate.of(2025, 10, 12));

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Количество не может быть отрицательным",
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
    }

    @Test
    void createProduct_whenNoAuthHeader_shouldThrowUnauthorized() {
        assertThrowsResponseStatus(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь",
                () -> productService.createProduct(validProductDTO, null));
    }

    @Test
    void checkAdminAccess_whenUserNotAdmin_shouldThrowForbidden() {
        mockAdminAccess(false);
        assertThrowsResponseStatus(HttpStatus.FORBIDDEN, "Доступ запрещен",
                () -> productService.createProduct(validProductDTO, AUTH_HEADER));
    }

    @Test
    void updateProduct_whenNameIsNull_shouldThrowException() {
        mockAdminAccess(true);
        ProductDTO invalidDTO = new ProductDTO(
                1L, null, 2000, 15, 1L,
                LocalDate.of(2025, 10, 12));

        assertThrowsResponseStatus(HttpStatus.BAD_REQUEST, "Имя продукта не может быть пустым",
                () -> productService.updateProduct(1L, invalidDTO, AUTH_HEADER));
    }

    @Test
    void testGroupProductsByCategory() {
        Category c1 = new Category(1L, "Electronics", new ArrayList<>());
        Category c2 = new Category(2L, "Furniture", new ArrayList<>());

        List<Product> products = List.of(
                new Product(1L, "Laptop", 1000, 5, c1, null , null),
                new Product(2L, "Smartphone", 800, 10, c1, null, null),
                new Product(3L, "Table", 200, 3, c2, null ,null)
        );

        Mockito.when(productRepository.findAll()).thenReturn(products);

        Map<String, List<ProductByCategoryDTO>> result = productService.groupProductsByCategory();

        assertEquals(2, result.size());
        assertTrue(result.containsKey("Electronics"));
        assertTrue(result.containsKey("Furniture"));

        List<ProductByCategoryDTO> electronics = result.get("Electronics");
        assertEquals(2, electronics.size());
        assertEquals("Laptop", electronics.get(0).getProductName());
        assertEquals("Smartphone", electronics.get(1).getProductName());

        List<ProductByCategoryDTO> furniture = result.get("Furniture");
        assertEquals(1, furniture.size());
        assertEquals("Table", furniture.get(0).getProductName());
    }

    @Test
    void getPagedSortedProducts_ShouldReturnPagedProducts() {
        List<Product> products = List.of(
                new Product(1L, "ProductA", 1000, 5, category, null, null),
                new Product(2L, "ProductB", 1500, 7, category, null, null)
        );

        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("price").ascending());

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductByCategoryDTO> result = productService.getPagedSortedProducts(0, 2);

        assertEquals(2, result.getContent().size());
        assertEquals("ProductA", result.getContent().get(0).getProductName());
        assertEquals("ProductB", result.getContent().get(1).getProductName());
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
