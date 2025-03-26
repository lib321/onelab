package com.onelab.microservices.service;

import com.onelab.dto.InventoryItemDTO;
import com.onelab.microservices.dto.ProductByCategoryDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.event.KafkaProducerService;
import com.onelab.microservices.feign.ProductFeignInterface;
import com.onelab.microservices.feign.UserFeignInterface;
import com.onelab.microservices.model.Category;
import com.onelab.microservices.model.Product;
import com.onelab.microservices.repository.CategoryRepository;
import com.onelab.microservices.repository.ProductRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductFeignInterface productFeignInterface;
    private final UserFeignInterface userFeignInterface;
    private final KafkaProducerService kafkaProducerService;

    public Category createCategory(Category category, String authHeader) {
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Категория не может быть пустой");
        }
        checkAdminAccess(authHeader);
        return categoryRepository.save(Category.builder()
                .categoryName(category.getCategoryName())
                .build());
    }


    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, String authHeader) {
        checkAdminAccess(authHeader);
        validateProduct(productDTO);

        if (productRepository.findByProductName(productDTO.getProductName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Продукт с таким именем уже существует");
        }

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Категория не найдена"));

        Product product = productRepository.save(mapToProduct(productDTO, category));

        InventoryItemDTO inventoryItemDTO = mapToInventoryItemDTO(product);

        try {
            productFeignInterface.addProduct(inventoryItemDTO);
            kafkaProducerService.sendMessage("product-events", "CREATED", inventoryItemDTO);
        } catch (Exception e) {
            productRepository.delete(product);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка при добавлении товара в inventory-service");
        }

        return mapToProductDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = getProductByIdOrThrow(id);

        ResponseEntity<Integer> response = productFeignInterface.getStock(id);
        Integer stock = (response != null) ? response.getBody() : null;

        if (stock != null && product.getQuantity() != stock) {
            product.setQuantity(stock);
            productRepository.save(product);
        }
        return mapToProductDTO(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        Map<Long, Integer> stockMap = productFeignInterface.getAllStock().getBody();
        return productRepository.findAll().stream()
                .map(product -> mapToProductDTO(product, stockMap != null ? stockMap.getOrDefault(product.getId(), 0) : 0))
                .collect(Collectors.toList());
    }


    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, String authHeader) {
        checkAdminAccess(authHeader);
        validateProduct(productDTO);

        Product product = getProductByIdOrThrow(id);
        int oldQuantity = product.getQuantity();

        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());

        productRepository.save(product);
        InventoryItemDTO inventoryItemDTO = mapToInventoryItemDTO(product);

        try {
            productFeignInterface.restockProduct(inventoryItemDTO);
            kafkaProducerService.sendMessage("product-events-update", "UPDATE", inventoryItemDTO);
        } catch (Exception e) {
            product.setQuantity(oldQuantity);
            productRepository.save(product);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка при обновлении товара в inventory-service");
        }

        return mapToProductDTO(product);
    }

    @Transactional
    public void deleteProduct(Long id, String authHeader) {
        checkAdminAccess(authHeader);
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден");
        }

        try {
            productFeignInterface.deleteProduct(id);
            kafkaProducerService.sendMessage("product-events", "DELETE", "Product ID: " + id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ошибка при удалении товара из inventory-service");
        }

        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, List<ProductByCategoryDTO>> groupProductsByCategory() {
        return productRepository.findAll().stream()
                .map(this::mapToProductByCategoryDTO)
                .collect(Collectors.groupingBy(
                        ProductByCategoryDTO::getCategoryName,
                        Collectors.collectingAndThen(Collectors.toList(), list ->
                                list.stream().sorted(Comparator.comparingInt(ProductByCategoryDTO::getPrice).reversed()).toList()
                        )
                ));
    }

    @Transactional(readOnly = true)
    public Page<ProductByCategoryDTO> getPagedSortedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        return productRepository.findAll(pageable).map(this::mapToProductByCategoryDTO);
    }

    private Product getProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Продукт не найден"));
    }

    private void validateProduct(ProductDTO productDTO) {
        if (productDTO.getProductName() == null || productDTO.getProductName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя продукта не может быть пустым");
        }
        if (productDTO.getPrice() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Цена должна быть больше 0");
        }
        if (productDTO.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Количество не может быть отрицательным");
        }
    }

    private void checkAdminAccess(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }
        try {
            if (!Boolean.TRUE.equals(userFeignInterface.validateUserRole(authHeader, "ADMIN"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
            }
        } catch (FeignException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неавторизованный пользователь");
        }
    }

    private Product mapToProduct(ProductDTO dto, Category category) {
        return Product.builder()
                .productName(dto.getProductName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .category(category)
                .build();
    }

    private ProductDTO mapToProductDTO(Product product) {
        return mapToProductDTO(product, product.getQuantity());
    }

    private ProductDTO mapToProductDTO(Product product, int stock) {
        return new ProductDTO(
                product.getId(),
                product.getProductName(),
                product.getPrice(),
                stock,
                product.getCategory().getId(),
                product.getUpdatedAt()
        );
    }

    private InventoryItemDTO mapToInventoryItemDTO(Product product) {
        return new InventoryItemDTO(
                product.getId(),
                product.getProductName(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getCategoryName(),
                product.getAddedAt(),
                product.getUpdatedAt()
        );
    }

    private ProductByCategoryDTO mapToProductByCategoryDTO(Product product) {
        return new ProductByCategoryDTO(
                product.getId(),
                product.getProductName(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getCategoryName()
        );
    }
}

