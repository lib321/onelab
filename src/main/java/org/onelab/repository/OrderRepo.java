package org.onelab.repository;

import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;

import java.util.List;

public interface OrderRepo extends DAO<Orders>{

    List<Orders> findAllByUserId(int id);

    List<OrderProducts> findProductsByOrderId(int orderId);
}
