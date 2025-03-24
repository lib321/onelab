package com.onelab.microservices;


import aj.org.objectweb.asm.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onelab.microservices.model.InventoryItems;
import com.onelab.microservices.repository.InventoryRepository;
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
    private final InventoryRepository inventoryRepository;


    @Override
    public void run(String... args) throws Exception {
        if (inventoryRepository.count() == 0) {
            String ITEMS_JSON = "/data/items.json";
            log.info("Loading items into database from JSON: {}", ITEMS_JSON);
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(ITEMS_JSON)) {
                InventoryItems response = objectMapper.readValue(inputStream, InventoryItems.class);
                inventoryRepository.saveAll(response.items());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        }
    }
}
