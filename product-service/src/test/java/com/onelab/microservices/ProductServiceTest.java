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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductFeignInterface productFeignInterface;
    @Mock
    private UserFeignInterface userFeignInterface;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private ProductDTO validProductDTO;
    private Product existingProduct;
    private Category category;
    private final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        category = new Category(1L, "Category1", new ArrayList<>());
        validProductDTO = new ProductDTO(1L, "ProductA", 1000, 10, 1L, LocalDate.of(2024, 9, 10));
        existingProduct = new Product(1L, "ProductA", 1000, 10, category, LocalDate.of(2024, 9, 10), null);
        category.getProducts().add(existingProduct);
    }

    @Test
    void createCategory_whenValid_shouldSaveCategory() {
        mockAdminAccess(true);

        when(categoryRepository.save(any())).thenReturn(category);

        Category result = productService.createCategory(category, AUTH_HEADER);

        assertNotNull(result);
        assertEquals("Category1", result.getCategoryName());
        verify(categoryRepository).save(any());
    }

    @Test
    void createCategory_whenNull_shouldThrowBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.createCategory(null, AUTH_HEADER));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createProduct_whenValid_shouldSaveAndSendKafka() {
        mockAdminAccess(true);
        when(productRepository.findByProductName(validProductDTO.getProductName())).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenReturn(existingProduct);

        ProductDTO result = productService.createProduct(validProductDTO, AUTH_HEADER);

        assertNotNull(result);
        assertEquals("ProductA", result.getProductName());
        verify(productRepository).save(any());
        verify(kafkaProducerService).sendMessage(eq("product-events"), eq("CREATED"), any(InventoryItemDTO.class));
    }

    @Test
    void createProduct_whenAlreadyExists_shouldThrowConflict() {
        mockAdminAccess(true);
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
    }

    @Test
    void getAllProducts_whenCalled_shouldReturnProductList() {
        List<Product> products = List.of(
                new Product(1L, "ProductA", 1000, 5, category, null, null),
                new Product(2L, "ProductB", 1500, 7, category, null, null)
        );
        Map<Long, Integer> stockMap = Map.of(1L, 5, 2L, 7);

        when(productRepository.findAll()).thenReturn(products);
        when(productFeignInterface.getAllStock()).thenReturn(ResponseEntity.ok(stockMap));

        List<ProductDTO> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("ProductA", result.get(0).getProductName());
        assertEquals(5, result.get(0).getQuantity());
    }

    @Test
    void deleteProduct_whenExists_shouldDeleteAndSendKafka() {
        mockAdminAccess(true);
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L, AUTH_HEADER);

        verify(productRepository).deleteById(1L);
        verify(kafkaProducerService).sendMessage(eq("product-events"), eq("DELETE"), anyString());
    }

    @Test
    void deleteProduct_whenNotExists_shouldThrowNotFound() {
        mockAdminAccess(true);
        when(productRepository.existsById(2L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.deleteProduct(2L, AUTH_HEADER));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Продукт не найден", ex.getReason());
    }


    @Test
    void updateProduct_whenValid_shouldUpdateAndSendKafka() {
        mockAdminAccess(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any())).thenReturn(existingProduct);

        ProductDTO updatedDTO = new ProductDTO(1L, "UpdatedProduct", 2000, 20, 1L, LocalDate.of(2025, 9, 10));
        ProductDTO result = productService.updateProduct(1L, updatedDTO, AUTH_HEADER);

        assertNotNull(result);
        assertEquals("UpdatedProduct", result.getProductName());
        verify(productRepository).save(any());
        verify(kafkaProducerService).sendMessage(eq("product-events-update"), eq("UPDATE"), any(InventoryItemDTO.class));
    }

    @Test
    void groupProductsByCategory_whenCalled_shouldGroupAndSort() {
        List<Product> products = List.of(
                new Product(1L, "ProductA", 1000, 5, category, null, null),
                new Product(2L, "ProductB", 1500, 7, category, null, null),
                new Product(3L, "ProductC", 500, 3, category, null, null)
        );

        when(productRepository.findAll()).thenReturn(products);

        Map<String, List<ProductByCategoryDTO>> result = productService.groupProductsByCategory();

        assertTrue(result.containsKey("Category1"));
        assertEquals(3, result.get("Category1").size());
        assertEquals("ProductB", result.get("Category1").get(0).getProductName());
    }


    @ParameterizedTest
    @MethodSource("invalidProducts")
    void createProduct_whenInvalid_shouldThrowBadRequest(ProductDTO invalidDTO, String errorMessage) {
        mockAdminAccess(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> productService.createProduct(invalidDTO, AUTH_HEADER));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(errorMessage, ex.getReason());
    }

    private static Stream<Arguments> invalidProducts() {
        return Stream.of(
                Arguments.of(new ProductDTO(1L, null, 2000, 15, 1L, LocalDate.of(2025, 10, 12)), "Имя продукта не может быть пустым"),
                Arguments.of(new ProductDTO(1L, "UpdatedName", 0, 15, 1L, LocalDate.of(2025, 10, 12)), "Цена должна быть больше 0"),
                Arguments.of(new ProductDTO(1L, "UpdatedName", 1000, -1, 1L, LocalDate.of(2025, 10, 12)), "Количество не может быть отрицательным")
        );
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
    }

    private void mockAdminAccess(boolean isAdmin) {
        when(userFeignInterface.validateUserRole(AUTH_HEADER, "ADMIN")).thenReturn(isAdmin);
    }
}

