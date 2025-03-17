package com.onelab.microservices;

import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.InventoryResponseDTO;
import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.repository.InventoryRepository;
import com.onelab.microservices.service.InventoryService;
import com.onelab.microservices.service.KafkaLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaTemplate<String, InventoryResponseDTO> kafkaTemplate;

    @Mock
    private KafkaLoggingService kafkaLoggingService;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryItem = InventoryItem.builder()
                .productId(1L)
                .productName("ProductA")
                .quantity(10).build();
    }

    @Test
    void testCheckAndReserveInventory_whenProductExistsAndAvailable() {
        InventoryRequestDTO requestDTO = new InventoryRequestDTO("ProductA", 5, "Customer1");
        when(inventoryRepository.findByProductName("ProductA")).thenReturn(Optional.of(inventoryItem));

        inventoryService.checkAndReserveInventory(requestDTO);

        verify(kafkaTemplate, times(1)).send(eq("inventory-response-topic"), any(InventoryResponseDTO.class));
        verify(kafkaLoggingService, atLeastOnce()).log(anyString(), anyString());
        verify(inventoryRepository).save(any());
        assertEquals(5, inventoryItem.getQuantity());
    }

    @Test
    void testCheckAndReserveInventory_WhenProductExistsButNotEnoughStock() {
        InventoryRequestDTO request = new InventoryRequestDTO("ProductA", 15, "Customer1");
        when(inventoryRepository.findByProductName("ProductA")).thenReturn(Optional.of(inventoryItem));

        inventoryService.checkAndReserveInventory(request);

        verify(kafkaTemplate, times(1)).send(eq("inventory-response-topic"), any(InventoryResponseDTO.class));
        verify(kafkaLoggingService, atLeastOnce()).log(anyString(), anyString());
        verify(inventoryRepository, never()).save(any());
        assertEquals(10, inventoryItem.getQuantity());
    }

    @Test
    void testCheckAndReserveInventory_WhenProductDoesNotExist() {
        InventoryRequestDTO request = new InventoryRequestDTO("ProductB", 5, "Customer1");
        when(inventoryRepository.findByProductName("ProductB")).thenReturn(Optional.empty());

        inventoryService.checkAndReserveInventory(request);

        verify(kafkaTemplate, times(1)).send(eq("inventory-response-topic"), any(InventoryResponseDTO.class));
        verify(kafkaLoggingService, atLeastOnce()).log(anyString(), anyString());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void testAddInventoryItem() {
        inventoryService.add(inventoryItem);

        verify(inventoryRepository, times(1)).save(inventoryItem);
    }

    @Test
    void testGetStockByProductId_WhenProductExists() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventoryItem));

        int stock = inventoryService.getStockByProductId(1L);
        assertEquals(10, stock);
    }

    @Test
    void testGetStockByProductId_WhenProductDoesNotExist() {
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.empty());

        int stock = inventoryService.getStockByProductId(2L);
        assertEquals(0, stock);
    }

    @Test
    void testRestockProduct_WhenProductExists() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventoryItem));

        inventoryService.restockProduct(1L, 5);

        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
        assertEquals(15, inventoryItem.getQuantity());
    }

    @Test
    void testRestockProduct_WhenProductDoesNotExist() {
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.restockProduct(2L, 5);
        });

        assertEquals("Продукт не найден", exception.getMessage());
    }
}

