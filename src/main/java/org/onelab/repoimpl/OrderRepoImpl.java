package org.onelab.repoimpl;

import org.onelab.model.OrderProducts;
import org.onelab.model.Orders;
import org.onelab.model.Product;
import org.onelab.repository.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class OrderRepoImpl implements OrderRepo {

    private Logger logger = LoggerFactory.getLogger(UserRepoImpl.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public OrderRepoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    RowMapper<Orders> rowMapper = (rs, rowNum) -> {
        return Orders.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .build();
    };

    @Override
    public List<Orders> findAll() {
        String sql = "SELECT * from orders";
        List<Orders> orders = jdbcTemplate.query(sql, rowMapper);
        for (Orders order : orders) {
           order.setOrderProducts(findProductsByOrderId(order.getId()));
        }

        return orders;
    }

    public List<Orders> findAllByUserId(int id) {
        String sql = "SELECT * from orders where user_id = ?";
        return jdbcTemplate.query(sql, new Object[]{id}, rowMapper);
    }

    @Override
    public Optional<Orders> findById(int id) {
        String sql = "SELECT id from orders where user_id = ?";
        Orders order = null;
        try {
            order = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
        } catch (DataAccessException exception) {
            logger.info("Order not found: " + id);
        }
        return Optional.ofNullable(order);
    }

    @Override
    public void save(Orders order) {
        String sql = "insert into orders (user_id) values(?)";
        int insert = jdbcTemplate.update(sql, order);
        if (insert == 1) {
            logger.info("New order created: " + order);
        }
    }

    @Override
    public void update(Orders order, int id) {

    }

    @Override
    public void remove(int id) {
        String sql = "delete from orders where id = ?";
        int delete = jdbcTemplate.update(sql, id);
        if (delete == 1) {
            logger.info("order deleted: " + id);
        }
    }

    @Override
    public List<OrderProducts> findProductsByOrderId(int orderId) {
        String sql = "SELECT op.order_id, op.product_id, p.id, p.name, p.price " +
                "FROM product p " +
                "JOIN order_products op ON p.id = op.product_id " +
                "WHERE op.order_id = ?";

        return jdbcTemplate.query(sql, new Object[]{orderId}, (rs, rowNum) -> {
            Product product = Product.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .price(rs.getInt("price"))
                    .build();

            return OrderProducts.builder()
                    .orderId(rs.getInt("order_id"))
                    .productId(rs.getInt("product_id"))
                    .product(product)
                    .build();
        });
    }

}
