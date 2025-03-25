package com.onelab.microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.microservices.dto.ProductByCategoryDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.ProductFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Category;
import com.onelab.microservices.service.ProductService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");


    @MockitoBean
    private UserFeignInterface userFeignInterface;

    @MockitoBean
    private ProductFeignInterface productFeignInterface;

    @MockitoBean
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;
    private Category category;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        when(userFeignInterface.validateUserRole(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        productDTO = new ProductDTO(
                1L, "ProductA", 1000, 10, 1L,
                LocalDate.of(2024, 9, 10));
        category = new Category(1L, "CategoryA", new ArrayList<>());
    }

    @Test
    void saveProductTest() throws Exception {
        when(productService.createProduct(Mockito.any(), Mockito.anyString())).thenReturn(productDTO);

        mockMvc.perform(post("/api/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.productName").value("ProductA"));
    }

    @Test
    void saveCategory() throws Exception {
        when(productService.createCategory(Mockito.any(), Mockito.anyString())).thenReturn(Optional.of(category));

        mockMvc.perform(post("/api/products/add/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.categoryName").value("CategoryA"));
    }

    @Test
    void getProductByIdTest() throws Exception {
        when(productService.getProductById(Mockito.anyLong())).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/get/{id}", productDTO.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.productName").value("ProductA"));
    }

    @Test
    void getAllProductsTest() throws Exception {
        List<ProductDTO> productDTOList = List.of(
                productDTO,
                new ProductDTO(
                        2L, "ProductB", 2000, 5, 2L,
                        LocalDate.of(2025, 10, 6))
        );

        when(productService.getAllProducts()).thenReturn(productDTOList);

        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(productDTOList.size()));
    }

    @Test
    void updateProductTest() throws Exception {
        productDTO.setProductName("updatedName");
        productDTO.setQuantity(8);

        when(productService.updateProduct(Mockito.anyLong(), Mockito.any(), Mockito.anyString()))
                .thenReturn(productDTO);

        mockMvc.perform(put("/api/products/update/{id}", productDTO.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value(productDTO.getProductName()))
                .andExpect(jsonPath("$.quantity").value(productDTO.getQuantity()));
    }

    @Test
    void deleteProductTest() throws Exception {
        mockMvc.perform(delete("/api/products/delete/{id}", productDTO.getProductId())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getProductsGroupedByCategoryTest() throws Exception {
        Map<String, List<ProductByCategoryDTO>> groupedProducts = Map.of(
                "Electronics", List.of(
                        new ProductByCategoryDTO(1L, "Laptop", 1000, 5, "Electronics"),
                        new ProductByCategoryDTO(2L, "Smartphone", 800, 10, "Electronics")
                ),
                "Furniture", List.of(
                        new ProductByCategoryDTO(3L, "Table", 200, 3, "Furniture")
                )
        );

        when(productService.groupProductsByCategory()).thenReturn(groupedProducts);

        mockMvc.perform(get("/api/products/grouped-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Electronics.size()").value(2))
                .andExpect(jsonPath("$.Electronics[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.Electronics[1].productName").value("Smartphone"))
                .andExpect(jsonPath("$.Furniture.size()").value(1))
                .andExpect(jsonPath("$.Furniture[0].productName").value("Table"));
    }

    @Test
    void getPagedProducts_ShouldReturnPagedProducts() throws Exception {
        List<ProductByCategoryDTO> products = List.of(
                new ProductByCategoryDTO(1L, "ProductA", 1000, 5, category.getCategoryName()),
                new ProductByCategoryDTO(2L, "ProductB", 1500, 7, category.getCategoryName())
        );

        Page<ProductByCategoryDTO> productPage = new PageImpl<>(products);
        when(productService.getPagedSortedProducts(0, 2)).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("ProductA"))
                .andExpect(jsonPath("$.content[1].productName").value("ProductB"));
    }

}
