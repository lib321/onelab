package org.onelab.repoimpl;

import org.onelab.model.Product;
import org.onelab.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepoImpl implements ProductRepo {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ProductRepoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Product> findAll() {
        return null;
    }

    @Override
    public void save(Product product) {

    }

    @Override
    public Optional<Product> findById(int id) {
        return Optional.empty();
    }

    @Override
    public void update(Product product, int id) {

    }

    @Override
    public void remove(int id) {

    }
}
