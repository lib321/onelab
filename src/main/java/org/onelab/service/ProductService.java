package org.onelab.service;

import org.onelab.model.Product;
import org.onelab.model.Users;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> getProducts();

    Optional<Product> getProductById(int id);

    Product save(Product product);

    void deleteById(int id);
}
