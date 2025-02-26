package org.onelab.repository;

import org.onelab.dto.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepo {

    void save(Product product);

    List<Product> findAll();

    Optional<Product> findById(int id);

    void remove(Product product);
}
