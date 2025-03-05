package org.onelab.service;

import org.onelab.dto.OrderTotalDTO;
import org.onelab.model.Orders;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    List<Orders> getOrders();

    Optional<Orders> getOrderById(int id);

    Orders save(Orders order);

    void deleteById(int id);

    List<Orders> findOrdersByUserId(int userId);

    public List<OrderTotalDTO> getTotalProductsPerOrderByUserId(int userId);
}
