package com.onelab.microservices;

import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.InventoryResponseDTO;
import com.onelab.microservices.dto.InventoryUpdateDTO;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.repository.InventoryRepository;
import com.onelab.microservices.service.InventoryFilter;
import com.onelab.microservices.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private InventoryFilter filter;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItemDTO itemDTO;
    private InventoryItem item;
    private List<InventoryItem> items;

    @BeforeEach
    void setUp() {
        itemDTO = new InventoryItemDTO(
                1L, "ProductA", 500, 10, "CategoryA",
                LocalDate.of(2024, 9, 25),
                LocalDate.of(2024, 9, 25));
        item = new InventoryItem(
                null, 1L, "ProductA", 500, 10, "CategoryA",
                LocalDate.of(2024, 9, 25),
                LocalDate.of(2024, 9, 25));

        items = List.of(
                new InventoryItem(null, 1L, "P1", 100, 3, "C1",
                        null, null),
                new InventoryItem(null, 2L, "P2", 80, 2, "C2",
                        null, null),
                new InventoryItem(null, 3L, "P3", 150, 4, "C2",
                        null, null),
                new InventoryItem(null, 4L, "P4", 130, 1, "C1",
                        null, null),
                new InventoryItem(null, 5L, "P5", 90, 7, "C3",
                        null, null)
        );
    }

    @Test
    void createInventoryItem_whenValid_shouldSave() {
        when(inventoryRepository.existsByProductId(item.getProductId())).thenReturn(false);
        when(inventoryRepository.save(any())).thenReturn(item);

        InventoryItemDTO result = inventoryService.add(itemDTO);

        assertNotNull(result);
        assertEquals(itemDTO.getProductName(), result.getProductName());
        assertEquals(itemDTO.getAddedAt(), result.getAddedAt());
    }

    @Test
    void createInventoryItem_whenInvalid_shouldNotSave() {
        when(inventoryRepository.existsByProductId(item.getProductId())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> inventoryService.add(itemDTO));

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void checkAndReserveInventory_whenValid_shouldReserve() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO(
                "ProductA", 3, "customer1");

        when(inventoryRepository.findByProductName(requestDTO.getProductName())).thenReturn(Optional.of(item));

        InventoryResponseDTO responseDTO = inventoryService.checkAndReserveInventory(requestDTO);

        assertTrue(responseDTO.isAvailable());
        assertEquals(7, item.getQuantity());
        verify(kafkaProducerService).sendMessage("order-events", "CONFIRMED", requestDTO);
    }

    @Test
    void checkAndReserveInventory_whenInValid_shouldNotReserve() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO(
                "ProductA", 11, "customer1");

        when(inventoryRepository.findByProductName(requestDTO.getProductName())).thenReturn(Optional.of(item));

        InventoryResponseDTO responseDTO = inventoryService.checkAndReserveInventory(requestDTO);

        assertFalse(responseDTO.isAvailable());
        verify(kafkaProducerService).sendMessage("order-events", "NOT_ENOUGH", requestDTO);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void getStockByProductId_whenValid_shouldReturnInt() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));
        int stock = inventoryService.getStockByProductId(item.getProductId());

        assertEquals(10, stock);
    }

    @Test
    void getAllStock_shouldReturnMap() {
        InventoryItem item2 = new InventoryItem(
                null, 2L, "ProductB", 550, 8, "CategoryA",
                LocalDate.of(2024, 5, 24),
                LocalDate.of(2024, 5, 24));

        List<InventoryItem> items = List.of(item, item2);
        when(inventoryRepository.findAll()).thenReturn(items);

        Map<Long, Integer> stockMap = inventoryService.getAllStock();

        assertNotNull(stockMap);
        assertEquals(2, stockMap.size());
        assertEquals(10, stockMap.get(1L));
        assertEquals(8, stockMap.get(2L));
    }

    @Test
    void restockProduct_whenValid_shouldReturnUpdatedItem() {
        InventoryItemDTO updatedDTO = new InventoryItemDTO(
                1L, "Update", 500, 2, "CategoryA",
                LocalDate.of(2024, 10, 24),
                LocalDate.of(2024, 10, 28));

        when(inventoryRepository.findByProductId(updatedDTO.getProductId())).thenReturn(Optional.of(item));

        InventoryItemDTO result = inventoryService.restockProduct(updatedDTO);

        assertNotNull(result);
        assertEquals("Update", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertNotEquals(item.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void isStockLow_shouldReturnFalse() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));

        boolean isLow = inventoryService.isStockLow(item.getProductId());
        assertFalse(isLow);
    }

    @Test
    void getAllItems_shouldReturnNotEmptyList() {
        InventoryItem item2 = new InventoryItem(
                null, 2L, "ProductB", 550, 8, "CategoryA",
                LocalDate.of(2024, 5, 24),
                LocalDate.of(2024, 5, 24));
        List<InventoryItem> items = List.of(item, item2);
        when(inventoryRepository.findAll()).thenReturn(items);

        List<InventoryItemDTO> result = inventoryService.getAllItems();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getQuantity());
        assertEquals(8, result.get(1).getQuantity());
    }

    @Test
    void deleteProduct_whenExist_shouldDelete() {
        when(inventoryRepository.existsByProductId(1L)).thenReturn(true);
        inventoryService.deleteProduct(1L);
        verify(inventoryRepository).deleteByProductId(1L);
    }

    @Test
    void deleteProduct_whenNotExist_shouldNotDelete() {
        when(inventoryRepository.existsByProductId(2L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> inventoryService.deleteProduct(2L));
        verify(inventoryRepository, never()).deleteById(any());
    }

    @Test
    void checkStock_ShouldReturnStockAvailability() {
        InventoryItem item2 = new InventoryItem(
                null, 2L, "ProductB", 550, 5, "CategoryA",
                LocalDate.of(2024, 5, 24),
                LocalDate.of(2024, 5, 24));

        List<InventoryItemDTO> requestItems = List.of(
                new InventoryItemDTO(1L, "ProductA", 550, 8, "CategoryA",
                        LocalDate.of(2024, 5, 24),
                        LocalDate.of(2024, 5, 24)),
                new InventoryItemDTO(2L, "ProductB", 600, 6, "CategoryA",
                        LocalDate.of(2024, 5, 24),
                        LocalDate.of(2024, 5, 24)),
                new InventoryItemDTO(3L, "ProductC", 450, 4, "CategoryA",
                        LocalDate.of(2024, 5, 24),
                        LocalDate.of(2024, 5, 24))
        );

        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.of(item2));
        when(inventoryRepository.findByProductId(3L)).thenReturn(Optional.empty());

        Map<Long, Boolean> stockMap = inventoryService.checkStock(requestItems);

        assertNotNull(stockMap);
        assertEquals(3, stockMap.size());
        assertTrue(stockMap.get(1L));
        assertFalse(stockMap.get(2L));
        assertFalse(stockMap.get(3L));
    }

    @Test
    void updateInventory_shouldUpdateStock() {
        InventoryUpdateDTO updateItemStock = new InventoryUpdateDTO(1L, 2, 4);
        when(inventoryRepository.findByProductId(updateItemStock.getProductId())).thenReturn(Optional.of(item));
        inventoryService.updateInventory(updateItemStock);

        assertEquals(8, item.getQuantity());
    }

    @Test
    void filterWithLambda_shouldReturnSortedItemsByPrice() {
        when(inventoryRepository.findAll()).thenReturn(items);
        when(filter.filter(any())).thenReturn(true);

        List<InventoryItem> result = inventoryService.filterWithLambda(filter);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(80, result.get(0).getPrice());
        assertEquals(90, result.get(1).getPrice());
        assertEquals(100, result.get(2).getPrice());
        assertEquals(130, result.get(3).getPrice());
        assertEquals(150, result.get(4).getPrice());
    }

    @Test
    void getProductNamesByPriceRange_shouldReturnProductNamesByPriceBetween() {
        List<InventoryItem> rangePriceItems = List.of(
                new InventoryItem(
                        null, 5L, "P5", 90, 7, "C3", null, null
                ),
                new InventoryItem(
                        null, 1L, "P1", 100, 3, "C1", null, null
                ),
                new InventoryItem(
                        null, 4L, "P4", 130, 1, "C1", null, null
                )
        );
        when(inventoryRepository.findByPriceBetween(90, 130)).thenReturn(rangePriceItems);
        List<String> result = inventoryService.getProductNamesByPriceRange(90, 130);
        assertNotNull(result);
        assertEquals(result.get(0), "P5 - " + rangePriceItems.get(0).getPrice());
        assertEquals(result.get(1), "P1 - " + rangePriceItems.get(1).getPrice());
        assertEquals(result.get(2), "P4 - " + rangePriceItems.get(2).getPrice());
    }

    @Test
    void getProductNamesByCategory_shouldReturnListOfProductNamesByCategory() {
        List<InventoryItem> productNamesByCategory = List.of(
                new InventoryItem(
                        null, 2L, "P2", 2, 80, "C2", null, null
                ),
                new InventoryItem(
                        null, 3L, "P3", 4, 150, "C2", null, null
                )
        );
        when(inventoryRepository.findByCategoryName("C2")).thenReturn(productNamesByCategory);
        List<String> result = inventoryService.getProductNamesByCategory("C2");
        assertNotNull(result);
        assertEquals("P2", productNamesByCategory.get(0).getProductName());
        assertEquals("P3", productNamesByCategory.get(1).getProductName());
        assertEquals("C2", productNamesByCategory.get(0).getCategoryName());
        assertEquals("C2", productNamesByCategory.get(1).getCategoryName());
    }

    @Test
    void getTotalValue_shouldReturnTotalPriceOfAllProducts() {
        when(inventoryRepository.findAll()).thenReturn(items);
        int total = inventoryService.getTotalInventoryValue();
        assertEquals(total, 550);
    }

    @Test
    void groupByCategory_shouldReturnItemsGroupedByCategory() {
        when(inventoryRepository.findAll()).thenReturn(items);
        Map<String, List<InventoryItem>> result = inventoryService.groupByCategory();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("C1"));
        assertTrue(result.containsKey("C2"));
        assertTrue(result.containsKey("C3"));
        assertEquals(2, result.get("C1").size());
        assertEquals(2, result.get("C2").size());
        assertEquals(1, result.get("C3").size());
    }

    @Test
    void partitionByPrice_shouldReturnItemsGroupedByPrice() {
        when(inventoryRepository.findAll()).thenReturn(items);
        Map<Boolean, List<InventoryItem>> result = inventoryService.partitionByPrice(90);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(3, result.get(true).size());
        assertEquals(2, result.get(false).size());

        assertTrue(result.get(true).stream().allMatch(item -> item.getPrice() > 90));
        assertTrue(result.get(false).stream().allMatch(item -> item.getPrice() <= 90));
    }
}
