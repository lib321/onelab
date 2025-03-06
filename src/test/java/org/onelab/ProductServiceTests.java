package org.onelab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onelab.model.Product;
import org.onelab.repository.ProductRepo;
import org.onelab.serviceImpl.ProductServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepo productRepo;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    public void setUp() {
        product = Product.builder()
                .id(1)
                .name("prod1")
                .price(2000)
                .orders(new ArrayList<>())
                .build();
    }

    @DisplayName("Тест метода save()")
    @Test
    public void givenProductObject_whenSaveProduct_thenReturnProductObject() {
        when(productRepo.save(product)).thenReturn(product);

        Product savedProduct = productService.save(product);
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("prod1");
    }

    @DisplayName("Тест метода getProducts()")
    @Test
    public void givenProductList_whenGetProducts_thenReturnProductList() {
        Product product1 = Product.builder()
                .id(2)
                .name("prod2")
                .price(3500)
                .orders(new ArrayList<>())
                .build();

        when(productRepo.findAll()).thenReturn(List.of(product, product1));

        List<Product> productList = productService.getProducts();
        assertThat(productList).isNotNull();
        assertThat(productList.size()).isEqualTo(2);
    }

    @DisplayName("Тест метода getProductById(int productId)")
    @Test
    public void givenProductId_whenGetProductById_thenReturnProductObject() {
        when(productRepo.findById(1)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.getProductById(1);
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.get().getName()).isEqualTo("prod1");
    }

    @DisplayName("Тест метода deleteById(int productId)")
    @Test
    public void givenProductId_whenDeleteById_thenNothing() {
        productService.deleteById(1);

        verify(productRepo).deleteById(1);
    }
}
