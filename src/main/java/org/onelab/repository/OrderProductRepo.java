package org.onelab.repository;

import org.onelab.model.OrderProducts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepo extends JpaRepository<OrderProducts, Integer> {
}
