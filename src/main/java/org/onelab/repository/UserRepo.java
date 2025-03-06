package org.onelab.repository;

import org.onelab.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<Users, Integer> {

    Users findUsersByLogin(String login);
}
