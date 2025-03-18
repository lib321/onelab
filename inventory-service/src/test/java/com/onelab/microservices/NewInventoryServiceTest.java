package com.onelab.microservices;

import com.onelab.microservices.model.Category;
import com.onelab.microservices.model.NewInventoryItem;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.NewInventoryRepository;
import com.onelab.microservices.service.InventoryFilter;
import com.onelab.microservices.service.NewInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewInventoryServiceTest {

    @Mock
    private NewInventoryRepository inventoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private NewInventoryService inventoryService;

    @Mock
    private InventoryFilter filter;
    private List<NewInventoryItem> items;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category(null, "C1");

        items = List.of(
                new NewInventoryItem(
                        null, 1L, "P1", 3, "C1", 100, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 2L, "P2", 2, "C2", 80, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 3L, "P3", 4, "C2", 150, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 4L, "P4", 1, "C1", 130, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 5L, "P5", 7, "C3", 90, LocalDate.now()
                )
        );
    }

    @Test
    void filterWithLambda_shouldReturnSortedItemsByPrice() {
        when(inventoryRepository.findAll()).thenReturn(items);
        when(filter.filter(any())).thenReturn(true);

        List<NewInventoryItem> result = inventoryService.filterWithLambda(filter);

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
        List<NewInventoryItem> rangePriceItems = List.of(
                new NewInventoryItem(
                        null, 5L, "P5", 7, "C3", 90, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 1L, "P1", 3, "C1", 100, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 4L, "P4", 1, "C1", 130, LocalDate.now()
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
        List<NewInventoryItem> productNamesByCategory = List.of(
                new NewInventoryItem(
                        null, 2L, "P2", 2, "C2", 80, LocalDate.now()
                ),
                new NewInventoryItem(
                        null, 3L, "P3", 4, "C2", 150, LocalDate.now()
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
        Map<String, List<NewInventoryItem>> result = inventoryService.groupByCategory();

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
        Map<Boolean, List<NewInventoryItem>> result = inventoryService.partitionByPrice(90);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(3, result.get(true).size());
        assertEquals(2, result.get(false).size());

        assertTrue(result.get(true).stream().allMatch(item -> item.getPrice() > 90));
        assertTrue(result.get(false).stream().allMatch(item -> item.getPrice() <= 90));
    }

    @Test
    void addCategory_shouldSaveCategory() {
        Optional<Category> categoryOptional = Optional.of(new Category(null, "C1"));

        when(categoryRepository.save(any())).thenReturn(category);
        Optional<Category> savedCategory = inventoryService.addCategory(categoryOptional);
        assertNotNull(savedCategory);
        assertEquals("C1", savedCategory.get().getName());
    }

    @Test
    void addItem_whenCategoryExist_shouldSaveItem() {
        NewInventoryItem item = new NewInventoryItem(
                null, 1L, "P1", 3, "C1", 100, LocalDate.now()
        );
        Optional<Category> categoryOptional = Optional.of(new Category(null, "C1"));
        Optional<NewInventoryItem> optionalItem = Optional.of(new NewInventoryItem(
                null, 1L, "P1", 3, "C1", 100, LocalDate.now()
        ));

        when(categoryRepository.findCategoryByName("C1")).thenReturn(categoryOptional);
        when(inventoryRepository.save(any())).thenReturn(item);

        Optional<NewInventoryItem> savedItem = inventoryService.addItem(optionalItem);
        assertNotNull(savedItem);
        assertEquals("P1", savedItem.get().getProductName());
        assertEquals("C1", savedItem.get().getCategoryName());
        assertEquals(100, savedItem.get().getPrice());
        assertEquals(3, savedItem.get().getQuantity());
    }

    @Test
    void addItem_whenCategoryDoesNotExist_shouldCreateAndSaveCategory() {
        NewInventoryItem item = new NewInventoryItem(
                null, 10L, "P10", 3, "C10", 120, LocalDate.now()
        );
        Optional<NewInventoryItem> optionalItem = Optional.of(item);

        when(categoryRepository.findCategoryByName("C10")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenReturn(new Category(null, "C10"));
        when(inventoryRepository.save(any())).thenReturn(item);

        Optional<NewInventoryItem> savedItem = inventoryService.addItem(optionalItem);

        assertNotNull(savedItem);
        assertEquals("P10", savedItem.get().getProductName());
        assertEquals("C10", savedItem.get().getCategoryName());
        assertEquals(120, savedItem.get().getPrice());
        assertEquals(3, savedItem.get().getQuantity());
        verify(categoryRepository).save(any(Category.class));
    }

}
