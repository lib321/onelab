package com.onelab.microservices;

import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.repository.InventoryRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ExtendWith(SpringExtension.class)
@DataElasticsearchTest
public class InventoryRepositoryTest {

    @Container
    private static final ElasticsearchContainer elasticsearchContainer = new InventoryElasticSearchContainer();

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeAll
    static void setUp() {
        elasticsearchContainer.start();
    }

    @BeforeEach
    void testIsContainerRunning() {
        assertTrue(elasticsearchContainer.isRunning());
        InventoryItem item = new InventoryItem(null, 1L, "ProductA", 10);
        inventoryRepository.save(item);
    }

    @Test
    void connectionEstablished() {
        assertThat(elasticsearchContainer.isCreated()).isTrue();
        assertThat(elasticsearchContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnItemByProductName() {
        Optional<InventoryItem> item = inventoryRepository.findByProductName("ProductA");
        assertThat(item).isPresent();
    }

    @Test
    void shouldReturnItemByProductId() {
        Optional<InventoryItem> item = inventoryRepository.findByProductId(1L);
        assertThat(item).isPresent();
    }

    @Test
    void shouldReturnTrueIfProductExist() {
        boolean exist = inventoryRepository.existsByProductId(1L);
        assertTrue(exist);
    }

    @Test
    void shouldDeleteByProductId() {
        inventoryRepository.deleteByProductId(1L);
        assertThat(inventoryRepository.findByProductId(1L)).isEmpty();
    }

    @AfterAll
    static void destroy() {
        elasticsearchContainer.stop();
    }
}
