package org.onelab.repoimpl;

import org.onelab.model.Orders;
import org.onelab.model.Users;
import org.onelab.repository.DAO;
import org.onelab.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepoImpl implements UserRepo {

    private Logger logger = LoggerFactory.getLogger(UserRepoImpl.class);

    private JdbcTemplate jdbcTemplate;

    private OrderRepoImpl orderRepo;

    @Autowired
    public UserRepoImpl(JdbcTemplate jdbcTemplate, OrderRepoImpl orderRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderRepo = orderRepo;
    }

    RowMapper<Users> rowMapper = (rs, rowNum) -> {
        return Users.builder()
                .id(rs.getInt("id"))
                .firstname(rs.getString("firstname"))
                .lastname(rs.getString("lastname"))
                .orders(new ArrayList<>())
                .build();
    };

    @Override
    public List<Users> findAll() {
        String sql = "SELECT * from users";
        List<Users> users = jdbcTemplate.query(sql, rowMapper);
        for (Users user : users) {
            List<Orders> orders = orderRepo.findAllByUserId(user.getId());
            user.setOrders(orders);
        }
        return users;
    }

    @Override
    public Optional<Users> findById(int id) {
        String sql = "SELECT id, firstname, lastname from users where id = ?";
        Users users = null;
        try {
            users = jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
        } catch (DataAccessException exception) {
            logger.info("User not found: " + id);
        }
        return Optional.ofNullable(users);
    }

    @Override
    public void save(Users users) {
        String sql = "insert into users(firstname,lastname) values(?,?)";
        int insert = jdbcTemplate.update(sql, users.getFirstname(), users.getLastname());
        if (insert == 1) {
            logger.info("New user created: " + users.getFirstname());
        }
    }


    @Override
    public void update(Users users, int id) {
        String sql = "update users set firstname = ?, lastname = ? where id = ?";
        int update = jdbcTemplate.update(sql, users.getFirstname(), users.getLastname(), id);
        if (update == 1) {
            logger.info("user updated: " + users.getFirstname());
        }
    }

    @Override
    public void remove(int id) {
        String sql = "delete from users where id = ?";
        int delete = jdbcTemplate.update(sql, id);
        if (delete == 1) {
            logger.info("user deleted: " + id);
        }
    }
}
