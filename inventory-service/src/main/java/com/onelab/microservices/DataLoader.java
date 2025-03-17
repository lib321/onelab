package com.onelab.microservices;

import com.onelab.microservices.model.InventoryItem;
import com.onelab.microservices.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
//@RequiredArgsConstructor
//public class DataLoader implements CommandLineRunner {
//
//    private final InventoryRepository inventoryRepository;
//
//    @Override
//    public void run(String... args) {
//        if (inventoryRepository.count() == 0) {
//            List<InventoryItem> items = List.of(
//                    new InventoryItem(null, 1L, "Ноутбук Lenovo", 10),
//                    new InventoryItem(null, 2L, "Беспроводные наушники Sony", 5),
//                    new InventoryItem(null, 3L, "Смартфон Samsung Galaxy S24", 6)
//            );
//
//            inventoryRepository.saveAll(items);
//        }
//    }
//}
