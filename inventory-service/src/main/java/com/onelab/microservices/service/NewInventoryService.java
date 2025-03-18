package com.onelab.microservices.service;

import com.onelab.microservices.model.Category;
import com.onelab.microservices.model.NewInventoryItem;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.NewInventoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class NewInventoryService {

    private final CategoryRepository categoryRepository;
    private final NewInventoryRepository inventoryRepository;

    private final Random random = new Random();

    public NewInventoryService(CategoryRepository categoryRepository, NewInventoryRepository inventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public Optional<Category> addCategory(Optional<Category> category) {
        return category.map(c -> {
            Category savedCategory = Category.builder()
                    .name(c.getName())
                    .build();

            return categoryRepository.save(savedCategory);
        });
    }

    public Optional<NewInventoryItem> addItem(Optional<NewInventoryItem> item) {
        return item.map(i -> {
            Category category = categoryRepository.findCategoryByName(i.getCategoryName())
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder().name(i.getCategoryName()).build()
                    ));

            NewInventoryItem savedItem = NewInventoryItem.builder()
                    .productId(i.getProductId())
                    .productName(i.getProductName())
                    .quantity(i.getQuantity())
                    .categoryName(category.getName())
                    .price(i.getPrice())
                    .addedAt(i.getAddedAt()) // Не использую LocalDate.now(), чтобы хранить разную дату
                    .build();

            return inventoryRepository.save(savedItem);
        });
    }

    public List<String> getProductNamesByCategory(String categoryName) {
        return inventoryRepository.findByCategoryName(categoryName).stream()
                .map(NewInventoryItem::getProductName)
                .collect(Collectors.toList());
    }

    public List<String> getProductNamesByPriceRange(int minPrice, int maxPrice) {
        List<NewInventoryItem> items = inventoryRepository.findByPriceBetween(minPrice, maxPrice);

        Predicate<NewInventoryItem> priceFilter = item -> item.getPrice() >= minPrice && item.getPrice() <= maxPrice;
        Function<NewInventoryItem, String> nameMapper = item -> item.getProductName() + " - " + item.getPrice();

        return items.stream()
                .filter(priceFilter)
                .sorted(Comparator.comparingInt(NewInventoryItem::getPrice))
                .map(nameMapper)
                .collect(Collectors.toList());
    }

    public List<NewInventoryItem> filterWithLambda(InventoryFilter filter) {
        List<NewInventoryItem> items = StreamSupport
                .stream(inventoryRepository.findAll().spliterator(), false)
                .toList();

        return items.stream()
                .filter(filter::filter)
                .sorted(Comparator.comparing(NewInventoryItem::getPrice))
                .collect(Collectors.toList());
    }

    public void compareStreamPerformance() {
        List<NewInventoryItem> items = StreamSupport.stream(inventoryRepository.findAll().spliterator(), false)
                .toList();

        long startTime = System.nanoTime();
        items.stream()
                .map(item -> item.getProductName() + " price is " + calculatePrice(item))
                .count();
        long sequentialTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        items.parallelStream()
                .map(item -> item.getProductName() + " price is " + calculatePrice(item))
                .count();
        long parallelTime = System.nanoTime() - startTime;

        System.out.println("Sequential time: " + sequentialTime);
        System.out.println("Parallel time: " + parallelTime);
    }

    public int getTotalInventoryValue() {
        List<NewInventoryItem> items = StreamSupport.stream(inventoryRepository.findAll().spliterator(), false)
                .toList();

        return items.stream()
                .map(NewInventoryItem::getPrice)
                .reduce(0, Integer::sum);
    }

    public Map<String, List<NewInventoryItem>> groupByCategory() {
        List<NewInventoryItem> items = StreamSupport.stream(inventoryRepository.findAll().spliterator(), false)
                .toList();

        return items.stream()
                .collect(Collectors.groupingBy(NewInventoryItem::getCategoryName));
    }

    public Map<Boolean, List<NewInventoryItem>> partitionByPrice(int price) {
        List<NewInventoryItem> items = StreamSupport.stream(inventoryRepository.findAll().spliterator(), false)
                .toList();

        return items.stream()
                .collect(Collectors.partitioningBy(item -> item.getPrice() > price));
    }


    private int calculatePrice(NewInventoryItem item) {
        return Math.abs(random.nextInt()) % Integer.MAX_VALUE + item.getPrice();
    }
}
