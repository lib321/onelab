package com.onelab.microservices;

import com.onelab.microservices.dto.InventoryItemDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.ProductRepository;
import com.onelab.microservices.service.KafkaLoggingService;
import com.onelab.microservices.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private KafkaLoggingService kafkaLoggingService;

    @InjectMocks
    private ProductService productService;

    private ProductDTO validProductDTO;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        validProductDTO = new ProductDTO(1L, "ProductA", "Description", 100.0, 10);
        existingProduct = new Product(1L, "ProductA", "Description", 100.0);
    }

    @Test
    void testConsume_whenProductIsValidAndDoesNotExist_shouldSaveAndSendKafkaMessages() {
        when(productRepository.findByName(validProductDTO.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        productService.consume(validProductDTO);

        verify(productRepository).save(any(Product.class));
        verify(kafkaTemplate).send(eq("product-response-topic"), any(ProductDTO.class));
        verify(kafkaTemplate).send(eq("inventory-topic"), any(InventoryItemDTO.class));
        verify(kafkaLoggingService, atLeastOnce()).log(anyString(), anyString());
    }

    @Test
    void testConsume_whenProductAlreadyExists_shouldNotSaveOrSendKafkaMessages() {
        when(productRepository.findByName(validProductDTO.getName())).thenReturn(Optional.of(existingProduct));

        productService.consume(validProductDTO);

        verify(productRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void testConsume_whenProductIsInvalid_shouldNotSaveOrSendKafkaMessages() {
        ProductDTO invalidProduct = new ProductDTO(null, "", "Description", -10.0, -1);

        productService.consume(invalidProduct);

        verify(productRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }
}

