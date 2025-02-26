package org.onelab.repository;

import org.onelab.dto.Order;
import org.onelab.dto.Product;

import java.util.List;
import java.util.Optional;

public interface OrderRepo {

    void save(Order order);

    List<Order> findAll();

    Optional<Order> findById(int id);

    void remove(Order order);
}
