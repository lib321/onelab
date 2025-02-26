package org.onelab.repoimpl;

import org.onelab.dto.Product;
import org.onelab.repository.ProductRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepoImpl implements ProductRepo {

    List<Product> products = new ArrayList<>();

    @Override
    public void save(Product product) {
        products.add(product);
    }

    @Override
    public List<Product> findAll() {
        return products;
    }

    @Override
    public Optional<Product> findById(int productId) {
        if (products.isEmpty()) return Optional.empty();

        for (Product product : products) {
            if (product.getId() == productId) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    @Override
    public void remove(Product product) {
        products.remove(product);
    }
}
