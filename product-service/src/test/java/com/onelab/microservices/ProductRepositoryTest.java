package com.onelab.microservices;

import com.onelab.microservices.model.Category;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@DataJpaTest
public class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0");

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @BeforeEach
    void setUp() {
        Category category = new Category(null, "CategoryName", new ArrayList<>());
        Product product = new Product(null, "ProductA", 1000, 10, category,
                LocalDate.of(2024, 9, 10), null);
        category.getProducts().add(product);
        categoryRepository.save(category);
        productRepository.save(product);
    }

    @Test
    @Transactional
    void shouldReturnProductByName() {
        Optional<Product> product = productRepository.findByProductName("ProductA");
        assertThat(product).isPresent();
    }
}
