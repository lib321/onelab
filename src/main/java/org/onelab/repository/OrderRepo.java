package org.onelab.repository;

import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Orders, Integer> {

    List<Orders> findByUserId(int userId);

}
