package com.onelab.microservices;


import aj.org.objectweb.asm.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.onelab.microservices.model.Categories;
import com.onelab.microservices.model.Products;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductsDataLoader implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            String CATEGORIES_JSON = "/data/categories.json";
            log.info("Loading categories into database from JSON: {}", CATEGORIES_JSON);
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(CATEGORIES_JSON)) {
                Categories response = objectMapper.readValue(inputStream, Categories.class);
                categoryRepository.saveAll(response.categories());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        }

        if (productRepository.count() == 0) {
            String PRODUCTS_JSON = "/data/products.json";
            log.info("Loading products into database from JSON: {}", PRODUCTS_JSON);
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(PRODUCTS_JSON)) {
                Products response = objectMapper.readValue(inputStream, Products.class);
                productRepository.saveAll(response.products());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        }
    }
}
