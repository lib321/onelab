package com.onelab.microservices.controller;

import com.onelab.microservices.dto.ProductByCategoryDTO;
import com.onelab.microservices.dto.ProductDTO;
import com.onelab.microservices.model.Category;
import com.onelab.microservices.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @PostMapping("/add")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO,
                                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(productService.createProduct(productDTO, authHeader));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO,
                                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO, authHeader));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        productService.deleteProduct(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add/category")
    public ResponseEntity<Category> addCategory(@RequestBody Category category,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(productService.createCategory(category, authHeader));
    }

    @GetMapping("/grouped-by-category")
    public ResponseEntity<Map<String, List<ProductByCategoryDTO>>> getProductsGroupedByCategory() {
        return ResponseEntity.ok(productService.groupProductsByCategory());
    }

    @GetMapping
    public ResponseEntity<Page<ProductByCategoryDTO>> getPagedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getPagedSortedProducts(page, size));
    }
}
