package com.onelab.microservices;



//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class NewInventoryDataLoader implements CommandLineRunner {
//
//    private final ObjectMapper objectMapper;
//    private final CategoryRepository categoryRepository;
//    private final NewInventoryRepository newInventoryRepository;
//
//
//    @Override
//    public void run(String... args) throws Exception {
//        if (categoryRepository.count() == 0) {
//            String CATEGORIES_JSON = "/data/categories.json";
//            log.info("Loading items into database from JSON: {}", CATEGORIES_JSON);
//            try (InputStream inputStream = TypeReference.class.getResourceAsStream(CATEGORIES_JSON)) {
//                Categories response = objectMapper.readValue(inputStream, Categories.class);
//                categoryRepository.saveAll(response.categories());
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to read JSON data", e);
//            }
//        }
//
//        if (newInventoryRepository.count() == 0) {
//            String ITEMS_JSON = "/data/items.json";
//            log.info("Loading items into database from JSON: {}", ITEMS_JSON);
//            try (InputStream inputStream = TypeReference.class.getResourceAsStream(ITEMS_JSON)) {
//                NewInventoryItems response = objectMapper.readValue(inputStream, NewInventoryItems.class);
//                newInventoryRepository.saveAll(response.items());
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to read JSON data", e);
//            }
//        }
//    }
//}
