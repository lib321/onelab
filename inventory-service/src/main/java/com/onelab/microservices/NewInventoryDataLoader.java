package com.onelab.microservices;

import aj.org.objectweb.asm.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.microservices.model.Categories;
import com.onelab.microservices.model.NewInventoryItems;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.NewInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewInventoryDataLoader implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;
    private final NewInventoryRepository newInventoryRepository;


    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            String CATEGORIES_JSON = "/data/categories.json";
            log.info("Loading items into database from JSON: {}", CATEGORIES_JSON);
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(CATEGORIES_JSON)) {
                Categories response = objectMapper.readValue(inputStream, Categories.class);
                categoryRepository.saveAll(response.categories());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        }

        if (newInventoryRepository.count() == 0) {
            String ITEMS_JSON = "/data/items.json";
            log.info("Loading items into database from JSON: {}", ITEMS_JSON);
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(ITEMS_JSON)) {
                NewInventoryItems response = objectMapper.readValue(inputStream, NewInventoryItems.class);
                newInventoryRepository.saveAll(response.items());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        }
    }
}
