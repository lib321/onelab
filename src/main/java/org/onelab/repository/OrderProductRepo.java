package org.onelab.repository;

import org.onelab.dto.OrderTotalDTO;
import org.onelab.model.OrderProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderProductRepo extends JpaRepository<OrderProducts, Integer> {

    @Query("SELECT new org.onelab.dto.OrderTotalDTO(op.order.id, SUM(op.count)) " +
            "FROM OrderProducts op WHERE op.order.user.id = :userId GROUP BY op.order.id")
    List<OrderTotalDTO> getTotalProductsPerOrderByUserId(@Param("userId") int userId);
}
