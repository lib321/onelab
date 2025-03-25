package com.onelab.microservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.dto.*;
import com.onelab.microservices.dto.ItemDTO;
import com.onelab.microservices.service.InventoryService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryControllerTest {

    @Container
    private static final ElasticsearchContainer elasticsearchContainer = new InventoryElasticSearchContainer();

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryItemDTO itemDTO;
    private List<ItemDTO> itemDTOs;


    @BeforeAll
    static void setUp() {
        elasticsearchContainer.start();
    }

    @BeforeEach
    void testIsContainerRunning() {
        assertTrue(elasticsearchContainer.isRunning());
        itemDTO = new InventoryItemDTO(
                1L, "ProductA", 500, 10, "CategoryA",
                LocalDate.of(2024, 9, 25),
                LocalDate.of(2024, 9, 25));

        itemDTOs = List.of(
                new ItemDTO("ASUS ROG Zephyrus G14", 179990, 5),
                new ItemDTO("Lenovo Legion 5", 149990, 3)
        );
    }

    @Test
    void addProductTest() throws Exception {
        Mockito.when(inventoryService.add(itemDTO)).thenReturn(itemDTO);

        mockMvc.perform(post("/api/inventory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.productName").value("ProductA"));
    }

    @Test
    void getStockTest() throws Exception {
        Mockito.when(inventoryService.getStockByProductId(1L)).thenReturn(10);

        mockMvc.perform(get("/api/inventory/stock/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void reserveProductTest() throws Exception {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO(
                "ProductA", 5, "CustomerA");
        InventoryResponseDTO responseDTO = new InventoryResponseDTO(
                1L, "ProductA", 5, "CustomerA", true
        );
        Mockito.when(inventoryService.checkAndReserveInventory(requestDTO)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(responseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.productName").value("ProductA"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.customerName").value("CustomerA"));
    }

    @Test
    void restockProductTest() throws Exception {
        InventoryItemDTO updatedDTO = new InventoryItemDTO(
                1L, "Update", 500, 6, "CategoryA",
                LocalDate.of(2024, 9, 30),
                LocalDate.of(2024, 10, 2));
        Mockito.when(inventoryService.restockProduct(Mockito.any())).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/inventory/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.productName").value("Update"))
                .andExpect(jsonPath("$.quantity").value(6))
                .andExpect(jsonPath("$.updatedAt").value("2024-10-02"));
    }

    @Test
    void checkStockProductTest() throws Exception {
        Mockito.when(inventoryService.isStockLow(1L)).thenReturn(false);

        mockMvc.perform(get("/api/inventory/check-stock/{productId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getAllProductsTest() throws Exception {
        InventoryItemDTO itemDTO1 = new InventoryItemDTO(
                2L, "ProductB", 800, 8, "CategoryB",
                LocalDate.of(2024, 9, 30),
                LocalDate.of(2024, 9, 30));
        List<InventoryItemDTO> items = List.of(itemDTO, itemDTO1);

        Mockito.when(inventoryService.getAllItems()).thenReturn(items);
        mockMvc.perform(get("/api/inventory/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void deleteProductTest() throws Exception {
        mockMvc.perform(delete("/api/inventory/delete/{productId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllStockTest() throws Exception {
        Map<Long, Integer> allStocks = new HashMap<>();
        allStocks.put(1L, 10);
        allStocks.put(2L, 8);
        Mockito.when(inventoryService.getAllStock()).thenReturn(allStocks);

        mockMvc.perform(get("/api/inventory/all-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.['1']").value(10))
                .andExpect(jsonPath("$.['2']").value(8));
    }

    @Test
    void checkStockAvailabilityTest() throws Exception {
        InventoryItemDTO itemDTO1 = new InventoryItemDTO(
                2L, "ProductB", 800, 8, "CategoryB",
                LocalDate.of(2024, 9, 30),
                LocalDate.of(2024, 9, 30));
        List<InventoryItemDTO> items = List.of(itemDTO, itemDTO1);

        Map<Long, Boolean> stockMap = new HashMap<>();
        stockMap.put(1L, true);
        stockMap.put(2L, false);

        Mockito.when(inventoryService.checkStock(items)).thenReturn(stockMap);

        mockMvc.perform(post("/api/inventory/check-stock-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['1']").value(true))
                .andExpect(jsonPath("$.['2']").value(false))
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getFilteredItems_shouldReturnFilteredItems() throws Exception {
        when(inventoryService.getItemsByCategoryAndPriceRange("Ноутбуки", 100000, 200000)).thenReturn(itemDTOs);

        mockMvc.perform(get("/api/inventory/filter")
                        .param("category", "Ноутбуки")
                        .param("minPrice", "100000")
                        .param("maxPrice", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].productName").value("ASUS ROG Zephyrus G14"))
                .andExpect(jsonPath("$[1].productName").value("Lenovo Legion 5"));
    }

    @Test
    void searchByProductName_shouldReturnMatchingItems() throws Exception {
        when(inventoryService.searchByProductName("ASUS")).thenReturn(List.of(itemDTOs.get(0)));

        mockMvc.perform(get("/api/inventory/search")
                        .param("keyword", "ASUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].productName").value("ASUS ROG Zephyrus G14"));
    }

    @AfterAll
    static void destroy() {
        elasticsearchContainer.stop();
    }
}
