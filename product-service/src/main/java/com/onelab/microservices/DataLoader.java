package com.onelab.microservices;

import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
//@RequiredArgsConstructor
//public class DataLoader implements CommandLineRunner {
//
//    private final ProductRepository productRepository;
//
//    @Override
//    public void run(String... args) {
//        if (productRepository.count() == 0) {
//            List<Product> products = List.of(
//                    new Product(null, "Ноутбук Lenovo", "Мощный ноутбук для работы и учебы", 1200.50, 10),
//                    new Product(null, "Беспроводные наушники Sony", "Шумоподавление и качественный звук", 250.99, 5),
//                    new Product(null, "Смартфон Samsung Galaxy S24", "Флагманский смартфон с AMOLED дисплеем", 999.99, 6)
//            );
//
//            productRepository.saveAll(products);
//        }
//    }
//}

