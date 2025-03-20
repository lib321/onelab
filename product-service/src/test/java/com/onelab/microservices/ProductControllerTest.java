package com.onelab.microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Mockito.when(userFeignInterface.validateUserRole(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        productDTO = new ProductDTO(
                1L, "ProductA", 1000, 10, 1L,
                LocalDate.of(2024, 9, 10));
        category = new Category(1L, "CategoryA", new ArrayList<>());
    }

    @Test
    void saveProductTest() throws Exception {
        Mockito.when(productService.createProduct(Mockito.any(), Mockito.anyString())).thenReturn(productDTO);

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
        Mockito.when(productService.createCategory(Mockito.any(), Mockito.anyString())).thenReturn(Optional.of(category));

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
        Mockito.when(productService.getProductById(Mockito.anyLong())).thenReturn(productDTO);

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

        Mockito.when(productService.getAllProducts()).thenReturn(productDTOList);

        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(productDTOList.size()));
    }

    @Test
    void updateProductTest() throws Exception {
        productDTO.setProductName("updatedName");
        productDTO.setQuantity(8);

        Mockito.when(productService.updateProduct(Mockito.anyLong(), Mockito.any(), Mockito.anyString()))
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
}
