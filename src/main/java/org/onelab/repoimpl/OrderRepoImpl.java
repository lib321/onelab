package org.onelab.repoimpl;

import org.onelab.dto.Order;
import org.onelab.dto.Product;
import org.onelab.repository.OrderRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepoImpl implements OrderRepo {

    private List<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.add(order);
    }

    @Override
    public List<Order> findAll() {
        return orders;
    }

    @Override
    public Optional<Order> findById(int orderId) {
        if (orders.isEmpty()) return Optional.empty();

        for (Order order : orders) {
            if (order.getId() == orderId) {
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    @Override
    public void remove(Order order) {
        orders.remove(order);
    }
}
