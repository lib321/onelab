package com.onelab.microservices.console;

import com.onelab.microservices.dto.InventoryCheckRequestDTO;
import com.onelab.microservices.dto.InventoryRequestDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.service.KafkaLoggingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Scanner;

@Slf4j
@Component
public class ConsoleApplication implements CommandLineRunner {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaLoggingService kafkaLoggingService;

    public ConsoleApplication(KafkaTemplate<String, Object> kafkaTemplate, KafkaLoggingService kafkaLoggingService) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaLoggingService = kafkaLoggingService;
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);
        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1 - Добавить продукт");
            System.out.println("2 - Сделать заказ");
            System.out.println("3 - Проверить остатки");
            System.out.println("0 - Выйти");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                System.out.println("Выход...");
                scanner.close();
                System.exit(0);
            }

            if (choice == 1) {
                System.out.print("Введите название продукта: ");
                String productName = scanner.nextLine();

                System.out.print("Введите описание: ");
                String description = scanner.nextLine();

                double price;
                while (true) {
                    System.out.print("Введите цену: ");
                    if (scanner.hasNextDouble()) {
                        price = scanner.nextDouble();
                        scanner.nextLine();
                        break;
                    } else {
                        System.out.println("Ошибка: введите число.");
                        scanner.next();
                    }
                }
                System.out.print("Введите количество: ");
                int quantity = scanner.nextInt();

                ProductDTO productDTO = new ProductDTO(null, productName, description, price, quantity);
                kafkaTemplate.send("product-topic", productDTO);
                kafkaLoggingService.log("INFO", "Продукт отправлен на добавление: " + productDTO);
            }

            if (choice == 2) {
                System.out.print("Введите имя покупателя: ");
                String customerName = scanner.nextLine();

                System.out.print("Введите название товара: ");
                String productName = scanner.nextLine();

                int quantity;
                while (true) {
                    System.out.print("Введите количество: ");
                    if (scanner.hasNextInt()) {
                        quantity = scanner.nextInt();
                        scanner.nextLine();
                        break;
                    } else {
                        System.out.println("Ошибка: введите число.");
                        scanner.next();
                    }
                }

                InventoryRequestDTO requestDTO = new InventoryRequestDTO(productName, quantity, customerName);
                kafkaTemplate.send("inventory-check-topic", requestDTO);
                kafkaLoggingService.log("INFO", "Запрос на проверку остатков: " + requestDTO);
            }

            if (choice == 3) {
                System.out.print("Введите id товара: ");
                Long productId = scanner.nextLong();
                InventoryCheckRequestDTO request = new InventoryCheckRequestDTO(productId);
                kafkaTemplate.send("inventory-check-stock-topic", String.valueOf(productId), request);
                kafkaLoggingService.log("INFO", "Отправлен запрос на проверку остатков для продукта ID: " + productId);
            }
        }
    }
}

